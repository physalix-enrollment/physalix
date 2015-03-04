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

import antlr.debug.Event;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.dao.IAbstractMandatorableDao;

import java.util.List;

/**
 * Interface for accessing all {@link ConfirmedRegistration} model objects.
 *
 * @author klassm
 */
public interface IConfirmedRegistrationDao extends IAbstractMandatorableDao<ConfirmedRegistration, Long> {
  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a given id of an {@link Event} and counts them.
   *
   * @param eventId id of the event
   * @return amount of found {@link ConfirmedRegistration}s.
   */
  long countItemsByEventId(Long eventId);

  long countItemsByEventIdAndMandator(Long eventId, Long mandatorId);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a given {@link Procedure} and counts them.
   *
   * @param procedure {@link Procedure} to look for.
   * @return amount of found {@link ConfirmedRegistration}s.
   * @throws IllegalArgumentException if given procedure was null.
   */
  long countItemsByProcedure(Procedure procedure);

  long countItemsByProcedureAndMandator(Procedure procedure, Long mandatorId);

  long countItemsByParticipantIdAndCampaignId(Long participantId, Long campaignId);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an event in {@link Campaign}.
   *
   * @param campaign {@link Campaign} to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findByCampaign(Campaign campaign);

  /**
   * Looks for all {@link ConfirmedRegistration}s which are created by the given {@link Procedure}.
   *
   * @param procedure the referenced {@link Procedure} of the {@link ConfirmedRegistration}s
   * @return all {@link ConfirmedRegistration}s created by the given {@link Procedure}
   */
  List<ConfirmedRegistration> findByProcedure(Procedure procedure);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an {@link Event}.
   *
   * @param eventId event to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findItemsByEventId(Long eventId);

  List<ConfirmedRegistration> findItemsByEventIdAndMandator(Long eventId, Long mandatorId);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a participant.
   *
   * @param participantId participantId to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findItemsByParticipantId(Long participantId);

  boolean hasParticipantConfirmedRegistrationInEvent(Long participantId, Long eventId);

  List<ConfirmedRegistration> findItemsByParticipantIdAndMandator(Long participantId, Long mandatorId);

  List<ConfirmedRegistration> findItemsByParticipantIdAndProcedure(Long participantId, Procedure procedure);

  List<ConfirmedRegistration> findItemsByParticipantIdAndProcedureAndMandator(Long participantId, Procedure procedure, Long mandatorId);
}
