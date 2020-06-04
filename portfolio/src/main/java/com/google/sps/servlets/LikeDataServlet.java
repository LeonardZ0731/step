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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that likes a single comment. */
@WebServlet("/comments/like")
public class LikeDataServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyString = request.getParameter("key");
        System.out.println("Receive like: " + keyString);

        Key commentKey = KeyFactory.stringToKey(keyString);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity comment;
        try {
            comment = datastore.get(commentKey);
        } catch (EntityNotFoundException e) {
            System.err.println("Unable to find the entity based on the input key");
            return;
        }

        long likes = (long)comment.getProperty("like");
        System.out.println("Current comment is: " + (String)comment.getProperty("comment"));
        System.out.println("Current likes is: " + likes);
        comment.setProperty("like", likes + 1);
        System.out.println("Now likes become: " + (long)comment.getProperty("like"));
        datastore.put(comment);

        response.sendRedirect("/index.html");
    }
}