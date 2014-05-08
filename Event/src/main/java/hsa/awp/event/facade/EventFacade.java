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

import hsa.awp.event.dao.*;
import hsa.awp.event.model.*;
import hsa.awp.user.model.SingleUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Facade for interacting with the Event Context.
 *
 * @author klassm
 */
public class EventFacade implements IEventFacade {
  /**
   * Data Access Object for interacting with the {@link Category} domain object.
   */
  private ICategoryDao categoryDao;

  /**
   * Data Access Object for interacting with the {@link Event} domain object.
   */
  private IEventDao eventDao;

  /**
   * Data Access Object for interacting with the {@link Exam} domain object.
   */
  private IExamDao examDao;

  /**
   * Data Access Object for interacting with the {@link Subject} domain object.
   */
  private ISubjectDao subjectDao;

  /**
   * Data Access Object for interacting with the {@link Term} domain object.
   */
  private ITermDao termDao;

  /**
   * Data Access Object for interacting with the {@link Occurrence} domain object.
   */
  private IOccurrenceDao occurenceDao;

  /**
   * Data Access Object for interacting with the {@link Timetable} domain object.
   */
  private ITimetableDao timetableDao;

  /**
   * Constructor for creating an event Facade.
   */
  public EventFacade() {

  }

  @Transactional(readOnly = true)
  @Override
  public List<Category> getAllCategories() {

    return categoryDao.findAll();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Event> getAllEvents() {

    List<Event> evList = eventDao.findAll();
    return evList;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Subject> getAllSubjects() {

    return subjectDao.findAll();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Subject> findAllSubjectsByCategoryId(long id){
    return subjectDao.findAllSubjectsByCategoryId(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Event> findEventsBySubjectId(long subjectId) {
    return eventDao.findEventsBySubjectId(subjectId);
  }

  @Override
  @Transactional(readOnly = true)
  public long findCategoryIdByEventId(long id) {
    return eventDao.findCategoryIdByEventId(id);
  }

  @Transactional(readOnly = true)
  @Override
  public Category getCategoryById(Long id) {

    return categoryDao.findById(id);
  }

  @Transactional(readOnly = true)
  @Override
  public Category getCategoryByName(String name) {

    return categoryDao.findByName(name);
  }

  @Transactional(readOnly = true)
  @Override
  public Category getCategoryByNameAndMandator(String modelObject, Long activeMandator) {

    return categoryDao.findByNameAndMandator(modelObject, activeMandator);
  }

  @Transactional(readOnly = true)
  @Override
  public Event getEventByEventId(Integer eventId) {

    return eventDao.findEventByEventId(eventId);
  }

  @Transactional(readOnly = true)
  @Override
  public Event getEventById(Long eventId) {

    return eventDao.findById(eventId);
  }

  @Transactional(readOnly = true)
  @Override
  public Exam getExamById(Long examId) {

    return examDao.findById(examId);
  }

  @Transactional(readOnly = true)
  @Override
  public Subject getSubjectById(Long id) {

    return subjectDao.findById(id);
  }

  @Transactional(readOnly = true)
  @Override
  public Timetable getTimetableById(Long id) {

    return timetableDao.findById(id);
  }

  @Transactional
  @Override
  public void removeCategory(Category c) {

    categoryDao.remove(c);
  }

  @Transactional
  @Override
  public void removeEvent(Event e) {

    if (e == null) {
      throw new IllegalArgumentException("no event given");
    }

    if (e.getSubject() != null) {
      e.getSubject().getEvents().remove(e);
    }
    if (e.getExams() != null) {
      for (Exam exam : new LinkedList<Exam>(e.getExams())) {
        e.getExams().remove(exam);
        removeExam(exam);
      }
    }
    if (e.getTimetable() != null) {
      removeTimetable(e.getTimetable());
      e.setTimetable(null);
    }
    eventDao.remove(e);
  }

  @Transactional
  @Override
  public void removeTimetable(Timetable timetable) {
    if (timetable.getOccurrences() != null) {
      Iterator<Occurrence> iterator = timetable.getOccurrences().iterator();
      while (iterator.hasNext()) {
        Occurrence occurrence = iterator.next();
        iterator.remove();
        removeOccurence(occurrence);
      }
    }

    timetableDao.remove(timetable);
  }

  @Transactional
  @Override
  public void removeExam(Exam e) {

    examDao.remove(e);
  }

  @Transactional
  @Override
  public void removeOccurence(Occurrence o) {

    occurenceDao.remove(o);
  }

  @Transactional
  @Override
  public void removeSubject(Subject s) {

    if (s == null) {
      throw new IllegalArgumentException("no subject given");
    }

    if (s.getEvents().size() > 0) {
      throw new IllegalArgumentException("only subjects with no attached event can be removed.");
    }

    if (s.getCategory() != null) {
      s.getCategory().getSubjects().remove(s);
    }
    subjectDao.remove(s);
  }

  @Transactional
  @Override
  public void removeTerm(Term term) {
    if (getEventsByTermId(term.getId()).size() > 0) {
      throw new IllegalArgumentException("Term contains events");
    }
    termDao.remove(term);
  }

  @Transactional
  @Override
  public Category saveCategory(Category c) {

    return categoryDao.persist(c);
  }

  @Transactional
  @Override
  public Event saveEvent(Event e) {

    return eventDao.persist(e);
  }

  @Transactional
  @Override
  public Exam saveExam(Exam e) {

    return examDao.persist(e);
  }

  @Transactional
  @Override
  public Occurrence saveOccurrence(Occurrence o) {

    return occurenceDao.persist(o);
  }

  @Transactional
  @Override
  public Subject saveSubject(Subject s) {

    return subjectDao.persist(s);
  }

  @Transactional
  @Override
  public Term saveTerm(Term t) {

    return termDao.persist(t);
  }

  @Transactional
  @Override
  public Timetable saveTimetable(Timetable timetable) {

    return timetableDao.persist(timetable);
  }

  @Transactional
  @Override
  public Category updateCategory(Category c) {

    return categoryDao.merge(c);
  }

  @Transactional
  @Override
  public Event updateEvent(Event e) {

    return eventDao.merge(e);
  }

  @Transactional
  @Override
  public Exam updateExam(Exam e) {

    return examDao.merge(e);
  }

  @Transactional
  @Override
  public Occurrence updateOccurrence(Occurrence o) {

    return occurenceDao.merge(o);
  }

  @Transactional
  @Override
  public Subject updateSubject(Subject s) {

    return subjectDao.merge(s);
  }

  @Transactional
  @Override
  public Timetable updateTimetable(Timetable timetable) {

    return timetableDao.merge(timetable);
  }

  @Transactional
  @Override
  public List<Term> getAllTerms() {

    return termDao.findAll();
  }

  @Override
  @Transactional
  public List<Event> getEventsByTeacher(SingleUser user) {
    return eventDao.findEventsByTeacher(user.getId());
  }

  @Override
  @Transactional
  public List<Event> getEventsByTerm(String term) {
    return eventDao.findEventsByTerm(term);
  }

  @Transactional
  @Override
  public List<Event> getEventsByTermId(Long id) {
    return eventDao.findEventsByTermId(id);  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Transactional
  @Override
  public Subject getSubjectByNameAndMandatorId(String name, Long activeMandator) {
    return subjectDao.findByNameAndMandatorId(name, activeMandator);  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @Transactional
  public List<Event> findEventsByTermAndMandator(String termDesc, Long mandatorId) {
    return eventDao.findEventsByTermAndMandator(termDesc, mandatorId);
  }

  @Override
  @Transactional
  public List<Event> findEventsByMandator(Long mandatorId) {
    return eventDao.findByMandator(mandatorId);
  }

  @Override
  @Transactional
  public List<Subject> getSubjectsByMandator(Long mandator) {
    return subjectDao.findByMandator(mandator);
  }

  @Override
  @Transactional
  public List<Term> getTermsByMandator(Long mandator) {
    return termDao.findByMandator(mandator);
  }

  @Override
  @Transactional
  public List<Category> getCategoryByMandator(Long mandator) {
    return categoryDao.findByMandator(mandator);
  }

  @Override
  @Transactional
  public List<Event> convertToEventList(List<Long> ids) {

    List<Event> events = new LinkedList<Event>();
    for (Long id : ids) {
      events.add(eventDao.findById(id));
    }

    return events;
  }

  /**
   * Sets the {@link ICategoryDao}.
   *
   * @param dao {@link ICategoryDao}
   */
  public void setCategoryDao(ICategoryDao dao) {

    this.categoryDao = dao;
  }

  /**
   * Sets the {@link IEventDao}.
   *
   * @param dao {@link IEventDao}
   */
  public void setEventDao(IEventDao dao) {

    this.eventDao = dao;
  }

  /**
   * Setter for examDao.
   *
   * @param examDao the examDao to set
   */
  public void setExamDao(IExamDao examDao) {

    this.examDao = examDao;
  }

  /**
   * Sets the {@link IOccurrenceDao}.
   *
   * @param dao {@link IOccurrenceDao}
   */
  public void setOccurrenceDao(IOccurrenceDao dao) {

    this.occurenceDao = dao;
  }

  /**
   * Sets the {@link ISubjectDao}.
   *
   * @param dao {@link ISubjectDao}
   */
  public void setSubjectDao(ISubjectDao dao) {

    this.subjectDao = dao;
  }

  /**
   * Sets the {@link ITermDao}.
   *
   * @param dao {@link ITermDao}
   */
  public void setTermDao(ITermDao dao) {

    this.termDao = dao;
  }

  /**
   * Setter for timetableDao need for Spring.
   *
   * @param timetableDao the timetableDao to set
   */
  public void setTimetableDao(ITimetableDao timetableDao) {

    this.timetableDao = timetableDao;
  }
}
