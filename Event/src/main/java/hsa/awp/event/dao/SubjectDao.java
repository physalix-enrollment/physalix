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

import hsa.awp.common.dao.AbstractMandatorableDao;
import hsa.awp.event.model.Subject;

import javax.persistence.Query;

/**
 * Data Access Object for CRUD methods of Subject.
 * 
 * @author klassm
 */
public class SubjectDao extends AbstractMandatorableDao<Subject, Long>
		implements ISubjectDao {
	/**
	 * Constructor for creating a SubjectDao.
	 */
	public SubjectDao() {

		super(Subject.class);
	}

	@Override
	public Subject findByNameAndMandatorId(String name, Long activeMandator) {
		Query query = getEntityManager()
				.createQuery(
						"select o from Subject o where o.name=:name and o.mandatorId = :mandator");
		query.setParameter("name", name);
		query.setParameter("mandator", activeMandator);
		try {
			return (Subject) query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Subject> findAllSubjectsByCategoryId(long id) {
		Query query = getEntityManager().createQuery(
				"select sub from Subject sub where category.id = :id ");
		query.setParameter("id", id);
		try {
			return (List<Subject>) query.getResultList();
		} catch (Exception e) {
			return null;
		}
	}
}
