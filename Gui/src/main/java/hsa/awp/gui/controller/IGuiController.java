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

package hsa.awp.gui.controller;

import hsa.awp.campaign.facade.CampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.event.model.Category;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.User;

import java.util.List;
import java.util.Set;

public interface IGuiController {
  /**
   * Looks for all {@link ConfirmedRegistration}s associated with a given id of an {@link Event} and counts them.
   *
   * @param eventId id of the event
   * @return amount of found {@link ConfirmedRegistration}s.
   */
  long countConfirmedRegistrationsByEventId(long eventId);

  /**
   * Removes a {@link ConfirmedRegistration}.
   *
   * @param confirmedRegistration {@link ConfirmedRegistration} to remove.
   * @throws IllegalArgumentException   if {@link ConfirmedRegistration} is null
   * @throws NoMatchingElementException if {@link ConfirmedRegistration} does not exist
   */
  void deleteConfirmedRegistration(ConfirmedRegistration confirmedRegistration);

  /**
   * Returns all existing {@link Campaign}s.
   *
   * @return all existing {@link Campaign}
   */
  public abstract List<Campaign> getAllCampaigns();

  /**
   * Returns all {@link Category}s which exists.
   *
   * @return all existing {@link Category}s
   */
  public abstract List<Category> getAllCategories();

  /**
   * Returns all {@link Event}s which exist.
   *
   * @return all existing {@link Event}s
   */
  public abstract List<Event> getAllEvents();

  /**
   * Returns all {@link Subject} which exists.
   *
   * @return all existing {@link Subject}s
   */
  public abstract List<Subject> getAllSubjects();

  /**
   * Returns a Category with the given name.
   *
   * @param name the name of the {@link Category}
   * @return the {@link Category} with the given name
   */
  public abstract Category getCategoryByName(String name);

  List<ConfirmedRegistration> getConfirmedRegistrationsByIds(Set<Long> ids);

  Event getEventById(Long id);

  /**
   * Generates a list of all events in a campaign.
   *
   * @param campaign {@link Campaign} to look for.
   * @return List of events.
   */
  List<Event> getEventsByCampaign(Campaign campaign);

  /**
   * Looks for a {@link SingleUser} using his id.
   *
   * @param id unique identifier.
   * @return {@link SingleUser} object or null.
   */
  SingleUser getUserById(Long id);

  /**
   * Looks for a {@link SingleUser} using a given user name.
   *
   * @param username user name to look for.
   * @return found {@link SingleUser} or null
   */
  SingleUser getUserByName(String username);

  /**
   * Sets priolist invalid.
   *
   * @param priorityList priolist to set invalid
   * @see CampaignFacade#removePriorityList(PriorityList)
   */
  void removePriolist(PriorityList priorityList);

  /**
   * Looks for all {@link hsa.awp.campaign.model.ConfirmedRegistration}s associated with a participant.
   *
   * @param participantId participantId to look for.
   * @return List of {@link hsa.awp.campaign.model.ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantId(Long participantId);

  long countConfirmedRegistrationsByParticipantIdAndCampaignId(Long participantId, Long campaignId);

  boolean hasParticipantConfirmedRegistrationInEvent(User participant, Event event);

  long findCategoryIdByEventId(long id);

  List<Event> findEventsBySubjectId(long subjectId, Procedure proc);

  List<Subject> findAllSubjectsByCategoryId(long id, Procedure proc);
}
