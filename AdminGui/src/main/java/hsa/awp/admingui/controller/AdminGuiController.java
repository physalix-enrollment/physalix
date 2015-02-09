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

import hsa.awp.admingui.AuthenticatedSpringSecuritySession;
import hsa.awp.campaign.model.*;
import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.exception.ProgrammingErrorException;
import hsa.awp.common.mail.IMail;
import hsa.awp.common.mail.IMailFactory;
import hsa.awp.common.model.TemplateDetail;
import hsa.awp.common.model.TemplateType;
import hsa.awp.common.services.TemplateService;
import hsa.awp.event.facade.EventFacade;
import hsa.awp.event.model.*;
import hsa.awp.gui.controller.GuiController;
import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.Rule;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.wicket.Session;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.StringWriter;
import java.util.*;

/**
 * The AdminGuiController is the link point between AdminGui and {@link EventFacade}. It prepares the calls and the data from the
 * AdminGui and to the AdminGui.
 *
 * @author Rico Lieback
 * @author basti
 */
public class AdminGuiController extends GuiController implements IAdminGuiController {
  /**
   * Default Constructor. Prepares a new AdminGuiController reference.
   */
  public AdminGuiController() {

  }

  private TemplateService templateService;

  private IMailFactory mailFactory;

  @Override
  public void addRule(Campaign campaign, Event event, Rule rule, Session session) {

    ruleFacade.addRuleToRegistrationRuleSet(campaign.getId(), event.getId(), getActiveMandator(session), rule);
  }

  @Override
  public Category getCategoryById(Long id) {
    return evtFacade.getCategoryById(id);
  }

  @Override
  public void addRoleMappingToSingleUser(SingleUser user, RoleMapping roleMapping) {

    user = getUserById(user.getId());

    roleMapping = user.addRoleMapping(roleMapping);

    if (roleMapping.getId() == 0L) {
      userFacade.saveRoleMapping(roleMapping);
    } else {
      updateRoleMapping(roleMapping);
    }
    updateSingleUser(user);
  }

  @Override
  public void removeRoleMappingForMandator(SingleUser user, RoleMapping roleMapping, Mandator mandator) {

    if (!roleMapping.getMandators().contains(mandator)) {
      throw new IllegalArgumentException("SingleUser has no rolemapping for this mandator");
    }

    if (roleMapping.getMandators().size() > 1) {
      roleMapping.getMandators().remove(mandator);
      updateRoleMapping(roleMapping);
    } else if (roleMapping.getMandators().size() == 1) {
      user.removeRoleMapping(roleMapping);
      userFacade.removeRoleMapping(roleMapping);
      updateSingleUser(user);
    }
  }

  @Override
  public RoleMapping updateRoleMapping(RoleMapping roleMapping) {
    return userFacade.updateRoleMapping(roleMapping);
  }

  @Override
  public Campaign alterCampaign(Long id, Calendar endShow, Calendar startShow, List<Procedure> procedures, List<Long> events) {

    Campaign cam = null;
    try {
      cam = camFacade.getCampaignById(id);
      cam.setEndShow(endShow);
      cam.setStartShow(startShow);
      cam.setEventIds(new HashSet<Long>(events));

      for (Procedure procedure : procedures) {
        cam.addProcedure(procedure);
      }
      cam = camFacade.updateCampaign(cam);
    } catch (DataAccessException dae) {
      return null;
    }

    return cam;
  }

  @Override
  public DrawProcedure alterDrawProcedure(Long id, String name, Calendar startDate, Calendar endDate, Calendar drawDate) {

    DrawProcedure draw = null;
    try {
      draw = camFacade.getDrawProcedureById(id);

      draw.setDrawDate(drawDate);
      draw.setInterval(startDate, endDate);
      draw.setName(name);
      draw = camFacade.updateDrawProcedure(draw);
    } catch (DataAccessException dae) {
      return null;
    }
    return draw;
  }

  @Override
  public FifoProcedure alterFifoProcedure(Long id, String name, Calendar startDate, Calendar endDate) {

    FifoProcedure fifo = null;
    try {
      fifo = camFacade.getFifoProcedureById(id);

      fifo.setInterval(startDate, endDate);
      fifo.setName(name);

      fifo = camFacade.updateFifoProcedure(fifo);
    } catch (DataAccessException dae) {
      return null;
    }
    return fifo;
  }

  @Override
  public Subject alterSubject(Subject sub) {

    return evtFacade.updateSubject(sub);
  }

  @Override
  public Campaign createCampaign(String name, String email, Calendar endShow, Calendar startShow, List<Long> evts, List<Procedure> proc, Session session, Set<Long> studyCourses, String detailText) {

    Campaign campaign = null;
    try {
      campaign = Campaign.getInstance(getActiveMandator(session));
      campaign.setEndShow(endShow);
      campaign.setStartShow(startShow);
      campaign.setEventIds(new HashSet<Long>(evts));
      campaign.setName(name);
      campaign.setCorrespondentEMail(email);
      campaign.setStudyCourseIds(studyCourses);
      campaign.setDetailText(detailText);

      camFacade.saveCampaign(campaign);

      for (Procedure p : proc) {
        campaign.addProcedure(p);
        camFacade.updateProcedure(p);
      }

      camFacade.updateCampaign(campaign);
    } catch (DataAccessException dae) {
      dae.printStackTrace();
      return null;
    }

    return campaign;
  }

  @Override
  public Category createCategory(String name, Long mandatorId) {

    Category cat = null;
    try {
      cat = Category.getInstance(name, mandatorId);
      evtFacade.saveCategory(cat);
    } catch (DataAccessException e) {
      return null;
    }
    return cat;
  }

  @Override
  public void createConfirmedRegistration(SingleUser participant, Event event, Session session) {

    SingleUser initiator = userFacade.getSingleUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

    if (initiator == null) {
      throw new ProgrammingErrorException("NO INITIATOR (= LOGGED IN USER) FOUND");
    } else if (participant == null) {
      throw new IllegalArgumentException("no user given");
    } else if (event == null) {
      throw new IllegalArgumentException("no event given");
    }

    ConfirmedRegistration registration = ConfirmedRegistration.getInstance(event.getId(), getActiveMandator(session));
    System.err.println(participant.getName() + " " + participant.getId());
    registration.setInitiator(initiator.getId());
    registration.setParticipant(participant.getId());

    camFacade.saveConfirmedRegistration(registration);

    event = getEventById(event.getId());

    event.getConfirmedRegistrations().add(registration.getId());
    this.updateEvent(event);
  }

  @Override
  public void createConfirmedRegistration(User participant, Event event, Session session) {

    User initiator = userFacade.getSingleUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

    if (initiator == null) {
      throw new ProgrammingErrorException("NO INITIATOR (= LOGGED IN USER) FOUND");
    } else if (participant == null) {
      throw new IllegalArgumentException("no user given");
    } else if (event == null) {
      throw new IllegalArgumentException("no event given");
    }

    ConfirmedRegistration registration = ConfirmedRegistration.getInstance(event.getId(), getActiveMandator(session));
    registration.setInitiator(initiator.getId());
    registration.setParticipant(participant.getId());

    camFacade.saveConfirmedRegistration(registration);
  }

  @Override
  public DrawProcedure createDrawProcedure(String name, Calendar startDate, Calendar endDate, Calendar drawDate, Session session) {

    DrawProcedure draw = null;
    try {
      draw = DrawProcedure.getInstance(getActiveMandator(session));
      draw.setDrawDate(drawDate);
      draw.setInterval(startDate, endDate);
      draw.setName(name);

      camFacade.saveDrawProcedure(draw);
    } catch (DataAccessException dae) {
      return null;
    }
    return draw;
  }

  @Override
  public Event createEvent(int eventId, Subject sub, Term term, List<Exam> exam, List<SingleUser> teacher, int maxParticipants,
                           boolean groupEvt, int groupSize, Session session) {

    Event evt = null;
    try {
      if (!groupEvt) {
        evt = Event.getInstance(eventId, getActiveMandator(session));

        evt.setExams(new HashSet<Exam>(exam));
        evt.setSubject(sub);
        evt.setTerm(term);
        evt.setMaxParticipants(maxParticipants);
        HashSet<Long> t = new HashSet<Long>();
        for (SingleUser teacher2 : teacher) {
          t.add(teacher2.getId());
        }
        evt.setTeachers(t);

        evtFacade.saveEvent(evt);
      } else {
        // TODO GROUPEVENT
        throw new UnsupportedOperationException("not yet implemented");
      }
      sub.addEvent(evt);
      sub = evtFacade.updateSubject(sub);
    } catch (DataAccessException e) {
      return null;
    }
    return evt;
  }

  @Override
  public FifoProcedure createFifoProcedure(String name, Calendar startDate, Calendar endDate, Session session) {

    FifoProcedure fifo = null;
    try {
      fifo = FifoProcedure.getInstance(getActiveMandator(session));
      fifo.setInterval(startDate, endDate);
      fifo.setName(name);

      camFacade.saveFifoProcedure(fifo);
    } catch (DataAccessException dae) {
      dae.printStackTrace();
      return null;
    }
    return fifo;
  }

  @Override
  public Subject createSubject(String name, Category category, String desc, String link, Session session) {

    Subject sub = null;

    try {
      sub = Subject.getInstance(getActiveMandator(session));
      sub.setName(name);
      sub.setDescription(desc);
      sub.setLink(link);
      sub = evtFacade.saveSubject(sub);

      // add the new created subject to the concerning category
      category.addSubject(sub);
      category = evtFacade.updateCategory(category);
    } catch (DataAccessException dae) {
      return null;
    }

    return sub;
  }

  @Override
  public void deleteCampaign(Campaign campaign) {

    campaign = getCampaignById(campaign.getId());
    if (campaign.getAppliedProcedures().size() > 0) {
      throw new IllegalArgumentException("Kampagne enthält Prozeduren");
    }
    camFacade.removeCampaign(campaign);

  }

  @Override
  public void deleteProcedure(Procedure procedure) {
    if (procedure instanceof DrawProcedure) {
      deleteDrawProcedure((DrawProcedure) procedure);
    } else if (procedure instanceof FifoProcedure) {
      deleteFifoProcedure((FifoProcedure) procedure);
    }
  }

  @Override
  public void deleteCategory(Category category) {
    if (category.getSubjects().size() == 0) {
      evtFacade.removeCategory(category);
    } else {
      throw new IllegalArgumentException("Kategorie enthält noch Fächer");
    }
  }

  @Override
  public void deleteTerm(Term term) {

    Iterator<Event> iterator = getEventsByTermId(term.getId()).iterator();
    while (iterator.hasNext()) {
      Event event = iterator.next();

      deleteEvent(event);
    }
    evtFacade.removeTerm(term);
  }

  @Override
  public Category getCategoryByNameAndMandator(String modelObject, Session session) {
    return evtFacade.getCategoryByNameAndMandator(modelObject, getActiveMandator(session));  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void deleteMandator(Mandator mandator) {

    if (!isMandatorDeletable(mandator)) {
      throw new IllegalArgumentException("Mandant enthält noch objekte");
    }

    for (SingleUser singleUser : findSingleUsersByRole(Role.APPADMIN)) {
      RoleMapping roleMapping = singleUser.roleMappingForRole(Role.APPADMIN);
      if (roleMapping.getMandators().contains(mandator)) {
        removeRoleMappingForMandator(singleUser, roleMapping, mandator);
      }
    }

    for (SingleUser singleUser : findSingleUsersByRole(Role.SECRETARY)) {
      RoleMapping roleMapping = singleUser.roleMappingForRole(Role.SECRETARY);
      if (roleMapping.getMandators().contains(mandator)) {
        removeRoleMappingForMandator(singleUser, roleMapping, mandator);
      }
    }

    userFacade.removeMandator(mandator);
  }

  @Override
  public boolean isMandatorDeletable(Mandator mandator) {

    if (evtFacade.getCategoryByMandator(mandator.getId()).size() > 0) {
      return false;
    }
    if (camFacade.findCampaignsByMandatorId(mandator.getId()).size() > 0) {
      return false;
    }
    if (camFacade.findProceduresByMandatorId(mandator.getId()).size() > 0) {
      return false;
    }
    return true;
  }

  @Override
  public void deleteDrawProcedure(DrawProcedure drawProc) {
    camFacade.removeDrawProcedure(drawProc);

  }

  @Override
  public void deleteEvent(Event evt) {

    evt = getEventById(evt.getId());

    Iterator<Long> registrationIterator = evt.getConfirmedRegistrations().iterator();
    while (registrationIterator.hasNext()) {
      Long id = registrationIterator.next();
      ConfirmedRegistration registration = getConfirmedRegistrationById(id);
      registrationIterator.remove();
      camFacade.removeConfirmedRegistration(registration);
    }
    for (Campaign campaign : getCampaignsByEvent(evt)) {

      campaign.getEventIds().remove(evt.getId());
      updateCampaign(campaign);
    }

    Iterator<Long> teacherIterator = evt.getTeachers().iterator();
    while (teacherIterator.hasNext()) {
      SingleUser user = getUserById(teacherIterator.next());
      teacherIterator.remove();
      evt.removeTeacher(user);
    }

    evtFacade.removeEvent(evt);
  }

  @Override
  public void deleteExam(Exam exam, Event event) {

    if (!event.getExams().remove(exam)) {
      throw new IllegalStateException("Could not remove exam from event");
    }

    evtFacade.updateEvent(event);
    evtFacade.removeExam(exam);
  }

  @Override
  public void deleteFifoProcedure(FifoProcedure fifoProc) {

    camFacade.removeFifoProcedure(fifoProc);
  }

  @Override
  public void deleteLecture(SingleUser user, Event event) {

    event.removeTeacher(user);
    evtFacade.updateEvent(event);
    userFacade.updateSingleUser(user);
    updateTeacherRole(user);
  }

  @Override
  public void deleteOccurrence(Occurrence occurrence, Timetable timetable) {

    timetable.removeOccurrence(occurrence);
    evtFacade.removeOccurence(occurrence);
  }

  @Override
  public void deleteRule(Rule rule) {

    if (rule == null) {
      throw new IllegalArgumentException("no rule given");
    }

    ruleFacade.removeRule(rule);
  }

  @Override
  public void deleteRulesConnection(Long campaignId, Long eventId) {

    ruleFacade.removeRulesConnection(campaignId, eventId);
  }

  @Override
  public void deleteRuleFromRegistrationRuleSet(Long campaignId, Long eventId, Rule rule) {
    ruleFacade.removeRuleFromRegistrationRuleSet(campaignId, eventId, rule);
  }

  @Override
  public void deleteSubject(Subject subject) {

    evtFacade.removeSubject(subject);
  }

  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByCampaign(Campaign campaign) {

    return camFacade.findConfirmedRegistrationsByCampaign(campaign);
  }

  @Override
  public List<SingleUser> findSingleUsersByRole(Role role) {

    if (role == null) {
      throw new IllegalArgumentException("no role given");
    }

    return userFacade.getAllSingleUsersByRole(role);
  }

  @Override
  public List<Campaign> getActiveCampaigns() {

    return camFacade.findActiveCampaigns();
  }

  @Override
  public List<Procedure> getAllProcedures() {

    List<Procedure> procs = null;
    try {
      procs = camFacade.getAllProcedures();
    } catch (DataAccessException dae) {
      return null;
    }
    return procs;
  }

  @Override
  public List<Rule> getAllRules() {

    return ruleFacade.findAllRules();
  }

  @Override
  public List<StudyCourse> getAllStudyCourses() {

    return userFacade.getAllStudyCourses();
  }

  @Override
  public List<SingleUser> getAllTeacher() {

    List<SingleUser> teachers = null;
    try {
      teachers = userFacade.getAllTeachers();
    } catch (Exception dae) { // TODO: Just until userFacade is running again
      return new LinkedList<SingleUser>();
    }
    return teachers;
  }

  @Override
  public List<Procedure> getAllUnusedProcedures() {

    try {
      return camFacade.getAllUnusedProcedures();
    } catch (DataAccessException e) {
      return new LinkedList<Procedure>();
    }
  }

  @Override
  public Campaign getCampaignById(Long id) {

    Campaign c;
    try {
      c = camFacade.getCampaignById(id);
    } catch (DataAccessException e) {
      return null;
    }
    return c;
  }

  @Override
  public Campaign getCampaignByNameAndMandator(String name, Session session) {

    Campaign c;
    try {
      c = camFacade.getCampaignByNameAndMandator(name, getActiveMandator(session));
    } catch (DataAccessException e) {
      return null;
    }
    return c;
  }

  @Override
  public List<Campaign> getCampaignsByEvent(Event event) {

    return camFacade.getCampaignsByEventId(event.getId());
  }

  @Override
  public ConfirmedRegistration getConfirmedRegistrationById(Long id) {

    return camFacade.getConfirmedRegistrationById(id);
  }

  @Override
  public List<ConfirmedRegistration> getConfirmedRegistrationsByEvent(Event event) {

    if (event == null) {
      throw new IllegalArgumentException("no event given : null");
    }

    return camFacade.findConfirmedRegistrationsByEvent(event.getId());
  }

  @Override
  public DrawProcedure getDrawProcedureById(Long id) {

    DrawProcedure draw = null;
    try {
      draw = camFacade.getDrawProcedureById(id);
    } catch (DataAccessException dae) {
      return null;
    }
    return draw;
  }

  @Override
  public List<Event> getEventsById(Collection<Long> ids) {

    if (ids == null) {
      return null;
    }

    List<Event> events = new LinkedList<Event>();

    for (Long id : ids) {
      try {
        events.add(evtFacade.getEventById(id));
      } catch (DataAccessException e) {
        continue;
      }
    }
    return events;
  }

  @Override
  public Exam getExamById(Long id) {

    Exam exam = null;
    try {
      exam = evtFacade.getExamById(id);
    } catch (DataAccessException dae) {
      return null;
    }
    return exam;
  }

  @Override
  public FifoProcedure getFifoProcedureById(Long id) {

    FifoProcedure fifo = null;
    try {
      fifo = camFacade.getFifoProcedureById(id);
    } catch (DataAccessException dae) {
      return null;
    }
    return fifo;
  }

  @Override
  public User getParticipantById(Long id) {

    User participant = null;
    try {
      participant = userFacade.getUserById(id);
    } catch (DataAccessException dae) {
      return null;
    }
    return participant;
  }

  @Override
  public List<PriorityListItem> getPriorityListItemsByEvent(Event event) {

    if (event == null) {
      throw new IllegalArgumentException("no event given : null");
    }

    return camFacade.findPriorityListItemsByEventId(event.getId());
  }

  @Override
  public List<RegistrationRuleSet> getRegistrationRuleSetsByEvent(Event event) {

    return getRegistrationRuleSetsByEventId(event.getId());
  }

  @Override
  public List<RegistrationRuleSet> getRegistrationRuleSetsByEventId(Long eventId) {

    return ruleFacade.findRegistrationRuleSetsByEventId(eventId);
  }

  @Override
  public Rule getRuleByName(String name) {

    return ruleFacade.findRuleByName(name);
  }

  @Override
  public Rule getRuleByNameAndMandator(String value, Session session) {
    return ruleFacade.findRuleByNameAndMandator(value, getActiveMandator(session));  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public StudyCourse getStudyCourseById(Long id) {
    return userFacade.getStudyCourseById(id);
  }

  @Override
  public void testTemplate(String email, String template) {
    VelocityEngine velocityEngine = setUpEngine();
    VelocityContext velocityContext = setUpDummyContext();

    velocityContext.put("name", "Test-Name");
    velocityContext.put("eventlist", "Test-Veranstaltung");
    velocityContext.put("priolists", "Test-Wunschliste");
    velocityContext.put("campaign", "Test-Kampagne");
    velocityContext.put("procedure", "Test-Prozedur");

    StringWriter writer = new StringWriter();
    velocityEngine.evaluate(velocityContext, writer, "templateTest", template);

    String mailContent = writer.toString();

    IMail mail = mailFactory.getInstance(email, "Template Test", mailContent, "template-test");
    mail.send();

  }

  private VelocityContext setUpDummyContext() {
    VelocityContext context = new VelocityContext();

    return context;

  }

  private VelocityEngine setUpEngine() {

    VelocityEngine velocityEngine = new VelocityEngine();
    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    velocityEngine.init();

    return velocityEngine;
  }

  @Override
  public StudyCourse getStudyCourseByName(String name) {

    return userFacade.getStudyCourseByName(name);
  }

  @Override
  public Subject getSubjectByNameAndMandator(String name, Session session) {


    return evtFacade.getSubjectByNameAndMandatorId(name, getActiveMandator(session));
  }

  @Override
  public Category mergeCategory(Category category) {

    Category cat = null;
    try {
      cat = evtFacade.updateCategory(category);
    } catch (DataAccessException e) {
      return null;
    }
    return cat;
  }

  @Override
  public Campaign updateCampaign(Campaign campaign) {

    return camFacade.updateCampaign(campaign);
  }

  @Override
  public Event updateEvent(Event event) {

    return evtFacade.updateEvent(event);
  }

  @Override
  public Exam updateExam(Exam exam) {

    return evtFacade.updateExam(exam);
  }

  @Override
  public Occurrence updateOccurrence(Occurrence occurrence) {

    return evtFacade.updateOccurrence(occurrence);
  }

  @Override
  public SingleUser updateSingleUser(SingleUser user) {

    return userFacade.updateSingleUser(user);
  }

  @Override
  public Subject updateSubject(Subject subject) {

    return evtFacade.updateSubject(subject);
  }

  @Override
  public Campaign writeCampaign(Campaign cam) {

    if (!cam.getId().equals(0L)) {
      return camFacade.updateCampaign(cam);
    } else {
      return camFacade.saveCampaign(cam);
    }
  }

  @Override
  public Category writeCategory(Category cat) {

    if (!cat.getId().equals(0L)) {
      return evtFacade.updateCategory(cat);
    } else {
      return evtFacade.saveCategory(cat);
    }
  }

  @Override
  public DrawProcedure writeDrawProcedure(DrawProcedure drawProc) {

    if (!drawProc.getId().equals(0L)) {
      return camFacade.updateDrawProcedure(drawProc);
    } else {
      return camFacade.saveDrawProcedure(drawProc);
    }
  }

  @Override
  public Event writeEvent(Event evt) {

    if (!evt.getId().equals(0L)) {
      return evtFacade.updateEvent(evt);
    } else {
      return evtFacade.saveEvent(evt);
    }
  }

  @Override
  public Exam writeExam(Exam ex) {

    if (!ex.getId().equals(0L)) {
      return evtFacade.updateExam(ex);
    } else {
      return evtFacade.saveExam(ex);
    }
  }

  @Override
  public FifoProcedure writeFifoProcedure(FifoProcedure fifoProc) {

    if (!fifoProc.getId().equals(0L)) {
      return camFacade.updateFifoProcedure(fifoProc);
    } else {
      return camFacade.saveFifoProcedure(fifoProc);
    }
  }

  @Override
  public void writeLecture(SingleUser user, Event event) {

    event.addTeacher(user);
    evtFacade.updateEvent(event);
    userFacade.updateSingleUser(user);
    //updateTeacherRole(user);
  }

  @Override
  public Occurrence writeOccurrenceToEvent(Occurrence occurrence, Event event, Session session) {

    if (occurrence.getId().equals(0L)) {
      event = evtFacade.getEventById(event.getId());
      writeOccurrence(occurrence);
      if (event.getTimetable() == null) {
        Timetable time = Timetable.getInstance(getActiveMandator(session));
        writeTimetable(time);
        event.setTimetable(time);
        writeEvent(event);
      }
      event.getTimetable().addOccurrence(occurrence);
      event = evtFacade.updateEvent(event);
    } else {
      evtFacade.updateOccurrence(occurrence);
    }

    return evtFacade.updateOccurrence(occurrence);
  }

  @Override
  public Rule writeRule(Rule rule, Session session) {

    if (isNullOrZero(rule.getMandatorId())) {
      rule.setMandatorId(getActiveMandator(session));
    }

    if (isNotNullOrZero(rule.getId())) {
      return ruleFacade.updateRule(rule);
    } else {
      return ruleFacade.saveRule(rule);
    }
  }

  private boolean isNullOrZero(Long number) {
    return number == null || number.compareTo(0L) == 0;
  }

  private boolean isNotNullOrZero(Long number) {
    return !isNullOrZero(number);
  }

  @Override
  public Subject writeSubject(Subject subject) {

    if (!subject.getId().equals(0L)) {
      return evtFacade.updateSubject(subject);
    } else {
      return evtFacade.saveSubject(subject);
    }
  }

  @Override
  public Timetable writeTimetable(Timetable timetable) {

    if (!timetable.getId().equals(0L)) {
      return evtFacade.updateTimetable(timetable);
    } else {
      return evtFacade.saveTimetable(timetable);
    }
  }

  @Override
  public List<Term> getAllTerms() {
    return evtFacade.getAllTerms();
  }

  @Override
  public Term createTerm(String termDesc, Session session) {
    Term t = Term.getInstance(getActiveMandator(session));
    t.setTermDesc(termDesc);

    return evtFacade.saveTerm(t);
  }

  @Override
  public List<Event> getEventsByTeacher(String userName) {
    SingleUser user = getUserByName(userName);
    return evtFacade.getEventsByTeacher(user);

  }

  @Override
  public List<Event> getEventsByTerm(String term) {
    return evtFacade.getEventsByTerm(term);
  }

  @Override
  public List<Event> getEventsByTermId(Long id) {
    return evtFacade.getEventsByTermId(id);
  }

  @Override
  public List<SingleUser> searchForUser(String searchString) {
    return userFacade.searchForUser(searchString);
  }

  @Override
  public void readAllStudyCourses() {
    userFacade.readAllStudyCourses();
  }

  @Override
  public void updateTeacherRole(SingleUser singleUser) {
    singleUser = userFacade.updateSingleUser(singleUser);
    removeTeacherRoleIfPossible(singleUser);
    grantTeacherRoleIfPossible(singleUser);
  }

  private void grantTeacherRoleIfPossible(SingleUser singleUser) {
    if (singleUser.getLectures().size() > 0 && !singleUser.hasRole(Role.TEACHER)) {
      addRoleMappingToSingleUser(singleUser, RoleMapping.getInstance(Role.TEACHER, userFacade.getAllMandator(), singleUser));
    }
  }

  private void removeTeacherRoleIfPossible(SingleUser singleUser) {
    if (singleUser.getLectures().size() == 0 && singleUser.hasRole(Role.TEACHER)) {
      singleUser.getRolemappings().remove(singleUser.roleMappingForRole(Role.TEACHER));
      userFacade.updateSingleUser(singleUser);
    }
  }

  @Override
  public Mandator getTheAllMandator() {
    return userFacade.getAllMandator();
  }

  @Override
  public List<Mandator> getAllMandators() {
    return userFacade.getAllMandators();
  }

  @Override
  public Mandator createMandator(String name) {
    Mandator mandator = userFacade.saveMandator(Mandator.getInstance(name));
    getTemplateAsString(mandator.getId(), TemplateType.FIFO);
    getTemplateAsString(mandator.getId(), TemplateType.DRAWN);
    getTemplateAsString(mandator.getId(), TemplateType.DRAWN_NO_LUCK);
    return mandator;
  }

  @Override
  public Mandator getMandatorByName(String object) {
    return userFacade.getMandatorByName(object);
  }

  @Override
  public List<Mandator> getMandatorsFromSingleUser(SingleUser singleUser) {

    Set<Mandator> mandatorSet = new HashSet<Mandator>();

    for (RoleMapping roleMapping : singleUser.getRolemappings()) {
      if (!roleMapping.getRole().equals(Role.GUEST) && !roleMapping.getRole().equals(Role.REGISTERED)) {
        mandatorSet.addAll(roleMapping.getMandators());
      }
    }

    return new ArrayList<Mandator>(mandatorSet);
  }

  @Override
  public Long getActiveMandator(Session session) {

    if (session == null) {
      return 1L; // for test purposes
    }

    AuthenticatedSpringSecuritySession securitySession = (AuthenticatedSpringSecuritySession) session;

    if (securitySession.getMandatorId() == null) {
      return determineInitialActiveMandator();
    }

    return securitySession.getMandatorId();
  }

  private Long determineInitialActiveMandator() {
    SingleUser user;
    try {
      user = userFacade.getSingleUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
    } catch (Exception e) {
      return 1L;
    }

    List<Mandator> mandators = getMandatorsFromSingleUser(user);

    if (mandators.size() == 0) {
      throw new IllegalStateException("User can have no mandator");
    } else if (mandators.size() == 1) {
      return mandators.get(0).getId();
    } else {
      if (mandators.get(0).equals(getTheAllMandator())) {
        return mandators.get(1).getId();
      } else {
        return mandators.get(0).getId();
      }
    }
  }

  @SuppressWarnings("unused")
  private List<Mandator> getMandatorsFromUser(SingleUser user) {
    List<Mandator> mandators = new ArrayList<Mandator>();
    for (RoleMapping roleMapping : user.getRolemappings()) {
      for (Mandator mandator : roleMapping.getMandators()) {
        mandators.add(mandator);
      }
    }

    Collections.sort(mandators, new Comparator<Mandator>() {
      @Override
      public int compare(Mandator mandator, Mandator mandator1) {
        return mandator.getName().compareTo(mandator1.getName());
      }
    });

    return mandators;
  }

  @Override
  public Campaign createCampaign(String name, String email, Calendar endShow,
                                 Calendar startShow, LinkedList<Long> linkedList,
                                 List<Procedure> procedures, Session session, Set<Long> studyCourseIds,
                                 String detailText) {
    Campaign campaign = null;
    try {
      campaign = Campaign.getInstance(getActiveMandator(session));
      campaign.setEndShow(endShow);
      campaign.setStartShow(startShow);
      campaign.setEventIds(new HashSet<Long>(linkedList));
      campaign.setName(name);
      campaign.setCorrespondentEMail(email);
      campaign.setStudyCourseIds(studyCourseIds);
      campaign.setDetailText(detailText);

      camFacade.saveCampaign(campaign);

      for (Procedure p : procedures) {
        campaign.addProcedure(p);
        camFacade.updateProcedure(p);
      }

      camFacade.updateCampaign(campaign);
    } catch (DataAccessException dae) {
      dae.printStackTrace();
      return null;
    }

    return campaign;
  }

  /**
   * Setter for the {@link IUserFacade}.
   *
   * @param usrFacade the usrFacade to set
   */
  public void setUserFacade(IUserFacade usrFacade) {

    this.userFacade = usrFacade;
  }

  /**
   * writes a occurence to the db.
   *
   * @param occurrence occurrence to write
   * @return written occurrence.
   */
  private Occurrence writeOccurrence(Occurrence occurrence) {

    if (!occurrence.getId().equals(0L)) {
      return evtFacade.updateOccurrence(occurrence);
    } else {
      return evtFacade.saveOccurrence(occurrence);
    }
  }

  @Override
  public List<Campaign> getCampaignsByMandator(Session session) {
    return camFacade.findCampaignsByMandatorId(getActiveMandator(session));
  }

  @Override
  public List<Event> getEventsByTermAndMandator(String termDesc, Session session) {
    return evtFacade.findEventsByTermAndMandator(termDesc, getActiveMandator(session));
  }

  @Override
  public List<Event> getEventsByMandator(Session session) {
    return evtFacade.findEventsByMandator(getActiveMandator(session));
  }

  @Override
  public List<Procedure> getProceduresByMandator(Session session) {
    return camFacade.findProceduresByMandatorId(getActiveMandator(session));
  }

  @Override
  public List<Subject> getSubjectsByMandator(Session session) {
    return evtFacade.getSubjectsByMandator(getActiveMandator(session));
  }

  @Override
  public List<Campaign> getActiveCampaignsByMandator(Session session) {
    return camFacade.getActiveCampaignsByMandatorId(getActiveMandator(session));
  }

  @Override
  public List<Rule> getRulesByMandator(Session session) {
    return ruleFacade.findRuleByMandator(getActiveMandator(session));
  }

  @Override
  public List<Term> getTermsByMandator(Session session) {
    return evtFacade.getTermsByMandator(getActiveMandator(session));

  }

  @Override
  public List<Procedure> getAllUnusedProceduresByMandator(Session session) {
    return camFacade.getAllUnusedProceduresByMandator(getActiveMandator(session));
  }

  @Override
  public List<Category> getCategoriesByMandator(Session session) {
    return evtFacade.getCategoryByMandator(getActiveMandator(session));
  }

  @Override
  public Mandator getMandatorById(Long mandator) {
    return userFacade.getMandatorById(mandator);
  }

  @Override
  public List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantIdAndMandator(Long id, Long activeMandator) {
    return camFacade.findConfirmedRegistrationsByParticipantIdAndMandator(id, activeMandator);
  }

  @Override
  public String getTemplateAsString(Long mandatorId, TemplateType templateType) {
    return templateService.loadTemplate(TemplateDetail.getInstance(mandatorId, templateType));
  }

  @Override
  public void saveTemplate(Long mandatorId, String content, TemplateType templateType) {
    templateService.saveTemplate(content, TemplateDetail.getInstance(mandatorId, templateType));
  }

  @Override
  public String loadDefaultTemplate(TemplateType templateType) {
    return templateService.loadDefaultTemplate(TemplateDetail.getInstance(0L, templateType));
  }

  public void setTemplateService(TemplateService templateService) {
    this.templateService = templateService;
  }

  public void setMailFactory(IMailFactory mailFactory) {
    this.mailFactory = mailFactory;
  }
}

