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

package hsa.awp.user.security;

import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.user.facade.IUserFacade;
import hsa.awp.user.model.Mandator;
import hsa.awp.user.model.Role;
import hsa.awp.user.model.RoleMapping;
import hsa.awp.user.model.SingleUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.*;

/**
 * This populator sets the roles of an authentication depending on the data of the user objects.
 */
public class AWPAuthoritiesPopulator implements LdapAuthoritiesPopulator {
  /**
   * Logger object.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Dao object to lookup a user.
   */
  private IUserFacade userFacade;

  private List<String> initialSysadmins;

  public AWPAuthoritiesPopulator() {

    initialSysadmins = new LinkedList<String>();
  }

//  @Transactional
  @Override
  public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

    if (username == null) {
      logger.warn("username was null");
      throw new IllegalArgumentException("username must not be null");
    }
    Collection<GrantedAuthority> result = new HashSet<GrantedAuthority>();

    SingleUser currentUser = getAndPersistUser(username);

    for (RoleMapping roleMapping : currentUser.getRolemappings()) {
      result.add(new SimpleGrantedAuthority(roleMapping.getRole().toString()));
    }
    return result;
  }

  /**
   * Looks up the user from ldap, persists it in database and returns it.
   *
   * @param username - the username to lookup
   * @return A new user object.
   */
  private SingleUser getAndPersistUser(String username) {

    SingleUser currentUser;
    try {
      currentUser = userFacade.getSingleUserByLogin(username);

      if (initialSysadmins.contains(currentUser.getUsername())) {
        RoleMapping roleMapping = getAllMandatorWithSysAdmin(currentUser);
        currentUser.addRoleMapping(roleMapping);
      }

      currentUser = userFacade.updateSingleUser(currentUser);

      // At this time, we persist all users in the database
      // TODO should we persist in the dao?
      // userFacade.saveSingleUser(currentUser);
    } catch (NoMatchingElementException e) {
      // this should never occur
      logger.warn("SingleUser " + username + " not found", e);
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    /*
    * FIXME just a workaround because this exception will be thrown if a SingleUser logs in via UserGui (alex) *
    */
    // catch (IllegalArgumentException e) {
    // logger.error("Problem with Mapping Exception occured", e);
    // }
    return currentUser;
  }

  private RoleMapping getAllMandatorWithSysAdmin(SingleUser currentUser) {
    RoleMapping roleMapping = userFacade.getRoleMappingByExample(currentUser, Mandator.allMandator, Role.SYSADMIN);

    if (roleMapping == null) {
      roleMapping = RoleMapping.getInstance(Role.SYSADMIN, userFacade.getAllMandator(), currentUser);
      userFacade.saveRoleMapping(roleMapping);
    }
    return roleMapping;
  }

  /**
   * Setter for the initialSysadmins.
   *
   * @param initialSysadmins mapping for the user type contexts.
   */
  public void setInitialSysadmins(String initialSysadmins) {

    this.initialSysadmins = Arrays.asList(initialSysadmins.split(","));
  }

  /**
   * Setter of UserFacade.
   *
   * @param facade the UserFacade to set
   */
  public void setUserFacade(IUserFacade facade) {

    this.userFacade = facade;
  }
}
