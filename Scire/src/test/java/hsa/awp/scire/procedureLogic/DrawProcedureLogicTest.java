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

package hsa.awp.scire.procedureLogic;

import hsa.awp.campaign.facade.ICampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.rule.ICampaignRuleChecker;
import hsa.awp.common.exception.ProgrammingErrorException;
import hsa.awp.common.mail.IMail;
import hsa.awp.common.mail.IMailFactory;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.scire.exception.DuplicatePriorityListElementException;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.User;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link DrawProcedureLogic}. The main focus is on testing the draw algorithm.
 *
 * @author johannes
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/DrawProcedureLogicTest.xml")
public class DrawProcedureLogicTest {
  /**
   * {@link DrawProcedureLogic} under test.
   */
  private DrawProcedureLogic drawProcedureLogic;

  /**
   * {@link ICampaignFacade} for accessing domain models in the campaign component.
   */
  @Resource(name = "campaign.facade")
  private ICampaignFacade campaignFacade;

  /**
   * {@link IUserFacade} for accessing domain models in the singleUser component.
   */
  @Resource(name = "user.facade")
  private IUserFacade userFacade;

  /**
   * {@link IEventFacade} for accessing domain models in the event component.
   */
  @Resource(name = "event.facade")
  private IEventFacade eventFacade;

  /**
   * RuleChecker to check rules for campaigns.
   */
  @Resource(name = "rule.facade")
  private ICampaignRuleChecker campaignRuleChecker;

  /**
   * Campaign to use for testing.
   */
  private Campaign campaign;

  /**
   * Events to register with.
   */
  private List<Event> events;

  /**
   * {@link Subject} for generated {@link Event}s.
   */
  private Subject subject;

  /**
   * {@link DrawProcedure} as model object of the {@link DrawProcedureLogic}.
   */
  private DrawProcedure drawProcedure;

  /**
   * SingleUser to join the Event.
   */
  private SingleUser singleUser;

  /**
   * SingleUser who initiates the registration.
   */
  private SingleUser init;

  /**
   * Iterator of userids.
   */
  private long userId;

  @Before
  @Transactional
  public void setup() {

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password"));

    Mockery context = new JUnit4Mockery();
    final IMail mail = context.mock(IMail.class);
    context.checking(new Expectations() {
      {
        allowing(mail).send();
      }
    });

    IMailFactory mailFactory = new IMailFactory() {
      @Override
      public IMail getInstance(String recipient, String subject, String message, String sender) {

        return mail;
      }
    };

    drawProcedureLogic = new DrawProcedureLogic();

    drawProcedureLogic.setCampaignFacade(campaignFacade);
    drawProcedureLogic.setEventFacade(eventFacade);
    drawProcedureLogic.setUserFacade(userFacade);
    drawProcedureLogic.setMailFactory(mailFactory);
    drawProcedureLogic.setCampaignRuleChecker(campaignRuleChecker);

    /** create Campaign **/
    campaign = Campaign.getInstance(0L);
    campaign.setName("halloweltCampaignTest" + Math.random());
    campaign.setStartShow(Calendar.getInstance());
    campaign.setEndShow(Calendar.getInstance());
    campaignFacade.saveCampaign(campaign);

    /** create DrawProcedure **/
    drawProcedure = DrawProcedure.getInstance(0L);
    drawProcedure.setInterval(Calendar.getInstance(), Calendar.getInstance());
    drawProcedure.setName("hallowelt");
    drawProcedure.setMaximumPriorityLists(3);
    drawProcedure.setMaximumPriorityListItems(3);
    campaign.addProcedure(drawProcedure);
    campaignFacade.saveDrawProcedure(drawProcedure);

    drawProcedureLogic.setProcedure(drawProcedure);

    /** Create Subject **/
    subject = Subject.getInstance(0L);
    subject.setName("Testvorlesung");
    eventFacade.saveSubject(subject);

    events = new LinkedList<Event>();
    for (int i = 0; i < 30; i++) {
      events.add(createEvent(i));
    }
    /** merge the Campaign **/
    campaignFacade.updateCampaign(campaign);

    /** reset singleUser id counter **/
    userId = 0L;

    /** create SingleUser **/
    singleUser = createUser("halloWelt");
    userFacade.saveSingleUser(singleUser);

    /** create initiator **/
    init = createUser("init");
    userFacade.saveSingleUser(init);
  }

  /**
   * Generates an {@link Event} with the given event id. The created {@link Event} is saved in the database.
   *
   * @param eventId the id of the {@link Event}
   * @return the generated and saved {@link Event}
   */
  @Transactional
  private Event createEvent(int eventId) {

    Event event;

    event = Event.getInstance(eventId, 0L);
    event.setSubject(subject);
    eventFacade.saveEvent(event);

    subject.getEvents().add(event);
    campaign.getEventIds().add(event.getId());

    return event;
  }

  @Test
  @Transactional
  @DirtiesContext
  @Ignore //priolists will be deleted after the mails are sent because of this change the test fails
  public void testCleanUpAfterDraw() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);

    List<Event> events = new ArrayList<Event>(3);
    events.add(a);
    events.add(b);
    events.add(c);

    // prepare events for draw
    a.setMaxParticipants(0);
    b.setMaxParticipants(0);
    c.setMaxParticipants(3);

    generateRegistrationsSimple(events, 3);
    assertEquals(3, campaignFacade.getAllPriorityLists().size());

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    //cleanUp

    drawProcedureLogic.afterActive();

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals(0, drawProcedureLogic.getProcedure().getPriorityLists().size());
  }

  /**
   * Generates the specified amount simple registrations with the given priority lists. The {@link PriorityList} is generated from
   * the given event list.
   *
   * @param events the list of events where the {@link PriorityList} should be created from
   * @param amount the amount of registrations to be created
   */
  private void generateRegistrationsSimple(List<Event> events, int amount) {

    PriorityList prioList;
    SingleUser singleUser;

    for (int i = 0; i < amount; i++) {
      singleUser = createUser(Integer.toString(i));
      singleUser = userFacade.saveSingleUser(singleUser);
      prioList = generatePriorityList(singleUser, singleUser, events);
      drawProcedureLogic.register(prioList);
    }
  }

  @Test
  @Transactional
  @DirtiesContext
  @Ignore //priolists will be deleted after the mails are sent because of this change the test fails
  public void testCleanUpAfterDrawWithUserInstructedDeletionOfPrioLists() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);

    List<Event> events = new ArrayList<Event>(3);
    events.add(a);
    events.add(b);
    events.add(c);

    // prepare events for draw
    a.setMaxParticipants(0);
    b.setMaxParticipants(0);
    c.setMaxParticipants(3);

    generateRegistrationsSimple(events, 3);
    assertEquals(3, campaignFacade.getAllPriorityLists().size());

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    //user deletes priolist on his own
    PriorityList prioList = drawProcedureLogic.getProcedure().getPriorityLists().iterator().next();
    campaignFacade.removePriorityList(prioList);

    //cleanUp
    drawProcedureLogic.afterActive();

    assertEquals(0, drawProcedureLogic.getProcedure().getPriorityLists().size());
  }

  @Test
  @Transactional
  @DirtiesContext
  public void testDrawComplex1() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);
    Event d = events.get(3);
    Event e = events.get(4);
    Event f = events.get(5);

    // prepare events for draw
    a.setMaxParticipants(2);
    b.setMaxParticipants(2);
    c.setMaxParticipants(2);
    d.setMaxParticipants(2);
    e.setMaxParticipants(2);
    f.setMaxParticipants(2);

    generateAndRegisterComplexPriorityLists();

    // create a priority list which is not registered with this draw
    // procedure
    campaignFacade.savePriorityList(generatePriorityList(singleUser, init, events.subList(0, 2)));

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    // validate draw
    List<ConfirmedRegistration> crs = campaignFacade.getAllConfirmedRegistrations();

    // System.err.println(""
    // + "A=" + a.getConfirmedRegistrations().size() + " "
    // + "B=" + b.getConfirmedRegistrations().size() + " "
    // + "C=" + c.getConfirmedRegistrations().size() + " "
    // + "D=" + d.getConfirmedRegistrations().size() + " "
    // + "E=" + e.getConfirmedRegistrations().size() + " "
    // + "F=" + f.getConfirmedRegistrations().size());

    assertEquals(13, campaignFacade.getAllPriorityLists().size());
    assertEquals(12, crs.size());
    assertEquals(2, a.getConfirmedRegistrations().size());
    assertEquals(2, b.getConfirmedRegistrations().size());
    assertEquals(2, c.getConfirmedRegistrations().size());
    assertEquals(2, d.getConfirmedRegistrations().size());
    assertEquals(2, e.getConfirmedRegistrations().size());
    assertEquals(2, f.getConfirmedRegistrations().size());
  }

  /**
   * Generates and registers complex priority lists. The priority list combination is shown in the matrix below.
   * <p/>
   * <pre>
   * Symbols:
   * Priority list 1 : 1, 2, 3
   * Priority list 2 : 4, 5, 6
   *
   *     | A | B | C | D | E | F
   * ----+---+---+---+---+---+---
   *  s1 | 1 | 2 | 3 | 4 | 5 | 6
   * ----+---+---+---+---+---+---
   *  s2 | 6 | 1 | 2 | 3 | 4 | 5
   * ----+---+---+---+---+---+---
   *  s3 | 5 | 6 | 1 | 2 | 3 | 4
   * ----+---+---+---+---+---+---
   *  s4 | 1 | 4 | 2 | 5 | 3 | 6
   * ----+---+---+---+---+---+---
   *  s5 | 3 | 6 | 1 | 4 | 2 | 5
   * ----+---+---+---+---+---+---
   *  s6 | 2 | 5 | 3 | 6 | 1 | 4
   * </pre>
   */
  private void generateAndRegisterComplexPriorityLists() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);
    Event d = events.get(3);
    Event e = events.get(4);
    Event f = events.get(5);

    SingleUser singleUser;
    List<Event> events = new ArrayList<Event>();
    Set<PriorityList> lists = new HashSet<PriorityList>();

    // student 1
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("1"));
    events.add(a);
    events.add(b);
    events.add(c);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(d);
    events.add(e);
    events.add(f);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 1
    drawProcedureLogic.register(lists);

    // ------------------------------------------------------------------------------------------------------------------------

    // student 2
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("2"));
    events.add(b);
    events.add(c);
    events.add(d);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(e);
    events.add(f);
    events.add(a);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 2
    drawProcedureLogic.register(lists);

    // ------------------------------------------------------------------------------------------------------------------------

    // student 3
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("3"));
    events.add(c);
    events.add(d);
    events.add(e);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(f);
    events.add(a);
    events.add(b);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 3
    drawProcedureLogic.register(lists);

    // ------------------------------------------------------------------------------------------------------------------------

    // student 4
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("4"));
    events.add(a);
    events.add(c);
    events.add(e);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(b);
    events.add(d);
    events.add(f);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 4
    drawProcedureLogic.register(lists);

    // ------------------------------------------------------------------------------------------------------------------------

    // student 5
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("5"));
    events.add(c);
    events.add(e);
    events.add(a);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(d);
    events.add(f);
    events.add(b);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 5
    drawProcedureLogic.register(lists);

    // ------------------------------------------------------------------------------------------------------------------------

    // student 6
    events.clear();
    lists.clear();
    singleUser = userFacade.saveSingleUser(createUser("6"));
    events.add(e);
    events.add(a);
    events.add(c);
    lists.add(generatePriorityList(singleUser, singleUser, events));
    events.clear();
    events.add(f);
    events.add(b);
    events.add(d);
    lists.add(generatePriorityList(singleUser, singleUser, events));

    // register student 6
    drawProcedureLogic.register(lists);
  }

  /**
   * Creates a singleUser object and gives it a unique id.
   *
   * @param name - The login name of the singleUser.
   * @return A new singleUser object
   */
  private SingleUser createUser(String name) {

    SingleUser singleUser = SingleUser.getInstance();
    singleUser.setUuid(userId);
    userId++;
    return singleUser;
  }

  private PriorityList generatePriorityList(User participant, SingleUser initiator, List<Event> events) {
    // prepare event list
    List<Long> longs = new ArrayList<Long>(events.size());
    for (Event event : events) {
      longs.add(event.getId());
    }

    PriorityList p = PriorityList.getInstance(participant.getId(), initiator.getId(), longs, 0L);

    return p;
  }

  @Test
  @Transactional
  @DirtiesContext
  public void testDrawSimple1() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);

    List<Event> events = new ArrayList<Event>(3);
    events.add(a);
    events.add(b);
    events.add(c);

    // prepare events for draw
    a.setMaxParticipants(0);
    b.setMaxParticipants(0);
    c.setMaxParticipants(3);

    generateRegistrationsSimple(events, 3);
    assertEquals(3, campaignFacade.getAllPriorityLists().size());

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    // validate draw
    List<ConfirmedRegistration> crs = campaignFacade.getAllConfirmedRegistrations();

    assertEquals(3, campaignFacade.getAllPriorityLists().size());
    assertEquals(3, crs.size());
    assertEquals(0, a.getConfirmedRegistrations().size());
    assertEquals(0, b.getConfirmedRegistrations().size());
    assertEquals(3, c.getConfirmedRegistrations().size());
    assertEquals(c.getId(), crs.get(0).getEventId());
  }

  @Test
  @DirtiesContext
  @Transactional
  public void testDrawSimple2() {

    Event a = events.get(0);
    Event b = events.get(1);
    Event c = events.get(2);

    List<Event> events = new ArrayList<Event>(3);
    events.add(a);
    events.add(b);
    events.add(c);

    // prepare events for draw
    a.setMaxParticipants(2);
    b.setMaxParticipants(2);
    c.setMaxParticipants(2);

    generateRegistrationsSimple(events, 6);

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    // validate draw
    List<ConfirmedRegistration> crs = campaignFacade.getAllConfirmedRegistrations();

    assertEquals(6, campaignFacade.getAllPriorityLists().size());
    assertEquals(6, crs.size());
    assertEquals(2, a.getConfirmedRegistrations().size());
    assertEquals(2, b.getConfirmedRegistrations().size());
    assertEquals(2, c.getConfirmedRegistrations().size());
  }

  @Test
  @Transactional
  @DirtiesContext
  public void testDrawSpecial() {
    // prepare DrawProcedureLogic with mocked Random object
    Random rand = new Random() {
      private static final long serialVersionUID = 1L;

      @Override
      public int nextInt(int n) {

        return 0;
      }
    };
    drawProcedureLogic.setRandom(rand);

    Event a = events.get(0);
    Event b = events.get(1);

    // prepare events for draw
    a.setMaxParticipants(0);
    b.setMaxParticipants(2);

    // list to suspend
    List<Event> events = new ArrayList<Event>(2);
    events.add(a);
    events.add(b);
    generateRegistrationsSimple(events, 1);

    // list to kill the first
    events = new ArrayList<Event>(2);
    events.add(b);
    events.add(a);
    generateRegistrationsSimple(events, 1);

    assertEquals(2, campaignFacade.getAllPriorityLists().size());

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    drawProcedureLogic.whileActive();

    // validate draw
    List<ConfirmedRegistration> crs = campaignFacade.getAllConfirmedRegistrations();

    assertEquals(2, campaignFacade.getAllPriorityLists().size());
    assertEquals(2, crs.size());
    assertEquals(0, a.getConfirmedRegistrations().size());
    assertEquals(2, b.getConfirmedRegistrations().size());
    assertEquals(b.getId(), crs.get(0).getEventId());
    assertEquals(b.getId(), crs.get(1).getEventId());
    assertTrue(crs.get(0).getParticipant() != crs.get(1).getParticipant());
  }

  @Test
  @Transactional
  public void testRegister() {

    PriorityList list = PriorityList.getInstance(0L);
    list.addItem(events.get(5).getId(), 1);
    list.addItem(events.get(8).getId(), 2);
    list.addItem(events.get(13).getId(), 3);
    list.setInitiator(init.getId());
    list.setParticipant(singleUser.getId());

    drawProcedureLogic.register(list);

    assertEquals(1, campaignFacade.getAllPriorityLists().size());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Transactional
  public void testRegisterAlreadySaved() {

    PriorityList list = PriorityList.getInstance(0L);
    list.addItem(events.get(5).getId(), 1);
    list.addItem(events.get(8).getId(), 2);
    list.addItem(events.get(13).getId(), 3);
    list.setInitiator(init.getId());
    list.setParticipant(singleUser.getId());

    // save list
    campaignFacade.savePriorityList(list);

    // try to register with saved priority list
    drawProcedureLogic.register(list);
  }

  @Test
  @Transactional
  public void testRegisterExamOnly() {

    Event e = events.get(0);
    eventFacade.updateEvent(e);
    drawProcedureLogic.registerExamOnly(init, singleUser, e);
  }

  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRegisterSetOfListsDifferentInitiator() {
    // create different singleUser
    User p = createUser("TestParticipant");
    userFacade.saveUser(p);

    PriorityList prioList3 = PriorityList.getInstance(0L);
    prioList3.addItem(events.get(7).getId(), 1);
    prioList3.setInitiator(init.getId());
    prioList3.setParticipant(singleUser.getId());

    PriorityList prioList4 = PriorityList.getInstance(0L);
    prioList4.addItem(events.get(8).getId(), 1);
    // different Initiator
    prioList4.setInitiator(p.getId());
    prioList4.setParticipant(singleUser.getId());

    Set<PriorityList> registration = new HashSet<PriorityList>();
    registration.add(prioList3);
    registration.add(prioList4);

    drawProcedureLogic.register(registration);
  }

  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRegisterSetOfListsDifferentParticipant() {
    // create different singleUser
    SingleUser u = createUser("DrawTest");
    userFacade.saveSingleUser(u);

    PriorityList prioList3 = PriorityList.getInstance(0L);
    prioList3.addItem(events.get(7).getId(), 1);
    prioList3.setInitiator(init.getId());
    prioList3.setParticipant(singleUser.getId());

    PriorityList prioList4 = PriorityList.getInstance(0L);
    prioList4.addItem(events.get(8).getId(), 1);
    prioList4.setInitiator(init.getId());
    // different SingleUser
    prioList4.setParticipant(u.getId());

    Set<PriorityList> registration = new HashSet<PriorityList>();
    registration.add(prioList3);
    registration.add(prioList4);

    drawProcedureLogic.register(registration);
  }

  @Test(expected = DuplicatePriorityListElementException.class)
  @Transactional
  public void testRegisterSetOfListsDuplicateItems() {

    PriorityList prioList3 = PriorityList.getInstance(0L);
    prioList3.addItem(events.get(7).getId(), 1);
    prioList3.addItem(events.get(10).getId(), 2);
    prioList3.addItem(events.get(15).getId(), 3);
    prioList3.setInitiator(init.getId());
    prioList3.setParticipant(singleUser.getId());

    PriorityList prioList4 = PriorityList.getInstance(0L);
    prioList4.addItem(events.get(7).getId(), 1);
    prioList4.addItem(events.get(11).getId(), 2);
    prioList4.addItem(events.get(14).getId(), 3);
    prioList4.setInitiator(init.getId());
    prioList4.setParticipant(singleUser.getId());

    Set<PriorityList> registration = new HashSet<PriorityList>();
    registration.add(prioList3);
    registration.add(prioList4);

    drawProcedureLogic.register(registration);
  }

  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRegisterSetOfListsEmptySet() {

    drawProcedureLogic.register(new HashSet<PriorityList>());
  }

  @Test
  @Transactional
  public void testRegisterSetOfListsSuccessful() {

    PriorityList prioList1 = PriorityList.getInstance(0L);
    prioList1.addItem(events.get(5).getId(), 1);
    prioList1.addItem(events.get(8).getId(), 2);
    prioList1.addItem(events.get(13).getId(), 3);
    prioList1.setInitiator(init.getId());
    prioList1.setParticipant(singleUser.getId());

    PriorityList prioList2 = PriorityList.getInstance(0L);
    prioList2.addItem(events.get(6).getId(), 1);
    prioList2.addItem(events.get(9).getId(), 2);
    prioList2.addItem(events.get(14).getId(), 3);
    prioList2.setInitiator(init.getId());
    prioList2.setParticipant(singleUser.getId());

    PriorityList prioList3 = PriorityList.getInstance(0L);
    prioList3.addItem(events.get(7).getId(), 1);
    prioList3.addItem(events.get(10).getId(), 2);
    prioList3.addItem(events.get(15).getId(), 3);
    prioList3.setInitiator(init.getId());
    prioList3.setParticipant(singleUser.getId());

    PriorityList prioList4 = PriorityList.getInstance(0L);
    prioList4.addItem(events.get(20).getId(), 1);
    prioList4.addItem(events.get(21).getId(), 2);
    prioList4.addItem(events.get(22).getId(), 3);
    prioList4.setInitiator(init.getId());
    prioList4.setParticipant(singleUser.getId());

    Set<PriorityList> registration = new HashSet<PriorityList>();
    registration.add(prioList1);
    registration.add(prioList2);
    registration.add(prioList3);
    registration.add(prioList4);

    drawProcedureLogic.register(registration);

    List<PriorityList> prioLists = campaignFacade.getAllPriorityLists();
    assertEquals(4, prioLists.size());
  }
}
