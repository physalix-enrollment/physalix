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

package hsa.awp.user.facade;

import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.exception.ItemNotSavedException;
import hsa.awp.common.naming.DummyUserData;
import hsa.awp.common.test.OpenEntityManagerInTest;
import hsa.awp.user.model.Group;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.StudyCourse;
import hsa.awp.user.model.User;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Integration test for the {@link UserFacade}. This test plays some scenarios to prove that the {@link UserFacade} does the right
 * things and integrates with the dao layer. Spring is used for wiring the daos into the facade layer.
 *
 * @author johannes
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/config/spring/LDAPUserFacadeTest.xml")
public class LDAPUserFacadeTest extends OpenEntityManagerInTest {
  /**
   * The Facade under test.
   */
  @Resource(name = "user.facade")
  private IUserFacade facade;

  /**
   * Cleans up the database after each single test case to ensure that every test has a clean database to work with.
   */
  @After
  public void cleanUp() {
    // remove study courses
    facade.removeAllStudyCourses();
    facade.removeAllGroups();
  }

  /**
   * Creates and persists some {@link Group}s and checks if all {@link Group}s are found correctly.
   */
  @Test
  public void testGetAllGroups() {

    List<Group> created = new ArrayList<Group>();

    // create some UserGroups
    for (int i = 0; i < 9; i++) {
      created.add(facade.saveGroup(Group.getInstance()));
    }

    // find all created UserGroups
    List<Group> found = facade.getAllGroups();

    // check data
    assertEquals(created.size(), found.size());
    assertTrue(found.containsAll(created));
  }

  /**
   * Creates some {@link SingleUser}s and checks if all were found by the {@link UserFacade#getAllSingleUsers()} works correctly.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testGetAllSingleUsers() {

    List<SingleUser> created = new ArrayList<SingleUser>();

    // create some Users
    for (int i = 0; i < 34; i++) {
      created.add(facade.saveSingleUser(SingleUser.getInstance("UserFacadeTest" + i)));
    }
  }

  /**
   * Creates some {@link StudyCourse}s and checks if all were found by the {@link UserFacade#getAllStudyCourses()} works
   * correctly.
   */
  @Test
  public void testGetAllStudyCourses() {

    List<StudyCourse> created = new ArrayList<StudyCourse>();

    // create some StudyCourses
    for (int i = 0; i < 43; i++) {
      created.add(facade.saveStudyCourse(StudyCourse.getInstance("UserFacadeTest" + i)));
    }

    // find all StudyCourses
    List<StudyCourse> found = facade.getAllStudyCourses();

    // check data
    assertEquals(created.size(), found.size());
    assertTrue(found.containsAll(created));
  }

  /**
   * Creates some {@link SingleUser}s and checks if all were found by the {@link UserFacade#getAllUsers()} method.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testGetAllUsers() {

    List<SingleUser> created = new ArrayList<SingleUser>();

    // create some Users
    for (int i = 0; i < 32; i++) {
      created.add(facade.saveSingleUser(SingleUser.getInstance("UserFacadeTest" + i)));
    }
  }

  /**
   * Test method for {@link UserFacade#getGroupById(Long)}. Checks if formerly created {@link Group}s are found by their id.
   */
  @Test
  public void testGetGroupById() {

    List<Group> created = new ArrayList<Group>();
    int amount = 43;

    // create some UserGroups
    for (int i = 0; i < amount; i++) {
      created.add(facade.saveGroup(Group.getInstance()));
    }

    // find all created UserGroups by id
    for (int i = 0; i < amount; i++) {
      Group test = created.get(i);
      Group found = facade.getGroupById(test.getId());
      assertEquals(test, found);
    }
  }

  /**
   * Test method for {@link UserFacade#getGroupById(Long)}. Tests if a {@link DataAccessException} is thrown when a not existing
   * id is passed as parameter.
   */
  @Test(expected = DataAccessException.class)
  public void testGetGroupByIdNotExisting() {

    facade.getGroupById(Long.MAX_VALUE);
  }

  /**
   * Test method for {@link UserFacade#getGroupById(Long)}. Tests if an {@link IllegalArgumentException} is thrown when
   * <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetGroupByIdNull() {

    facade.getGroupById(null);
  }

  /**
   * Test method for {@link UserFacade#getSingleUserById(Long)}. Tests if a {@link DataAccessException} is thrown when a not
   * existing id is passed as parameter.
   */
  @Test(expected = DataAccessException.class)
  public void testGetSingleUserByIdNotExisting() {

    facade.getSingleUserById(Long.MAX_VALUE);
  }

  /**
   * Test method for {@link UserFacade#getSingleUserById(Long)}. Tests if an {@link IllegalArgumentException} is thrown when
   * <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleUserByIdNull() {

    facade.getSingleUserById(null);
  }

  public void testGetStudentByName() {

  }

  /**
   * Test method for {@link UserFacade#getStudyCourseById(Long)}. Checks if formerly created {@link StudyCourse}s are found by
   * their id.
   */
  @Test
  public void testGetStudyCourseById() {

    List<StudyCourse> created = new ArrayList<StudyCourse>();
    int amount = 43;

    // create some StudyCourses
    for (int i = 0; i < amount; i++) {
      created.add(facade.saveStudyCourse(StudyCourse.getInstance("UserFacadeTest" + i)));
    }

    // find all created StudyCourses by id
    for (int i = 0; i < amount; i++) {
      StudyCourse test = created.get(i);
      StudyCourse found = facade.getStudyCourseById(test.getId());
      assertEquals(test, found);
    }
  }

  /**
   * Test method for {@link UserFacade#getStudyCourseById(Long)}. Tests if a {@link DataAccessException} is thrown when a not
   * existing id is passed as parameter.
   */
  @Test(expected = DataAccessException.class)
  public void testGetStudyCourseByIdNotExisting() {

    facade.getStudyCourseById(Long.MAX_VALUE);
  }

  /**
   * Test method for {@link UserFacade#getStudyCourseById(Long)}. Tests if an {@link IllegalArgumentException} is thrown when
   * <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetStudyCourseByIdNull() {

    facade.getStudyCourseById(null);
  }

  /**
   * Test method for {@link UserFacade#getStudyCourseByName(String)}. Checks if formerly created {@link StudyCourse}s are found by
   * their name.
   */
  @Test
  public void testGetStudyCourseByName() {

    List<StudyCourse> created = new ArrayList<StudyCourse>();
    int amount = 43;

    // create some StudyCourses
    for (int i = 0; i < amount; i++) {
      created.add(facade.saveStudyCourse(StudyCourse.getInstance("UserFacadeTest" + i)));
    }

    // find all created StudyCourses by name
    for (int i = 0; i < amount; i++) {
      StudyCourse test = created.get(i);
      StudyCourse found = facade.getStudyCourseByName(test.getName());
      assertEquals(test, found);
    }
  }

  /**
   * Test method for {@link UserFacade#getStudyCourseByName(String)}. Tests if a {@link DataAccessException} is thrown when a not
   * existing id is passed as parameter.
   */
  public void testGetStudyCourseByNameNonExistend() {

    assertNull(facade.getStudyCourseByName("UserFacadeTestStudyCourseNonExistend463271462"));
  }

  /**
   * Test method for {@link UserFacade#getStudyCourseByName(String)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetStudyCourseByNameNull() {

    facade.getStudyCourseByName(null);
  }

  /**
   * Test method for {@link UserFacade#getUserById(Long)}. Checks if formerly created {@link User}s are found by their id.
   */
  @Test
  public void testGetUserById() {

    List<User> created = new ArrayList<User>();
    int amount = 43;

    // create some Participants
    for (int i = 0; i < amount; i++) {
      created.add(facade.saveUser(SingleUser.getInstance()));
    }

    // find all created Participants by id
    for (int i = 0; i < amount; i++) {
      User test = created.get(i);
      User found = facade.getUserById(test.getId());
      assertEquals(test, found);
    }
  }

  /**
   * Test method for {@link UserFacade#getUserById(Long)}. Tests if a {@link DataAccessException} is thrown when a not existing id
   * is passed as parameter.
   */
  @Test(expected = DataAccessException.class)
  public void testGetUserByIdNotExisting() {

    facade.getUserById(Long.MAX_VALUE);
  }

  /**
   * Test method for {@link UserFacade#getUserById(Long)}. Tests if an {@link IllegalArgumentException} is thrown when
   * <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetUserByIdNull() {

    facade.getUserById(null);
  }

  /**
   * Test method for {@link UserFacade#removeAllGroups()}. Creates some {@link Group}s and removes them again.
   */
  @Test
  public void testRemoveAllGroups() {

    facade.removeAllGroups();

    List<Group> created = new ArrayList<Group>();
    int amount = 67;

    for (int i = 0; i < amount; i++) {
      created.add(facade.saveGroup(Group.getInstance()));
    }

    // check if all UserGroups are created
    assertEquals(amount, facade.getAllGroups().size());

    // delete all UserGroups
    facade.removeAllGroups();

    // check if all UserGroup are removed
    assertEquals(0, facade.getAllGroups().size());
  }

  /**
   * Test method for {@link UserFacade#removeAllSingleUsers()}. Creates some {@link SingleUser}s and removes them again.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveAllSingleUsers() {

    facade.removeAllSingleUsers();
  }

  /**
   * Test method for {@link UserFacade#removeAllStudyCourses()}. Creates some {@link StudyCourse}s and removes them again.
   */
  @Test
  public void testRemoveAllStudyCourses() {

    List<StudyCourse> created = new ArrayList<StudyCourse>();
    int amount = 67;

    for (int i = 0; i < amount; i++) {
      created.add(facade.saveStudyCourse(StudyCourse.getInstance("UserFacadeTestStudyCourse" + i)));
    }

    // check if all StudyCourses are created
    assertEquals(amount, facade.getAllStudyCourses().size());

    // delete all StudyCourses
    facade.removeAllStudyCourses();

    // check if all StudyCourse are removed
    assertEquals(0, facade.getAllStudyCourses().size());
  }

  /**
   * Test method for {@link UserFacade#removeGroup(Group)}. Creates some {@link Group}s and removes them step by step again.
   */
  @Test
  public void testRemoveGroup() {

    List<Group> created = new ArrayList<Group>();
    int amount = 50;

    for (int i = 0; i < amount; i++) {
      created.add(facade.saveGroup(Group.getInstance()));
    }

    // check if all UserGroups are created
    assertEquals(amount, facade.getAllGroups().size());

    // deletes the
    for (int i = amount - 1; i >= 0; i--) {
      facade.removeGroup(created.get(i));
      created.remove(i);
      assertEquals(i, facade.getAllGroups().size());
    }

    // check if all UserGroup are removed
    assertEquals(0, facade.getAllGroups().size());
  }

  /**
   * Test method for {@link UserFacade#removeSingleUser(SingleUser)}. Creates some {@link SingleUser}s and removes them step by
   * step again.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveSingleUser() {

    facade.removeSingleUser(null);
  }

  /**
   * Test method for {@link UserFacade#removeStudyCourse(StudyCourse)}. Creates some {@link StudyCourse}s and removes them step by
   * step again.
   */
  @Test
  public void testRemoveStudyCourse() {

    List<StudyCourse> created = new ArrayList<StudyCourse>();
    int amount = 50;

    for (int i = 0; i < amount; i++) {
      created.add(facade.saveStudyCourse(StudyCourse.getInstance("UserFacadeTestStudyCourse" + i)));
    }

    // check if all StudyCourses are created
    assertEquals(amount, facade.getAllStudyCourses().size());

    // deletes the
    for (int i = amount - 1; i >= 0; i--) {
      facade.removeStudyCourse(created.get(i));
      created.remove(i);
      assertEquals(i, facade.getAllStudyCourses().size());
    }

    // check if all StudyCourse are removed
    assertEquals(0, facade.getAllStudyCourses().size());
  }

  /**
   * Saves some {@link SingleUser}s and checks the values.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testSaveSingleUser() {
    // create user
    String username = "FacadeTestUser";
    SingleUser u = SingleUser.getInstance(username);
    facade.saveSingleUser(u);
  }

  /**
   * Saves some {@link StudyCourse}s and checks the values.
   */
  @Test
  public void testSaveStudyCourse() {

    List<StudyCourse> courses;

    // create study course
    String name = "FacadeTestCourse";
    StudyCourse s = StudyCourse.getInstance(name);
    s = facade.saveStudyCourse(s);

    // fetch all study courses
    courses = facade.getAllStudyCourses();

    // assert values
    assertEquals(1, courses.size());
    assertEquals(name, courses.get(0).getName());
    assertThat(courses.get(0).getId(), is(not(0L)));

    // ------------------------------------------------------------------------------------------------------------------------

    // create another study course
    name = "FacadeTestCourse2";
    s = StudyCourse.getInstance(name);
    s = facade.saveStudyCourse(s);

    // fetch all study courses
    courses = facade.getAllStudyCourses();

    // assert values
    assertEquals(2, courses.size());
    assertThat(courses.get(1).getName(), is(name));
    assertNotSame(courses.get(0), courses.get(1));
    assertThat(courses.get(1).getId(), is(not(0L)));
  }

  /**
   * Saves {@link Group}s and checks the objects.
   */
  @Test
  public void testSaveUserGroup() {

    facade.removeAllGroups();

    List<Group> groups;

    // create user group
    Group g = Group.getInstance();
    facade.saveGroup(g);

    // fetch all groups out of the database
    groups = facade.getAllGroups();

    // assert values
    assertEquals(1, groups.size());
    assertThat(groups.get(0).getId(), is(not(0L)));

    // create a second user group
    g = Group.getInstance();
    facade.saveGroup(g);

    // fetch all groups
    groups = facade.getAllGroups();

    // assert values
    assertEquals(2, groups.size());
    assertNotSame(groups.get(0), groups.get(1));
    assertTrue(groups.get(0).getId() != groups.get(1).getId());
    // the reference must not be the same!
    assertNotSame("the member list reference must not be the same", groups.get(0).getMembers(), groups.get(1).getMembers());
  }

  /**
   * Test method for {@link UserFacade#updateGroup(Group)}. Creates a new UserGroup and changes some values. Then checks all
   * changes.
   */
  @Test
  @Transactional
  public void testUpdateGroup() {
    // create UserGroup
    Group created = Group.getInstance();
    facade.saveGroup(created);

    // values to change
    HashSet<User> members = new HashSet<User>();
    members.add(facade.getSingleUserByLogin(DummyUserData.getSecretaryLogin()));

    // change values
    created.setMembers(members);

    // update UserGroup
    facade.updateGroup(created);

    // find UserGroup again
    Group merged = facade.getGroupById(created.getId());

    // check values
    assertEquals(created, merged);
    assertEquals(members, merged.getMembers());
  }

  /**
   * Test method for {@link UserFacade#updateGroup(Group)}. Verifies that an {@link ItemNotSavedException} is thrown when a non
   * persisted {@link Group} is passed as parameter.
   */
  @Test(expected = ItemNotSavedException.class)
  public void testUpdateGroupNotExisting() {

    Group created = Group.getInstance();
    facade.updateGroup(created);
  }

  /**
   * Test method for {@link UserFacade#updateGroup(Group)}. Verifies that an {@link IllegalArgumentException} is thrown when
   * <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateGroupNull() {

    facade.updateGroup(null);
  }

  /**
   * Test method for {@link UserFacade#updateSingleUser(SingleUser)}. Creates a new SingleUser and changes some values. Then
   * checks all changes.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSingleUser() {
    // update SingleUser
    facade.updateSingleUser(null);
  }

  /**
   * Test method for {@link UserFacade#updateStudyCourse(StudyCourse)}. Creates a new StudyCourse and changes some values. Then
   * checks all changes.
   */
  @Test
  public void testUpdateStudyCourse() {
    // create StudyCourse
    StudyCourse created = StudyCourse.getInstance("UserFacadeTestStudyCourse");
    facade.saveStudyCourse(created);

    // values to change
    String name = "NewCourseName";

    // change values
    created.setName(name);

    // update StudyCourse
    facade.updateStudyCourse(created);

    // find StudyCourse again
    StudyCourse merged = facade.getStudyCourseById(created.getId());

    // check values
    assertEquals(created, merged);
    assertEquals(name, merged.getName());
  }

  /**
   * Test method for {@link UserFacade#updateStudyCourse(StudyCourse)}. Verifies that an {@link ItemNotSavedException} is thrown
   * when a non persisted {@link StudyCourse} is passed as parameter.
   */
  @Test(expected = ItemNotSavedException.class)
  public void testUpdateStudyCourseNotExisting() {

    StudyCourse created = StudyCourse.getInstance("UserFacadeTestStudyCourse");
    facade.updateStudyCourse(created);
  }

  /**
   * Test method for {@link UserFacade#updateStudyCourse(StudyCourse)}. Verifies that an {@link IllegalArgumentException} is
   * thrown when <code>null</code> is passed as parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateStudyCourseNull() {

    facade.updateStudyCourse(null);
  }

  /**
   * Test method for {@link UserFacade#updateUser(User)}. Creates a new Participant and changes some values. Then checks all
   * changes.
   */
  @Test
  public void testUpdateUser() {
    // create Participant
    User created = SingleUser.getInstance();
    facade.saveUser(created);

    // update Participant
    facade.updateUser(created);

    // find Participant again
    User merged = facade.getUserById(created.getId());

    // check values
    assertEquals(created, merged);
  }

  /**
   * Test method for {@link UserFacade#updateUser(User)}. Verifies that an {@link ItemNotSavedException} is thrown when a non
   * persisted {@link User} is passed as parameter.
   */
  @Test(expected = ItemNotSavedException.class)
  public void testUpdateUserNotExisting() {

    User created = SingleUser.getInstance();
    facade.updateUser(created);
  }

}
