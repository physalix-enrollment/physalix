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

package hsa.awp.scire.controller;

import hsa.awp.campaign.facade.ICampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.common.test.OpenEntityManagerInTest;
import hsa.awp.scire.procedureLogic.IProcedureLogic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/TestController.xml")
public class TestController extends OpenEntityManagerInTest {
  @Resource(name = "scire.controller")
  private IScireController controller;

  @Resource(name = "campaign.facade")
  private ICampaignFacade campaignFacade;

  private Logger logger;

  @Before
  public void setup() {

    logger = LoggerFactory.getLogger(this.getClass());
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password"));

    controller.setTimerInterval(1000);
  }

  @Test
  public void testCall() {

    Set<Class<? extends IProcedureLogic<?>>> types = new HashSet<Class<? extends IProcedureLogic<?>>>();
    types.add(LogicDummy.class);
    controller.setProcedureLogicTypeList(types);

    int interval = 500;
    controller.setTimerInterval(interval);

    Campaign camp = Campaign.getInstance(0L);

    Calendar endShow = Calendar.getInstance();
    endShow.roll(Calendar.YEAR, 1);

    camp.setStartShow(Calendar.getInstance());
    camp.setName("123");
    camp.setEndShow(endShow);
    campaignFacade.saveCampaign(camp);

    FifoProcedure p = FifoProcedure.getInstance(0L);
    Calendar startDate = Calendar.getInstance();
    startDate.add(Calendar.SECOND, 2);

    Calendar endDate = Calendar.getInstance();
    while (startDate.compareTo(endDate) >= 0) {
      endDate.add(Calendar.SECOND, 5);
    }

    p.setInterval(startDate, endDate);

    campaignFacade.saveFifoProcedure(p);

    camp.addProcedure(p);

    campaignFacade.updateCampaign(camp);

    controller.startTimer();

    int i = 0;
    boolean foundStarted = false;
    boolean foundTerminated = false;
    LogicDummy dummy = null;

    while (i++ < 300) {
      if (foundStarted) {
        if (controller.findActiveLogicByProcedure(p.getId()) == null) {
          foundTerminated = true;
          logger.info("found started procedure");
          break;
        }
      } else if (controller.findActiveLogicByProcedure(p.getId()) != null) {
        foundStarted = true;
        dummy = (LogicDummy) controller.findActiveLogicByProcedure(p.getId());
        logger.info("found terminated procedure");
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    assertTrue(foundStarted);
    assertTrue(foundTerminated);
    assertTrue(dummy.getAfterActiveCount() > 0);
    assertTrue(dummy.getBeforeActiveCount() > 0);
    assertTrue(dummy.getWhileActiveCount() > 0);

    controller.stopTimer();
  }

  @Test
  public void testStartCheck() {

    int interval = 500;

    controller.setTimerInterval(interval);
    controller.startTimer();
    assertTrue(controller.isCheckingForProcedureStates());
    controller.stopTimer();
    assertEquals(interval, controller.getTimerInterval());
  }
}
