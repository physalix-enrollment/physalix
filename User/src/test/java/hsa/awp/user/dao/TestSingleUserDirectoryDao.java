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

package hsa.awp.user.dao;

import hsa.awp.common.exception.ItemNotSavedException;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.common.naming.IAbstractDirectory;
import hsa.awp.common.test.GenericDaoTest;
import hsa.awp.common.test.IObjectFactory;
import hsa.awp.user.model.Role;
import hsa.awp.user.model.RoleMapping;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.Student;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit Test for the UserDaoclass. This test is designed to be a base class for JUnit tests for children of user.
 *
 * @author alex, matthias
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/config/spring/SingleUserDirectoryDaoTest.xml")
public class TestSingleUserDirectoryDao extends GenericDaoTest<SingleUser, SingleUserDirectoryDao, Long> {
  /**
   * The low level directory - will be mocked.
   */
  private IAbstractDirectory abstractDirectory;

  private IMandatorDao mandatorDao;

  /**
   * Mockery.
   */
  private Mockery mockery = new JUnit4Mockery();

  /**
   * State machine to do advanced configuration of the mockery. In the "default" state of the state machine, all calls of
   * IAbstractDirectory.getUserProperties(Long) will be ignored by the mockery. In the state "custom" you can define your own
   * expectations. "default" is the default state. To change the state, call <code>state.become("newState");</code>.
   */
  private States state = mockery.states("DirectoryStateMachine");

  /**
   * Sets the test dao, initializes object factory.
   */
  public TestSingleUserDirectoryDao() {

    setDao(new SingleUserDirectoryDao());
    setupObjectFactory();
  }

  /**
   * Configures the object factory of the abstract dao test.
   */
  protected void setupObjectFactory() {

    super.setIObjectFactory(new IObjectFactory<SingleUser, Long>() {
      /** Cycles threw available ids. */
      private long id = 1L;

      /** SingleUser name must be unique (this is not useful. */
      private String username = "TestUser";

      @Override
      public SingleUser getInstance() {

        Properties dummyProps = DummyData.getUser(id);
        SingleUser u = SingleUser.getInstance();
        u.setUsername(username + id);
        u.setUuid(id);
        u.setFaculty(dummyProps.getProperty(IAbstractDirectory.FACULTY));
        u.setMail(dummyProps.getProperty(IAbstractDirectory.EMAIL));
        u.setName(dummyProps.getProperty(IAbstractDirectory.NAME));

        id++;
        return u;
      }
    });
  }

  /**
   * Getter of the state machine for jmock.
   *
   * @return The state machine.
   */
  public States getState() {

    return state;
  }

  /**
   * Getter of the state machine for jmock.
   *
   * @param state - The state machine.
   */
  public void setState(States state) {

    this.state = state;
  }

  /**
   * Sets up a new mock.
   */
  @Before
  public void setUp() {

    abstractDirectory = mockery.mock(IAbstractDirectory.class);
    getDao().setDirectory(abstractDirectory);
    initDao(getDao());

    StudyCourseDao studyCourseDao = new StudyCourseDao();
    initDao(studyCourseDao);
    getDao().setStudyCourseDao(studyCourseDao);

    mandatorDao = new MandatorDao();
    initDao(mandatorDao);
    getDao().setMandatorDao(mandatorDao);

    getDao().setRoleMappingDao(initDao(new RoleMappingDao()));

    /*
    * By default, the mockery will ignore every getUserProperties() calls (this is useful for the GenericDaoTest which calls it
    * very often).
    */
    mockery.checking(new Expectations() {
      {
        ignoring(abstractDirectory).getUserProperties(with(any(Long.class)));
        will(returnValue(DummyData.getUser(1)));
        when(state.is("default"));
      }
    });
    state.become("default");
    super.before();
  }

  /**
   * Asserts that all mock expectations were satisfied.
   */
  @Override
  @After
  public void tearDown() {

    try {
      mockery.assertIsSatisfied();
      super.tearDown();
    } catch (UnsupportedOperationException e) {
      // ignore it
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Override
  public void testCountAll() {
    getDao().countAll();
  }

  @Override
  protected List<SingleUser> generateAndPersistObjects(int count) {

    long start = 0L;
    List<SingleUser> result = new ArrayList<SingleUser>(count);
    startTransaction();
    for (int i = 0; i < count; i++) {
      SingleUser s = SingleUser.getInstance();
      s.setUuid(++start);
      getDao().persist(s);
      result.add(s);
    }
    commit();
    return result;
  }

  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAll() {
    getDao().findAll();
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntFirst() {

    getDao().findAll(1, 1);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntLast() {

    getDao().findAll(1, 1);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntNegativeIndex() {

    getDao().findAll(1, 1);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntNegativeNumberOfObjects() {

    getDao().findAll(1, 1);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntStartTooHigh() {

    getDao().findAll(1, 1);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testFindAllIntIntTooMuch() {

    getDao().findAll(1, 1);
  }

  /**
   * Tests the lookup of a user by his login.
   */
  @Test
  @Override
  @Ignore
  public void testFindById() {

    Properties userProp = DummyData.getStudent();


    setupMock(userProp.getProperty(IAbstractDirectory.LOGIN), userProp);
    setupMock(Long.valueOf(userProp.getProperty(IAbstractDirectory.UUID)), userProp);

    startTransaction();
    SingleUser user = getDao().findByUsername(userProp.getProperty(IAbstractDirectory.LOGIN));
    assertTrue(user instanceof Student);
    SingleUser result = getDao().findById(user.getId());
    checkProperties(result, userProp);
    commit();
  }

  /**
   * Sets up the mockery for the lookup.
   *
   * @param loginname - The login a lookup will come for.
   * @param result    - The properties, the mock should return.
   */
  public void setupMock(final String loginname, final Properties result) {

    state.become("custom");
    mockery.checking(new Expectations() {
      {
        oneOf(abstractDirectory).getUserProperties(loginname);
        will(returnValue(result));
      }
    });
  }

  /**
   * Sets up the mockery for the lookup.
   *
   * @param uuid   - The uuid a lookup will come for.
   * @param result - The properties, the mock should return.
   */
  public void setupMock(final Long uuid, final Properties result) {

    state.become("custom");
    mockery.checking(new Expectations() {
      {
        oneOf(abstractDirectory).getUserProperties(uuid);
        will(returnValue(result));
      }
    });
  }

  /**
   * Checks the common properties of the all objects.
   *
   * @param singleUser The user object to check.
   * @param props      The properties to compare with.
   */
  public void checkProperties(SingleUser singleUser, Properties props) {

    assertEquals(props.getProperty(IAbstractDirectory.LOGIN), singleUser.getUsername());
    assertEquals(props.getProperty(IAbstractDirectory.NAME), singleUser.getName());
    assertEquals(props.getProperty(IAbstractDirectory.EMAIL), singleUser.getMail());
    Long uuis = Long.parseLong(props.getProperty(IAbstractDirectory.UUID));
    assertEquals(uuis, singleUser.getUuid());

//        if (singleUser instanceof Student) {
//            Student student = (Student) singleUser;
//            assertTrue(student.getOrCreateStudyCourseIfNotPersistent() != null);
//            assertEquals(props.getProperty(IAbstractDirectory.STUDYCOURSE), student.getOrCreateStudyCourseIfNotPersistent().getName());
//        }
  }

  /**
   * Tests the lookup for a user that does not exist. This test case tests also the case for a teacher and a student lookup (will
   * do the same code.
   */
  @Test(expected = NoMatchingElementException.class)
  @Override
  public void testFindByIdNonExistent() {

    getDao().findById(-3498L);
  }

  /**
   * Tests the lookup of a user by his login.
   */
  @Test
  public void testFindByNameGood() {

    startTransaction();
    final String login = DummyData.getSecretary().getProperty(IAbstractDirectory.LOGIN);
    setupMock(login, DummyData.getSecretary());
    SingleUser result = getDao().findByUsername(login);
    checkProperties(result, DummyData.getSecretary());
    rollback();
  }

  /**
   * Tests the lookup of a nonexistent user.
   */
  @Test(expected = NoMatchingElementException.class)
  public void testFindByNameNonExistend() {

    final String login = "hmlbrmpf";

    state.become("custom");
    mockery.checking(new Expectations() {
      {
        oneOf(abstractDirectory).getUserProperties(login);
        will(throwException(new NoMatchingElementException()));
      }
    });

    @SuppressWarnings("unused")
    SingleUser result = getDao().findByUsername(login);
  }

  /**
   * Tests the database query looking for all teachers.
   */
  @Test
  public void testGetAllTeachers() {

    Properties sec = DummyData.getSecretary();
    Properties student = DummyData.getStudent();
    Properties teacher = DummyData.getTeacher();

    setupMock(sec.getProperty(IAbstractDirectory.LOGIN), sec);

    setupMock(student.getProperty(IAbstractDirectory.LOGIN), student);

    setupMock(teacher.getProperty(IAbstractDirectory.LOGIN), teacher);

    startTransaction();
    SingleUser uSec = getDao().findByUsername(sec.getProperty(IAbstractDirectory.LOGIN));
    SingleUser uStudent = getDao().findByUsername(student.getProperty(IAbstractDirectory.LOGIN));
    ((Student)uStudent).setStudyCourse(null);
    getDao().findByUsername(teacher.getProperty(IAbstractDirectory.LOGIN));

    uSec.getLectures().add(1L);
    uStudent.getLectures().add(1L);

    assertEquals(2, getDao().getAllTeachers().size());
    rollback();
  }

  @Test(expected = UnsupportedOperationException.class)
  @Override
  public void testMerge() {
    // create and persist user
    List<SingleUser> singleUsers = generateAndPersistObjects(1);
    SingleUser u = singleUsers.get(0);

    // modify values
    String username = "wursti";
    String name = "Hans Wurst";
    RoleMapping roleMapping = RoleMapping.getInstance(Role.SECRETARY);
    u.addRoleMapping(roleMapping);
    u.setUsername(username);
    u.setName(name);

    // merge changes
    super.startTransaction();
    SingleUser merged = getDao().merge(u);
    super.commit();

    // check changes
    assertEquals(u, merged);
    assertTrue(merged.getRolemappings().contains(roleMapping));
    assertEquals(1, merged.getRolemappings().size());
    assertEquals(username, merged.getUsername());
    assertEquals(name, merged.getName());
    assertTrue(merged.getId() != 0L);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = ItemNotSavedException.class)
  public void testMergeNonExistent() {

    getDao().merge(SingleUser.getInstance());
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = IllegalArgumentException.class)
  public void testMergeNull() {

    getDao().merge(null);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testPersistNull() {

    getDao().persist(null);
  }

  /**
   * Reimplemented this test because the equals method does not work on the dummy objects - this test only tests the count of the
   * objects.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testRemove() {
    getDao().remove(null);
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveAll() {

    getDao().removeAll();
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveNonExistent() {

    getDao().remove(SingleUser.getInstance());
  }

  /**
   * Overwritten method from {@link GenericDaoTest}. This method has to be called in a different way because an
   * {@link UnsupportedOperationException} is expected.
   */
  @Override
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveNull() {

    getDao().remove(null);
  }
}
