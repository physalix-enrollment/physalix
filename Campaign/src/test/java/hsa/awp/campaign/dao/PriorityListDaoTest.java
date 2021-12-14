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

package hsa.awp.campaign.dao;

import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.persistence.TPersistenceUtil;
import hsa.awp.common.test.GenericDaoTest;
import hsa.awp.common.test.IObjectFactory;
import hsa.awp.user.model.SingleUser;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * {@link PriorityListDaoTest}.
 *
 * @author kai
 */
public class PriorityListDaoTest extends GenericDaoTest<PriorityList, PriorityListDao, Long> {
  /**
   * Creates a {@link PriorityListDaoTest}.
   */
  public PriorityListDaoTest() {

    super();
    super.setDao(new PriorityListDao());

    super.setIObjectFactory(new IObjectFactory<PriorityList, Long>() {
      private Long id = 0L;

      @Override
      public PriorityList getInstance() {

        PriorityList list = PriorityList.getInstance(0L);
        list.setDate(Calendar.getInstance());
        list.setInitiator(id++);
        list.setParticipant(id++);
        return list;
      }
    });
  }

  /**
   * Tests the selection of {@link PriorityList}s for a given {@link SingleUser} and a given {@link Procedure}.
   */
  @Test
  public void testFindPriorityListsForUserAndProcedure() {

    startTransaction();

    DrawProcedureDao drawDao = new DrawProcedureDao();
    initDao(drawDao);

    PriorityListDao prioDao = new PriorityListDao();
    initDao(prioDao);

    List<PriorityList> lists = new ArrayList<PriorityList>(5);

    Long user1 = 54212L;
    Long user2 = 1058432L;

    DrawProcedure proc1 = DrawProcedure.getInstance(0L);
    DrawProcedure proc2 = DrawProcedure.getInstance(0L);

    drawDao.persist(proc1);
    drawDao.persist(proc2);

    List<Long> evList1 = generateRandomEventList();
    List<Long> evList2 = generateRandomEventList();
    List<Long> evList3 = generateRandomEventList();
    List<Long> evList4 = generateRandomEventList();
    List<Long> evList5 = generateRandomEventList();

    lists.add(PriorityList.getInstance(user1, user2, evList1, 0L));
    lists.add(PriorityList.getInstance(user1, user2, evList2, 0L));
    lists.add(PriorityList.getInstance(user2, user2, evList3, 0L));
    lists.add(PriorityList.getInstance(user2, user2, evList4, 0L));
    lists.add(PriorityList.getInstance(user2, user2, evList5, 0L));

    for (PriorityList prio : lists) {
      prioDao.persist(prio);
    }

    proc1.addPriorityList(lists.get(0));
    proc1.addPriorityList(lists.get(2));
    proc1.addPriorityList(lists.get(4));

    proc2.addPriorityList(lists.get(1));
    proc2.addPriorityList(lists.get(3));

    drawDao.merge(proc1);
    drawDao.merge(proc2);

    for (PriorityList prio : lists) {
      prioDao.merge(prio);
    }

    commit();

    startTransaction();

    assertEquals(1, getDao().findByUserAndProcedure(user1, proc1).size());
    assertEquals(2, getDao().findByUserAndProcedure(user2, proc1).size());
    assertEquals(1, getDao().findByUserAndProcedure(user1, proc2).size());
    assertEquals(1, getDao().findByUserAndProcedure(user2, proc2).size());

    commit();

    startTransaction();

//        for (PriorityList prio : lists) {
//            prio.setProcedure(null);
//        }
//        
//        drawDao.merge(proc1);
//        drawDao.merge(proc2);

    for (PriorityList prio : lists) {
      prioDao.remove(prio);
    }

    drawDao.remove(proc1);
    drawDao.remove(proc2);

    commit();
  }

  /**
   * Tests the selection of {@link PriorityList}s for a given {@link SingleUser} and a given {@link Procedure}.
   */
  @Test
  public void testFindPriorityListsForUserAndProcedureAndMandatorId() {

    startTransaction();

    DrawProcedureDao drawDao = new DrawProcedureDao();
    initDao(drawDao);

    PriorityListDao prioDao = new PriorityListDao();
    initDao(prioDao);

    List<PriorityList> lists = new ArrayList<PriorityList>(5);

    Long user1 = 54212L;
    Long user2 = 1058432L;

    DrawProcedure proc1 = DrawProcedure.getInstance(1L);
    DrawProcedure proc2 = DrawProcedure.getInstance(2L);

    drawDao.persist(proc1);
    drawDao.persist(proc2);

    List<Long> evList1 = generateRandomEventList();
    List<Long> evList2 = generateRandomEventList();
    List<Long> evList3 = generateRandomEventList();
    List<Long> evList4 = generateRandomEventList();
    List<Long> evList5 = generateRandomEventList();

    lists.add(PriorityList.getInstance(user1, user2, evList1, 1L));
    lists.add(PriorityList.getInstance(user1, user2, evList2, 2L));
    lists.add(PriorityList.getInstance(user2, user2, evList3, 1L));
    lists.add(PriorityList.getInstance(user2, user2, evList4, 2L));
    lists.add(PriorityList.getInstance(user2, user2, evList5, 1L));

    for (PriorityList prio : lists) {
      prioDao.persist(prio);
    }

    proc1.addPriorityList(lists.get(0));
    proc1.addPriorityList(lists.get(2));
    proc1.addPriorityList(lists.get(4));

    proc2.addPriorityList(lists.get(1));
    proc2.addPriorityList(lists.get(3));

    drawDao.merge(proc1);
    drawDao.merge(proc2);

    for (PriorityList prio : lists) {
      prioDao.merge(prio);
    }

    commit();

    startTransaction();

    assertEquals(1, getDao().findByUserAndProcedureAndMandatorId(user1, proc1, 1L).size());
    assertEquals(2, getDao().findByUserAndProcedureAndMandatorId(user2, proc1, 1L).size());
    assertEquals(1, getDao().findByUserAndProcedureAndMandatorId(user1, proc2, 2L).size());
    assertEquals(1, getDao().findByUserAndProcedureAndMandatorId(user2, proc2, 2L).size());

    commit();

    startTransaction();

//        for (PriorityList prio : lists) {
//            prio.setProcedure(null);
//        }
//
//        drawDao.merge(proc1);
//        drawDao.merge(proc2);

    for (PriorityList prio : lists) {
      prioDao.remove(prio);
    }

    drawDao.remove(proc1);
    drawDao.remove(proc2);

    commit();
  }

  /**
   * Generates a random list of longs. (5 items).
   *
   * @return list of longs.
   */
  private List<Long> generateRandomEventList() {

    List<Long> events = new LinkedList<Long>();
    Random random = new Random();

    for (int i = 0; i < 5; i++) {
      events.add(random.nextLong());
    }

    return events;
  }

  @Override
  public void testMerge() {

    List<PriorityList> prioLists = generateAndPersistObjects(2);
    prioLists.get(0).setParticipant(753L);
    assertEquals(prioLists.get(0), getDao().merge(prioLists.get(0)));
    assertEquals(2, getDao().findAll().size());
    assertEquals(753L, getDao().findById(prioLists.get(0).getId()).getParticipant().longValue());
    assertEquals(prioLists.get(0), getDao().findById(prioLists.get(0).getId()));
    assertEquals(prioLists.get(1), getDao().findById(prioLists.get(1).getId()));
  }

  /**
   * Test method for {@link hsa.awp.campaign.dao.PriorityListDao#PriorityListDao()}.
   */
  @Test
  public void testPriorityListDao() {

    assertEquals(PriorityListDao.class, getDao().getClass());
  }

  @Test
  public void testRemovePrioLists() {

    PriorityListItemDao itemDao = new PriorityListItemDao();
    initDao(itemDao);

    DrawProcedureDao drawDao = new DrawProcedureDao();
    initDao(drawDao);

    int listSize = 10;

    startTransaction();
    DrawProcedure dp = DrawProcedure.getInstance(0L);
    dp.setName("TestDrawProcedure");

    dp = drawDao.persist(dp);

    for (int i = 0; i < listSize; i++) {
      dp.addPriorityList(createAndPersistRandomPriorityList());
    }
    commit();

    List<PriorityList> lists = getDao().findAll();
    assertEquals(listSize, lists.size());
    assertEquals(listSize, dp.getPriorityLists().size());

    dp = drawDao.findById(dp.getId());

    assertEquals(listSize, dp.getPriorityLists().size());

    Random random = new Random();
    while (lists.size() > 0) {
      int nr = random.nextInt(lists.size());
      PriorityList prioList = lists.get(nr);
      lists.remove(nr);

      removePriorityListDummyLogic(prioList, drawDao);
    }


    assertEquals(0, dp.getPriorityLists().size());
    assertEquals(0, getDao().findAll().size());
  }

  private PriorityList createAndPersistRandomPriorityList() {

    Random random = new Random();
    PriorityList list = PriorityList.getInstance(0L);

    list.setInitiator(random.nextLong());
    list.setParticipant(random.nextLong());

    for (int i = 1; i < 10; i++) {
      list.addItem(random.nextLong(), i);
    }

    return getDao().persist(list);
  }

  private void removePriorityListDummyLogic(PriorityList item, DrawProcedureDao drawProcedureDao) {

    startTransaction();
    DrawProcedure proc = item.getProcedure();

    if (proc != null) {
      int size = proc.getPriorityLists().size();

      proc.getPriorityLists().remove(item);

      if (size - 1 != proc.getPriorityLists().size()) {
        throw new IllegalStateException("could not remove priority list from procedure");
      }
      drawProcedureDao.merge(proc);
    }

    getDao().remove(item);

    commit();
  }
}
