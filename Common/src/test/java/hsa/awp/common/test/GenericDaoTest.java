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

package hsa.awp.common.test;

import hsa.awp.common.IGenericDomainModel;
import hsa.awp.common.dao.IAbstractDao;
import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.exception.ItemNotSavedException;
import hsa.awp.common.persistence.TPersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This class contains generic test routines to run tests on DAO objects derived from AbstractDao.
 *
 * @param <T> The type (e. g. Exam) of the domain objects
 * @param <D> The type of the DAO objects (e. g. ExamDao)
 * @param <K> The type of the id (i.e. Long)
 * @author alex
 */
public abstract class GenericDaoTest<T extends IGenericDomainModel<K>, D extends IAbstractDao<T, K>, K> {

  /**
   * Data Access Object to interact with the database.
   */
  private D dao;

  /**
   * EntityManager for database interaction.
   */
  private EntityManager entityManager;

  /**
   * This factory generates objects that can be persisted with <code>dao</code>.
   */
  private IObjectFactory<T, K> factory;

  /**
   * Generates a new dao test and opens an entity manager for this test context.
   */
  public GenericDaoTest() {

    entityManager = TPersistenceUtil.getEntityManager();
  }

  /**
   * Starts a new transaction.
   */
  protected void startTransaction() {

    entityManager.getTransaction().begin();
  }

  /**
   * Commits changes to the database.
   */
  protected void commit() {

    entityManager.getTransaction().commit();
  }

  /**
   * Rollbacks the current transaction.
   */
  protected void rollback() {
    entityManager.getTransaction().rollback();
  }

  /**
   * Initializes the given data access object with the held {@link EntityManager}.
   *
   * @param <E> type of the dao
   * @param dao the dao to be initialized.
   * @return the initialized dao
   */
  public <E extends IAbstractDao<?, ?>> E initDao(E dao) {

    dao.setEntityManager(this.entityManager);
    return dao;
  }

  /**
   * Asserts that {@link GenericDaoTest#setDao(IAbstractDao)} and {@link GenericDaoTest#setIObjectFactory(IObjectFactory)} are
   * called with proper parameters before the tests are executed. If there is any inconsistency {@link Assert#fail(String))} with
   * a error message will be invoked.
   */
  private void assertInitializedDependencies() {

    if (dao == null) {
      fail("No dao set. Please use setDao() to set one.");
    } else if (factory == null) {
      fail("No factory set. Please use setIObjectFactory() to set one.");
    }
  }

  /**
   * Method to be executed everytime before a test is started.
   */
  @Before
  public void before() {

    assertInitializedDependencies();
    cleanUpDatabase();
  }

  /**
   * Closes open transactions and cleans up the database.
   */
  @After
  public void tearDown() {

    // if a test failed, there is an active transaction that must be rolled back.
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
    cleanUpDatabase();
  }

  private void cleanUpDatabase() {
    // cleanup the database
    startTransaction();
    try {
      getDao().removeAll();
    } catch (Exception ignored) {}
    commit();
  }

  /**
   * Sets the object factory which creates objects that can be persisted.
   *
   * @param factory An instance of ObjectFactory that instantiates test objects.
   */
  protected void setIObjectFactory(IObjectFactory<T, K> factory) {

    this.factory = factory;
  }

  /**
   * Initializes with current {@link EntityManager} and sets the dao object.
   *
   * @param dao the dao to be tested
   */
  protected void setDao(D dao) {

    initDao(dao);
    this.dao = dao;
  }

  /**
   * Returns the dao object under test.
   *
   * @return The dao object under test.
   */
  protected D getDao() {

    return this.dao;
  }

  /*
  * ======================================================== TESTS ============================================================
  */

  /**
   * persist two objects, countAll must return 2.
   */
  @Test
  public void testCountAll() {

    T object1 = factory.getInstance();
    T object2 = factory.getInstance();

    startTransaction();
    getDao().persist(object1);
    getDao().persist(object2);
    commit();
    startTransaction();
    long val = getDao().countAll();
    assertEquals(2, val);
    commit();
  }

  /**
   * Persists two objects and checks if findAll() returns them.
   */
  @Test
  public void testFindAll() {

    T object1 = factory.getInstance();
    T object2 = factory.getInstance();
    startTransaction();
    object1 = getDao().persist(object1);
    object2 = getDao().persist(object2);

    assertEquals(2, getDao().findAll().size());
    assertTrue(getDao().findAll().contains(object1));
    assertTrue(getDao().findAll().contains(object2));
    commit();
  }

  /**
   * Persists 5 objects and gets the first two.
   */
  @Test
  public void testFindAllIntIntFirst() {

    List<T> objects = generateAndPersistObjects(5);

    startTransaction();
    List<T> result = getDao().findAll(0, 2);
    commit();
    assertEquals(2, result.size());
    assertTrue(result.contains(objects.get(0)));
    assertTrue(result.contains(objects.get(1)));
  }

  /**
   * Persists 5 objects and gets last two.
   */
  @Test
  public void testFindAllIntIntLast() {

    List<T> objects = generateAndPersistObjects(5);

    startTransaction();
    List<T> result = getDao().findAll(3, 2);
    commit();
    assertEquals(2, result.size());
    assertTrue(result.contains(objects.get(3)));
    assertTrue(result.contains(objects.get(4)));
  }

  /**
   * Persists 5 objects and tries to get object 3 and the next 10.
   */
  @Test
  public void testFindAllIntIntTooMuch() {

    List<T> objects = generateAndPersistObjects(5);

    startTransaction();
    List<T> result = getDao().findAll(4, 5);
    assertEquals(1, result.size());
    assertTrue(result.contains(objects.get(4)));
    commit();
  }

  /**
   * Persists 5 objects and tries to get objects with a non existent start index.
   */
  @Test
  public void testFindAllIntIntStartTooHigh() {

    @SuppressWarnings("unused")
    List<T> objects = generateAndPersistObjects(5);

    startTransaction();
    List<T> result = getDao().findAll(5, 2);
    assertTrue(result.isEmpty());
    commit();
  }

  /**
   * Tries to enter a negative start value for the start index.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFindAllIntIntNegativeIndex() {

    startTransaction();
    getDao().findAll(-1, 5);
    commit();
  }

  /**
   * Tries to enter a negative start value for the number of objects.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFindAllIntIntNegativeNumberOfObjects() {

    generateAndPersistObjects(5);
    startTransaction();
    getDao().findAll(2, -1);
    commit();
  }

  /**
   * Generates two objects and tries to get them with their Ids.
   */
  @Test
  public void testFindById() {

    List<T> objects = generateAndPersistObjects(2);

    startTransaction();
    assertEquals(objects.get(0), getDao().findById(objects.get(0).getId()));
    assertEquals(objects.get(1), getDao().findById(objects.get(1).getId()));
    commit();
  }

  /**
   * Tries to find an object with a nonexistent Id.
   */
  @Test(expected = DataAccessException.class)
  public void testFindByIdNonExistent() {

    getDao().findById(factory.getInstance().getId());
  }

  /**
   * Tries to find an object with the Id null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFindByIdNull() {

    getDao().findById(null);
  }

  /**
   * Tries to find an object with an example object.
   * <p/>
   * TODO All : implement method
   */
  @Test
  @Ignore
  public void testFindByExample() {

    fail("Not yet implemented");
  }

  /**
   * Tries to call persist() with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testPersistNull() {

    startTransaction();
    getDao().persist(null);
    commit();
  }

  /**
   * Changes a previously persisted object and calls merge().
   */
  @Test
  public abstract void testMerge();

  /**
   * Tries to merge an object, which is not stored in the database.
   */
  @Test(expected = ItemNotSavedException.class)
  public void testMergeNonExistent() {

    T object = factory.getInstance();
    getDao().merge(object);
  }

  /**
   * Tries to merge <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testMergeNull() {

    getDao().merge(null);
  }

  /**
   * Adds two objects and removes one.
   */
  @Test
  public void testRemove() {

    List<T> objects = generateAndPersistObjects(2);

    startTransaction();
    getDao().remove(objects.get(0));
    assertEquals(objects.get(1), getDao().findById(objects.get(1).getId()));
    assertEquals(1, getDao().findAll().size());
    commit();
  }

  /**
   * Tries to remove a nonexistent object.
   */
  @Test(expected = DataAccessException.class)
  public void testRemoveNonExistent() {

    getDao().remove(factory.getInstance());
  }

  /**
   * Tries to remove <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveNull() {

    startTransaction();
    getDao().remove(null);
    commit();
  }

  /**
   * Persists 20 objects and tries to remove all.
   */
  @Test
  public void testRemoveAll() {

    generateAndPersistObjects(20);

    startTransaction();
    getDao().removeAll();
    commit();

    startTransaction();
    assertEquals(0L, getDao().countAll());
    assertEquals(0, getDao().findAll().size());
    commit();
  }

  /**
   * Creates a bunch of objects and persists them.
   *
   * @param count Number of objects to be created.
   * @return A list with the persisted objects.
   */
  protected List<T> generateAndPersistObjects(int count) {

    List<T> result = new ArrayList<T>(count);
    startTransaction();
    for (int i = 0; i < count; i++) {
      T currentObject = factory.getInstance();
      currentObject = getDao().persist(currentObject);
      result.add(currentObject);
    }
    commit();
    return result;
  }

  /**
   * Creates an item by using the object factory.
   *
   * @return new object.
   */
  protected T getInstance() {

    return factory.getInstance();
  }

}
