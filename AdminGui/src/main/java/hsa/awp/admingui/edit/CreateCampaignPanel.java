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

package hsa.awp.admingui.edit;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Term;
import hsa.awp.user.model.StudyCourse;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.*;

/**
 * The {@link Panel} class which creates a new {@link Campaign}.
 *
 * @author klassm
 */
public class CreateCampaignPanel extends AbstractCampaign {
  /**
   * unique serialization id.
   */
  private static final long serialVersionUID = -3803350447257073195L;

  /**
   * Campaign to work with.
   */
  private Campaign campaign;

  /**
   * Constructor with ID for the {@link Panel}.
   *
   * @param id the {@link Panel}.
   */
  public CreateCampaignPanel(String id) {

    super(id);

    renderPage();
  }

  @Override
  protected Campaign getCampaign() {

    if (campaign == null) {
      campaign = Campaign.getInstance(getController().getActiveMandator(getSession()));
      campaign.getEndShow().add(Calendar.MINUTE, 10);
    }

    return campaign;
  }

  @Override
  protected List<StudyCourse> getSelectedStudyCourseItems() {
    return new ArrayList<StudyCourse>();
  }

  @Override
  protected List<StudyCourse> getStudyCourseItems() {
    return getController().getAllStudyCourses();
  }

  @Override
  protected List<Event> getEventModelItems() {
    Term selectedTerm = termModel.getObject();

    if (selectedTerm == null) {
      return getController().getEventsByMandator(getSession());
    } else {
      return getEventsSelectedByTerm(selectedTerm);
    }
  }

  @Override
  protected List<Event> getEventSelectedItems() {

    return new ArrayList<Event>();
  }

  @Override
  protected List<Procedure> getProcedureModelItems() {

    return getController().getAllUnusedProceduresByMandator(getSession());
  }

  @Override
  protected List<Procedure> getProcedureSelectedItems() {

    return new LinkedList<Procedure>();
  }

  @Override
  protected String getSuccessText() {
    //TODO Sprache
    return "Eingabe übernommen.";
  }

  @Override
  protected void workResult(String name, String email, List<Event> events, List<Procedure> procedures, Calendar startShow, Calendar endShow, List<StudyCourse> studyCourses, String detailText, Integer maximumConfirmedRegistrations) {

    Set<Long> eventIds = new HashSet<Long>();
    for (Event event : events) {
      eventIds.add(event.getId());
    }

    Set<Long> studyCourseIds = new HashSet<Long>();
    for (StudyCourse studyCourse : studyCourses) {
      studyCourseIds.add(studyCourse.getId());
    }

    getController().createCampaign(name, email, endShow, startShow, eventIds, procedures, getSession(), studyCourseIds, detailText, maximumConfirmedRegistrations);
  }
}
