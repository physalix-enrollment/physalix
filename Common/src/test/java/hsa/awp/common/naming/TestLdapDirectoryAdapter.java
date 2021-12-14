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

package hsa.awp.common.naming;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This unit test validates the LdapDirectoryAdapter. The underlying directorycontext will be mocked out. The test checks if the
 * right calls will be submittet to the dircontext.
 *
 * @author alex
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:config/test-config.xml"})
public class TestLdapDirectoryAdapter {
  /**
   * Path to ldap configuration file.
   */
  private static final String LDAP_CONFIG_FILE = "config/naming.test.properties";

  /**
   * The class under test.
   */
  @Resource(name = "common.naming.ldapdirectoryAdapter")
  private LdapDirectoryAdapter adapter;

  /**
   * Mocks the direcotycontext.
   */
  private InitialDirContext directoryContext;

  /**
   * The ldap configuration.
   */
  private Properties ldapConfig;

  @Before
  public void setUp() throws Exception {
    ldapConfig = new Properties();
    ldapConfig.load(ClassLoader.getSystemResourceAsStream(LDAP_CONFIG_FILE));

    directoryContext = mock(InitialDirContext.class);
    adapter.setDirContext(directoryContext);
  }

  /**
   * If a new configuration would be set, I expect the dircontext to be reconfigured automatically at the next lookup.
   *
   * @throws Exception if something went wrong.
   */
  @Test
  public void testAutoConfiguration() throws Exception {

    mockIgnoreAttributesLookup();
    adapter.getAttributes("test");
  }

  /**
   * Ignores a call of getAttributes(String, String) at the mockobject.
   *
   * @throws Exception if something went wrong.
   */
  private void mockIgnoreAttributesLookup() throws Exception {
    when(directoryContext.getAttributes(any(String.class), any(String[].class)))
            .thenReturn(new BasicAttributes());
  }

  /**
   * Tests if all attributes will be fetched.
   *
   * @throws Exception if an exception occurred.
   */
  @Test
  public void testGetAttributesString() throws Exception {
    String testname = "hans";
    adapter.getAttributes(testname);
    verify(directoryContext).getAttributes("uid=" + testname + ",ou=People,dc=domain,dc=com",
            ldapConfig.getProperty("naming.fields").split(","));
  }

  /**
   * Tests if it is possible to get only a minor set of attributes.
   *
   * @throws Exception if something went wrong.
   */
  @Test
  public void testGetAttributesStringStringArray() throws Exception {
    String testname = "hans";
    String[] testAttribs = {"uid", "name", "semester"};
    adapter.getAttributes(testname, testAttribs);
    verify(directoryContext).getAttributes("uid=" + testname + ",ou=People,dc=domain,dc=com", testAttribs);
  }
}
