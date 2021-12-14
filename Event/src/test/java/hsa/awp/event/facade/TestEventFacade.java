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

package hsa.awp.event.facade;

import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.test.AbstractFacadeTest;
import hsa.awp.event.model.Category;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.event.model.Term;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for the EventFacade. This test plays some scenarios to prove that the EventFacade does the right things and
 * integrates with the dao layer.
 *
 * @author alex
 * @author johannes
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/TestEventFacade.xml")
public class TestEventFacade extends AbstractFacadeTest<EventFacade> {
  /**}
   * The instance under test.
   */
  @Resource(name = "event.facade")
  private IEventFacade eventFacade;

  public TestEventFacade() {

    super(EventFacade.class);
  }

  /**
   * Adds some categories and tries if they are inserted correctly.
   */
  @Test
  @Transactional
  public void testCreateCategory() {

    List<Category> result;

    Category e = Category.getInstance("Test1", 0L);
    eventFacade.saveCategory(e);

    result = eventFacade.getAllCategories();
    assertEquals(1, result.size());
    assertEquals("Test1", result.get(0).getName());

    Category c = Category.getInstance("Test2", 0L);
    eventFacade.saveCategory(c);

    result = eventFacade.getAllCategories();
    assertEquals(2, result.size());
    assertEquals("Test1", result.get(0).getName());
    assertEquals("Test2", result.get(1).getName());
  }

  /******************************************************************************
   * CATEGORY
   ******************************************************************************/

  /**
   * Tries to create two categories with the same name.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testCreateDuplicateCategory() {

    Category c1 = Category.getInstance("DuplicateCategory", 0L);
    eventFacade.saveCategory(c1);

    Category c2 = Category.getInstance("DuplicateCategory", 0L);
    eventFacade.saveCategory(c2);
  }

  /**
   * Creates two Categories and tries to get them by their names.
   */
  @Test
  @Transactional
  public void testGetCategoryByName() {

    eventFacade.saveCategory(Category.getInstance("1", 0L));
    eventFacade.saveCategory(Category.getInstance("2", 0L));

    assertEquals("1", eventFacade.getCategoryByName("1").getName());
    assertEquals("2", eventFacade.getCategoryByName("2").getName());
  }

  /**
   * Tries to get by a non existent name.
   */
  @Test(expected = DataAccessException.class)
  public void testGetCategoryByNameNonExistent() {

    eventFacade.getCategoryByName("Non Existent");
  }

  /**
   * Tries to get by <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetCategoryByNameNull() {

    eventFacade.getCategoryByName(null);
  }

  /**
   * Creates two events and tries to get them by their id.
   */
  @Test
  @Transactional
  public void testGetEventById() {

    Event e1;
    e1 = Event.getInstance(1, 0L);
    e1 = eventFacade.saveEvent(e1);

    Event e2;
    e2 = Event.getInstance(2, 0L);
    e2 = eventFacade.saveEvent(e2);

    assertEquals(e1.getId().longValue(), eventFacade.getEventById(e1.getId()).getId().longValue());
    assertEquals(e2.getId().longValue(), eventFacade.getEventById(e2.getId()).getId().longValue());
  }

  /**
   * Tries to get by a non existent id.
   */
  @Test(expected = DataAccessException.class)
  public void testGetEventByIdNonExistent() {

    eventFacade.getEventById(123L);
  }

  /**
   * Tries to get by <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetEventByIdNull() {

    eventFacade.getEventById(null);
  }

  /**
   * removes all entries.
   */
  @Before
  @Rollback(value = false)
  @Transactional
  public void testRemoveAll() {

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password"));

    List<Category> categories = eventFacade.getAllCategories();
    List<Subject> subjects = eventFacade.getAllSubjects();
    List<Event> events = eventFacade.getAllEvents();

    for (Event e : events) {
      eventFacade.removeEvent(e);
    }
    for (Subject s : subjects) {
      eventFacade.removeSubject(s);
    }
    for (Category c : categories) {
      eventFacade.removeCategory(c);
    }

    assertEquals(0, eventFacade.getAllCategories().size());
    assertEquals(0, eventFacade.getAllSubjects().size());
    assertEquals(0, eventFacade.getAllEvents().size());
  }

  /**
   * Tries to create and remove categories.
   */
  @Test
  @Transactional
  public void testRemoveCategory() {

    List<Category> categories;

    eventFacade.saveCategory(Category.getInstance("Test1", 0L));
    eventFacade.saveCategory(Category.getInstance("Test2", 0L));

    categories = eventFacade.getAllCategories();
    assertEquals(2, categories.size());

    eventFacade.removeCategory(categories.get(1));
    categories = eventFacade.getAllCategories();
    assertEquals(1, categories.size());
    assertEquals(categories.get(0), eventFacade.getAllCategories().get(0));

    eventFacade.removeCategory(categories.get(0));
    categories = eventFacade.getAllCategories();
    assertEquals(0, categories.size());
  }

  /**
   * Tries to remove a nonexistent category.
   */
  @Test(expected = DataAccessException.class)
  public void testRemoveCategoryNonExisitent() {

    eventFacade.removeCategory(Category.getInstance("nonExistend", 0L));
  }

  /**
   * Tries to remove <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveCategoryNull() {

    eventFacade.removeCategory(null);
  }

  /******************************************************************************
   * SUBJECT
   ******************************************************************************/

  /**
   * Tries to create and remove events.
   */
  @Test
  public void testRemoveEvent() {

    Event e;

    e = Event.getInstance(1, 0L);
    eventFacade.saveEvent(e);

    e = Event.getInstance(2, 0L);
    eventFacade.saveEvent(e);

    List<Event> events = eventFacade.getAllEvents();
    assertEquals(2, events.size());

    eventFacade.removeEvent(events.get(1));
    events.remove(1);
    assertEquals(1, events.size());
    assertEquals(events.get(0), eventFacade.getAllEvents().get(0));

    eventFacade.removeEvent(events.get(0));
    events.remove(0);
    assertEquals(0, events.size());
  }

  /**
   * Tries to delete an event which is already removed.
   */
  @Test(expected = DataAccessException.class)
  @Transactional
  public void testRemoveEventAlreadyRemoved() {

    Event e = Event.getInstance(542, 0L);
    eventFacade.saveEvent(e);

    eventFacade.removeEvent(e);
    eventFacade.removeEvent(e);
  }

  /**
   * Tries to remove a nonexistent event.
   */
  @Test(expected = DataAccessException.class)
  public void testRemoveEventNonExisitent() {

    eventFacade.removeEvent(new Event(543, 0L));
  }

  /**
   * Tries to remove <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveEventNull() {

    eventFacade.removeEvent(null);
  }

  /**
   * Tries to create and remove events.
   */
  @Test
  @Transactional
  public void testRemoveSubject() {

    List<Subject> subjects;

    Subject s;

    s = Subject.getInstance(0L);
    eventFacade.saveSubject(s);

    s = Subject.getInstance(0L);
    eventFacade.saveSubject(s);

    subjects = eventFacade.getAllSubjects();
    assertEquals(2, subjects.size());

    eventFacade.removeSubject(subjects.get(1));
    subjects = eventFacade.getAllSubjects();
    assertEquals(1, subjects.size());
    assertEquals(subjects.get(0), eventFacade.getAllSubjects().get(0));

    eventFacade.removeSubject(subjects.get(0));
    subjects = eventFacade.getAllSubjects();
    assertEquals(0, subjects.size());
  }

  /******************************************************************************
   * EVENT
   ******************************************************************************/

  /**
   * Tries to remove a nonexistent subject.
   */
  @Test(expected = DataAccessException.class)
  public void testRemoveSubjectNonExisitent() {

    eventFacade.removeSubject(Subject.getInstance(0L));
  }

  // /** Tries to create two events with the same id. */
  // @Test(expected = DataAccessException.class)
  // @Transactional
  // public void testCreateDuplicateEvent() {
  //
  // Event.getInstance(1);
  // Event.getInstance(1);
  // }

  /**
   * Tries to remove <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveSubjectNull() {

    eventFacade.removeSubject(null);
  }

  /**
   * Tries to merge a category in different configurations. Detailed merge tests with all members have been performed in DAO
   * tests.
   */
  @Test
  @Transactional
  public void testUpdateCategory() {

    List<Category> resultList;
    Category testCategory;

    Category c = Category.getInstance("Test1", 0L);
    eventFacade.saveCategory(c);

    resultList = eventFacade.getAllCategories();
    assertEquals(1, resultList.size());

    // merge without a change (should work and nothing change)
    testCategory = resultList.get(0);
    eventFacade.updateCategory(testCategory);
    resultList = eventFacade.getAllCategories();
    assertEquals(1, resultList.size());
    assertEquals("Test1", resultList.get(0).getName());
    testCategory = resultList.get(0);

    // merge a new name
    testCategory.setName("NewTest1");
    eventFacade.updateCategory(testCategory);
    resultList = eventFacade.getAllCategories();
    assertEquals(1, resultList.size());
    assertEquals("NewTest1", resultList.get(0).getName());
  }

  /**
   * Tries to merge a non existent category.
   */
  @Test(expected = DataAccessException.class)
  public void testUpdateCategoryNonExistent() {

    Category c = Category.getInstance("nonExistend", 0L);
    eventFacade.updateCategory(c);
  }

  /**
   * Tries to call merge with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateCategoryNull() {

    eventFacade.updateCategory(null);
  }

  /**
   * Tries to merge an event in different configurations. Detailed merge tests with all members have been performed in DAO tests.
   */
  @Test
  public void testUpdateEvent() {

    List<Event> resultList;
    Event testEvent;

    testEvent = Event.getInstance(123, 0L);
    testEvent = eventFacade.saveEvent(testEvent);

    resultList = eventFacade.getAllEvents();
    assertEquals(1, resultList.size());

    // merge without a change (should work and nothing change)
    Event e2 = resultList.get(0);
    eventFacade.updateEvent(e2);

    resultList = eventFacade.getAllEvents();

    assertEquals(1, resultList.size());
    assertEquals(testEvent.getId().longValue(), resultList.get(0).getId().longValue());

    testEvent = resultList.get(0);

    // merge a new term
    Term t = Term.getInstance(0L);
    t = eventFacade.saveTerm(t);

    testEvent.setTerm(t);
    eventFacade.updateEvent(testEvent);

    resultList = eventFacade.getAllEvents();
    assertEquals(1, resultList.size());
    assertTrue(resultList.get(0).getTerm() != null);
  }

  /**
   * Tries to merge an event which is already merged.
   */
  @Test
  public void testUpdateEventAlreadyRemoved() {

    Event e = Event.getInstance(542, 0L);
    e = eventFacade.saveEvent(e);

    e.setMaxParticipants(50);
    e = eventFacade.updateEvent(e);
    e = eventFacade.updateEvent(e);
    List<Event> events = eventFacade.getAllEvents();
    assertEquals(50, events.get(0).getMaxParticipants());
  }

  /**
   * Tries to merge a nonexistent event.
   */
  @Test(expected = DataAccessException.class)
  public void testUpdateEventNonExisitent() {

    eventFacade.updateEvent(Event.getInstance(543, 0L));
  }

  /**
   * Tries to call merge with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateEventNull() {

    eventFacade.updateEvent(null);
  }

  /**
   * Tries to merge a subject in different configurations. Detailed merge tests with all members have been performed in DAO tests.
   */
  @Test
  @Transactional
  public void testUpdateSubject() {

    List<Subject> resultList;
    Subject testSubject;

    eventFacade.saveSubject(Subject.getInstance(0L));

    resultList = eventFacade.getAllSubjects();
    assertEquals(1, resultList.size());

    // merge without a change (should work and nothing change)
    testSubject = resultList.get(0);
    eventFacade.updateSubject(testSubject);
    resultList = eventFacade.getAllSubjects();
    assertEquals(1, resultList.size());
    testSubject = resultList.get(0);

    // merge a new name
    testSubject.setName("NewTest1");
    eventFacade.updateSubject(testSubject);
    resultList = eventFacade.getAllSubjects();
    assertEquals(1, resultList.size());
    assertEquals("NewTest1", resultList.get(0).getName());
  }

  /**
   * Tries to call merge with <code>null</code>.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSubjectNull() {

    eventFacade.updateSubject(null);
  }
}
