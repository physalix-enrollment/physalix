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

package hsa.awp.admingui.util;

import hsa.awp.admingui.AuthenticatedSpringSecuritySession;
import hsa.awp.user.model.Role;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class AccessUtil {

  public static void allowRender(Component component, String right) {
    MetaDataRoleAuthorizationStrategy.authorize(component, Component.RENDER, right);
  }

  public static void allowRender(Component component, String... rights) {
    for (String right : rights) {
      allowRender(component, right);
    }
  }

  public static boolean hasRole(Session session, String role) {
    return ((AuthenticatedSpringSecuritySession) session).getRoles().hasRole(role);
  }

  public static boolean isTeacher() {

    for (GrantedAuthority authority : getAuthorities()) {
      if (authority.getAuthority().equals(Role.TEACHER.toString())) {
        return true;
      }
    }

    return false;
  }

  public static boolean hasAdministrativeAccess() {
    for (GrantedAuthority authority : getAuthorities()) {
      if (Role.isAdministrativeAuthority(authority.getAuthority())) {
        return true;
      }
    }
    return false;
  }

  public static Collection<? extends GrantedAuthority> getAuthorities() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
  }
}
