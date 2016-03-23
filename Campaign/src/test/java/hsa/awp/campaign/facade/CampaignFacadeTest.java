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

package hsa.awp.campaign.facade;

import hsa.awp.campaign.model.*;
import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.common.test.AbstractFacadeTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link CampaignFacadeTest}.
 *
 * @author kai
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/campaignFacadeTest.xml")
@TransactionConfiguration(defaultRollback = true)
public class CampaignFacadeTest extends AbstractFacadeTest<CampaignFacade> {
  /**
   * Campaign facade.
   */
  @Resource(name = "campaign.facade")
  private ICampaignFacade campFac;

  /**
   * Initializes the {@link AbstractFacadeTest}.
   */
  public CampaignFacadeTest() {

    super(CampaignFacade.class);
  }

  /**
   * We do not want to test security issues. So we set our login to admin.
   */
  @Before
  public void setUp() {

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password"));
  }

  @Transactional
  @Test
  public void testEventIds() {

    Campaign c = Campaign.getInstance(0L);
    c.getEventIds().add(1L);
    c.getEventIds().add(4L);
    c.getEventIds().add(6L);
    c.setName("abc");
    campFac.saveCampaign(c);

    Campaign cFound = campFac.getCampaignById(c.getId());
    assertEquals(c.getEventIds().size(), cFound.getEventIds().size());
  }

  @Test
  @Transactional
  public void testFindActiveProcedureSince() {

    Campaign camp = Campaign.getInstance(0L);
    camp.setName("hallowelt");
    campFac.saveCampaign(camp);

    Procedure p1 = DrawProcedure.getInstance(0L);

    Calendar p1StartDate = Calendar.getInstance();
    p1StartDate.add(Calendar.SECOND, -1);

    Calendar p1EndDate = Calendar.getInstance();
    p1EndDate.add(Calendar.SECOND, 1);

    p1.setInterval(p1StartDate, p1EndDate);
    p1.setName("testFindActiveProcedureSince");

    campFac.saveProcedure(p1);

    camp.addProcedure(p1);

    // --------------

    Calendar since = Calendar.getInstance();
    since.add(Calendar.SECOND, -2);

    List<Procedure> res = campFac.findActiveProcedureSince(since);
    assertEquals(1, res.size());
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getAllCampaigns()}.
   */
  @Test
  @Transactional
  public void testGetAllCampaigns() {
    clearCampaigns();

    List<Campaign> list = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("hallowelt" + i);
      c = campFac.saveCampaign(c);

      list.add(c);
    }
    assertTrue(list.containsAll(campFac.getAllCampaigns()));
  }

  private void clearCampaigns() {
    List<Campaign> allPreviouslyFound = campFac.getAllCampaigns();
    for (Campaign campaign : allPreviouslyFound) {
      campFac.removeCampaign(campaign);
    }
  }

  /**
   * Persists some special procedures and tries to find all as normal procedures again.
   */
  @Test
  public void testGetAllProcedures() {

    List<Procedure> procedure = new LinkedList<Procedure>();
    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        FifoProcedure p = FifoProcedure.getInstance(0L);
        p.setName("proc " + i);
        campFac.saveFifoProcedure(p);
        procedure.add(p);
      } else {
        DrawProcedure p = DrawProcedure.getInstance(0L);
        p.setName("proc " + i);
        campFac.saveDrawProcedure(p);
        procedure.add(p);
      }
    }

    List<Procedure> found = campFac.getAllProcedures();

    assertEquals(procedure.size(), found.size());
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getCampaignById(java.lang.Long)} .
   */
  @Test
  @Transactional
  public void testGetCampaignById() {

    Campaign c = Campaign.getInstance(0L);
    c.setName("hallowelt" + Math.random());
    c = campFac.saveCampaign(c);

    assertEquals(c, campFac.getCampaignById(c.getId()));
  }

  /**
   * Test if an IllegalArgumenException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testGetCampaignByIdNull() {

    List<Campaign> list = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("hallowelt" + i);
      c = campFac.saveCampaign(c);
      list.add(c);
    }
    campFac.getCampaignById(null);
  }

  /**
   * Test if a DataAccessExeption appears when the method is called with a wrong number.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testGetCampaignByIdWrongId() {

    List<Campaign> list = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c = campFac.saveCampaign(c);
      list.add(c);
    }
    long wrongNumber = 4325;
    campFac.getCampaignById(wrongNumber);
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getConfirmProcedureById(java.lang.Long)} .
   */
  @Test
  @Transactional
  public void testGetConfirmProcedureById() {

    ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
    c = campFac.saveConfirmProcedure(c);
    assertEquals(c, campFac.getConfirmProcedureById(c.getId()));
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testGetConfirmProcedureByIdNull() {

    List<ConfirmProcedure> list = new ArrayList<ConfirmProcedure>();
    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      list.add(c);
    }
    campFac.getDrawProcedureById(null);
  }

  /**
   * Tests if an DataAccessExeption appears when the method is called with a wrong number.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testGetConfirmProcedureByIdWrongId() {

    List<ConfirmProcedure> list = new ArrayList<ConfirmProcedure>();
    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      list.add(c);
    }
    long wrongNumber = 4234;
    campFac.getConfirmProcedureById(wrongNumber);
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getConfirmedRegistrationById(java.lang.Long)} .
   */
  @Test
  @Ignore
  public void testGetConfirmedRegistrationById() {

  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getDrawProcedureById(java.lang.Long)} .
   */
  @Test
  @Transactional
  public void testGetDrawProcedureById() {

    DrawProcedure d = DrawProcedure.getInstance(0L);
    d = campFac.saveDrawProcedure(d);
    assertEquals(d, campFac.getDrawProcedureById(d.getId()));
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testGetDrawProcedureByIdNull() {

    List<DrawProcedure> list = new ArrayList<DrawProcedure>();
    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      list.add(d);
    }
    campFac.getDrawProcedureById(null);
  }

  /**
   * Tests if an DataAccessException appears when the method is called with a wrong number.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testGetDrawProcedureByIdWrongId() {

    List<DrawProcedure> list = new ArrayList<DrawProcedure>();
    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);

      list.add(d);
    }
    long wrongNumber = 4234;
    campFac.getDrawProcedureById(wrongNumber);
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#getFifoProcedureById(java.lang.Long)} .
   */
  @Test
  @Transactional
  public void testGetFifoProcedureById() {

    FifoProcedure f = FifoProcedure.getInstance(0L);
    f = campFac.saveFifoProcedure(f);
    assertEquals(f, campFac.getFifoProcedureById(f.getId()));
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testGetFifoProcedureByIdNull() {

    List<FifoProcedure> list = new ArrayList<FifoProcedure>();
    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      list.add(f);
    }
    campFac.getDrawProcedureById(null);
  }

  /**
   * Tests if an DataAccessExeption appears when the method is called with a wrong number.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testGetFifoProcedureByIdWrongId() {

    List<FifoProcedure> list = new ArrayList<FifoProcedure>();
    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      list.add(f);
    }
    long wrongNumber = 4234;
    campFac.getDrawProcedureById(wrongNumber);
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#removeCampaign(hsa.awp.campaign.model.Campaign)} .
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveCampaign() {

    List<Campaign> campaigns = new ArrayList<Campaign>();

    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("123" + i);
      c = campFac.saveCampaign(c);
      campaigns.add(c);
    }
    campFac.removeCampaign(campaigns.get(4));
    campFac.getCampaignById(campaigns.get(4).getId());
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRemoveCampaignNull() {

    List<Campaign> campaigns = new ArrayList<Campaign>();

    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("hallowelt123" + i);
      c = campFac.saveCampaign(c);
      campaigns.add(c);
    }
    campFac.removeCampaign(null);
  }

  /**
   * Tests if an DataAccessException appears when the method is called with a not existent Campaign.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveCampaignWrongId() {

    List<Campaign> campaigns = new ArrayList<Campaign>();

    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c = campFac.saveCampaign(c);
      campaigns.add(c);
    }
    campFac.removeCampaign(Campaign.getInstance(0L));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#removeConfirmProcedure(ConfirmProcedure)} .
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveConfirmProcedure() {

    List<ConfirmProcedure> campaigns = new ArrayList<ConfirmProcedure>();

    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      campaigns.add(c);
    }
    campFac.removeConfirmProcedure(campaigns.get(4));
    campFac.getConfirmProcedureById(campaigns.get(4).getId());
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRemoveConfirmProcedureNull() {

    List<ConfirmProcedure> campaigns = new ArrayList<ConfirmProcedure>();

    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      campaigns.add(c);
    }
    campFac.removeConfirmProcedure(null);
  }

  /**
   * Tests if an DataAccessException appears when the method is called with a not existing ConfirmProcedure.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveConfirmProcedureWrongId() {

    List<ConfirmProcedure> campaigns = new ArrayList<ConfirmProcedure>();

    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      campaigns.add(c);
    }
    campFac.removeConfirmProcedure(ConfirmProcedure.getInstance(0L));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#removeDrawProcedure(hsa.awp.campaign.model.DrawProcedure)} .
   */
  @Test
  @Transactional
  public void testRemoveDrawProcedure() {

    int procedureCount = 5;
    int prioListCount = 500;

    List<DrawProcedure> procedures = new ArrayList<DrawProcedure>();

    for (int i = 0; i < procedureCount; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      procedures.add(d);
    }

    for (int i = 0; i < prioListCount; i++) {
      PriorityList prioList = PriorityList.getInstance(0L);
      prioList.setInitiator(5L);
      prioList.setParticipant(5L);

      campFac.savePriorityList(prioList);
      procedures.get(i % procedureCount).addPriorityList(prioList);
    }

    for (DrawProcedure proc : procedures) {
      campFac.removeDrawProcedure(proc);
    }
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRemoveDrawProcedureNull() {

    List<DrawProcedure> draws = new ArrayList<DrawProcedure>();

    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      draws.add(d);
    }
    campFac.removeDrawProcedure(null);
  }

  /**
   * Tests if an DataAccessException appears when the method is called with a not existent DrawProcedure.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveDrawProcedureWrongId() {

    List<DrawProcedure> draws = new ArrayList<DrawProcedure>();

    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      draws.add(d);
    }
    campFac.removeDrawProcedure(DrawProcedure.getInstance(0L));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#removeFifoProcedure(FifoProcedure)} .
   */
  @Test
  @Transactional
  public void testRemoveFifoProcedure() {

    for (FifoProcedure fifo : campFac.getAllFifoProcedures()) {
      for (ConfirmedRegistration c : campFac.getConfirmedRegistrationsByProcedure(fifo)) {
        campFac.removeConfirmedRegistration(c);
      }
      campFac.removeFifoProcedure(fifo);
    }

    assertEquals(0, campFac.getAllFifoProcedures().size());
    assertEquals(0, campFac.getAllConfirmedRegistrations().size());

    int procedureCount = 5;
    int confirmedRegistrationsCount = 500;

    List<FifoProcedure> procedures = new ArrayList<FifoProcedure>();

    for (int i = 0; i < procedureCount; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      procedures.add(f);
    }

    for (int i = 0; i < confirmedRegistrationsCount; i++) {
      ConfirmedRegistration confReg = ConfirmedRegistration.getInstance(5L, 0L);
      confReg.setProcedure(procedures.get(i % procedureCount));
    }

    for (FifoProcedure fifo : procedures) {
      campFac.removeFifoProcedure(fifo);
    }

    assertEquals(0, campFac.getAllFifoProcedures().size());
    assertEquals(0, campFac.getAllConfirmedRegistrations().size());
  }

  /**
   * Tests if an IllegalArgumentException appears when the method is called with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  @Transactional
  public void testRemoveFifoProcedureNull() {

    List<FifoProcedure> campaigns = new ArrayList<FifoProcedure>();

    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      campaigns.add(f);
    }
    campFac.removeFifoProcedure(null);
  }

  /**
   * Tests if an DataAccessException appears when the method is called with a not existing FifoProcedure.
   */
  @Test(expected = NoMatchingElementException.class)
  @Transactional
  public void testRemoveFifoProcedureWrongId() {

    List<FifoProcedure> campaigns = new ArrayList<FifoProcedure>();

    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      campaigns.add(f);
    }
    campFac.removeFifoProcedure(FifoProcedure.getInstance(0L));
  }

  @Rollback(false)
  public void testRemovePriorityList() {

    int listSize = 10000;

    DrawProcedure dp = DrawProcedure.getInstance(0L);
    dp.setName("TestDrawProcedure");

    dp = campFac.saveDrawProcedure(dp);

    for (int i = 0; i < listSize; i++) {
      dp.addPriorityList(createAndPersistRandomPriorityList());
    }

    List<PriorityList> lists = campFac.getAllPriorityLists();
    assertEquals(listSize, lists.size());
    assertEquals(listSize, dp.getPriorityLists().size());

    dp = campFac.getDrawProcedureById(dp.getId());

    assertEquals(listSize, dp.getPriorityLists().size());

    Random random = new Random();
    while (lists.size() > 0) {
      int nr = random.nextInt(lists.size());
      PriorityList prioList = lists.get(nr);
      lists.remove(nr);

      campFac.removePriorityList(prioList);
    }

    assertEquals(0, dp.getPriorityLists().size());
    assertEquals(0, campFac.getAllPriorityLists().size());
  }

  /**
   * Creates and persists a newly created {@link PriorityList} with random items.
   *
   * @return persisted PriorityList.
   */
  private PriorityList createAndPersistRandomPriorityList() {

    Random random = new Random();
    PriorityList list = PriorityList.getInstance(0L);

    list.setInitiator(random.nextLong());
    list.setParticipant(random.nextLong());

    for (int i = 1; i < 10; i++) {
      list.addItem(random.nextLong(), i);
    }

    return campFac.savePriorityList(list);
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#createCampaign()}.
   */
  @Test
  @Transactional
  public void testSaveCampaign() {
    clearCampaigns();

    List<Campaign> list = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("hallowelt" + i);
      c = campFac.saveCampaign(c);

      list.add(c);
    }
    assertEquals(list.size(), campFac.getAllCampaigns().size());
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#createConfirmProcedure()}.
   */
  @Test
  @Transactional
  public void testSaveConfirmProcedure() {

    List<ConfirmProcedure> list = new ArrayList<ConfirmProcedure>();
    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      list.add(c);
    }
    assertEquals(list.get(3), campFac.getConfirmProcedureById((list.get(3).getId())));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#createDrawProcedure()}.
   */
  @Test
  @Transactional
  public void testSaveDrawProcedure() {

    List<DrawProcedure> list = new ArrayList<DrawProcedure>();
    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      list.add(d);
    }
    assertEquals(list.get(3), campFac.getDrawProcedureById(list.get(3).getId()));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#createFifoProcedure()}.
   */
  @Test
  @Transactional
  public void testSaveFifoProcedure() {

    List<FifoProcedure> list = new ArrayList<FifoProcedure>();
    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      list.add(f);
    }
    assertEquals(list.get(3), campFac.getFifoProcedureById((list.get(3).getId())));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#updateCampaign(hsa.awp.campaign.model.Campaign)} .
   */
  @Test
  @Transactional
  public void testUpdateCampaign() {
    clearCampaigns();

    List<Campaign> campaigns = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("h" + i);
      c = campFac.saveCampaign(c);
      campaigns.add(c);
    }
    Calendar cal = Calendar.getInstance();
    campaigns.get(0).setStartShow(cal);

    assertEquals(campaigns.get(0), campFac.updateCampaign(campaigns.get(0)));
    assertEquals(5, campFac.getAllCampaigns().size());
    assertEquals(cal, campFac.getCampaignById(campaigns.get(0).getId()).getStartShow());
    assertEquals(campaigns.get(0), campFac.getCampaignById(campaigns.get(0).getId()));
    assertEquals(campaigns.get(1), campFac.getCampaignById(campaigns.get(1).getId()));
  }

  /**
   * Test method for {@link CampaignFacade#updateConfirmProcedure(ConfirmProcedure)} .
   */
  @Test
  @Transactional
  public void testUpdateConfirmProcedure() {

    List<ConfirmProcedure> campaigns = new ArrayList<ConfirmProcedure>();
    for (int i = 0; i < 5; i++) {
      ConfirmProcedure c = ConfirmProcedure.getInstance(0L);
      c = campFac.saveConfirmProcedure(c);
      campaigns.add(c);
    }
    Calendar cal = Calendar.getInstance();

    campaigns.get(0).setInterval(cal, Calendar.getInstance());

    assertEquals(campaigns.get(0), campFac.updateConfirmProcedure(campaigns.get(0)));
    assertEquals(cal, campFac.getConfirmProcedureById(campaigns.get(0).getId()).getStartDate());
    assertEquals(campaigns.get(0), campFac.getConfirmProcedureById(campaigns.get(0).getId()));
    assertEquals(campaigns.get(1), campFac.getConfirmProcedureById(campaigns.get(1).getId()));
  }

  /**
   * Test method for
   * {@link hsa.awp.campaign.facade.CampaignFacade#updateConfirmedRegistration(hsa.awp.campaign.model.ConfirmedRegistration)} .
   */
  @Test
  public void testUpdateConfirmedRegistration() {

    for (Campaign c : campFac.getAllCampaigns()) {
      campFac.removeCampaign(c);
    }

    List<Campaign> campaigns = new ArrayList<Campaign>();
    for (int i = 0; i < 5; i++) {
      Campaign c = Campaign.getInstance(0L);
      c.setName("abcgkoerkogrekfokwreeg" + i);
      c = campFac.saveCampaign(c);
      campaigns.add(c);
    }
    Calendar cal = Calendar.getInstance();
    campaigns.get(0).setStartShow(cal);

    assertEquals(campaigns.get(0), campFac.updateCampaign(campaigns.get(0)));
    assertEquals(5, campFac.getAllCampaigns().size());
    assertEquals(cal, campFac.getCampaignById(campaigns.get(0).getId()).getStartShow());
    assertEquals(campaigns.get(0), campFac.getCampaignById(campaigns.get(0).getId()));
    assertEquals(campaigns.get(1), campFac.getCampaignById(campaigns.get(1).getId()));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#updateDrawProcedure(hsa.awp.campaign.model.DrawProcedure)} .
   */
  @Test
  @Transactional
  public void testUpdateDrawProcedure() {

    List<DrawProcedure> draws = new ArrayList<DrawProcedure>();
    for (int i = 0; i < 5; i++) {
      DrawProcedure d = DrawProcedure.getInstance(0L);
      d = campFac.saveDrawProcedure(d);
      draws.add(d);
    }
    Calendar cal = Calendar.getInstance();
    draws.get(0).setDrawDate(cal);

    assertEquals(draws.get(0), campFac.updateDrawProcedure(draws.get(0)));
    assertEquals(cal, campFac.getDrawProcedureById(draws.get(0).getId()).getDrawDate());
    assertEquals(draws.get(0), campFac.getDrawProcedureById(draws.get(0).getId()));
    assertEquals(draws.get(1), campFac.getDrawProcedureById(draws.get(1).getId()));
  }

  /**
   * Test method for {@link hsa.awp.campaign.facade.CampaignFacade#updateFifoProcedure(hsa.awp.campaign.model.FifoProcedure)} .
   */
  @Test
  @Transactional
  public void testUpdateFifoProcedure() {

    List<FifoProcedure> campaigns = new ArrayList<FifoProcedure>();
    for (int i = 0; i < 5; i++) {
      FifoProcedure f = FifoProcedure.getInstance(0L);
      f = campFac.saveFifoProcedure(f);
      campaigns.add(f);
    }
    Calendar cal = Calendar.getInstance();

    campaigns.get(0).setInterval(cal, Calendar.getInstance());

    assertEquals(campaigns.get(0), campFac.updateFifoProcedure(campaigns.get(0)));
    assertEquals(cal, campFac.getFifoProcedureById(campaigns.get(0).getId()).getStartDate());
    assertEquals(campaigns.get(0), campFac.getFifoProcedureById(campaigns.get(0).getId()));
    assertEquals(campaigns.get(1), campFac.getFifoProcedureById(campaigns.get(1).getId()));
  }
}
