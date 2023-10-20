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
import hsa.awp.common.mail.IMail;
import hsa.awp.common.mail.IMailFactory;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
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

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/DrawProcedureLogicTest.xml")
@Ignore
public class DrawLoadTest {
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
    campaign.addProcedure(drawProcedure);
    campaignFacade.saveDrawProcedure(drawProcedure);

    drawProcedureLogic.setProcedure(drawProcedure);

    /** Create Subject **/
    subject = Subject.getInstance(0L);
    subject.setName("Testvorlesung");
    eventFacade.saveSubject(subject);

    events = new LinkedList<Event>();
    for (int i = 0; i < 60; i++) {
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
    event.setMaxParticipants(25);
    eventFacade.saveEvent(event);

    subject.getEvents().add(event);
    campaign.getEventIds().add(event.getId());

    return event;
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

  @Test
  @Transactional
  @DirtiesContext
  public void testDrawComplex1() {

    generateAndRegisterComplexPriorityLists();

    // prepare Procedure for drawing
    Calendar date = Calendar.getInstance();
    date.add(Calendar.MINUTE, -1);
    drawProcedure.setDrawDate(date);

    // draw
    System.out.println("begin drawing");
    drawProcedureLogic.whileActive();
    System.out.println("finished drawing");

    // validate draw
    List<ConfirmedRegistration> crs = campaignFacade.getAllConfirmedRegistrations();

    assertTrue(drawProcedureLogic.isDrawn());
    assertTrue(crs.size() > 0);
  }

  private void generateAndRegisterComplexPriorityLists() {

    for (int i = 0; i < 2000; i++) {
      singleUser = userFacade.saveSingleUser(createUser(String.valueOf(i)));
      Set<PriorityList> lists = new HashSet<PriorityList>();
      List<Event> prioEvents = new LinkedList<Event>();

      if (i % 200 == 0) {
        System.out.println(i + "Lists have been created");
      }

      prioEvents.add(events.get((i + 3) % 60));
      prioEvents.add(events.get((i + 7) % 60));
      prioEvents.add(events.get((i + 13) % 60));

      lists.add(generatePriorityList(singleUser, singleUser, prioEvents));
      prioEvents.clear();

      prioEvents.add(events.get((i + 17) % 60));
      prioEvents.add(events.get((i + 19) % 60));
      prioEvents.add(events.get((i + 31) % 60));

      lists.add(generatePriorityList(singleUser, singleUser, prioEvents));
      prioEvents.clear();

      prioEvents.add(events.get((i + 11) % 60));
      prioEvents.add(events.get((i + 23) % 60));
      prioEvents.add(events.get((i + 29) % 60));

      lists.add(generatePriorityList(singleUser, singleUser, prioEvents));
      drawProcedureLogic.register(lists);
    }
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
}
