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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;
import com.google.sps.marker.Marker;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/markers")
public class MarkerServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Query query = new Query("Marker");

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      List<Marker> markers = new ArrayList<>();
      for (Entity entity: results.asIterable()) {
          double latitude = (double) entity.getProperty("latitude");
          double longitude = (double) entity.getProperty("longitude");

          Marker markerEntry = new Marker(latitude, longitude);
          markers.add(markerEntry);
      }

      Gson gson = new Gson();
      String json = gson.toJson(markers);
      response.setContentType("application/json;");
      response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String latitudeString = request.getParameter("latitude");
      String longitudeString = request.getParameter("longitude");
      double latitude = Double.parseDouble(latitudeString);
      double longitude = Double.parseDouble(longitudeString);

      Entity markerEntity = new Entity("Marker");
      markerEntity.setProperty("latitude", latitude);
      markerEntity.setProperty("longitude", longitude);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(markerEntity);

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String latitudeString = request.getParameter("latitude");
      String longitudeString = request.getParameter("longitude");
      double latitude = Double.parseDouble(latitudeString);
      double longitude = Double.parseDouble(longitudeString);
      System.out.println("Remove lag: " + latitude + ", lng: " + longitude);

      Filter latitudeFilter =
        new FilterPredicate("latitude", FilterOperator.EQUAL, latitude);

      Filter longitudeFilter = 
        new FilterPredicate("longitude", FilterOperator.EQUAL, longitude);

      CompositeFilter latLngFilter = CompositeFilterOperator.and(latitudeFilter, longitudeFilter);

      Query query = new Query("Marker").setFilter(latLngFilter);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);
      
      // Loop through all entities and delete them using the key
      for (Entity entity: results.asIterable()) {
        System.out.println("DELETE");
        Key key = entity.getKey();
        datastore.delete(key);
      }

      // Redirect back to the HTML page.
      response.sendRedirect("/index.html"); 
    }
}