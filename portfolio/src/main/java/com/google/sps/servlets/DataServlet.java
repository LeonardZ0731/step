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
import com.google.gson.Gson;
import com.google.sps.comment.Comment;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private static int maxComments = 10;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      List<Comment> comments = new ArrayList<>();
    //   int outputCount = 0;
      for (Entity entity: results.asIterable()) {
        //   if (outputCount == this.maxComments) {
        //       break;
        //   }
          long id = entity.getKey().getId();
          String comment = (String) entity.getProperty("comment");
          long timestamp = (long) entity.getProperty("timestamp");

          Comment commentEntry = new Comment(id, comment, timestamp);
          comments.add(commentEntry);
        //   outputCount++;
      }
      comments.add(new Comment(0, "", maxComments));

      Gson gson = new Gson();
      String json = gson.toJson(comments);

      // Send out json as response
      response.setContentType("application/json;");
      response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Get the input from the form
      int maxComment = getUserInput(request);
      if (maxComment == -1) {
          response.setContentType("text/html");
          response.getWriter().println("Please enter a valid positive integer");
          return;
      }
      this.maxComments = maxComment;
      System.out.println(this.maxComments);

      String inputText = "";
      String inputValue = request.getParameter("comment-input");
      if (inputValue != null) {
          inputText = inputValue;
      }
      if (!inputText.equals("")) {
          long timestamp = System.currentTimeMillis();

          // Create an Entity that stores the input comment
          Entity commentEntity = new Entity("Comment");
          commentEntity.setProperty("comment", inputText);
          commentEntity.setProperty("timestamp", timestamp);

          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          datastore.put(commentEntity);
      }

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html");
  }

  /** Returns the input entered by the user, or -1 if the input was invalid. */
  private int getUserInput(HttpServletRequest request) {
    // Get the input from the form.
    String userInputString = request.getParameter("maximum-comments");

    // Convert the input to an int.
    int userInput;
    try {
      userInput = Integer.parseInt(userInputString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + userInputString);
      return -1;
    }

    // Check that the input is positive.
    if (userInput < 1) {
      System.err.println("User input is out of range: " + userInputString);
      return -1;
    }

    System.out.println(userInput);
    return userInput;
  }
}