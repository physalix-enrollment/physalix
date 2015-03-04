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

import antlr.debug.Event;
import hsa.awp.campaign.model.*;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.user.model.SingleUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * Class includes all access methods for the Campaign Context.
 *
 * @author klassm
 */
public interface ICampaignFacade {
  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a given id of an {@link Event} and counts them.
   *
   * @param eventId id of the event
   * @return amount of found {@link ConfirmedRegistration}s.
   */
  long countConfirmedRegistrationsByEventId(long eventId);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a given {@link Procedure} and counts them.
   *
   * @param procedure {@link Procedure} to look for.
   * @return amount of found {@link ConfirmedRegistration}s.
   * @throws IllegalArgumentException if given procedure was null.
   */
  long countConfirmedRegistrationsByProcedure(Procedure procedure);

  long countConfirmedRegistrationsByParticipantIdAndCampaignId(Long participantId, Long campaignId);

  /**
   * Looks for newly active {@link Campaign}s where the startShow of the {@link Campaign} is between a given since data and now.
   * If no since date is given, all activate {@link Campaign}s will be returned.
   *
   * @param since since interval
   * @return {@link List} of newly active {@link Campaign}s.
   * @throws IllegalArgumentException if since is null or since is after now.
   */
  List<Campaign> findActiveCampaignSince(Calendar since);

  /**
   * Looks for all currently active {@link Campaign}s.
   *
   * @return {@link List} of active {@link Campaign}s.
   */
  List<Campaign> findActiveCampaigns();

  /**
   * Looks for newly active {@link Procedure}s where the startDate of the {@link Procedure} is between a given since data and now.
   *
   * @param since since interval
   * @return {@link List} of newly active {@link Procedure}s.
   * @throws IllegalArgumentException if since is after now.
   */
  List<Procedure> findActiveProcedureSince(Calendar since);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an event in {@link Campaign}.
   *
   * @param campaign {@link Campaign} to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findConfirmedRegistrationsByCampaign(Campaign campaign);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an {@link Event}.
   *
   * @param eventId event to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findConfirmedRegistrationsByEvent(Long eventId);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a participant.
   *
   * @param participantId participantId to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantId(Long participantId);

  boolean hasParticipantConfirmedRegistrationInEvent(Long participantId, Long eventId);

  List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndProcedure(Long participantId, Procedure procedure);

  /**
   * Looks for {@link PriorityListItem} being applied to a given event.
   *
   * @param eventId id of a given Event.
   * @return {@link List} of {@link PriorityListItem}s.
   */
  List<PriorityListItem> findPriorityListItemsByEventId(Long eventId);

  /**
   * Looks for {@link PriorityList}s associated with a given {@link SingleUser} and a given {@link Procedure}.
   *
   * @param userId    SingleUser to look for.
   * @param procedure {@link Procedure} to look for.
   * @return associated {@link PriorityList}s.
   */
  List<PriorityList> findPriorityListsByUserAndProcedure(Long userId, Procedure procedure);

  /**
   * Looks for all persistent {@link Campaign}s.
   *
   * @return List of {@link Campaign}s.
   */
  List<Campaign> getAllCampaigns();

  /**
   * Looks for all {@link ConfirmedRegistration}s.
   *
   * @return {@link List} of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> getAllConfirmedRegistrations();

  /**
   * Looks for all {@link FifoProcedure}s.
   *
   * @return {@link List} of {@link Procedure}s.
   */
  List<FifoProcedure> getAllFifoProcedures();

  /**
   * Looks for all {@link PriorityList}s.
   *
   * @return {@link List} of {@link PriorityList}s.
   */
  List<PriorityList> getAllPriorityLists();

  /**
   * Looks for all persistent {@link Procedure}s.
   *
   * @return {@link List} of {@link Procedure}s.
   */
  List<Procedure> getAllProcedures();

  /**
   * Looks for all {@link Procedure}s not used by any {@link Campaign}.
   *
   * @return {@link List} of {@link Procedure}s.
   */
  List<Procedure> getAllUnusedProcedures();

  /**
   * Looks for a {@link Campaign} using its id.
   *
   * @param id identifier
   * @return found Campaign, {@link DataAccessException} if no matching campaign was found, {@link IllegalArgumentException} if id
   *         is invalid.
   */
  Campaign getCampaignById(Long id);

  /**
   * Looks for a {@link Campaign} using its unique name.
   *
   * @param name       unique name.
   * @param mandatorId
   * @return found {@link Campaign} or {@link NoMatchingElementException}.
   */
  Campaign getCampaignByNameAndMandator(String name, Long mandatorId);

  /**
   * Looks for all {@link Campaign}s associated with an Event.
   *
   * @param id event id.
   * @return associated {@link Campaign}s.
   */
  List<Campaign> getCampaignsByEventId(Long id);

  /**
   * Looks for a {@link ConfirmProcedure} by its id.
   *
   * @param id identifier.
   * @return found {@link ConfirmProcedure} or {@link DataAccessException}
   */
  ConfirmProcedure getConfirmProcedureById(Long id);

  /**
   * Looks for a {@link ConfirmedRegistration} by its id.
   *
   * @param id identifier
   * @return found object or {@link DataAccessException}
   */
  ConfirmedRegistration getConfirmedRegistrationById(Long id);

  /**
   * Looks for all {@link ConfirmedRegistration}s which are created by the given {@link Procedure}.
   *
   * @param p the referenced {@link Procedure} of the {@link ConfirmedRegistration}s
   * @return all {@link ConfirmedRegistration}s created by the given {@link Procedure}
   */
  List<ConfirmedRegistration> getConfirmedRegistrationsByProcedure(Procedure p);

  /**
   * Looks for a {@link DrawProcedure} by its id.
   *
   * @param id identifier.
   * @return found {@link DrawProcedure} or {@link DataAccessException}
   */
  DrawProcedure getDrawProcedureById(Long id);

  /**
   * Looks for a {@link FifoProcedure} by its id.
   *
   * @param id identifier.
   * @return found {@link FifoProcedure} or {@link DataAccessException}
   */
  FifoProcedure getFifoProcedureById(Long id);

  /**
   * Looks for a {@link PriorityList} by its id.
   *
   * @param id identifier.
   * @return found {@link PriorityList} or {@link DataAccessException}
   */
  PriorityList getPriorityListById(Long id);

  /**
   * Removes a given {@link Campaign} form the database.
   *
   * @param campaign {@link Campaign} to remove.
   */
  void removeCampaign(Campaign campaign);

  /**
   * Removes a given {@link ConfirmProcedure}.
   *
   * @param c {@link ConfirmProcedure} to remove.
   */
  void removeConfirmProcedure(ConfirmProcedure c);

  /**
   * Removes an already persistent {@link ConfirmedRegistration}.
   *
   * @param confirmedRegistration persistent {@link ConfirmedRegistration}
   */
  void removeConfirmedRegistration(ConfirmedRegistration confirmedRegistration);

  /**
   * Removes a given {@link DrawProcedure}.
   *
   * @param d {@link FifoProcedure} to remove.
   */
  void removeDrawProcedure(DrawProcedure d);

  /**
   * Removes a given {@link FifoProcedure}.
   *
   * @param f {@link FifoProcedure} to remove.
   */
  void removeFifoProcedure(FifoProcedure f);

  /**
   * Removes a given {@link PriorityList}.
   *
   * @param item {@link PriorityList} to remove.
   */
  void removePriorityList(PriorityList item);

  /**
   * Removes a given {@link PriorityListItem}.
   *
   * @param item {@link PriorityListItem} to remove.
   */
  void removePriorityListItem(PriorityListItem item);

  /**
   * Removes all priority lists in the database who belong to the associated procedure.
   */
  void removePriorityListsAssociatedWithDrawProcedure(DrawProcedure procedure);

  /**
   * Makes a {@link Campaign} persistent.
   *
   * @param c {@link Campaign} to make persistent
   * @return persistent {@link Campaign} or {@link DataAccessException}
   */
  Campaign saveCampaign(Campaign c);

  /**
   * Makes a {@link ConfirmProcedure} persistent.
   *
   * @param c {@link ConfirmProcedure} to make persistent
   * @return persistent {@link ConfirmProcedure} or {@link DataAccessException}
   */
  ConfirmProcedure saveConfirmProcedure(ConfirmProcedure c);

  /**
   * Makes a {@link ConfirmedRegistration} persistent.
   *
   * @param c {@link ConfirmedRegistration} to make persistent
   * @return persistent {@link ConfirmedRegistration} or {@link DataAccessException}
   */
  ConfirmedRegistration saveConfirmedRegistration(ConfirmedRegistration c);

  /**
   * Makes a {@link DrawProcedure} persistent.
   *
   * @param d {@link DrawProcedure} to make persistent
   * @return persistent {@link DrawProcedure} or {@link DataAccessException}
   */
  DrawProcedure saveDrawProcedure(DrawProcedure d);

  /**
   * Makes a {@link FifoProcedure} persistent.
   *
   * @param f {@link FifoProcedure} to make persistent
   * @return persistent {@link FifoProcedure} or {@link DataAccessException}
   */
  FifoProcedure saveFifoProcedure(FifoProcedure f);

  /**
   * Makes a {@link PriorityList} persistent.
   *
   * @param prioList {@link PriorityList} to make persistent
   * @return persistent {@link PriorityList} or {@link DataAccessException}
   */
  PriorityList savePriorityList(PriorityList prioList);

  /**
   * Makes a {@link PriorityListItem} persistent.
   *
   * @param item {@link PriorityListItem} to make persistent
   * @return persistent {@link PriorityListItem} or {@link DataAccessException}
   */
  PriorityListItem savePriorityListItem(PriorityListItem item);

  /**
   * Saves a given {@link Procedure}.
   *
   * @param proc {@link Procedure} to make persistent
   * @return persistent {@link Procedure} or {@link DataAccessException}
   */
  Procedure saveProcedure(Procedure proc);

  /**
   * Puts all changes of a given {@link Campaign} into the database.
   *
   * @param campaign campaign to synchronize changes
   * @return merged campaign
   */
  Campaign updateCampaign(Campaign campaign);

  /**
   * Merges a given {@link ConfirmProcedure} with the database. All values in the database will be overwritten.
   *
   * @param c {@link ConfirmProcedure} to merge.
   * @return merged {@link ConfirmProcedure}.
   */
  ConfirmProcedure updateConfirmProcedure(ConfirmProcedure c);

  /**
   * Merges a {@link ConfirmedRegistration}.
   *
   * @param c {@link ConfirmedRegistration} to merge.
   * @return refreshed {@link ConfirmedRegistration} object.
   */
  ConfirmedRegistration updateConfirmedRegistration(ConfirmedRegistration c);

  /**
   * Merges a given {@link DrawProcedure} with the database. All values in the database will be overwritten.
   *
   * @param d {@link DrawProcedure} to merge.
   * @return merged {@link FifoProcedure}.
   */
  DrawProcedure updateDrawProcedure(DrawProcedure d);

  /**
   * Merges a given {@link FifoProcedure} with the database. All values in the database will be overwritten.
   *
   * @param f {@link FifoProcedure} to merge.
   * @return merged {@link FifoProcedure}.
   */
  FifoProcedure updateFifoProcedure(FifoProcedure f);

  /**
   * Puts all changes of a given {@link PriorityList} into the database.
   *
   * @param prioList {@link PriorityList} to synchronize changes
   * @return merged {@link PriorityList}
   */
  PriorityList updatePriorityList(PriorityList prioList);

  /**
   * Puts all changes of a given {@link PriorityListItem} into the database.
   *
   * @param item {@link PriorityListItem} to synchronize changes
   * @return merged {@link PriorityListItem}
   */
  PriorityListItem updatePriorityListItem(PriorityListItem item);

  /**
   * Puts all changes of a given {@link Procedure} into the database.
   *
   * @param procedure {@link Procedure} to synchronize changes
   * @return merged {@link PriorityListItem}
   */
  Procedure updateProcedure(Procedure procedure);

  List<Campaign> findCampaignsByMandatorId(Long mandatorId);

  List<Procedure> findProceduresByMandatorId(Long mandatorId);

  List<Campaign> getActiveCampaignsByMandatorId(Long mandator);

  List<Procedure> getAllUnusedProceduresByMandator(Long mandator);

  List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndMandator(Long id, Long activeMandator);
}
