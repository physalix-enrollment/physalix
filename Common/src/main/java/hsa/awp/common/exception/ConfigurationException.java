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
 * This exception will be thrown if there is a configuration problem. For
 * example:
 * <ul>
 * <li>Configuration file not found.</li>
 * <li>Illegal configuration</li>
 * </ul>
 *
 * @author alex
 */
public class ConfigurationException extends RuntimeException {
  /**
   * Serial id.
   */
  private static final long serialVersionUID = -3943076590367861653L;

  /**
   * Constructs a new runtime exception with null as its detail message. The
   * cause is not initialized, and may subsequently be initialized by a call
   * to Throwable.initCause.
   *
   * @see java.lang.RunTimeException
   */
  public ConfigurationException() {

    super();
  }

  /**
   * Constructs a new runtime exception with null as its detail message. The
   * cause is not initialized, and may subsequently be initialized by a call
   * to Throwable.initCause.
   *
   * @param message - the detail message. The detail message is saved for later
   *                retrieval by the Throwable.getMessage() method.
   * @see java.lang.RunTimeException
   */
  public ConfigurationException(String message) {

    super(message);
  }

  /**
   * Constructs a new runtime exception with the specified cause and a detail
   * message of (cause==null ? null : cause.toString()) (which typically
   * contains the class and detail message of cause). This constructor is
   * useful for runtime exceptions that are little more than wrappers for
   * other throwables.
   *
   * @param cause - the cause (which is saved for later retrieval by the
   *              Throwable.getCause() method). (A null value is permitted, and
   *              indicates that the cause is nonexistent or unknown.)
   * @see java.lang.RunTimeException
   */
  public ConfigurationException(Throwable cause) {

    super(cause);
  }

  /**
   * Constructs a new runtime exception with the specified detail message and
   * cause.
   * <p/>
   * Note that the detail message associated with cause is not automatically
   * incorporated in this runtime exception's detail message.
   *
   * @param message - the detail message (which is saved for later retrieval by
   *                the Throwable.getMessage() method).
   * @param cause   - the cause (which is saved for later retrieval by the
   *                Throwable.getCause() method). (A null value is permitted, and
   *                indicates that the cause is nonexistent or unknown.)
   * @see java.lang.RunTimeException
   */
  public ConfigurationException(String message, Throwable cause) {

    super(message, cause);
  }
}
