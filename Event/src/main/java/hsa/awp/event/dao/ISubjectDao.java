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

import java.util.List;

import hsa.awp.common.dao.IAbstractMandatorableDao;
import hsa.awp.event.model.Subject;

/**
 * Interface for SubjectDao.
 *
 * @author klassm
 */
public interface ISubjectDao extends IAbstractMandatorableDao<Subject, Long> {
  Subject findByNameAndMandatorId(String name, Long activeMandator);
  List<Subject> findAllSubjectsByCategoryId(long id);
}
