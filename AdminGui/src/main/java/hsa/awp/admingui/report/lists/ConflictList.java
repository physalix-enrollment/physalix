/*
 * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
 *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
 *               Alexander Rampp, Kai Weidner
 *
 * This file is part of the Physalix Enrollment System
 *
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package hsa.awp.admingui.report.lists;

import hsa.awp.admingui.controller.IAdminGuiController;
import hsa.awp.admingui.report.util.ExportList;
import hsa.awp.admingui.report.util.Row;
import hsa.awp.admingui.report.util.formats.csv.CsvPrintable;
import hsa.awp.admingui.report.util.formats.csv.CsvProperties;
import hsa.awp.admingui.report.view.PrintableLists;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.event.model.Event;
import hsa.awp.user.model.SingleUser;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.*;

public class ConflictList extends ExportList implements CsvPrintable {
  /**
   * Controller which feeds the class with data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;

  Map<SingleUser, List<Event>> userEventListMap;
  Map<Event, List<SingleUser>> eventUserListMap;


  public ConflictList(List<Event> eventList) {
    InjectorHolder.getInjector().inject(this);

    userEventListMap = new HashMap<SingleUser, List<Event>>();
    eventUserListMap = new HashMap<Event, List<SingleUser>>();

    fillMaps(eventList);

    if (userEventListMap.size() == 0 || eventUserListMap.size() == 0) {
      return;
    }

    fillRows(eventList);
  }

  private void fillMaps(List<Event> events) {
    for (Event event : events) {
      event = controller.getEventById(event.getId());
      for (SingleUser user : getUsersFromEvent(event)) {
        List<SingleUser> userList = eventUserListMap.get(event);
        List<Event> eventList = userEventListMap.get(user);

        if (userList == null) {
          userList = new ArrayList<SingleUser>();
          eventUserListMap.put(event, userList);
        }
        if (eventList == null) {
          eventList = new ArrayList<Event>();
          userEventListMap.put(user, eventList);
        }

        userList.add(user);
        eventList.add(event);
      }
    }
  }


  private List<SingleUser> getUsersFromEvent(Event event) {
    List<SingleUser> users = new ArrayList<SingleUser>();

    for (Long regId : event.getConfirmedRegistrations()) {
      ConfirmedRegistration registration = controller.getConfirmedRegistrationById(regId);
      users.add(controller.getUserById(registration.getParticipant()));
    }
    return users;
  }


  private void fillRows(List<Event> eventList) {

    for (Event event : eventList) {
      List<String> content = new ArrayList<String>();

      content.add(expandInt(event.getEventId(), 4));
      Set<Integer> ids = new HashSet<Integer>();

      List<SingleUser> users = eventUserListMap.get(event);
      if (users == null) {
        users = new ArrayList<SingleUser>();

        content.add(expandInt(event.getEventId(), 4)); // every event conflicts with itself
      }
      for (SingleUser user : users) {
        List<Event> events = userEventListMap.get(user);
        if (events == null) {
          continue;
        }
        for (Event e : events) {
          ids.add(e.getEventId());
        }
      }
      for (Integer i : ids) {
        content.add(expandInt(i, 4));
      }

      rows.add(new Row(content));
    }


  }

  private String expandInt(int number, int digits) {
    String s = Integer.toString(number);

    while (s.length() < digits) {
      s = "0" + s;
    }
    return s;
  }

  @Override
  public String toString() {
    return PrintableLists.COURSECONFLICTLIST.getTitle();
  }

  @Override
  public CsvProperties getCsvProperties() {
    CsvProperties csvProperties = new CsvProperties();
    csvProperties.setSeparator(" ");
    return csvProperties;
  }
}
