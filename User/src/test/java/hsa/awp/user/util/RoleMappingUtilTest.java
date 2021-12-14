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

package hsa.awp.user.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/spring/RoleMappingUtilTest.xml")
public class RoleMappingUtilTest {


  private RoleMappingUtil roleMappingUtil;

  @Before
  public void init() {
    roleMappingUtil = new RoleMappingUtil();
    roleMappingUtil.setPropertyPath("/config/roleMappingTest.properties");
  }

  @Test
  public void testGetRightsForRole() {
    List<String> rights = roleMappingUtil.getRightsForRole("ROLE1");
    assertEquals("right1", rights.get(0));
  }

  @Test
  public void testGetRightsForRoleWithMultipleRights() {
    List<String> rights = roleMappingUtil.getRightsForRole("ROLE2");
    assertEquals("right1", rights.get(0));
    assertEquals("right2", rights.get(1));
    assertEquals("right3", rights.get(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShouldThrowExceptionIfRoleIsEmpty() {

    List<String> rights = roleMappingUtil.getRightsForRole("");
  }

}
