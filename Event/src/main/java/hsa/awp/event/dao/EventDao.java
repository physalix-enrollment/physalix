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

package hsa.awp.event.dao;

import hsa.awp.common.dao.AbstractMandatorableDao;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;

/**
 * Data Access Object for CRUD methods of Event.
 *
 * @author klassm
 */
public class EventDao extends AbstractMandatorableDao<Event, Long> implements IEventDao {
  /**
   * Constructor for creating an EventDao.
   */
  public EventDao() {

    super(Event.class);
  }

  @Override
  public Event findEventByEventId(Integer eventId) {

    try {
      Query query = getEntityManager().createQuery("select o from Event o where o.eventId=:eventId");
      query.setParameter("eventId", eventId);
      return (Event) query.getSingleResult();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public Event findEventByEventIdAndMandator(Integer eventId, Long mandatorId) {
    try {
      Query query = getEntityManager().createQuery("select o from Event o where o.eventId=:eventId and o.mandatorId = :mandatorId");
      query.setParameter("eventId", eventId);
      query.setParameter("mandatorId", mandatorId);
      return (Event) query.getSingleResult();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public List<Event> findEventsByTeacher(Long userId) {
    try {
      Query query = getEntityManager().createQuery("select o from Event o join o.teachers t where t = :teacher");
      query.setParameter("teacher", userId);
      return query.getResultList();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public List<Event> findEventsByTerm(String term) {

    try {
      Query query = getEntityManager().createQuery("select o from Event o where o.term.termDesc=:term");
      query.setParameter("term", term);
      return query.getResultList();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public List<Event> findEventsByTermAndMandator(String term, Long mandatorId) {
    try {
      Query query = getEntityManager().createQuery("select o from Event o where o.term.termDesc=:term and o.mandatorId = :mandatorId");
      query.setParameter("term", term);
      query.setParameter("mandatorId", mandatorId);
      return query.getResultList();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public List<Event> findEventsByTermId(Long id) {

    try {
      Query query = getEntityManager().createQuery("select o from Event o where o.term.id=:id");
      query.setParameter("id", id);
      return query.getResultList();
    } catch (NoResultException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

@SuppressWarnings("unchecked")
@Override
public List<Event> findEventsBySubjectId(long subjectId) {
	try{
		Query select = getEntityManager().createQuery("select e from Event e where subject.id=:id");
		select.setParameter("id", subjectId);
		return select.getResultList();
	}catch(Exception e){
		return null;
	}
}

@Override
public long findCategoryIdByEventId(long eventId) {
	try {
		Query select = getEntityManager().createQuery("select s.category.id from Subject s where s.id IN (select e.subject.id from Event e where e.id=:eventId)");
		select.setParameter("eventId", eventId);
		return Long.parseLong(select.getSingleResult().toString());
	}
	catch (Exception e){
		return -1;
	}
}
}
