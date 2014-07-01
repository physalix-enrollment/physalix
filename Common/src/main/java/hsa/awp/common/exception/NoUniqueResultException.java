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

package hsa.awp.common.exception;

/**
 * The {@link NoUniqueResultException} is thrown when a single result was requested
 * but more than one item was found
 *
 * @author johannes
 */
public class NoUniqueResultException extends DataAccessException {
  public NoUniqueResultException() {
  }

  public NoUniqueResultException(String s) {
    super(s);
  }

  public NoUniqueResultException(String s, Throwable cause) {
    super(s, cause);
  }
}
