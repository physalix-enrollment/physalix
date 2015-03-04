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

package hsa.awp.campaign.model;

import hsa.awp.common.AbstractMandatorableDomainObject;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistent object for {@link Campaign}.
 *
 * @author klassm
 */
@Entity
@Table(name = "`campaign`", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "mandatorId"})
})
public class Campaign extends AbstractMandatorableDomainObject<Long> {
  /**
   * Version UID which is used for serialization.
   */
  private static final long serialVersionUID = -4470037949918491800L;

  private String correspondentEMail;

  private Integer maximumConfirmedRegistrations;

  @Column(length = 800)
  private String detailText;

  /**
   * The date when a Campaign will disappear in the GUI.
   */
  @Column(nullable = false)
  private Calendar endShow;

  /**
   * List containing the corresponding Events.
   */
  @ElementCollection
  private Set<Long> eventIds;

  /**
   * List containing the corresponding StudyCourses.
   */
  @ElementCollection
  private Set<Long> studyCourseIds;

  /**
   * Unique identifier.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = 0L;

  /**
   * Unique name of the {@link Campaign}.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Procedures of the current campaign. Each procedure will be used to confirm or deny registrations according to the appropriate
   * mechanism.
   */
  @OneToMany(targetEntity = Procedure.class, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
  private Set<Procedure> procedures;

  /**
   * The date when a Campaign will start to show up in the GUI.
   */
  @Column(nullable = false)
  private Calendar startShow;


  /**
   * Creates a new {@link Campaign} and initializes it with appropriate values (e.g. inserts a HashSet at appropriate places, so
   * that the Sets can be used without risking a {@link NullPointerException}.
   *
   * @return new domain object.
   */
  public static Campaign getInstance(Long mandatorId) {

    Campaign campaign = new Campaign();
    campaign.setStartShow(Calendar.getInstance());
    campaign.setEndShow(Calendar.getInstance());
    campaign.setEventIds(new HashSet<Long>());
    campaign.setProcedures(new HashSet<Procedure>());
    campaign.setMandatorId(mandatorId);

    return campaign;
  }

  /**
   * Default constructor.
   */
  Campaign() {

  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Campaign)) {
      return false;
    }
    Campaign other = (Campaign) obj;

    // if item is persisted use only the id for checking
    if (id != null && id != 0L) {
      return id.equals(other.id);
    }

    if (endShow == null) {
      if (other.endShow != null) {
        return false;
      }
    } else if (!endShow.equals(other.endShow)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (startShow == null) {
      if (other.startShow != null) {
        return false;
      }
    } else if (!startShow.equals(other.startShow)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    // if item is persisted use only the id for calculating hash code
    if (id != null && id != 0L) {
      return id.hashCode();
    }

    final int prime = 31;
    int result = 1;
    result = prime * result + ((endShow == null) ? 0 : endShow.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((startShow == null) ? 0 : startShow.hashCode());
    return result;
  }

  @Override
  public String toString() {

    return this.getName();
  }

  /**
   * Getter for the unique identifier.
   *
   * @return unique identifier.
   */
  public Long getId() {

    return id;
  }


  /**
   * Adds the given {@link Procedure} to this {@link Campaign}.
   * <p/>
   * Asserts that the start and end date of the given {@link Procedure} doesn't overlap with other {@link Procedure}s that are
   * already added to this {@link Campaign}. If the {@link Procedure} is already added to this campaign, nothing is done.
   *
   * @param p the {@link Procedure} to be added.
   * @throws IllegalArgumentException if the given {@link Procedure} is <code>null</code> or overlapping any other {@link Procedure}.
   * @throws IllegalStateException    if the given {@link Procedure} is already added to another campaign.
   * @throws IllegalStateException    if the given {@link Procedure} is already added to this campaign but has no valid reference.
   */
  public void addProcedure(Procedure p) {

    if (p == null) {
      throw new IllegalArgumentException("no Procedure given.");
    }

    // if this procedure is already applied. Than nothing is to do.
    if (procedures.contains(p)) {
      if (p.getCampaign() == null) {
        throw new IllegalStateException("Procedure is added to campaign but has no campaign referenced!");
      }
      return;
    }

    if (p.getCampaign() != null) {
      throw new IllegalStateException("Procedure is already added to campaign '" + p.getCampaign().getName() + "'");
    }

    for (Procedure proc : procedures) {
      if (proc.isOverlapping(p)) {
        throw new IllegalArgumentException(proc.getName() + " is overlapping " + p.getName());
      }
    }

    p.setCampaign(this);
    procedures.add(p);
  }

  /**
   * Looks for the current {@link Procedure}: the startDate of the {@link Procedure} has to be before or exact now and the endDate
   * after or exact now.
   *
   * @return the current {@link Procedure} or <code>null</code> if no matching {@link Procedure} was found.
   */
  public Procedure findCurrentProcedure() {

    return findCurrentProcedure(Calendar.getInstance());
  }

  /**
   * Looks for the current {@link Procedure}: the startDate of the {@link Procedure} has to be before or exact now and the endDate
   * after or exact now.
   *
   * @param date a {@link Calendar} object which is used to search for the current {@link Procedure} relative to the given
   *             Calendar.
   * @return the current {@link Procedure} or <code>null</code> if no matching {@link Procedure} was found.
   */
  public Procedure findCurrentProcedure(Calendar date) {

    for (Procedure p : procedures) {
      if (p.getStartDate().before(date) && p.getEndDate().after(date)) {
        return p;
      }
    }

    return null;
  }

  /**
   * Returns an unmodifiable Set of {@link Procedure}s. To add or remove a {@link Procedure} use addProcedure or removeProcedure.
   *
   * @return unmodifiable Set of {@link Procedure}s.
   */
  public Set<Procedure> getAppliedProcedures() {

    return Collections.unmodifiableSet(procedures);
  }

  public String getCorrespondentEMail() {

    return correspondentEMail;
  }

  public void setCorrespondentEMail(String correspondentEMail) {

    this.correspondentEMail = correspondentEMail;
  }

  /**
   * Getter for endShow.
   *
   * @return date when a Campaign will disappear in the GUI
   */
  public Calendar getEndShow() {

    return endShow;
  }

  public String getDetailText() {
    return detailText;
  }

  public void setDetailText(String detailText) {
    this.detailText = detailText;
  }

  /**
   * Setter for endShow.
   *
   * @param endShow date when a Campaign will disappear in the GUI
   */
  public void setEndShow(Calendar endShow) {

    this.endShow = endShow;
  }

  /**
   * Getter for the associated Events.
   *
   * @return List of Events.
   */
  public Set<Long> getEventIds() {

    return eventIds;
  }

  /**
   * Setter for the associated Events.
   *
   * @param eventIds identifiers of the associated events as List.
   */
  public void setEventIds(Set<Long> eventIds) {

    this.eventIds = eventIds;
  }

  public Set<Long> getStudyCourseIds() {
    return studyCourseIds;
  }

  public void setStudyCourseIds(Set<Long> studyCourseIds) {
    this.studyCourseIds = studyCourseIds;
  }

  /**
   * Getter for name.
   *
   * @return the name
   */
  public String getName() {

    return name;
  }

  /**
   * Setter for name.
   *
   * @param name the name to set
   */
  public void setName(String name) {

    this.name = name;
  }

  /**
   * List of procedures used to confirm or deny registrations.
   *
   * @return List of procedures
   */
  Set<Procedure> getProcedures() {

    return procedures;
  }

  /**
   * Sets a list of procedures used to confirm or deny registrations.
   *
   * @param procedures list of procedures.
   */
  void setProcedures(Set<Procedure> procedures) {

    this.procedures = procedures;
  }

  /**
   * Getter for startShow.
   *
   * @return date when a Campaign will start to show up in the GUI
   */
  public Calendar getStartShow() {

    return startShow;
  }

  /**
   * Setter for startShow.
   *
   * @param startShow date when a Campaign will start to show up in the GUI
   */
  public final void setStartShow(Calendar startShow) {

    this.startShow = startShow;
  }

  /**
   * Checks whether the {@link Campaign} is currently running.
   *
   * @return true if the {@link Procedure} is currently running.
   */
  public boolean isRunning() {

    Calendar now = Calendar.getInstance();
    if (startShow.before(now) && endShow.after(now)) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether the {@link Campaign} is already terminated.
   *
   * @return true if the {@link Procedure} is terminated.
   */
  public boolean isTerminated() {

    Calendar now = Calendar.getInstance();
    if (endShow.before(now)) {
      return true;
    }
    return false;
  }

  // TODO Jojo : perhaps return the return value of procedures.remove()?

  /**
   * Removes the given {@link Procedure} from the {@link Campaign}s procedure list.
   *
   * @param p the {@link Procedure} to be removed
   */
  public void removeProcedure(Procedure p) {

    if (p == null) {
      throw new IllegalArgumentException("no Procedure given.");
    }
    // TODO Jojo : is Procedure cascading delete with campaign?
    p.setCampaign(null);
    procedures.remove(p);
  }

  /**
   * Setter for the unique id.
   *
   * @param id unique id.
   */
  public void setId(Long id) {

    this.id = id;
  }

  /**
   * @deprecated use {@link #getMaximumConfirmedRegistrationsOrDefault()}
   */
  @Deprecated
  public Integer getMaximumConfirmedRegistrations() {
    return maximumConfirmedRegistrations;
  }

  public int getMaximumConfirmedRegistrationsOrDefault() {
    return maximumConfirmedRegistrations == null ? 0 : maximumConfirmedRegistrations;
  }

  public void setMaximumConfirmedRegistrations(Integer maximumConfirmedRegistrations) {
    this.maximumConfirmedRegistrations = maximumConfirmedRegistrations == null ? 0 : maximumConfirmedRegistrations;
  }
}
