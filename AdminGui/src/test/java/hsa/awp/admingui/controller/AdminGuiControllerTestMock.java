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

package hsa.awp.admingui.controller;

import hsa.awp.campaign.facade.ICampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Category;
import hsa.awp.event.model.Event;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test class used for testing all *logic* elements of {@link AdminGuiController}.
 *
 * @author klassm
 */
@RunWith(JMock.class)
public class AdminGuiControllerTestMock {
  /**
   * {@link AdminGuiController} object used for testing.
   */
  private AdminGuiController adminGuiController;

  /**
   * Mocked {@link ICampaignFacade}.
   */
  private ICampaignFacade campaignFacade;

  /**
   * Mock context.
   */
  private Mockery context = new JUnit4Mockery();

  /**
   * Mocked {@link IEventFacade}.
   */
  private IEventFacade eventFacade;

  /**
   * Setup the test context. Create {@link AdminGuiController}, {@link IEventFacade} and {@link ICampaignFacade}.
   */
  @Before
  public void setup() {

    adminGuiController = new AdminGuiController();
    eventFacade = context.mock(IEventFacade.class);
    campaignFacade = context.mock(ICampaignFacade.class);

    adminGuiController.setEventFacade(eventFacade);
    adminGuiController.setCampaignFacade(campaignFacade);
  }

  /**
   * Test the alteration of a {@link Campaign}.
   */
  @Test
  public void testAlterCampaign() {

    final Campaign testCampaign = Campaign.getInstance(0L);

    context.checking(new Expectations() {
      {
        oneOf(campaignFacade).getCampaignById(0L);
        will(returnValue(testCampaign));

        oneOf(campaignFacade).updateCampaign(testCampaign);
        will(returnValue(testCampaign));
      }
    });

    Calendar endShow = Calendar.getInstance();
    Calendar startShow = Calendar.getInstance();

    List<Procedure> procedures = new LinkedList<Procedure>();
    procedures.add(FifoProcedure.getInstance(0L));

    List<Long> events = new LinkedList<Long>();
    events.add(1234L);

    Campaign campaign = adminGuiController.alterCampaign(0L, endShow, startShow, procedures, events);

    assertNotNull(campaign);
    assertEquals(0L, campaign.getId().longValue());
    assertEquals(endShow, campaign.getEndShow());
    assertEquals(startShow, campaign.getStartShow());
    assertEquals(procedures.size(), campaign.getAppliedProcedures().size());
    assertEquals(events.size(), campaign.getEventIds().size());
  }

  /**
   * Test the alteration of a {@link DrawProcedure}.
   */
  @Test
  public void testAlterDrawProcedure() {

    final DrawProcedure draw = DrawProcedure.getInstance(0L);

    context.checking(new Expectations() {
      {
        oneOf(campaignFacade).getDrawProcedureById(0L);
        will(returnValue(draw));

        oneOf(campaignFacade).updateDrawProcedure(draw);
        will(returnValue(draw));
      }
    });

    String name = "hallowelt " + Math.random();
    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();
    Calendar drawDate = Calendar.getInstance();

    DrawProcedure proc = adminGuiController.alterDrawProcedure(0L, name, startDate, endDate, drawDate);

    assertNotNull(proc);
    assertEquals(name, proc.getName());
    assertEquals(startDate, proc.getStartDate());
    assertEquals(endDate, proc.getEndDate());
    assertEquals(drawDate, proc.getDrawDate());
  }

  /**
   * Test the alteration of a {@link FifoProcedure}.
   */
  @Test
  public void testAlterFifoProcedure() {

    final FifoProcedure fifo = FifoProcedure.getInstance(0L);

    context.checking(new Expectations() {
      {
        oneOf(campaignFacade).getFifoProcedureById(0L);
        will(returnValue(fifo));

        oneOf(campaignFacade).updateFifoProcedure(fifo);
        will(returnValue(fifo));
      }
    });

    String name = "hallowelt " + Math.random();
    Calendar startDate = Calendar.getInstance();
    Calendar endDate = Calendar.getInstance();

    FifoProcedure proc = adminGuiController.alterFifoProcedure(0L, name, startDate, endDate);

    assertNotNull(proc);
    assertEquals(name, proc.getName());
    assertEquals(startDate, proc.getStartDate());
    assertEquals(endDate, proc.getEndDate());
  }

  /**
   * Test the creation of a {@link Campaign}.
   */
  @Test
  public void testCreateCampaign() {
    // String name, Calendar endShow, Calendar startShow, List<Long> evts, List<Procedure> proc
    final Campaign testCampaign = Campaign.getInstance(0L);

    context.checking(new Expectations() {
      {
        oneOf(campaignFacade).saveCampaign(testCampaign);
        will(returnValue(null));
      }
    });

    String name = "hallo" + Math.random();
    String email = "test@test.de";

    Calendar endShow = Calendar.getInstance();
    endShow.add(Calendar.MONTH, 1);

    Calendar startShow = Calendar.getInstance();

    List<Procedure> procedures = new LinkedList<Procedure>();
    procedures.add(FifoProcedure.getInstance(0L));

    Set<Long> events = new HashSet<Long>();
    events.add(32L);
    events.add(13L);

    Set<Long> studyCourses = new HashSet<Long>();
    studyCourses.add(32L);
    studyCourses.add(13L);

    Campaign campaign = adminGuiController.createCampaign(name, email, endShow, startShow, events, procedures, null, studyCourses, "text", 0);

    assertNotNull(campaign);
    assertEquals(0L, campaign.getId().longValue());
    assertEquals(endShow, campaign.getEndShow());
    assertEquals(startShow, campaign.getStartShow());
    assertEquals(procedures.size(), campaign.getAppliedProcedures().size());
    assertEquals(studyCourses.size(), campaign.getStudyCourseIds().size());
  }

  /**
   * Test the creation of a {@link Category}.
   */
  @Test
  public void testCreateCategory() {

    String name = "testCatName";
    final Category testCat = Category.getInstance(name, 0L);

    context.checking(new Expectations() {
      {
        oneOf(eventFacade).saveCategory(testCat);
        will(returnValue(testCat));
      }
    });

    Category cat = adminGuiController.createCategory(name, 0L);

    assertNotNull(cat);
    assertEquals(cat.getName(), name);
  }

  /**
   * Test the conversion of a given {@link Set} of eventIds into a {@link List} of {@link Event}s.
   */
  @Test
  public void testGetEventsById() {

    final Map<Long, Event> events = new HashMap<Long, Event>();
    events.put(13L, Event.getInstance(8, 0L));
    events.put(21L, Event.getInstance(7, 0L));
    events.put(33L, Event.getInstance(3, 0L));

    context.checking(new Expectations() {
      {
        oneOf(eventFacade).getEventById(13L);
        will(returnValue(events.get(13L)));

        oneOf(eventFacade).getEventById(21L);
        will(returnValue(events.get(21L)));

        oneOf(eventFacade).getEventById(33L);
        will(returnValue(events.get(33L)));
      }
    });

    List<Event> eventsConverted = adminGuiController.getEventsById(events.keySet());

    assertNotNull(eventsConverted);
    assertEquals(events.size(), eventsConverted.size());

    for (Event original : events.values()) {
      boolean found = false;
      for (Event converted : eventsConverted) {
        if (converted.getEventId() == original.getEventId()) {
          found = true;
          break;
        }
      }
      if (!found) {
        fail(original + " was not found");
      }
    }
  }
}
