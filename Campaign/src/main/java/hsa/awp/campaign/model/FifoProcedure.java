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

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Calendar;

import static javax.persistence.EnumType.STRING;

/**
 * Persistent object for {@link FifoProcedure}. {@link ConfirmedRegistration}s will be created by using a FIFO algorithm.
 *
 * @author klassm
 */
@Entity
@Table(name = "`fifoprocedure`")
public class FifoProcedure extends Procedure {
  /**
   * Version UID which is used for serialization.
   */
  private static final long serialVersionUID = -289359297968891789L;

  @Enumerated(STRING)
  private FifoDisplayMode displayMode;

  /**
   * Creates a new {@link FifoProcedure} and initializes it with appropriate values (e.g. inserts a HashSet at appropriate places,
   * so that the Sets can be used without risking a {@link NullPointerException}.
   *
   * @return new domain object.
   */
  public static FifoProcedure getInstance(Long mandatorId) {

    FifoProcedure fifoProcedure = new FifoProcedure();
    fifoProcedure.setInterval(Calendar.getInstance(), Calendar.getInstance());
    fifoProcedure.setMandatorId(mandatorId);
    return fifoProcedure;
  }

  /**
   * Constructs a new {@link FifoProcedure}.
   */
  FifoProcedure() {

    super();
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FifoProcedure)) {
      return false;
    }
//        FifoProcedure other = (FifoProcedure) obj;
//
//        // equality is already checked with super.equals()
//        if (getId() != null && getId() != 0L) {
//            return true;
//        }

    return true;
  }

  @Override
  public int hashCode() {
    // if item is persisted use only the id for calculating hash code
    if (getId() != null && getId() != 0L) {
      return super.hashCode();
    }

//        final int prime = 31;
    int result = super.hashCode();
    return result;
  }

  public FifoDisplayMode getDisplayMode() {
    return displayMode;
  }

  public void setDisplayMode(FifoDisplayMode displayMode) {
    this.displayMode = displayMode;
  }
}
