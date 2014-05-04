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

import hsa.awp.event.model.*;
import hsa.awp.user.model.SingleUser;

import java.util.List;

/**
 * Interface for interacting with the Event Context.
 *
 * @author klassm
 */
/**
 * @author michael
 *
 */
public interface IEventFacade {
  List<Event> convertToEventList(List<Long> ids);

  /**
   * Looks for all persistent categories.
   *
   * @return List of categories.
   */
  List<Category> getAllCategories();

  /**
   * Looks for all saved {@link Event}s.
   *
   * @return List of {@link Event}
   */
  List<Event> getAllEvents();

  /**
   * Looks for all persistent {@link Subject}s.
   *
   * @return List of {@link Subject}.
   */
  List<Subject> getAllSubjects();

  /**
   * Looks for a {@link Category} by its id.
   *
   * @param id identifier
   * @return found {@link Category} or {@link DataAccessException}
   */
  Category getCategoryById(Long id);

  /**
   * Looks for a category by its unique name.
   *
   * @param name name to look for.
   * @return found category or {@link DataAccessException} if no entity matches the name.
   */
  Category getCategoryByName(String name);

  /**
   * Looks for an {@link Event} by its unique eventId.
   *
   * @param eventId unique eventId.
   * @return found {@link Event}
   * @throws NoMatchingElementException if no element was found.
   */
  Event getEventByEventId(Integer eventId);

  /**
   * Looks for an {@link Event} by its identifier.
   *
   * @param eventId identifier to look for.
   * @return found {@link Event} or {@link DataAccessException if {@link Event} could not be found.
   */
  Event getEventById(Long eventId);

  Exam getExamById(Long examId);

  /**
   * Looks for a {@link Subject} by its id.
   *
   * @param id identifier
   * @return found {@link Subject} or {@link DataAccessException}
   */
  Subject getSubjectById(Long id);
  
 /**
  * Looks for all {@link Subject} with the appropriate category id
 * @param id identifer
 * @return found {@link List<Subject>} or {@link DataAccessException}
 */
  List<Subject> findAllSubjectsByCategoryId(long id); 
  /**
   * Looks for a {@link Timetable} by its id.
   *
   * @param id identifier
   * @return found {@link Timetable} or {@link DataAccessException}
   */
  Timetable getTimetableById(Long id);

  /**
   * Removes a {@link Category}.
   *
   * @param c {@link Category} to remove.
   */
  void removeCategory(Category c);

  /**
   * Removes an {@link Event}.
   *
   * @param e {@link Event} to remove.
   */
  void removeEvent(Event e);

  /**
   * Removes a {@link Exam}.
   *
   * @param e {@link Exam} to remove.
   */
  void removeExam(Exam e);


  void removeTimetable(Timetable timetable);

  /**
   * Removes a {@link Occurrence}.
   *
   * @param o {@link Occurrence} to remove.
   */
  void removeOccurence(Occurrence o);

  /**
   * Removes a {@link Subject}. The {@link Subject} to remove may not have any attached events.
   *
   * @param s {@link Subject} to remove.
   * @throws IllegalArgumentException if no subject was given
   * @throws IllegalArgumentException if at least one event is attached to this subject.
   */
  void removeSubject(Subject s);

  /**
   * Makes a {@link Category} persistent.
   *
   * @param c {@link Category} to make persistent.
   * @return persistent {@link Category} or {@link DataAccessException}
   */
  Category saveCategory(Category c);

  /**
   * Makes an {@link Event} persistent.
   *
   * @param e {@link Event} to make persistent.
   * @return persistent {@link Event} or {@link DataAccessException}
   */
  Event saveEvent(Event e);

  /**
   * Saves a given {@link Exam}.
   *
   * @param e {@link Exam} to save.
   * @return saved {@link Exam}.
   */
  Exam saveExam(Exam e);

  /**
   * Saves a given {@link Occurrence}.
   *
   * @param o {@link Occurrence} to save.
   * @return saved {@link Occurrence}.
   */
  Occurrence saveOccurrence(Occurrence o);

  /**
   * Makes a {@link Subject} persistent.
   *
   * @param s {@link Subject} to make persistent
   * @return persistent {@link Subject} or {@link DataAccessException}
   */
  Subject saveSubject(Subject s);

  /**
   * Makes a {@link Term} persistent.
   *
   * @param t {@link Term} to make persistent.
   * @return persistent {@link Term} or {@link DataAccessException}
   */
  Term saveTerm(Term t);

  /**
   * Makes a {@link Timetable} persistent.
   *
   * @param t {@link Timetable} to make persistent.
   * @return persistent {@link Timetable} or {@link DataAccessException}
   */
  Timetable saveTimetable(Timetable timetable);

  /**
   * Merges a category back to the database. All changes will be made persistent. If the category does not exist, a
   * {@link DataAccessException} will be thrown.
   *
   * @param c {@link Category} to merge. Will throw {@link IllegalArgumentException} if <code>null</code>.
   * @return merged category
   */
  Category updateCategory(Category c);

  /**
   * Merges an {@link Event} back, so that all changes become persistent. If the event does not exist, a
   * {@link DataAccessException} will be thrown.
   *
   * @param e {@link Event} to merge. Will throw {@link IllegalArgumentException} if <code>null</code>.
   * @return merged Event
   */
  Event updateEvent(Event e);

  /**
   * Merges a {@link Exam} so that all changes will become persistent. If the {@link Exam} does not exist, a
   * {@link DataAccessException} will be thrown. Will throw {@link IllegalArgumentException} if <code>null</code>.
   *
   * @param e {@link Exam} to merge.
   * @return merged {@link Exam}.
   */
  Exam updateExam(Exam e);

  /**
   * Merges a {@link Occurrence} so that all changes will become persistent. If the {@link Occurrence} does not exist, a
   * {@link DataAccessException} will be thrown. Will throw {@link IllegalArgumentException} if <code>null</code>.
   *
   * @param o {@link Occurrence} to merge.
   * @return merged {@link Occurrence}.
   */
  Occurrence updateOccurrence(Occurrence o);

  /**
   * Merges a {@link Subject} so that all changes will become persistent. If the subject does not exist, a
   * {@link DataAccessException} will be thrown. Will throw {@link IllegalArgumentException} if <code>null</code>.
   *
   * @param s {@link Subject} to merge.
   * @return merged subject.
   */
  Subject updateSubject(Subject s);

  /**
   * Merges a {@link Timetable} so that all changes will become persistent. If the subject does not exist, a
   * {@link DataAccessException} will be thrown. Will throw {@link IllegalArgumentException} if <code>null</code>.
   *
   * @param s {@link Timetable} to merge.
   * @return merged subject.
   */
  Timetable updateTimetable(Timetable timetable);

  List<Term> getAllTerms();

  List<Event> getEventsByTeacher(SingleUser user);

  List<Event> getEventsByTerm(String term);

  List<Event> findEventsByTermAndMandator(String termDesc, Long mandatorId);

  List<Event> findEventsByMandator(Long mandatorId);

  List<Subject> getSubjectsByMandator(Long mandator);

  List<Term> getTermsByMandator(Long mandator);

  List<Category> getCategoryByMandator(Long mandator);

  void removeTerm(Term term);

  List<Event> getEventsByTermId(Long id);

  Subject getSubjectByNameAndMandatorId(String name, Long activeMandator);

  Category getCategoryByNameAndMandator(String modelObject, Long activeMandator);

  List<Event> findEventsBySubjectId(long subjectId);

  long findCategoryIdByEventId(long id);

}
