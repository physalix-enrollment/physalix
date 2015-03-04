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

package hsa.awp.admingui.controller;

import hsa.awp.campaign.model.*;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.common.exception.ProgrammingErrorException;
import hsa.awp.common.model.TemplateType;
import hsa.awp.event.model.*;
import hsa.awp.gui.controller.IGuiController;
import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.Rule;
import hsa.awp.user.model.*;

import org.apache.wicket.Session;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The IAdminGuiController is the Interface for a AdminGuiController.
 *
 * @author Rico Lieback
 * @author Basti
 */
public interface IAdminGuiController extends IGuiController {
  /**
   * Looks for a {@link Category} using a given name.
   *
   * @param id of the {@link Category}.
   * @return foundCategory
   */
  Category getCategoryById(Long id);

  /**
   * Adds a Rule to a Campaign & Event combination.
   *
   * @param campaign campaign to use.
   * @param event    event to use.
   * @param rule     rule to add.
   */
  void addRule(Campaign campaign, Event event, Rule rule, Session session);

  void addRoleMappingToSingleUser(SingleUser user, RoleMapping roleMapping);

  void removeRoleMappingForMandator(SingleUser user, RoleMapping roleMapping, Mandator mandator);

  RoleMapping updateRoleMapping(RoleMapping roleMapping);

  /**
   * Alters a existing {@link Campaign}.
   *
   * @param id         the id of the existing {@link Campaign}
   * @param endShow    the new disappear {@link Calendar}
   * @param startShow  the new appear {@link Calendar}
   * @param procedures the new {@link Procedure}s
   * @return the altered {@link Campaign}
   */
  Campaign alterCampaign(Long id, Calendar endShow, Calendar startShow, List<Procedure> procedures, List<Long> events);

  /**
   * Alters a existing {@link DrawProcedure}.
   *
   * @param id        the id of the existing {@link DrawProcedure}
   * @param name      the new name of the {@link DrawProcedure}
   * @param startDate the new start {@link Calendar} of the {@link DrawProcedure}
   * @param endDate   the new disappear {@link Calendar}
   * @param drawDate  the new draw {@link Calendar} of the {@link DrawProcedure}
   * @return the altered {@link DrawProcedure}
   */
  DrawProcedure alterDrawProcedure(Long id, String name, Calendar startDate, Calendar endDate, Calendar drawDate);

  /**
   * Alters a existing {@link FifoProcedure}.
   *
   * @param id        the id of the existing {@link FifoProcedure}
   * @param name      the new name of the {@link FifoProcedure}
   * @param startDate the new start {@link Calendar} of the {@link FifoProcedure}
   * @param endDate   the new end {@link Calendar} of the {@link FifoProcedure}
   * @return the altered {@link FifoProcedure}
   */
  FifoProcedure alterFifoProcedure(Long id, String name, Calendar startDate, Calendar endDate);

  /**
   * Alters a existing {@link Subject}.
   *
   * @param sub the existing {@link Subject}
   * @return the edited {@link Subject}
   */
  Subject alterSubject(Subject sub);


  /**
   * Creates a new {@link Campaign}.
   *
   * @param name         of the {@link hsa.awp.campaign.model.Campaign}.
   * @param endShow      the {@link java.util.Calendar} when the event disappears
   * @param startShow    the {@link java.util.Calendar} when the event appears
   * @param eventIds         a {@link java.util.Set} of Event IDs
   * @param proc         a {@link java.util.List} of Procedures
   * @param studyCourses
   * @param maximumConfirmedRegistrations
   * @return the created {@link Campaign}
   */
  Campaign createCampaign(String name, String email, Calendar endShow, Calendar startShow, Set<Long> eventIds, List<Procedure> proc, Session session, Set<Long> studyCourses, String detailText, Integer maximumConfirmedRegistrations);

  /**
   * Creates a new {@link Category} with the given name.
   *
   * @param name name of the category
   * @return a new {@link Category}
   */
  Category createCategory(String name, Long mandatorId);

  void createConfirmedRegistration(SingleUser participant, Event event, Session session);

  /**
   * Creates a confirmed registration using a given participant and a given event. This registration will be directly visible!
   *
   * @param participant participant to book
   * @param event       event to book
   * @throws ProgrammingErrorException if initiator cannot be found
   * @throws IllegalArgumentException  if participant is null
   * @throws IllegalArgumentException  if event is null
   */
  void createConfirmedRegistration(User participant, Event event, Session session);

  /**
   * Creates a new {@link DrawProcedure}.
   *
   * @param name      the name of the {@link DrawProcedure}
   * @param startDate the startdate of the {@link DrawProcedure}
   * @param endDate   the enddate of the {@link DrawProcedure}
   * @param drawDate  the drawdate of the {@link DrawProcedure} (when will be drawed)
   * @return the created {@link DrawProcedure}
   */
  DrawProcedure createDrawProcedure(String name, Calendar startDate, Calendar endDate, Calendar drawDate, Session session);

  /**
   * Creates a new {@link Event}.
   *
   * @param eventId         the id of the {@link Event}
   * @param sub             the {@link Subject} of the new {@link Event}
   * @param term            the {@link Term} of the new {@link Event}
   * @param exam            the {@link Exam} of the new {@link Event}
   * @param teacher         the {@link List<SingleUser>} of the new {@link Event}
   * @param maxParticipants the max Number of Participants for the {@link Event}
   * @param groupEvt        is the {@link Event} a GroupEvent
   * @param groupSize       the size of the {@link Event}
   * @return the created {@link Event}
   */
  Event createEvent(int eventId, Subject sub, Term term, List<Exam> exam, List<SingleUser> teacher, int maxParticipants,
                    boolean groupEvt, int groupSize, Session session);

  /**
   * Creates a new {@link FifoProcedure}.
   *
   * @param name      the name of the {@link FifoProcedure}
   * @param startDate the startdate of the {@link FifoProcedure}
   * @param endDate   the enddate of the {@link FifoProcedure}
   * @return the created {@link FifoProcedure}
   */
  FifoProcedure createFifoProcedure(String name, Calendar startDate, Calendar endDate, Session session);

  /**
   * Creates a new {@link Subject}.
   *
   * @param name     the name of the new {@link Subject}
   * @param category the generic {@link Category} of the {@link Subject}
   * @param desc     the description of the {@link Subject}
   * @param link     the link to the Web Page of the {@link Subject}
   * @return the created {@link Subject}
   */
  Subject createSubject(String name, Category category, String desc, String link, Session session);

  /**
   * Deletes a given {@link Campaign}.
   *
   * @param campaign the {@link Campaign} which has to be deleted
   */
  void deleteCampaign(Campaign campaign);

  void deleteProcedure(Procedure procedure);

  /**
   * Deletes a given {@link DrawProcedure}.
   *
   * @param drawProc the {@link DrawProcedure} which has to be deleted
   */
  void deleteDrawProcedure(DrawProcedure drawProc);

  void deleteEvent(Event evt);

  /**
   * Deletes an exam.
   *
   * @param exam  Exam to be deleted
   * @param event event whose exam should be deleted
   */
  void deleteExam(Exam exam, Event event);

  /**
   * Deletes a given {@link FifoProcedure}.
   *
   * @param fifoProc the {@link FifoProcedure} which has to be deleted
   */
  void deleteFifoProcedure(FifoProcedure fifoProc);

  void deleteLecture(SingleUser user, Event event);

  /**
   * Deletes an occurence.
   *
   * @param occurrence occurence to be deleted.
   * @param timetable  timetable whose occurence should be deleted.
   */
  void deleteOccurrence(Occurrence occurrence, Timetable timetable);

  /**
   * Removes a given rule.
   *
   * @param rule rule to remove.
   */
  void deleteRule(Rule rule);

  /**
   * Deletes all rules for a given {@link Campaign} and a given {@link Event}.
   *
   * @param campaignId campaign to look for.
   * @param eventId    event to look for.
   */
  void deleteRulesConnection(Long campaignId, Long eventId);

  /**
   * Deletes all rules for a given {@link Campaign} and a given {@link Event}.
   *
   * @param campaignId campaign to look for.
   * @param eventId    event to look for.
   * @param rule       rule to remove.
   */
  void deleteRuleFromRegistrationRuleSet(Long campaignId, Long eventId, Rule rule);

  /**
   * Removes a {@link Subject}. Only works if there is no {@link Event} attached.
   *
   * @param subject {@link Subject} to delete.
   */
  void deleteSubject(Subject subject);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an event in {@link Campaign}.
   *
   * @param campaign {@link Campaign} to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> findConfirmedRegistrationsByCampaign(Campaign campaign);

  /**
   * Looks for {@link SingleUser}s being in a specified {@link Role}.
   *
   * @param role role the users belong to.
   * @return List of users.
   */
  List<SingleUser> findSingleUsersByRole(Role role);

  /**
   * Looks for all active {@link Campaign}s.
   *
   * @return active {@link Campaign}s
   */
  List<Campaign> getActiveCampaigns();

  /**
   * Returns all existing {@link Procedure}s.
   *
   * @return all existing {@link Procedure}
   */
  List<Procedure> getAllProcedures();

  /**
   * Looks for all rules.
   *
   * @return all rules.
   */
  List<Rule> getAllRules();

  /**
   * Looks for all saved {@link StudyCourse}s.
   *
   * @return saved {@link StudyCourse}s.
   */
  List<StudyCourse> getAllStudyCourses();

  /**
   * Returns all existing Teachers.
   *
   * @return all existing Teachers
   */
  List<SingleUser> getAllTeacher();

  /**
   * Looks for all unused {@link Procedure}s (not added to a {@link Campaign} ).
   *
   * @return {@link List} of {@link Procedure}s.
   */
  List<Procedure> getAllUnusedProcedures();

  /**
   * Looks for a {@link Campaign} by its id.
   *
   * @param id unique identifier.
   * @return {@link Campaign} object or null.
   */
  Campaign getCampaignById(Long id);

  /**
   * Looks for a {@link Campaign} using its unique name.
   *
   * @param name unique name.
   * @return null or found {@link Campaign}.
   */
  Campaign getCampaignByNameAndMandator(String name, Session session);

  /**
   * Looks for all {@link Campaign}s an {@link Event} is applied to.
   *
   * @param event event to look for.
   * @return all applied {@link Campaign}s.
   */
  List<Campaign> getCampaignsByEvent(Event event);

  /**
   * Looks for a {@link ConfirmedRegistration} by its id.
   *
   * @param id unique id.
   * @return {@link ConfirmedRegistration}
   * @throws NoMatchingElementException if no marching {@link ConfirmedRegistration} was found.
   */
  ConfirmedRegistration getConfirmedRegistrationById(Long id);

  /**
   * Looks for all {@link ConfirmedRegistration}s associated with an {@link Event}.
   *
   * @param event event to look for.
   * @return List of {@link ConfirmedRegistration}s.
   */
  List<ConfirmedRegistration> getConfirmedRegistrationsByEvent(Event event);

  /**
   * Looks for a {@link DrawProcedure} by its unique identifier.
   *
   * @param id unique identifier.
   * @return {@link DrawProcedure} or {@link NoMatchingElementException}.
   */
  DrawProcedure getDrawProcedureById(Long id);

  /**
   * Converts a given {@link Collection} of eventIds into {@link Event}s.
   *
   * @param ids eventId {@link Collection}.
   * @return {@link List} of {@link Event}s
   */
  List<Event> getEventsById(Collection<Long> ids);

  Exam getExamById(Long id);

  /**
   * Looks for a {@link FifoProcedure} by its unique identifier.
   *
   * @param id unique identifier.
   * @return {@link FifoProcedure} or {@link NoMatchingElementException}.
   */
  FifoProcedure getFifoProcedureById(Long id);

  /**
   * Looks for a {@link User} using his id.
   *
   * @param id unique identifier.
   * @return {@link User} object or null.
   */
  User getParticipantById(Long id);

  /**
   * Looks for {@link PriorityListItem}s being applied to a given {@link Event}.
   *
   * @param event {@link Event} to look for.
   * @return {@link List} of {@link PriorityListItem}s.
   * @throws IllegalArgumentException if event is null.
   */
  List<PriorityListItem> getPriorityListItemsByEvent(Event event);

  /**
   * Looks for all {@link RegistrationRuleSet} applied to an {@link Event}.
   *
   * @param event event to use.
   * @return applied {@link RegistrationRuleSet}.
   */
  List<RegistrationRuleSet> getRegistrationRuleSetsByEvent(Event event);

  /**
   * Looks for all {@link RegistrationRuleSet} applied to an {@link Event}.
   *
   * @param eventId event to use.
   * @return applied {@link RegistrationRuleSet}.
   */
  List<RegistrationRuleSet> getRegistrationRuleSetsByEventId(Long eventId);

  /**
   * Looks for a Rule using its unique name.
   *
   * @param name name to look for.
   * @return null if the rule was not found, else the found rule.
   */
  Rule getRuleByName(String name);

  /**
   * Looks for a {@link StudyCourse} matching a given name.
   *
   * @param name name to look for.
   * @return matching studyCourse or null.
   */
  StudyCourse getStudyCourseByName(String name);

  Subject getSubjectByNameAndMandator(String name, Session session);

  /**
   * Merges a {@link Category}y back to the Persistence.
   *
   * @param category the {@link Category} which has to be merged
   * @return the merged {@link Category}
   */

  Category mergeCategory(Category category);

  /**
   * Puts all changes of a given {@link Campaign} into the database.
   *
   * @param campaign campaign to synchronize changes
   * @return merged campaign
   */
  Campaign updateCampaign(Campaign campaign);

  /**
   * Merges an {@link Event} back, so that all changes become persistent. If the event does not exist, a
   * DataAccessException will be thrown.
   *
   * @param event {@link Event} to merge. Will throw {@link IllegalArgumentException} if <code>null</code>.
   * @return merged Event
   */
  Event updateEvent(Event event);

  /**
   * Merges an {@link Exam} back, so that all changes become persistent. If the event does not exist, a
   * DataAccessException will be thrown.
   *
   * @param exam {@link Exam} to merge. Will throw {@link IllegalArgumentException} if <code>null</code>.
   * @return merged Exam
   */
  Exam updateExam(Exam exam);

  Occurrence updateOccurrence(Occurrence occurrence);

  /**
   * Merges a user with the persistent user.
   *
   * @param user user to merge.
   */
  SingleUser updateSingleUser(SingleUser user);

  /**
   * Merges an {@link Subject} back, so that all changes become persistent. If the event does not exist, a
   * DataAccessException will be thrown.
   *
   * @param subject {@link Subject} to merge. Will throw {@link IllegalArgumentException} if <code>null</code>.
   * @return merged Subject
   */
  Subject updateSubject(Subject subject);

  /**
   * Writes a {@link Campaign} to to the Persistence.
   *
   * @param cam the {@link Campaign} which has to to be write.
   * @return the written {@link Campaign}
   */
  Campaign writeCampaign(Campaign cam);

  /**
   * Writes a {@link Category} to to the Persistence.
   *
   * @param cat the {@link Category} which has to to be write.
   * @return the written {@link Category}
   */
  Category writeCategory(Category cat);

  /**
   * Writes a {@link DrawProcedure} to to the Persistence.
   *
   * @param drawProc the {@link DrawProcedure} which has to to be write.
   * @return the written {@link DrawProcedure}
   */
  DrawProcedure writeDrawProcedure(DrawProcedure drawProc);

  /**
   * Writes a {@link Event} to to the Persistence.
   *
   * @param evt the {@link Event} which has to to be write.
   * @return the written {@link Event}
   */
  Event writeEvent(Event evt);

  Exam writeExam(Exam ex);

  /**
   * Writes a {@link FifoProcedure} to to the Persistence.
   *
   * @param fifoProc the {@link FifoProcedure} which has to to be write.
   * @return the written {@link FifoProcedure}
   */
  FifoProcedure writeFifoProcedure(FifoProcedure fifoProc);

  void writeLecture(SingleUser user, Event event);

  Occurrence writeOccurrenceToEvent(Occurrence occurrence, Event event, Session session);

  /**
   * Rule to merge with the database.
   *
   * @param rule rule to save or update.
   * @return saved or merged rule.
   */
  Rule writeRule(Rule rule, Session session);

  /**
   * Writes a {@link Subject} to to the Persistence.
   *
   * @param subject the {@link Subject} which has to to be write.
   * @return the written {@link Subject}
   */
  Subject writeSubject(Subject subject);

  /**
   * Writes a {@link Timetable} to to the Persistence.
   *
   * @param timetable the {@link Timetable} which has to to be write.
   * @return the written {@link Timetable}
   */
  Timetable writeTimetable(Timetable timetable);

  /**
   * gets all available terms.
   *
   * @return
   */
  List<Term> getAllTerms();

  Term createTerm(String termDesc, Session session);

  List<Event> getEventsByTeacher(String userName);

  List<Event> getEventsByTerm(String term);

  List<Event> getEventsByTermId(Long id);

  List<SingleUser> searchForUser(String searchString);

  void readAllStudyCourses();

  void updateTeacherRole(SingleUser singleUser);

  Mandator getTheAllMandator();

  List<Mandator> getAllMandators();

  Long getActiveMandator(Session session);

  Mandator createMandator(String name);

  Mandator getMandatorByName(String object);

  List<Mandator> getMandatorsFromSingleUser(SingleUser singleUser);

  List<Campaign> getCampaignsByMandator(Session session);

  List<Event> getEventsByTermAndMandator(String termDesc, Session session);

  List<Event> getEventsByMandator(Session session);

  List<Procedure> getProceduresByMandator(Session session);

  List<Subject> getSubjectsByMandator(Session session);

  List<Campaign> getActiveCampaignsByMandator(Session session);

  List<Rule> getRulesByMandator(Session session);

  List<Term> getTermsByMandator(Session session);

  List<Procedure> getAllUnusedProceduresByMandator(Session session);

  List<Category> getCategoriesByMandator(Session session);

  Mandator getMandatorById(Long mandator);

  String getTemplateAsString(Long activeMandator, TemplateType fifo);

  void saveTemplate(Long mandatorId, String content, TemplateType templateType);

  String loadDefaultTemplate(TemplateType modelObject);

  List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndMandator(Long id, Long activeMandator);

  void deleteCategory(Category category);

  void deleteTerm(Term term);

  boolean isMandatorDeletable(Mandator mandator);

  void deleteMandator(Mandator mandator);

  Category getCategoryByNameAndMandator(String modelObject, Session session);

  Rule getRuleByNameAndMandator(String value, Session session);

  StudyCourse getStudyCourseById(Long id);

  void testTemplate(String email, String template);
}
