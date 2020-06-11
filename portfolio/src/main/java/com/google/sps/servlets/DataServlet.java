// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.comment.Comment;
import com.google.sps.comment.CommentResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Query query = new Query("Comment").addSort("like", SortDirection.DESCENDING)
                                        .addSort("timestamp", SortDirection.DESCENDING);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      List<Comment> comments = new ArrayList<>();
      for (Entity entity: results.asIterable()) {
          String keyString = KeyFactory.keyToString(entity.getKey());
          String comment = (String) entity.getProperty("comment");
          long timestamp = (long) entity.getProperty("timestamp");
          String email = (String) entity.getProperty("email");
          String nickname = AuthenticationServlet.getNickname(email);
          if (nickname == null) {
              nickname = email;
          }
          System.out.println(comment);
          System.out.println((long) entity.getProperty("like"));

          Comment commentEntry = new Comment(keyString, comment, timestamp, nickname);
          comments.add(commentEntry);
      }

      // The default value for maxComments is 10
      long maxComments = 10;
      String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
      Query maxCommentQuery = new Query("MaxComment").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, userEmail));
      results = datastore.prepare(maxCommentQuery);
      Entity entity = results.asSingleEntity();
      // If the user has stored his preferred maxComments value before, replace the default value with stored value
      if (entity != null) {
          maxComments = (long) entity.getProperty("limit");
      }
      CommentResponse commentResponse = new CommentResponse(comments, maxComments);

      Gson gson = new Gson();
      String json = gson.toJson(commentResponse);

      // Send out json as response
      response.setContentType("application/json;");
      response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Only logged-in users are able to submit the form and create a POST request.
      UserService userService = UserServiceFactory.getUserService();
      if (!userService.isUserLoggedIn()) {
          System.err.println("POST request will not be handled if the user is not logged in");
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "You need to log into your account first");
          return;
      }
      // Get the input from the form
      int maxComment = getMaxComments(request);
      if (maxComment == -1) {
          response.setContentType("text/html");
          response.getWriter().println("Please enter a valid positive integer");
          return;
      }
      storeMaxComments(maxComment);

      String inputText = "";
      String inputValue = request.getParameter("comment-input");
      if (inputValue != null) {
          inputText = inputValue;
      }
      if (!inputText.equals("")) {
          long timestamp = System.currentTimeMillis();
          String email = userService.getCurrentUser().getEmail();

          // Create an Entity that stores the input comment
          Entity commentEntity = new Entity("Comment");
          commentEntity.setProperty("comment", inputText);
          commentEntity.setProperty("timestamp", timestamp);
          commentEntity.setProperty("like", 1);
          commentEntity.setProperty("email", email);

          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          datastore.put(commentEntity);
      }

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html");
  }

  /** Returns the input entered by the user, or -1 if the input was invalid. */
  private int getMaxComments(HttpServletRequest request) {
    // Get the input from the form.
    String maxCommentsString = request.getParameter("maximum-comments");

    // Convert the input to an int.
    int maxComments;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
      return -1;
    }

    // Check that the input is positive.
    if (maxComments < 1) {
      System.err.println("User input is out of range: " + maxCommentsString);
      return -1;
    }

    System.out.println(maxComments);
    return maxComments;
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("Receive request");
    Query query = new Query("Comment");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Loop through all entities and delete them using the key
    for (Entity entity: results.asIterable()) {
      Key key = entity.getKey();
      datastore.delete(key);
    }        

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
      System.out.println("Receive PUT request");
      int maxComment = getMaxComments(request);
      if (maxComment == -1) {
          response.setContentType("text/html");
          response.getWriter().println("Please enter a valid positive integer");
          return;
      }
      
      storeMaxComments(maxComment);
  }

  private void storeMaxComments(int maxComments) {
      // Store the maximum comments limit with the user
      UserService userService = UserServiceFactory.getUserService();

      String email = userService.getCurrentUser().getEmail();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("MaxComment").setFilter(
        new FilterPredicate("email", FilterOperator.EQUAL, email));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        entity = new Entity("MaxComment");
        entity.setProperty("limit", maxComments);
        entity.setProperty("email", email);
      } else {
        entity.setProperty("limit", maxComments);
      }
      datastore.put(entity);
  }
}