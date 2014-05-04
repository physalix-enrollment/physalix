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
import hsa.awp.campaign.facade.ICampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.exception.DataAccessException;
import hsa.awp.event.facade.EventFacade;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Category;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.rule.facade.IRuleFacade;
import hsa.awp.rule.facade.RuleFacade;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.Student;
import hsa.awp.user.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

public class GuiController implements IGuiController {
	/**
	 * {@link EventFacade} for creating and modifying components of event.
	 */
	protected IEventFacade evtFacade;

	/**
	 * {@link CampaignFacade} for creating and modifying components of campaign.
	 */
	protected ICampaignFacade camFacade;

	/**
	 * {@link RuleFacade} for accessing all facade methods.
	 */
	protected IRuleFacade ruleFacade;

	/**
	 * {@link IUserFacade} for accessing all facade methods.
	 */
	protected IUserFacade userFacade;

	public GuiController() {

		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hsa.awp.admingui.controller.IGuiController#getCategoryByName(java.lang
	 * .String)
	 */
	@Override
	public Category getCategoryByName(String name) {

		Category cat = null;
		try {
			cat = evtFacade.getCategoryByName(name);
		} catch (DataAccessException e) {
			return null;
		}
		return cat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hsa.awp.admingui.controller.IGuiController#getAllCategories()
	 */
	@Override
	public List<Category> getAllCategories() {

		List<Category> list = null;
		try {
			list = evtFacade.getAllCategories();
		} catch (DataAccessException dae) {
			return null;
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hsa.awp.admingui.controller.IGuiController#getAllSubjects()
	 */
	@Override
	public List<Subject> getAllSubjects() {

		List<Subject> list = null;
		try {
			list = evtFacade.getAllSubjects();
		} catch (DataAccessException dae) {
			return null;
		}
		return list;
	}

	@Override
	public List<Subject> findAllSubjectsByCategoryId(long id, Procedure proc) {
		List<Subject> list = new ArrayList<Subject>();
		try {
			Set<Long> subjectIds = new HashSet<Long>();
			for(Event event : getEventsByCampaign(proc.getCampaign())){
				subjectIds.add(event.getSubject().getId());
			}
			for(Subject sub : evtFacade.findAllSubjectsByCategoryId(id)){
				if(subjectIds.contains(sub.getId())){
					list.add(sub);
				}
			}
		} catch (DataAccessException dae) {
			return null;
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hsa.awp.admingui.controller.IGuiController#getAllEvents()
	 */
	@Override
	public List<Event> getAllEvents() {

		return evtFacade.getAllEvents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hsa.awp.admingui.controller.IGuiController#getAllCampaigns()
	 */
	@Override
	public List<Campaign> getAllCampaigns() {

		List<Campaign> cams = null;
		try {
			cams = camFacade.getAllCampaigns();
		} catch (DataAccessException dae) {
			return null;
		}

		return cams;
	}

	@Override
	public Event getEventById(Long id) {

		try {
			return evtFacade.getEventById(id);
		} catch (DataAccessException dae) {
			return null;
		}
	}

	@Override
	public List<ConfirmedRegistration> getConfirmedRegistrationsByIds(
			Set<Long> ids) {

		List<ConfirmedRegistration> confirmedRegs = new ArrayList<ConfirmedRegistration>();

		for (Long id : ids) {
			try {
				confirmedRegs.add(camFacade.getConfirmedRegistrationById(id));
			} catch (DataAccessException dae) {
				// element not found
			}
		}
		return confirmedRegs;
	}

	@Transactional
	public void deleteConfirmedRegistration(
			ConfirmedRegistration confirmedRegistration) {

		if (confirmedRegistration == null) {
			throw new IllegalArgumentException("no confirmedRegistration given");
		}

		camFacade.removeConfirmedRegistration(confirmedRegistration);

		Event e = evtFacade.getEventById(confirmedRegistration.getEventId());
		e.getConfirmedRegistrations().remove(confirmedRegistration.getId());
		evtFacade.updateEvent(e);
	}

	@Override
	public long countConfirmedRegistrationsByEventId(long eventId) {

		return camFacade.countConfirmedRegistrationsByEventId(eventId);
	}

	@Override
	public SingleUser getUserById(Long id) {

		SingleUser singleUser = null;
		try {
			singleUser = userFacade.getSingleUserById(id);
		} catch (DataAccessException dae) {
			return null;
		} catch (UnsupportedOperationException e) {
			// UNSUPPORTEDOPERATION IN USER COMPONENT
			Student stud = Student.getInstance("test", 1);
			stud.setName("");
			return stud;
		}
		return singleUser;
	}

	@Override
	public SingleUser getUserByName(String username) {

		try {
			return userFacade.getSingleUserByLogin(username);
		} catch (DataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Event> getEventsByCampaign(Campaign campaign) {

		List<Event> events = new LinkedList<Event>();

		campaign = camFacade.getCampaignById(campaign.getId());

		for (Long id : campaign.getEventIds()) {
			events.add(evtFacade.getEventById(id));
		}

		return events;
	}
	
	@Override
	public long findCategoryIdByEventId(long id){
		return evtFacade.findCategoryIdByEventId(id);
	}
	
	@Override
	public List<Event> findEventsBySubjectId(long subjectId, Procedure proc){
		List<Event> list = new ArrayList<Event>();
		try {
			Set<Long> ids = proc.getCampaign().getEventIds();
			for(Event e : evtFacade.findEventsBySubjectId(subjectId)){
				if(ids.contains(e.getId())){
					list.add(e);
				}
			}
		} catch (DataAccessException dae) {
			return null;
		}
		return list;
	}

	@Override
	public void removePriolist(PriorityList priorityList) {

		priorityList = camFacade.updatePriorityList(priorityList);
		camFacade.removePriorityList(priorityList);
	}

	/**
	 * Setter for camFacade.
	 * 
	 * @param camFacade
	 *            the camFacade to set
	 */
	public void setCampaignFacade(ICampaignFacade camFacade) {

		this.camFacade = camFacade;
	}

	/**
	 * Setter for evtFacade.
	 * 
	 * @param evtFacade
	 *            the evtFacade to set
	 */
	public void setEventFacade(IEventFacade evtFacade) {

		this.evtFacade = evtFacade;
	}

	/**
	 * @param ruleFacade
	 *            the ruleFacade to set
	 */
	public void setRuleFacade(IRuleFacade ruleFacade) {

		this.ruleFacade = ruleFacade;
	}

	/**
	 * @param userFacade
	 *            the userFacade to set
	 */
	public void setUserFacade(IUserFacade userFacade) {

		this.userFacade = userFacade;
	}

	@Override
	public List<ConfirmedRegistration> findConfirmedRegistrationsByParticipantId(
			Long participantId) {

		return camFacade
				.findConfirmedRegistrationsByParticipantId(participantId);
	}

	@Override
	public boolean hasParticipantConfirmedRegistrationInEvent(User participant,
			Event event) {
		return camFacade.hasParticipantConfirmedRegistrationInEvent(
				participant.getId(), event.getId());
	}
}
