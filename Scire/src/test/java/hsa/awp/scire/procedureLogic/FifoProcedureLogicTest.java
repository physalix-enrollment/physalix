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
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.campaign.rule.ICampaignRuleChecker;
import hsa.awp.common.dao.template.TemplateFileSystemDao;
import hsa.awp.common.dao.template.TemplateJarDao;
import hsa.awp.common.exception.NoSpaceAvailableException;
import hsa.awp.common.mail.IMail;
import hsa.awp.common.mail.IMailFactory;
import hsa.awp.common.naming.DummyUserData;
import hsa.awp.common.services.TemplateService;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.SingleUser;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
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
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/FifoProcedureLogicTest.xml")
public class FifoProcedureLogicTest {
  private FifoProcedureLogic fifoProcedureLogic;

  @Resource(name = "campaign.facade")
  private ICampaignFacade campaignFacade;

  @Resource(name = "user.facade")
  private IUserFacade userFacade;

  @Resource(name = "event.facade")
  private IEventFacade eventFacade;

  @Resource(name = "common.template.service")
  private TemplateService templateService;

  @Resource(name = "common.template.dao.jar")
  private TemplateJarDao templateJarDao;

  @Resource(name = "common.template.dao.filesystem")
  private TemplateFileSystemDao templateFileSystemDao;

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
   * Event to register with.
   */
  private Event event;

  /**
   * FifoProcedure as model object of the {@link FifoProcedureLogic}.
   */
  private FifoProcedure fifoProcedure;

  /**
   * Participant to join the Event.
   */
  private SingleUser singleUser;

  /**
   * SingleUser who initiates the registration.
   */
  private SingleUser init;

  @Before
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

//    String path = System.getProperty("user.home") + "/physalix/templates/";
//    VelocityEngine velocityEngine = new VelocityEngine();
//    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
//    velocityEngine.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
//    velocityEngine.setProperty("file.resource.loader.path", path);
//    velocityEngine.init();
//    templateFileSystemDao.setTemplatePath(path);
//    templateService.setVelocityEngine(velocityEngine);

    fifoProcedureLogic = new FifoProcedureLogic();

    fifoProcedureLogic.setCampaignFacade(campaignFacade);
    fifoProcedureLogic.setEventFacade(eventFacade);
    fifoProcedureLogic.setUserFacade(userFacade);
    fifoProcedureLogic.setMailFactory(mailFactory);
    fifoProcedureLogic.setCampaignRuleChecker(campaignRuleChecker);
    fifoProcedureLogic.setTemplateService(templateService);


    /** create Campaign **/
    campaign = Campaign.getInstance(0L);
    campaign.setName("halloweltCampaignTest" + Math.random());
    campaign.setStartShow(Calendar.getInstance());
    campaign.setEndShow(Calendar.getInstance());
    campaignFacade.saveCampaign(campaign);

    /** create FifoProcedure **/
    fifoProcedure = FifoProcedure.getInstance(1L);
    fifoProcedure.setInterval(Calendar.getInstance(), Calendar.getInstance());
    fifoProcedure.setName("hallowelt");
    campaign.addProcedure(fifoProcedure);
    campaignFacade.saveFifoProcedure(fifoProcedure);

    fifoProcedureLogic.setProcedure(fifoProcedure);

    /** Create Subject **/
    Subject subject = Subject.getInstance(0L);
    subject.setName("Testvorlesung");
    eventFacade.saveSubject(subject);

    event = Event.getInstance(5, 0L);
    eventFacade.saveEvent(event);
    event.setSubject(subject);
    subject.getEvents().add(event);
    campaign.getEventIds().add(event.getId());

    /** merge the Campaign **/
    campaignFacade.updateCampaign(campaign);

    /** create SingleUser **/
    singleUser = SingleUser.getInstance(DummyUserData.getStudentLogin());
    singleUser.setName("abc");
    userFacade.saveSingleUser(singleUser);

    /** create initiator **/
    init = SingleUser.getInstance(DummyUserData.getSecretaryLogin());
    userFacade.saveSingleUser(init);
  }

  /**
   * Register with a {@link FifoProcedure} having a max participant count of 0.
   */
  @Transactional
  @Test(expected = NoSpaceAvailableException.class)
  public void testNoSpaceAvailable() {

    fifoProcedureLogic.register(event, singleUser, init, false);
  }

  /**
   * Registers with a {@link FifoProcedure} and checks whether all necessary fields are being set.
   */
  @Transactional
  @Test
  public void testRegisterWithFifoProcedure() throws Exception {

    event.setMaxParticipants(50);

    eventFacade.getEventById(event.getId());

    /** register the SingleUser with the FifoProcedure **/
    fifoProcedureLogic.register(event, singleUser, init, false);
    assertEquals("Event should have a confirmed registration attached", 1, event.getConfirmedRegistrations().size());

    for (Long id : event.getConfirmedRegistrations()) {
      System.out.println(id);
    }

    /** search the ConfirmedRegistration in Event **/
    event = eventFacade.getEventById(event.getId());
    boolean foundInEvent = false;
    for (Long regId : event.getConfirmedRegistrations()) {
      ConfirmedRegistration reg = campaignFacade.getConfirmedRegistrationById(regId);
      if (reg.getInitiator().equals(init.getId()) && reg.getParticipant().equals(singleUser.getId())) {
        foundInEvent = true;
      }
    }
    if (!foundInEvent) {
      fail("ConfirmedRegistration not found in Event");
    }
  }
}
