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

package hsa.awp.user.model;

import hsa.awp.common.IGenericDomainModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistent object for SingleUser.
 *
 * @author johannes
 */
@Entity
@Table(name = "`singleuser`")
@Inheritance(strategy = InheritanceType.JOINED)
public class SingleUser extends User implements IGenericDomainModel<Long> {
  /**
   * Version UID which is used for serialization.
   */
  private static final long serialVersionUID = -6152162224635489405L;

  /**
   * faculty name.
   */
  private String faculty;

  /**
   * List of events the users is teacher.
   */
  @ElementCollection
  private Set<Long> lectures;

  /**
   * E-Mail adress of the user.
   */
  private String mail;

  /**
   * Name of the {@link SingleUser}.
   */
  private String name;

  /**
   * {@link Role} of the {@link SingleUser}.
   */
  @OneToMany(mappedBy = "singleUser", cascade = CascadeType.ALL)
  private Set<RoleMapping> rolemappings;

  /**
   * SingleUser name of the {@link SingleUser}.
   */
  @Column(unique = true)
  private String username;

  /**
   * unique id from ldap.
   */
  @Column(unique = true)
  private Long uuid;

  /**
   * Initializes a new user object.
   *
   * @return new domain object.
   */
  public static SingleUser getInstance() {

    SingleUser u = new SingleUser();

    u.initialize();
    return u;
  }

  @Override
  protected void initialize() {

    super.initialize();
    lectures = new HashSet<Long>();
    rolemappings = new HashSet<RoleMapping>();

  }

  /**
   * Creates a new {@link SingleUser} and initializes it with appropriate values (e.g. inserts a HashSet at appropriate places, so
   * that the Sets can be used without risking a {@link NullPointerException}.
   *
   * @param username username of the {@link SingleUser}
   * @return new domain object.
   */
  public static final SingleUser getInstance(String username) {

    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("no username given");
    }

    SingleUser u = getInstance();
    u.setUsername(username);
    return u;
  }

  /**
   * Default constructor is in default visibility, because hibernate needs a default constructor.
   */
  SingleUser() {

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
    if (!(obj instanceof SingleUser)) {
      return false;
    }
    SingleUser other = (SingleUser) obj;

    // equality is already checked with super.equals()
    if (getId() != null && getId() != 0L) {
      return true;
    }

    if (faculty == null) {
      if (other.faculty != null) {
        return false;
      }
    } else if (!faculty.equals(other.faculty)) {
      return false;
    }
    if (mail == null) {
      if (other.mail != null) {
        return false;
      }
    } else if (!mail.equals(other.mail)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (username == null) {
      if (other.username != null) {
        return false;
      }
    } else if (!username.equals(other.username)) {
      return false;
    }
    if (uuid == null) {
      if (other.uuid != null) {
        return false;
      }
    } else if (!uuid.equals(other.uuid)) {
      return false;
    }
    if (rolemappings == null) {
      if (other.rolemappings != null) {
        return false;
      }
    } else if (!rolemappings.equals(other.rolemappings)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    // if item is persisted use only the id for calculating hash code
    if (getId() != null && getId() != 0L) {
      return super.hashCode();
    }

    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((faculty == null) ? 0 : faculty.hashCode());
    result = prime * result + ((mail == null) ? 0 : mail.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    result = prime * result + ((rolemappings == null) ? 0 : rolemappings.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return super.getClass().getSimpleName() + "{" +
            "id='" + super.getId() + '\'' +
            ", name='" + name + '\'' +
            ", rolemappings=" + getRolemappings() +
            ", username='" + getUsername() + '\'' +
            ", uuid=" + getUuid() +
            '}';
  }

  /**
   * Adds a lecture to the user.
   *
   * @param id id of the event where the User should be lecturer.
   */
  public void addLecture(Long id) {

    lectures.add(id);
  }

  /**
   * Returns faculty.
   *
   * @return the faculty
   */
  public String getFaculty() {

    return faculty;
  }

  /**
   * Setter for faculty.
   *
   * @param faculty the faculty to set
   */
  public void setFaculty(String faculty) {

    this.faculty = faculty;
  }

  /**
   * Getter for lectures.
   *
   * @return the lectures
   */
  public Set<Long> getLectures() {

    return lectures;
  }

  /**
   * @param lectures the lectures to set
   */
  public void setLectures(Set<Long> lectures) {

    this.lectures = lectures;
  }

  /**
   * Returns the mail address of the {@link SingleUser}.
   *
   * @return the mail address
   */
  public String getMail() {

    return mail;
  }

  /**
   * Setter for the E-Mail address.
   *
   * @param mail - The E-Mail address.
   */
  public void setMail(String mail) {

    this.mail = mail;
  }

  /**
   * Returns name.
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
   * Returns roles.
   *
   * @return the roles a user has
   */
  public Set<RoleMapping> getRolemappings() {
    return rolemappings;
  }

  /**
   * Setter for roles.
   *
   * @param rolemappings the roles to set
   */
  public void setRolemappings(Set<RoleMapping> rolemappings) {

    if (rolemappings == null) {
      throw new IllegalArgumentException("rolemappings cant be null");
    }

    this.rolemappings = rolemappings;
  }

  /**
   * Returns username.
   *
   * @return the username
   */
  public String getUsername() {

    return username;
  }

  /**
   * Setter for username.
   *
   * @param username the username to set
   */
  public void setUsername(String username) {

    this.username = username;
  }

  /**
   * Getter for uuid.
   *
   * @return the uuid
   */
  public Long getUuid() {

    return uuid;
  }

  /**
   * @param uuid the uuid to set
   */
  public void setUuid(Long uuid) {

    this.uuid = uuid;
  }

  /**
   * removes a lecture from the user.
   *
   * @param id id of the event where the User is lecturer.
   */
  public void removeLecture(Long id) {

    lectures.remove(id);
  }

  /**
   * Extracts the first name out of the full name. This might not be 100%
   * correct in all cases.
   * @return the users first name
   */
  public String getFirstName() {
    return name.substring(0, name.lastIndexOf(" "));
  }

  /**
   * Extracts the last name out of the full name. This might not be 100%
   * correct in all cases.
   * @return the users last name
   */
  public String getLastName() {
    return name.substring(name.lastIndexOf(" "));
  }

  public boolean hasRole(Role role) {

    if (role == null) {
      throw new IllegalArgumentException("role is null");
    }

    for (RoleMapping roleMapping : rolemappings) {
      if (role.equals(roleMapping.getRole())) {
        return true;
      }
    }

    return false;
  }

  public RoleMapping roleMappingForRole(Role role) {

    RoleMapping found = null;

    for (RoleMapping mapping : rolemappings) {
      if (role.equals(mapping.getRole())) {
        if (found != null) {
          throw new IllegalStateException("multiple Roles");
        }
        found = mapping;
      }
    }

    if (found != null) {
      return found;
    }

    throw new IllegalStateException("user doesn't have rolemapping for role: " + role);
  }

  public RoleMapping addRoleMapping(RoleMapping roleMapping) {

    if (roleMapping == null) {
      throw new IllegalArgumentException("rolemapping is null");
    }

    if (hasRole(roleMapping.getRole())) {
      RoleMapping mapping = roleMappingForRole(roleMapping.getRole());
      if (!mapping.equals(roleMapping)) {
        mapping.getMandators().addAll(roleMapping.getMandators());
        roleMapping = mapping;
      }
    } else {
      getRolemappings().add(roleMapping);
    }

    roleMapping.setSingleUser(this);
    return roleMapping;
  }

  public void removeRoleMapping(RoleMapping roleMapping) {
    rolemappings.remove(roleMapping);

  }

  public boolean isTeacher() {
    return lectures.size() > 0;
  }
}
