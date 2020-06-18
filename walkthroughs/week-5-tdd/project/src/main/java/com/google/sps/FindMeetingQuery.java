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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  /**
   * Returns a Collection of {@code TimeRange} based on {@code events} and {@code request}. If there
   * exists available time slots for all the mandatory and optional attendees, return these time slots.
   * If there are no such time slots, return all time slots that are available for all mandatory attendees
   * to attend the meeting.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      Collection<String> attendees = request.getAttendees();
      Collection<String> optional = request.getOptionalAttendees();
      List<String> newAttendees = new ArrayList<>();
      for (String attendee: attendees) newAttendees.add(attendee);
      for (String attendee: optional) newAttendees.add(attendee);
      MeetingRequest newRequest = new MeetingRequest(newAttendees, request.getDuration());
      Collection<TimeRange> withOptional = queryWithMandatoryAttendee(events, newRequest);
      Collection<TimeRange> withoutOptional = queryWithMandatoryAttendee(events, request);
      if (attendees.isEmpty() && !optional.isEmpty()) {
          return withOptional;
      }
      if (withOptional.isEmpty()) {
          return withoutOptional;
      }
      return withOptional;
  }
  /**
   * Returns a Collection of {@code TimeRange} based on {@code events} and {@code request} such that
   * the returned time slots are available for all the mandatory attendees indicated by {@code request}.
   */
  public Collection<TimeRange> queryWithMandatoryAttendee(Collection<Event> events, MeetingRequest request) {
      long duration = request.getDuration();
      // If the duration of request is longer than one day, there will be no available time slot for the
      // request. Return empty list under such situation.
      if (duration > TimeRange.WHOLE_DAY.duration()) {
          return Arrays.asList();
      }
      Collection<String> attendees = request.getAttendees();
      Set<String> attendeeSet = new HashSet<>(attendees);

      List<TimeRange> matchingEvents = new ArrayList<>();
      for (Event event: events) {
          if (!Collections.disjoint(attendees, event.getAttendees())) {
              matchingEvents.add(event.getWhen());
          }
      }
      Collections.sort(matchingEvents, TimeRange.ORDER_BY_START);
      TimeRange initial = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true);
      List<TimeRange> result = new ArrayList<>();
      for (TimeRange range: matchingEvents) {
          if (range.contains(initial)) {
              initial = TimeRange.fromStartEnd(TimeRange.END_OF_DAY, TimeRange.END_OF_DAY, true);
              break;
          }
          if (range.end() <= initial.start()) {
              continue;
          }
          if (range.start() <= initial.start()) {
              initial = TimeRange.fromStartEnd(range.end(), TimeRange.END_OF_DAY, true);
              continue;
          }
          TimeRange temp = TimeRange.fromStartEnd(initial.start(), range.start(), false);
          if (temp.duration() >= duration) {
              result.add(TimeRange.fromStartEnd(initial.start(), range.start(), false));
          }
          initial = TimeRange.fromStartEnd(range.end(), TimeRange.END_OF_DAY, true);
      }
      if (initial.start() != initial.end() && initial.duration() >= duration) {
          result.add(initial);
      }
      return result;
  }
}
