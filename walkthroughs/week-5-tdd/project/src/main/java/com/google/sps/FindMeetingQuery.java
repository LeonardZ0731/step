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
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      long duration = request.getDuration();
      if (duration > TimeRange.WHOLE_DAY.duration()) {
          return Arrays.asList();
      }
      Collection<String> attendees = request.getAttendees();
      Set<String> attendeeSet = new HashSet<>(attendees);

      List<TimeRange> validEvents = new ArrayList<>();
      for (Event event: events) {
          Set<String> copy = new HashSet<>(attendees);
          copy.retainAll(event.getAttendees());
          if (copy.size() > 0) {
              validEvents.add(event.getWhen());
          }
      }
      Collections.sort(validEvents, TimeRange.ORDER_BY_START);
      TimeRange initial = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true);
      List<TimeRange> result = new ArrayList<>();
      for (TimeRange range: validEvents) {
          if (range.contains(initial)) {
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
