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

package hsa.awp.campaign.dao;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.dao.AbstractMandatorableDao;

import javax.persistence.Query;
import java.util.List;

/**
 * Class used for accessing all {@link ConfirmedRegistration} objects.
 *
 * @author klassm
 */
public class ConfirmedRegistrationDao extends AbstractMandatorableDao<ConfirmedRegistration, Long> implements IConfirmedRegistrationDao {
  /**
   * Default constructor.
   */
  public ConfirmedRegistrationDao() {

    super(ConfirmedRegistration.class);
  }

  @Override
  public long countItemsByEventId(Long eventId) {

    Query query = getEntityManager().createQuery(
        "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.eventId=:eventId");
    query.setParameter("eventId", eventId);

    return (Long) query.getSingleResult();
  }

  @Override
  public long countItemsByEventIdAndMandator(Long eventId, Long mandatorId) {

    Query query = getEntityManager().createQuery(
        "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.eventId=:eventId and o.mandatorId = :mandatorId");
    query.setParameter("eventId", eventId);
    query.setParameter("mandatorId", mandatorId);

    return (Long) query.getSingleResult();
  }

  @Override
  public long countItemsByProcedure(Procedure procedure) {

    Query query = getEntityManager().createQuery(
        "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.procedure = :procedure");
    query.setParameter("procedure", procedure);

    return (Long) query.getSingleResult();
  }

  @Override
  public long countItemsByProcedureAndMandator(Procedure procedure, Long mandatorId) {

    Query query = getEntityManager().createQuery(
        "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.procedure = :procedure and o.mandatorId = :mandatorId");
    query.setParameter("procedure", procedure);
    query.setParameter("mandatorId", mandatorId);

    return (Long) query.getSingleResult();
  }

  @Override
  public long countItemsByParticipantIdAndCampaignId(Long participantId, Long campaignId) {
    Query query = getEntityManager().createQuery(
            "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant = :participant and o.procedure.campaign.id = :campaign");
    query.setParameter("participant", participantId);
    query.setParameter("campaign", campaignId);

    return (Long) query.getSingleResult();

  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ConfirmedRegistration> findByProcedure(Procedure procedure) {

    if (procedure == null) {
      throw new IllegalArgumentException("no procedure given");
    }

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.procedure = :procedure");
    query.setParameter("procedure", procedure);

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ConfirmedRegistration> findItemsByEventId(Long eventId) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.eventId=:eventId");
    query.setParameter("eventId", eventId);

    return query.getResultList();
  }

  @Override
  public List<ConfirmedRegistration> findItemsByEventIdAndMandator(Long eventId, Long mandatorId) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.eventId=:eventId and o.mandatorId = :mandatorId");
    query.setParameter("eventId", eventId);
    query.setParameter("mandatorId", mandatorId);

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ConfirmedRegistration> findItemsByParticipantId(Long participantId) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant=:participant");
    query.setParameter("participant", participantId);

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean hasParticipantConfirmedRegistrationInEvent(Long participantId, Long eventId) {

    Query query = getEntityManager().createQuery(
            "select count(o) from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant=:participant and o.eventId=:event");
    query.setParameter("participant", participantId);
    query.setParameter("event", eventId);

    Long count = (Long) query.getSingleResult();
    return count > 0;
  }

  @Override
  public List<ConfirmedRegistration> findItemsByParticipantIdAndMandator(Long participantId, Long mandatorId) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant=:participant and o.mandatorId = :mandatorId");
    query.setParameter("participant", participantId);
    query.setParameter("mandatorId", mandatorId);

    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ConfirmedRegistration> findByCampaign(Campaign campaign) {

    if (campaign == null) {
      throw new IllegalArgumentException("no campaign given");
    }

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.eventId IN (select e from "
            + Campaign.class.getSimpleName() + " c join c.eventIds e where c.id = :id)");
    query.setParameter("id", campaign.getId());

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ConfirmedRegistration> findItemsByParticipantIdAndProcedure(Long participantId, Procedure procedure) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant=:participant and o.procedure = :procedure");
    query.setParameter("participant", participantId);
    query.setParameter("procedure", procedure);

    return query.getResultList();
  }

  @Override
  public List<ConfirmedRegistration> findItemsByParticipantIdAndProcedureAndMandator(Long participantId, Procedure procedure, Long mandatorId) {

    Query query = getEntityManager().createQuery(
        "select o from " + ConfirmedRegistration.class.getSimpleName() + " o where o.participant=:participant and o.procedure = :procedure and o.mandatorId = :mandatorId");
    query.setParameter("participant", participantId);
    query.setParameter("procedure", procedure);
    query.setParameter("mandatorId", mandatorId);

    return query.getResultList();
  }
}
