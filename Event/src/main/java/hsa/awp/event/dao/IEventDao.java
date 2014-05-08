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

import hsa.awp.common.dao.IAbstractMandatorableDao;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.event.model.Event;

import java.util.List;

/**
 * Interface for EventDao.
 *
 * @author klassm
 */
public interface IEventDao extends IAbstractMandatorableDao<Event, Long> {
  /**
   * Looks for an {@link Event} by its unique eventId.
   *
   * @param eventId unique eventId.
   * @return found {@link Event}
   * @throws NoMatchingElementException if no element was found.
   */
  Event findEventByEventId(Integer eventId);

  Event findEventByEventIdAndMandator(Integer eventId, Long mandatorId);

  List<Event> findEventsByTeacher(Long userId);

  List<Event> findEventsByTerm(String term);

  List<Event> findEventsByTermAndMandator(String term, Long mandatorId);

  List<Event> findEventsByTermId(Long id);

  List<Event> findEventsBySubjectId(long subjectId);

  long findCategoryIdByEventId(long id);
}
