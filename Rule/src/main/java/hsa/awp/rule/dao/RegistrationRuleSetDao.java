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

package hsa.awp.rule.dao;

import hsa.awp.common.dao.AbstractMandatorableDao;
import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

/**
 * Data Access Object for CRUD methods of {@link RuleSet}.
 *
 * @author johannes
 */
public class RegistrationRuleSetDao extends AbstractMandatorableDao<RegistrationRuleSet, Long> implements IRegistrationRuleSetDao {

  private static Logger log = LoggerFactory.getLogger(RegistrationRuleSetDao.class);

  /**
   * Default constructor.
   */
  public RegistrationRuleSetDao() {

    super(RegistrationRuleSet.class);
  }

  @Override
  public RegistrationRuleSet findByCampaignAndEvent(Long campaign, Long event) {

    if (campaign == null) {
      throw new IllegalArgumentException("no campaign given");
    } else if (event == null) {
      throw new IllegalArgumentException("no event given");
    }

    Query query = getEntityManager().createQuery(
            "select o from " + RegistrationRuleSet.class.getSimpleName() + " o where o.campaign=:campaign and o.event=:event");
    query.setParameter("campaign", campaign);
    query.setParameter("event", event);

    try {
      return (RegistrationRuleSet) query.getSingleResult();
    } catch (NoResultException e) {
      //log.warn("no result for event id" + event);
      return null;
    }
  }

  @Override
  public List<RegistrationRuleSet> findByCampaign(Long campaign) {

    if (campaign == null) {
      throw new IllegalArgumentException("no campaign given");
    }

    Query query = getEntityManager().createQuery(
            "select o from " + RegistrationRuleSet.class.getSimpleName() + " o where o.campaign=:campaign");
    query.setParameter("campaign", campaign);

    try {
      return query.getResultList();
    } catch (NoResultException e) {
      log.warn("no result", e);
      return null;
    }
  }

  @Override
  public RegistrationRuleSet findByCampaignAndEventAndMandatorId(Long campaign, Long event, Long mandatorId) {

    if (campaign == null) {
      throw new IllegalArgumentException("no campaign given");
    } else if (event == null) {
      throw new IllegalArgumentException("no event given");
    }

    Query query = getEntityManager().createQuery(
        "select o from " + RegistrationRuleSet.class.getSimpleName() + " o where o.campaign=:campaign and o.event=:event and o.mandatorId = :mandatorId");
    query.setParameter("campaign", campaign);
    query.setParameter("event", event);
    query.setParameter("mandatorId", mandatorId);

    try {
      return (RegistrationRuleSet) query.getSingleResult();
    } catch (NoResultException e) {
      log.warn("no result", e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<RegistrationRuleSet> findByEventId(Long id) {

    if (id == null) {
      throw new IllegalArgumentException("no event given");
    }

    Query query = getEntityManager().createQuery(
        "select o from " + RegistrationRuleSet.class.getSimpleName() + " o where o.event=:event");
    query.setParameter("event", id);

    return query.getResultList();
  }

  @Override
  public List<RegistrationRuleSet> findByEventIdAndMandatorId(Long id, Long mandatorId) {

    if (id == null) {
      throw new IllegalArgumentException("no event given");
    }

    Query query = getEntityManager().createQuery(
        "select o from " + RegistrationRuleSet.class.getSimpleName() + " o where o.event=:event and o.mandatorId = :mandatorId");
    query.setParameter("event", id);
    query.setParameter("mandatorId", mandatorId);

    return query.getResultList();
  }
}
