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
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.campaign.rule.ICampaignRuleChecker;
import hsa.awp.common.mail.IMail;
import hsa.awp.common.mail.IMailFactory;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.Group;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/AbstractProcedureLogicTest.xml")
public class AbstractProcedureLogicTest {
  @Resource(name = "campaign.facade")
  private ICampaignFacade campFac;

  @Resource(name = "event.facade")
  private IEventFacade eventFac;

  @Resource(name = "user.facade")
  private IUserFacade userFac;

  /**
   * RuleChecker to check rules for campaigns.
   */
  @Resource(name = "rule.facade")
  private ICampaignRuleChecker campaignRuleChecker;

  private AbstractProcedureLogic<FifoProcedure> abs;

  @Before
  public void setUp() throws Exception {

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

    this.abs = new FifoProcedureLogic();
    // Set the Factories to the AbstractRegistration
    abs.setMailFactory(mailFactory);
    abs.setUserFacade(userFac);
    abs.setEventFacade(eventFac);
    abs.setCampaignFacade(campFac);
    abs.setCampaignRuleChecker(campaignRuleChecker);
  }

  @Test(expected = RuntimeException.class)
  public void testCheckRuleGroup() {

    Group group = Group.getInstance();
    abs.checkRules(group, null);
  }

  @Test
  public void testCheckRules() {

    ICampaignRuleChecker trueDummy = new ICampaignRuleChecker() {
      @Override
      public boolean isRegistrationAllowed(SingleUser user, Campaign campaign, Event event) {

        return true;
      }
    };

    abs.setCampaignRuleChecker(trueDummy);

    FifoProcedure procedure = FifoProcedure.getInstance(0L);
    abs.setProcedure(procedure);

    SingleUser user = SingleUser.getInstance();
    abs.checkRules(user, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckRulesRejected() {

    ICampaignRuleChecker falseDummy = new ICampaignRuleChecker() {
      @Override
      public boolean isRegistrationAllowed(SingleUser user, Campaign campaign, Event event) {

        return false;
      }
    };

    abs.setCampaignRuleChecker(falseDummy);

    FifoProcedure procedure = FifoProcedure.getInstance(0L);
    abs.setProcedure(procedure);

    SingleUser user = SingleUser.getInstance();
    abs.checkRules(user, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  @Transactional
  @Ignore
  // invalid test data
  public void testSendMail() {
    // Create Event.
    Event evt = Event.getInstance(1234, 0L);

    SingleUser singleUser5 = createUser("gesslein");
    singleUser5.setName("abc");

    // Create Subject.
    Subject sub = Subject.getInstance(0L);
    sub.setName("Bombenbau für Anfänger");

    // Save the Subject, Event, Participant.
    // And set the Subject to the Event.
    eventFac.saveSubject(sub);
    evt.setSubject(sub);
    eventFac.saveEvent(evt);

    // Create the ConfirmedRegistration.
    Campaign camp = Campaign.getInstance(0L);

    FifoProcedure proc = FifoProcedure.getInstance(0L);
    campFac.saveFifoProcedure(proc);

    camp.addProcedure(proc);
    campFac.saveCampaign(camp);
    campFac.saveFifoProcedure(proc);

    ConfirmedRegistration conf = ConfirmedRegistration.getInstance(evt.getId(), 0L);
    conf.setParticipant(singleUser5.getId());
    conf.setProcedure(proc);

    // Send a mail
    ProcedureLogicFactory plf = new ProcedureLogicFactory();
    abs = (AbstractProcedureLogic<FifoProcedure>) plf.getInstance(FifoProcedureLogic.class);
    abs.sendMail(conf);
  }

  /**
   * Creates a user object and gives it a unique id.
   *
   * @param name - The login name of the user.
   * @return A new user object
   */
  private SingleUser createUser(String name) {

    SingleUser user = SingleUser.getInstance();
    user = userFac.saveSingleUser(user);
    return user;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSendMailNull() {

    abs.sendMail(null);
  }

  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testSendMailWrongParticipant() {
    // Create Event.
    Event evt = Event.getInstance(42354, 0L);
    // Save the Event.
    eventFac.saveEvent(evt);

    // Create the ConfirmedRegistration. Without a Participant and a Subject. A IllegalArgumentException must appear.
    ConfirmedRegistration conf = ConfirmedRegistration.getInstance(evt.getId(), 0L);

    // Send a Mail.
    abs.sendMail(conf);
  }

  @Test
  public void testSetProcedure() {

    FifoProcedure procedure = FifoProcedure.getInstance(0L);
    assertTrue(!(procedure.equals(abs.procedure)));
    abs.setProcedure(procedure);
    assertEquals(procedure, abs.procedure);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetProcedureNull() {

    abs.setProcedure(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetProcedureWrong() {

    DrawProcedure procedure = DrawProcedure.getInstance(0L);
    abs.setProcedure(procedure);
  }

  @Test
  @Transactional
  public void testSingleRegistration() {

    Event evt = Event.getInstance(3456, 0L);
    evt.setMaxParticipants(5);
    eventFac.saveEvent(evt);
    User part = createUser("participant");
    SingleUser init = createUser("Kai");

    FifoProcedure procedure = FifoProcedure.getInstance(0L);
    campFac.saveFifoProcedure(procedure);

    Campaign campaign = Campaign.getInstance(0L);
    campaign.setName("testcampaign");
    campaign.addProcedure(procedure);
    campFac.saveCampaign(campaign);

    abs.setProcedure(procedure);

    ConfirmedRegistration conftest = ConfirmedRegistration.getInstance(evt.getId(), 0L);
    conftest.setDate(Calendar.getInstance());
    conftest.setExamOnly(true);
    conftest.setParticipant(part.getId());
    conftest.setInitiator(init.getId());

    campFac.saveConfirmedRegistration(conftest);

    ConfirmedRegistration conf = abs.singleRegistration(evt, part, init, true);

    assertEquals(conftest.getEventId(), conf.getEventId());
    assertEquals(conftest.getInitiator(), conf.getInitiator());
    assertEquals(conftest.getParticipant(), conf.getParticipant());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleRegistrationNull() {

    abs.singleRegistration(null, null, null, true);
  }

  @Test
  @Transactional
  public void testSingleRegistrationWithRule() {

    Event evt = Event.getInstance(3456, 0L);
    evt.setMaxParticipants(5);
    eventFac.saveEvent(evt);
    User part = createUser("participant");
    SingleUser init = createUser("Kai");

    FifoProcedure procedure = FifoProcedure.getInstance(0L);
    campFac.saveFifoProcedure(procedure);

    Campaign campaign = Campaign.getInstance(0L);
    campaign.setName("testcampaign");
    campaign.addProcedure(procedure);
    campFac.saveCampaign(campaign);

    abs.setProcedure(procedure);

    ConfirmedRegistration conftest = ConfirmedRegistration.getInstance(evt.getId(), 0L);
    conftest.setDate(Calendar.getInstance());
    conftest.setExamOnly(true);
    conftest.setParticipant(part.getId());
    conftest.setInitiator(init.getId());

    campFac.saveConfirmedRegistration(conftest);

    ConfirmedRegistration conf = abs.singleRegistration(evt, part, init, true);

    assertEquals(conftest.getEventId(), conf.getEventId());
    assertEquals(conftest.getInitiator(), conf.getInitiator());
    assertEquals(conftest.getParticipant(), conf.getParticipant());
  }
}
