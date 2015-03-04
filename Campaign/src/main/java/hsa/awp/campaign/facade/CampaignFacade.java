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

package hsa.awp.campaign.facade;

import hsa.awp.campaign.dao.*;
import hsa.awp.campaign.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Facade for accessing all domain objects in the Campaign Context.
 *
 * @author klassm
 */
public final class CampaignFacade implements ICampaignFacade {
  /**
   * {@link Campaign} Data Access Object.
   */
  private ICampaignDao campaignDao;

  /**
   * {@link ConfirmedRegistration} Data Access Object.
   */
  private IConfirmedRegistrationDao confirmedRegistrationDao;

  /**
   * {@link ConfirmProcedure} Data Access Object.
   */
  private IConfirmProcedureDao confirmProcedureDao;

  /**
   * {@link DrawProcedure} Data Access Object.
   */
  private IDrawProcedureDao drawProcedureDao;

  /**
   * {@link FifoProcedure} Data Access Object.
   */
  private IFifoProcedureDao fifoProcedureDao;

  /**
   * {@link PriorityList} Data Access Object.
   */
  private IPriorityListDao priorityListDao;

  /**
   * {@link PriorityListItem} Data Access Object.
   */
  private IPriorityListItemDao priorityListItemDao;

  /**
   * {@link Procedure} Data Access Object.
   */
  private IProcedureDao procedureDao;

  @Transactional
  @Override
  public long countConfirmedRegistrationsByEventId(long eventId) {

    return confirmedRegistrationDao.countItemsByEventId(eventId);
  }

  @Transactional
  @Override
  public long countConfirmedRegistrationsByProcedure(Procedure procedure) {

    return confirmedRegistrationDao.countItemsByProcedure(procedure);
  }

  @Transactional
  @Override
  public long countConfirmedRegistrationsByParticipantIdAndCampaignId(Long participantId, Long campaignId) {
    return confirmedRegistrationDao.countItemsByParticipantIdAndCampaignId(participantId, campaignId);
  }

  @Transactional
  @Override
  public List<Campaign> findActiveCampaigns() {

    return campaignDao.findActive();
  }

  @Transactional
  @Override
  public List<Campaign> findActiveCampaignSince(Calendar since) {

    if (since == null) {
      return campaignDao.findActive();
    }

    if (since.compareTo(Calendar.getInstance()) > 0) {
      throw new IllegalArgumentException("since data has to be before now.");
    }
    return campaignDao.findActiveSince(since);
  }

  @Transactional
  @Override
  public List<Procedure> findActiveProcedureSince(Calendar since) {

    if (since == null) {
      return procedureDao.findActive();
    }

    if (since.compareTo(Calendar.getInstance()) > 0) {
      throw new IllegalArgumentException("since data has to be before now.");
    }
    return procedureDao.findActiveSince(since);
  }

  @Transactional
  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByCampaign(Campaign campaign) {

    return confirmedRegistrationDao.findByCampaign(campaign);
  }

  @Transactional
  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByEvent(Long eventId) {

    return confirmedRegistrationDao.findItemsByEventId(eventId);
  }

  @Transactional
  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantId(Long participantId) {

    return confirmedRegistrationDao.findItemsByParticipantId(participantId);
  }

  @Transactional
  @Override
  public boolean hasParticipantConfirmedRegistrationInEvent(Long participantId, Long eventId) {
    return confirmedRegistrationDao.hasParticipantConfirmedRegistrationInEvent(participantId, eventId);
  }

  @Transactional
  @Override
  public List<PriorityListItem> findPriorityListItemsByEventId(Long eventId) {

    return priorityListItemDao.findItemsByEventId(eventId);
  }

  @Transactional
  @Override
  public List<PriorityList> findPriorityListsByUserAndProcedure(Long userId, Procedure procedure) {

    return priorityListDao.findByUserAndProcedure(userId, procedure);
  }

  @Transactional
  @Override
  public List<Campaign> getAllCampaigns() {

    return campaignDao.findAll();
  }

  @Override
  @Transactional
  public List<ConfirmedRegistration> getAllConfirmedRegistrations() {

    return confirmedRegistrationDao.findAll();
  }

  @Override
  @Transactional
  public List<FifoProcedure> getAllFifoProcedures() {

    return fifoProcedureDao.findAll();
  }

  @Override
  @Transactional
  public List<PriorityList> getAllPriorityLists() {

    return priorityListDao.findAll();
  }

  @Override
  @Transactional
  public List<Procedure> getAllProcedures() {

    return procedureDao.findAll();
  }

  @Override
  @Transactional
  public List<Procedure> getAllUnusedProcedures() {

    return procedureDao.findUnused();
  }

  @Transactional
  @Override
  public Campaign getCampaignById(Long id) {

    return campaignDao.findById(id);
  }

  @Transactional
  @Override
  public Campaign getCampaignByNameAndMandator(String name, Long mandatorId) {

    return campaignDao.findByNameAndMandator(name, mandatorId);
  }

  @Transactional
  @Override
  public List<Campaign> getCampaignsByEventId(Long id) {

    return campaignDao.findCampaignsByEventId(id);
  }

  @Transactional
  @Override
  public ConfirmedRegistration getConfirmedRegistrationById(Long id) {

    return confirmedRegistrationDao.findById(id);
  }

  @Transactional
  @Override
  public List<ConfirmedRegistration> getConfirmedRegistrationsByProcedure(Procedure p) {

    return confirmedRegistrationDao.findByProcedure(p);
  }

  @Transactional
  @Override
  public ConfirmProcedure getConfirmProcedureById(Long id) {

    return confirmProcedureDao.findById(id);
  }

  @Transactional
  @Override
  public DrawProcedure getDrawProcedureById(Long id) {

    return drawProcedureDao.findById(id);
  }

  @Transactional
  @Override
  public FifoProcedure getFifoProcedureById(Long id) {

    return fifoProcedureDao.findById(id);
  }

  @Transactional
  @Override
  public PriorityList getPriorityListById(Long id) {

    return priorityListDao.findById(id);
  }

  @Transactional
  @Override
  public void removeCampaign(Campaign campaign) {

    campaignDao.remove(campaign);
  }

  @Transactional
  @Override
  public void removeConfirmedRegistration(ConfirmedRegistration confirmedRegistration) {

    confirmedRegistrationDao.remove(confirmedRegistration);
  }

  @Transactional
  @Override
  public void removeConfirmProcedure(ConfirmProcedure c) {

    confirmProcedureDao.remove(c);
  }

  @Transactional
  @Override
  public void removeDrawProcedure(DrawProcedure d) {

    if (d == null) {
      throw new IllegalArgumentException("no procedure given");
    }

    d = drawProcedureDao.findById(d.getId());

    // remove all PriorityLists from the DrawProcedure
    for (PriorityList prio : new LinkedList<PriorityList>(d.getPriorityLists())) {
      removePriorityList(prio);
    }

    // remove set Procedures in ConfirmedRegistrations
    for (ConfirmedRegistration conf : confirmedRegistrationDao.findByProcedure(d)) {
      conf.setProcedure(null);
      updateConfirmedRegistration(conf);
    }

    Campaign campaign = d.getCampaign();
    if (campaign != null) {
      campaign.removeProcedure(d);
      updateCampaign(campaign);
    }

    drawProcedureDao.remove(d);
  }

  @Transactional
  @Override
  public void removeFifoProcedure(FifoProcedure f) {

    if (f == null) {
      throw new IllegalArgumentException("no procedure given");
    }

    f = fifoProcedureDao.findById(f.getId());

    // remove set Procedures in ConfirmedRegistrations
    for (ConfirmedRegistration conf : confirmedRegistrationDao.findByProcedure(f)) {
      conf.setProcedure(null);
    }

    Campaign campaign = f.getCampaign();
    if (campaign != null) {
      campaign.removeProcedure(f);
      updateCampaign(campaign);
    }

    fifoProcedureDao.remove(f);
  }

  @Transactional
  @Override
  public void removePriorityList(PriorityList item) {

    if (item == null) {
      throw new IllegalArgumentException("no PriorityList given");
    }

    DrawProcedure proc = updateDrawProcedure(item.getProcedure());


    if (proc != null) {
      int size = proc.getPriorityLists().size();

      proc.getPriorityLists().remove(item);

      if (size - 1 != proc.getPriorityLists().size()) {
        throw new IllegalStateException("could not remove priority list from procedure");
      }
      drawProcedureDao.merge(proc);
    }

    priorityListDao.remove(item);
  }

  @Transactional
  @Override
  public void removePriorityListItem(PriorityListItem item) {

    priorityListItemDao.remove(item);
  }

  @Transactional
  @Override
  public Campaign saveCampaign(Campaign c) {

    return campaignDao.persist(c);
  }

  @Transactional
  @Override
  public ConfirmedRegistration saveConfirmedRegistration(ConfirmedRegistration c) {

    return confirmedRegistrationDao.persist(c);
  }

  @Transactional
  @Override
  public ConfirmProcedure saveConfirmProcedure(ConfirmProcedure c) {

    return confirmProcedureDao.persist(c);
  }

  @Transactional
  @Override
  public DrawProcedure saveDrawProcedure(DrawProcedure d) {

    return drawProcedureDao.persist(d);
  }

  @Transactional
  @Override
  public FifoProcedure saveFifoProcedure(FifoProcedure f) {

    return fifoProcedureDao.persist(f);
  }

  @Transactional
  @Override
  public PriorityList savePriorityList(PriorityList prioList) {

    return priorityListDao.persist(prioList);
  }

  @Transactional
  @Override
  public PriorityListItem savePriorityListItem(PriorityListItem item) {

    return priorityListItemDao.persist(item);
  }

  @Transactional
  @Override
  public Procedure saveProcedure(Procedure proc) {

    return procedureDao.persist(proc);
  }

  @Transactional
  @Override
  public Campaign updateCampaign(Campaign campaign) {

    return campaignDao.merge(campaign);
  }

  @Transactional
  @Override
  public ConfirmedRegistration updateConfirmedRegistration(ConfirmedRegistration c) {

    return confirmedRegistrationDao.merge(c);
  }

  @Transactional
  @Override
  public ConfirmProcedure updateConfirmProcedure(ConfirmProcedure c) {

    return confirmProcedureDao.merge(c);
  }

  @Transactional
  @Override
  public DrawProcedure updateDrawProcedure(DrawProcedure d) {

    return drawProcedureDao.merge(d);
  }

  @Transactional
  @Override
  public FifoProcedure updateFifoProcedure(FifoProcedure f) {

    return fifoProcedureDao.merge(f);
  }

  @Transactional
  @Override
  public PriorityList updatePriorityList(PriorityList prioList) {

    return priorityListDao.merge(prioList);
  }

  @Transactional
  @Override
  public PriorityListItem updatePriorityListItem(PriorityListItem item) {

    return priorityListItemDao.merge(item);
  }

  @Transactional
  @Override
  public Procedure updateProcedure(Procedure procedure) {

    return procedureDao.merge(procedure);
  }

  @Transactional
  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndProcedure(Long participantId, Procedure procedure) {

    return confirmedRegistrationDao.findItemsByParticipantIdAndProcedure(participantId, procedure);
  }

  @Transactional
  @Override
  public void removePriorityListsAssociatedWithDrawProcedure(DrawProcedure procedure) {

    procedure = getDrawProcedureById(procedure.getId());
    Collection<PriorityList> lists = new LinkedList<PriorityList>(procedure.getPriorityLists());

    for (PriorityList list : lists) {
      removePriorityList(list);
    }
  }

  /**
   * Sets the CampaignDao.
   *
   * @param dao campaignDao
   */
  public void setCampaignDao(ICampaignDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    campaignDao = dao;
  }

  /**
   * Sets the {@link ConfirmProcedure} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setConfirmProcedureDao(IConfirmProcedureDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.confirmProcedureDao = dao;
  }

  /**
   * Sets the {@link FifoProcedure} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setConfirmedRegistrationDao(IConfirmedRegistrationDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.confirmedRegistrationDao = dao;
  }

  /**
   * Sets the {@link DrawProcedure} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setDrawProcedureDao(IDrawProcedureDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.drawProcedureDao = dao;
  }

  /**
   * Sets the {@link FifoProcedure} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setFifoProcedureDao(IFifoProcedureDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.fifoProcedureDao = dao;
  }

  /**
   * Sets the {@link PriorityList} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setPriorityListDao(IPriorityListDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.priorityListDao = dao;
  }

  /**
   * Setter for priorityListItemDao.
   *
   * @param priorityListItemDao the priorityListItemDao to set
   */
  public void setPriorityListItemDao(IPriorityListItemDao priorityListItemDao) {

    this.priorityListItemDao = priorityListItemDao;
  }

  /**
   * Sets the {@link Procedure} dao.
   *
   * @param dao Data Access Object to set.
   */
  public void setProcedureDao(IProcedureDao dao) {

    if (dao == null) {
      throw new IllegalArgumentException("no dao given");
    }
    this.procedureDao = dao;
  }

  @Override
  @Transactional
  public List<Campaign> findCampaignsByMandatorId(Long mandatorId) {
    return campaignDao.findByMandator(mandatorId);
  }

  @Override
  @Transactional
  public List<Procedure> findProceduresByMandatorId(Long mandatorId) {
    return procedureDao.findByMandator(mandatorId);
  }

  @Override
  @Transactional
  public List<Campaign> getActiveCampaignsByMandatorId(Long mandator) {
    return campaignDao.findActiveByMandator(mandator);
  }

  @Override
  @Transactional
  public List<Procedure> getAllUnusedProceduresByMandator(Long mandator) {
    return procedureDao.findUnusedByMandator(mandator);
  }

  @Override
  @Transactional
  public List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndMandator(Long id, Long activeMandator) {
    return confirmedRegistrationDao.findItemsByParticipantIdAndMandator(id, activeMandator);
  }
}
