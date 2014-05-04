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

package hsa.awp.event.model;

import hsa.awp.common.AbstractMandatorableDomainObject;
import hsa.awp.user.model.SingleUser;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistent object for Events.
 *
 * @author klassm
 */
@Entity
@Table(name = "`event`")
public class Event extends AbstractMandatorableDomainObject<Long> implements Serializable {
  /**
   * Version UID which is used for serialization.
   */
  private static final long serialVersionUID = 497338647575843048L;

  /**
   * List of ConfirmedRegistrations.
   */
  @ElementCollection
  private Set<Long> confirmedRegistrations;

  /**
   * Event number.
   */
  private int eventId;

  private String detailInformation = "";

  /**
   * List of related Exams.
   */
  @OneToMany(cascade = {CascadeType.MERGE})
  @Column(nullable = false)
  private Set<Exam> exams;

  /**
   * Identifier for an Event.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = 0L;

  /**
   * Maximum number of participants.
   */
  @Column(name = "maxParticipants")
  private int maxParticipants;

  /**
   * Subject the Event is related to.
   */
  @ManyToOne(cascade = {CascadeType.MERGE})
  private Subject subject;

  /**
   * List of teachers.
   */
  @ElementCollection
  private Set<Long> teachers;

  /**
   * Term the Event is related to.
   */
  @ManyToOne(cascade = {CascadeType.MERGE})
  private Term term;

  /**
   * Timetable the Event is related to.
   */
  @OneToOne(cascade = {CascadeType.MERGE})
  private Timetable timetable;

  /**
   * Creates a new {@link Event} and initializes it with appropriate values (e.g. inserts a HashSet at appropriate places, so that
   * the Sets can be used without risking a {@link NullPointerException}.
   *
   * @param eventId id of the associated {@link Event}.
   * @return new domain object.
   */
  public static Event getInstance(int eventId, Long mandatorId) {

    Event event = new Event(eventId, mandatorId);

    event.setExams(new HashSet<Exam>());
    event.setConfirmedRegistrations(new HashSet<Long>());
    event.setTeachers(new HashSet<Long>());

    return event;
  }

  /**
   * Default constructor..
   */
  protected Event() {

  }

  /**
   * Constructor for creating an event. eventId is unique!
   *
   * @param eventId identifier for the event.
   */
  public Event(int eventId, Long mandatorId) {

    this.eventId = eventId;
    this.setMandatorId(mandatorId);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Event)) {
      return false;
    }
    Event other = (Event) obj;

    // if item is persisted use only the id for checking
    if (id != null && id != 0L) {
      return id.equals(other.id);
    }

    if (eventId != other.eventId) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (maxParticipants != other.maxParticipants) {
      return false;
    }
    if (subject == null) {
      if (other.subject != null) {
        return false;
      }
    } else if (!subject.equals(other.subject)) {
      return false;
    }
    if (detailInformation == null) {
      if (other.detailInformation != null) {
        return false;
      }
    } else if (!detailInformation.equals(other.detailInformation)) {
      return false;
    }
    if (term == null) {
      if (other.term != null) {
        return false;
      }
    } else if (!term.equals(other.term)) {
      return false;
    }
    if (timetable == null) {
      if (other.timetable != null) {
        return false;
      }
    } else if (!timetable.equals(other.timetable)) {
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
    result = prime * result + eventId;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + maxParticipants;
    result = prime * result + ((subject == null) ? 0 : subject.hashCode());
    result = prime * result + ((detailInformation == null) ? 0 : detailInformation.hashCode());
    result = prime * result + ((term == null) ? 0 : term.hashCode());
    result = prime * result + ((timetable == null) ? 0 : timetable.hashCode());
    return result;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer();
    sb.append("id: ").append(id).append(", Exam: ").append(exams).append(", subject: ").append(subject).append(", term: ")
        .append(term).append(", timetable: ").append(timetable).append(", ").append(super.toString());

    return sb.toString();
  }

  /**
   * Adds a teacher to the event.
   *
   * @param user user who is teacher
   */
  public void addTeacher(SingleUser user) {

    if (user == null) {
      throw new IllegalArgumentException("User can't be null");
    }

    teachers.add(user.getId());
    user.addLecture(this.getId());
  }

  /**
   * Getter for the identifier.
   *
   * @return identifier.
   */
  public Long getId() {

    return id;
  }

  public String getDetailInformation() {
    return detailInformation;
  }

  public void setDetailInformation(String detailInformation) {
    this.detailInformation = detailInformation;
  }

  /**
   * Getter for ConfirmedRegistrations.
   *
   * @return List of ConfirmedRegistration identifiers.
   */
  public Set<Long> getConfirmedRegistrations() {

    return confirmedRegistrations;
  }

  /**
   * Setter for ConfirmedRegistrations.
   *
   * @param confirmedRegistrations List of {@link ConfirmedRegistration} identifiers.
   */
  public void setConfirmedRegistrations(HashSet<Long> confirmedRegistrations) {

    this.confirmedRegistrations = confirmedRegistrations;
  }

  /**
   * Returns the {@link Event} id.
   *
   * @return {@link Event} id
   */
  public int getEventId() {

    return eventId;
  }

  /**
   * Setter for the {@link Event} id.
   *
   * @param eventId {@link Event} id
   */
  public void setEventId(int eventId) {

    this.eventId = eventId;
  }

  /**
   * Getter for the related {@link Exam}s.
   *
   * @return List of {@link Exam}.
   */
  public Set<Exam> getExams() {

    return exams;
  }

  /**
   * Setter for the related {@link Exam}s.
   *
   * @param exam new {@link Exam} List
   */
  public void setExams(Set<Exam> exam) {

    this.exams = exam;
  }

  /**
   * Getter for the maximum number of participants.
   *
   * @return maximum number of participants.
   */
  public int getMaxParticipants() {

    return maxParticipants;
  }

  /**
   * Setter for the maximum number of participants.
   *
   * @param maxParticipants maximum number of participants
   */
  public void setMaxParticipants(int maxParticipants) {

    this.maxParticipants = maxParticipants;
  }

  /**
   * Getter for the related {@link Subject}.
   *
   * @return related {@link Subject}.
   */
  public Subject getSubject() {

    return subject;
  }

  /**
   * Setter for the related {@link Subject}.
   *
   * @param subject new {@link Subject}.
   */
  public void setSubject(Subject subject) {

    subject.addEvent(this);
    this.subject = subject;
  }

  /**
   * Getter for the teacher list.
   *
   * @return teachers.
   */
  @Column(nullable = false)
  public Set<Long> getTeachers() {

    return teachers;
  }

  /**
   * Setter for the teacher list.
   *
   * @param teachers teachers.
   */
  public void setTeachers(HashSet<Long> teachers) {

    this.teachers = teachers;
  }

  /**
   * Getter for the related {@link Term}.
   *
   * @return related {@link Term}.
   */
  public Term getTerm() {

    return term;
  }

  /**
   * Setter for the related {@link Term}.
   *
   * @param term new {@link Term}.
   */
  public void setTerm(Term term) {

    this.term = term;
  }

  /**
   * Getter for the related {@link Timetable}.
   *
   * @return related {@link Timetable}.
   */
  public Timetable getTimetable() {

    return timetable;
  }

  /**
   * Setter for the related {@link Timetable}.
   *
   * @param timetable new {@link Timetable}.
   */
  public void setTimetable(Timetable timetable) {

    this.timetable = timetable;
  }

  /**
   * Proves that a place is free in this {@link Event}.
   *
   * @return <code>true</code> when a place is free, otherwise <code>false</code>
   */
  public boolean hasPlaceLeft() {

    return confirmedRegistrations.size() < maxParticipants;
  }

  /**
   * Removes a teacher from event.
   *
   * @param user user to be removed.
   */
  public void removeTeacher(SingleUser user) {

    if (user == null) {
      throw new IllegalArgumentException("User can't be null");
    }

    teachers.remove(user.getId());
    user.removeLecture(this.getId());
  }

  /**
   * Setter for the eventId.
   *
   * @param id new identifier.
   */
  public void setId(Long id) {

    this.id = id;
  }


}
