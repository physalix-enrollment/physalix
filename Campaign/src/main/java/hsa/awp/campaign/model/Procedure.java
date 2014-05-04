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

/**
 * Persistent object for Procedure.
 * 
 * @author klassm
 */
@Entity
@Table(name = "`procedure`")
@Inheritance(strategy = InheritanceType.JOINED)
public class Procedure extends AbstractMandatorableDomainObject<Long> {
	/**
	 * Version UID which is used for serialization.
	 */
	private static final long serialVersionUID = -4432675108319355841L;

	/**
	 * {@link Campaign} the {@link Procedure} belongs to.
	 */
	@ManyToOne(targetEntity = Campaign.class)
	private Campaign campaign;

	/**
	 * End Date of the {@link Procedure}.
	 */
	@Column(nullable = false)
	private Calendar endDate;

	/**
	 * Unique identifier.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = 0L;

	/**
	 * Name of the procedure.
	 */
	private String name;

	/**
	 * Start Date of the {@link Procedure}.
	 */
	@Column(nullable = false)
	private Calendar startDate;

	@Column(nullable = false)
	private int ruleBased;

	/**
	 * Creates a new {@link Procedure} and initializes it with appropriate
	 * values (e.g. inserts a HashSet at appropriate places, so that the Sets
	 * can be used without risking a {@link NullPointerException}.
	 * 
	 * @return new domain object.
	 */
	public static Procedure getInstance(Long mandatorId) {

		Procedure procedure = new Procedure();
		procedure.setInterval(Calendar.getInstance(), Calendar.getInstance());
		procedure.setMandatorId(mandatorId);
		return procedure;
	}

	// TODO All : check if the part of business logic can be located at a single
	// point.
	// TODO All : check if we better use Calendar.clone() instead of holding the
	// original reference.

	/**
	 * Setter for the interval of a {@link Procedure}. If startDate is after
	 * endDate a {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param startDate
	 *            beginning of the {@link Procedure}.
	 * @param endDate
	 *            end of the {@link Procedure}.
	 */
	public void setInterval(Calendar startDate, Calendar endDate) {

		if (startDate == null || endDate == null
				|| startDate.compareTo(endDate) > 0) {
			throw new IllegalArgumentException("invalid dates.");
		}

		if (campaign != null) {
			for (Procedure p : campaign.getAppliedProcedures()) {
				if (p.getStartDate().compareTo(this.getStartDate()) <= 0
						&& p.getEndDate().compareTo(this.getEndDate()) >= 0) {
					throw new IllegalArgumentException(
							"Procedure with that start and endDate is already active : "
									+ p.getName());
				}
			}
		}

		this.setStartDate(startDate);
		this.setEndDate(endDate);
	}

	/**
	 * Default constructor.
	 */
	Procedure() {

	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Procedure)) {
			return false;
		}
		Procedure other = (Procedure) obj;

		// if item is persisted use only the id for checking
		if (id != null && id != 0L) {
			return id.equals(other.id);
		}

		if (campaign == null) {
			if (other.campaign != null) {
				return false;
			}
		} else if (!campaign.equals(other.campaign)) {
			return false;
		}
		if (endDate == null) {
			if (other.endDate != null) {
				return false;
			}
		} else if (!endDate.equals(other.endDate)) {
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
		if (startDate == null) {
			if (other.startDate != null) {
				return false;
			}
		} else if (!startDate.equals(other.startDate)) {
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
		result = prime * result
				+ ((campaign == null) ? 0 : campaign.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public String toString() {

		return getName();
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
	 * Getter for campaign.
	 * 
	 * @return the campaign
	 */
	public Campaign getCampaign() {

		return campaign;
	}

	/**
	 * Setter for campaign.
	 * 
	 * @param campaign
	 *            the campaign to set
	 */
	void setCampaign(Campaign campaign) {

		this.campaign = campaign;
	}

	/**
	 * Getter for the end Date.
	 * 
	 * @return end Date.
	 */
	public Calendar getEndDate() {

		return endDate;
	}

	/**
	 * Setter for the end Date.
	 * 
	 * @param end
	 *            new end Date.
	 */
	void setEndDate(Calendar end) {

		this.endDate = end;
	}

	/**
	 * Getter for the name of the procedure.
	 * 
	 * @return name of the procedure.
	 */
	public String getName() {

		return name;
	}

	/**
	 * Setter for the name of the procedure.
	 * 
	 * @param name
	 *            name of the procedure.
	 */
	public void setName(String name) {

		this.name = name;
	}

	/**
	 * Getter for the startDate.
	 * 
	 * @return startDate.
	 */
	public Calendar getStartDate() {

		return startDate;
	}

	/**
	 * Setter for the start Date.
	 * 
	 * @param start
	 *            new start Date.
	 */
	void setStartDate(Calendar start) {

		this.startDate = start;
	}

	/**
	 * Checks whether the {@link Procedure} is currently active.
	 * 
	 * @return true if the {@link Procedure} is currently active.
	 */
	public boolean isActive() {

		return isActive(Calendar.getInstance());
	}

	/**
	 * Checks whether the {@link Procedure} is active on a given date.
	 * 
	 * @param date
	 *            date to check
	 * @return true if the {@link Procedure} is active at the given date.
	 * @throws IllegalArgumentException
	 *             if date is null.
	 */
	public boolean isActive(Calendar date) {

		if (date == null) {
			throw new IllegalArgumentException("no date given");
		}

		if (startDate.before(date) && endDate.after(date)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether another {@link Procedure} is overlapping this
	 * {@link Procedure}. (same algorithm as in {@link Campaign})
	 * 
	 * @param p
	 *            {@link Procedure} to check.
	 * @return true if the {@link Procedure} is overlapping.
	 */
	public boolean isOverlapping(Procedure p) {
		// checks if the endDate overlaps
		// ....|-----------|
		// |---*****|
		// .......|*****|
		// if (proc.getStartDate().before(p.getEndDate()) &&
		// proc.getEndDate().after(p.getEndDate())) {
		if (this.getStartDate().compareTo(p.getEndDate()) <= 0
				&& this.getEndDate().compareTo(p.getStartDate()) >= 0) {
			return true;
		}

		// checks if the StartDate overlaps
		// |--------|
		// ....|*****----|
		// if (proc.getStartDate().before(p.getStartDate()) &&
		// proc.getEndDate().after(p.getEndDate())) {
		if (this.getStartDate().compareTo(p.getStartDate()) <= 0
				&& this.getEndDate().compareTo(p.getEndDate()) >= 0) {
			return true;
		}

		// checks if start and end date surrounds
		// ...|-----|
		// |--*******--|
		// if (proc.getStartDate().after(p.getStartDate()) &&
		// proc.getEndDate().before(p.getEndDate())) {
		if (this.getStartDate().compareTo(p.getStartDate()) >= 0
				&& this.getEndDate().compareTo(p.getEndDate()) <= 0) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            identifier
	 */
	void setId(Long id) {

		this.id = id;
	}

	public int getRuleBased() {
		return ruleBased;
	}

	public void setRuleBased(int ruleBased) {
		this.ruleBased = ruleBased;
	}

}
