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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that likes a single comment. */
@WebServlet("/likes-management")
public class LikeDataServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commentString = request.getParameter("comment");
        System.out.println("Receive like: " + commentString);

        Filter commentFilter = new FilterPredicate("comment", FilterOperator.EQUAL, commentString);
        Query query = new Query("Comment").setFilter(commentFilter);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        // Loop through all entities and delete them using the key
        for (Entity entity: results.asIterable()) {
            long likes = (long)entity.getProperty("like");
            System.out.println("Current comment is: " + (String)entity.getProperty("comment"));
            System.out.println("Current likes is: " + likes);
            entity.setProperty("like", likes + 1);
            System.out.println("Now likes become: " + (long)entity.getProperty("like"));
            datastore.put(entity);
        }

        response.sendRedirect("/index.html");
    }
}