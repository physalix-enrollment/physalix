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

import hsa.awp.admingui.ItemNotFoundPage;
import hsa.awp.admingui.OnePanelPage;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Term;
import hsa.awp.user.model.StudyCourse;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.*;

/**
 * Panel class editing a {@link Campaign}.
 *
 * @author klassm
 */
public class AlterCampaignPanel extends AbstractCampaign {
  /**
   * Serialization id.
   */
  private static final long serialVersionUID = 9006092448363099969L;

  /**
   * Campaign to work with.
   */
  private Campaign campaign;

  /**
   * Constructor with {@link Panel}ID and the ID of the {@link Campaign} which has to be edited.
   *
   * @param id         the {@link Panel}ID
   * @param campaignId the ID of the {@link Campaign} which has to be edit
   */
  public AlterCampaignPanel(String id, Long campaignId) {

    super(id);

    campaign = getController().getCampaignById(campaignId);
    if (campaign == null) {
      setResponsePage(new OnePanelPage(new CampaignNotFoundPage(id, campaign)));
    }

    renderPage();
  }

  @Override
  protected Campaign getCampaign() {

    return campaign;
  }

  @Override
  protected List<StudyCourse> getSelectedStudyCourseItems() {
    Set<Long> studyCourseIds = getCampaign().getStudyCourseIds();
    List<StudyCourse> studyCourses = new ArrayList<StudyCourse>();

    for (Long id : studyCourseIds) {
      studyCourses.add(getController().getStudyCourseById(id));
    }

    return studyCourses;
  }

  @Override
  protected List<StudyCourse> getStudyCourseItems() {
    return getController().getAllStudyCourses();
  }

  @Override
  protected List<Event> getEventModelItems() {

    List<Event> all = getController().getEventsByMandator(getSession());
    all.removeAll(getController().getEventsById(getCampaign().getEventIds()));
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("allEvents: " + all.size() + " " + all.toString());
    }

    Term term = termModel.getObject();
    if (term != null) {
      return getEventsSelectedByTerm(term, all);
    }

    return all;
  }

  @Override
  protected List<Event> getEventSelectedItems() {

    List<Event> selected = getController().getEventsById(getCampaign().getEventIds());
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("selectedEvents: " + selected.size() + " " + selected.toString());
    }

    return selected;
  }

  @Override
  protected List<Procedure> getProcedureModelItems() {

    List<Procedure> all = getController().getAllUnusedProceduresByMandator(getSession());
    all.removeAll(getCampaign().getAppliedProcedures());

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("allProcedureItems: " + all.size() + " " + all.toString());
    }

    return all;
  }

  @Override
  protected List<Procedure> getProcedureSelectedItems() {

    List<Procedure> selected = new LinkedList<Procedure>(getCampaign().getAppliedProcedures());

    if (getLogger().isTraceEnabled()) {
      getLogger().debug("selectedProcedures: " + selected.size() + " " + selected.toString());
    }

    return selected;
  }

  @Override
  protected String getSuccessText() {

    return "Eingaben übernommen.";
  }

  @Override
  protected void workResult(String name, String email, List<Event> events, List<Procedure> proceduresInput, Calendar startShow, Calendar endShow, List<StudyCourse> studyCourses, String detailText, Integer maximumConfirmedRegistrations) {

    List<Procedure> procedures = new LinkedList<Procedure>(proceduresInput);
    HashSet<Long> eventIds = new HashSet<Long>();
    for (Event event : events) {
      eventIds.add(event.getId());
    }

    Set<Long> studyCourseIds = new HashSet<Long>();
    for (StudyCourse studyCourse : studyCourses) {
      studyCourseIds.add(studyCourse.getId());
    }

    // look for no longer selected events
    Set<Long> oldEvents = new HashSet<Long>(campaign.getEventIds());
    for (Long id : eventIds) {
      oldEvents.remove(id);
    }

    // remove all rules applied to that event and campaign combination.
    for (Long id : oldEvents) {
      getController().deleteRulesConnection(campaign.getId(), id);
    }

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("selectedEvents after send: " + events.size() + " " + events.toString());
    }

    // look for no longer selected Procedures
    for (Procedure procOld : new LinkedList<Procedure>(campaign.getAppliedProcedures())) {
      // already in there - don't need to add
      if (procedures.contains(procOld)) {
        procedures.remove(procOld);
      } else {
        campaign.removeProcedure(procOld);
      }
    }
    // add the new ones.
    for (Procedure p : procedures) {
      campaign.addProcedure(p);
    }

    campaign.setName(name);
    campaign.setCorrespondentEMail(email);
    campaign.setStudyCourseIds(studyCourseIds);
    campaign.setDetailText(detailText);

    // campaign.setProcedures(new HashSet<Procedure>(procedures));
    campaign.setStartShow(startShow);
    campaign.setEndShow(endShow);
    campaign.setEventIds(eventIds);
    campaign.setMaximumConfirmedRegistrations(maximumConfirmedRegistrations);

    getController().updateCampaign(campaign);
  }

  /**
   * Page that will be called when a {@link Campaign} was not found.
   *
   * @author klassm
   */
  private class CampaignNotFoundPage extends ItemNotFoundPage<Campaign> {
    /**
     * unique serialization id.
     */
    private static final long serialVersionUID = -6255008803050707070L;

    /**
     * Creates a new {@link CampaignNotFoundPage}.
     *
     * @param id   wicket id.
     * @param item item that was not found.
     */
    public CampaignNotFoundPage(String id, Campaign item) {

      super(id, item);
    }

    @Override
    public String renderObject() {

      if (getItem() == null) {
        return "no Campaign given";
      }
      return getItem().getName() + " was not found.";
    }
  }
}
