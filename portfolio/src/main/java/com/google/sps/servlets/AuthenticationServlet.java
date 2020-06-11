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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class AuthenticationServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setContentType("text/html;");
      PrintWriter out = response.getWriter();

      UserService userService = UserServiceFactory.getUserService();
      // If the user has already logged, show the log out link
      if (!userService.isUserLoggedIn()) {
          String urlToRedirectToAfterUserLogsIn = "/login";
          String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
          out.println("<p>Hello stranger.</p>");
          out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
          return;
      }

      String nickname = getNickname(userService.getCurrentUser().getEmail());
      if (nickname == null) {
          response.sendRedirect("/nickname.html");
          return;
      } 

      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirctToAfterUserLogsOut = "/index.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirctToAfterUserLogsOut);
      out.println("<p>Hello " + userEmail + "!</p>");
      out.println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
      out.println("<p>If you want to change your nickname, click <a href=\"/nickname.html\">here</a>.</p>");
    }

    public static String getNickname(String email) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("UserInfo").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, email));
        PreparedQuery results = datastore.prepare(query);
        
        int count = 0;
        String nickname = null;
        for (Entity entity: results.asIterable()) {
            count += 1;
            if (entity == null) {
                return null;
            }
            nickname = (String) entity.getProperty("nickname");
        }
        if (count == 0) return null;
        return nickname;
    }
}