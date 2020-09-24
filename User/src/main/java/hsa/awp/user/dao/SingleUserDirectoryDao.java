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

package hsa.awp.user.dao;

import hsa.awp.common.dao.AbstractDao;
import hsa.awp.common.exception.ConfigurationException;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.common.naming.IAbstractDirectory;
import hsa.awp.user.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This abstract class is the api to look up users in the system.
 *
 * @author alex, klassm
 */
public class SingleUserDirectoryDao extends AbstractDao<SingleUser, Long> implements ISingleUserDao {
  /**
   * The directory we do the lookups.
   */
  private IAbstractDirectory directory;

  /**
   * Logger object.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private IStudyCourseDao studyCourseDao;

  private IMandatorDao mandatorDao;

  private IRoleMappingDao roleMappingDao;

  /**
   * Instantiates a user data access object.
   *
   * @throws ConfigurationException if there is a problem reading the properties file roles.default.properties.
   */
  public SingleUserDirectoryDao() {

    super(SingleUser.class);
  }

  @Override
  public long countAll() {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public List<SingleUser> findAll() {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public List<SingleUser> findAll(int start, int number) {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public SingleUser findById(Long id) {

    if (id == null) {
      throw new IllegalArgumentException("id must not be null");
    }

    SingleUser singleUser = getEntityManager().find(SingleUser.class, id);
    if (singleUser == null) {
      throw new NoMatchingElementException("user with " + id + " was not found");
    }

    logger.trace("found user instance of {}", singleUser.getClass().getSimpleName());

//    // update using LDAP Data
//    Properties userProps = directory.getUserProperties(singleUser.getUuid());
//    if (userProps == null) {
//      throw new NoMatchingElementException("user was not found in ldap");
//    }
//    initUser(singleUser, userProps);

    return singleUser;
  }

  @Override
  public SingleUser persist(SingleUser singleUser) {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public void remove(SingleUser user) {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public void removeAll() {

    throw new UnsupportedOperationException("not supported in LDAP Dao");
  }

  @Override
  public SingleUser findByUsername(String username) {
    /** check if username is valid */
    if (username == null) {
      throw new IllegalArgumentException("username must not be null");
    }

    /** check whether username exists in ldap */
    Properties userProps = directory.getUserProperties(username);
    if (userProps == null) {
      throw new NoMatchingElementException("SingleUser " + username + " does not exist");
    }

    SingleUser singleUser = createOrUpdateSingleUser(userProps);

    return singleUser;
  }

  private SingleUser createOrUpdateSingleUser(Properties userProps) {
    SingleUser singleUser = getSingleUserFromDB(userProps);
    initUser(singleUser, userProps);
    return singleUser;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<SingleUser> findUsersByRole(Role role) {

    Query query = getEntityManager().createQuery(
        "select mapping.singleUser from " + RoleMapping.class.getSimpleName() + " mapping  where mapping.role = :role");
    query.setParameter("role", role);

    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<SingleUser> getAllTeachers() {

    Query query = getEntityManager().createQuery(
        "select o from " + SingleUser.class.getSimpleName() + " o where size(o.lectures) > 0");
    return query.getResultList();
  }

  @Override
  public List<SingleUser> searchForUser(String searchString) {

    List<SingleUser> users = new ArrayList<SingleUser>();

    for (Properties properties : directory.searchForUser(searchString)) {
      users.add(createOrUpdateSingleUser(properties));
    }

    return users;
  }

  @Override
  public void readAllStudyCourses() {
    Set<String> studyCourses = directory.readAllStudyCourses();

    for (String studyCourse : studyCourses) {
      getOrCreateStudyCourseIfNotPersistent(studyCourse);
    }

  }

  private SingleUser getSingleUserFromDB(Properties userProps) {
    /** look for database entry for user */
    Long uuid = Long.valueOf(userProps.getProperty(IAbstractDirectory.UUID));
    SingleUser singleUser = findUUIDinDatabase(uuid);

    if (singleUser == null) {
      String role = userProps.getProperty(IAbstractDirectory.ROLE);

      // TODO Naming: hard coded value "Studenten"
      if ("Studenten".equalsIgnoreCase(role)) {
        singleUser = Student.getInstance();
      } else {
        singleUser = SingleUser.getInstance();
      }
      singleUser.setUuid(uuid);
      logger.debug("persisting user with uuid {}", uuid);

      String username = userProps.getProperty(IAbstractDirectory.LOGIN);
      renameOldUserIfUsernameIsDuplicated(username);

      super.persist(singleUser);
    }


    return singleUser;
  }

  private void renameOldUserIfUsernameIsDuplicated(String username) {
    SingleUser duplicateUsernameUser = findByUsernameInDatabase(username);
    if (duplicateUsernameUser != null) {
      Long uuid = duplicateUsernameUser.getUuid();

      if (userDoesNotExistInDirectory(uuid)) {
        String date = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(Calendar.getInstance().getTime());
        String renamedUsername = username + ".old-" + date;
        duplicateUsernameUser.setUsername(renamedUsername);
        logger.info("renamed user [{}] to [{}] because of reassigned username", username, renamedUsername);
        super.merge(duplicateUsernameUser);
      }
    }
  }

  private boolean userDoesNotExistInDirectory(Long uuid) {
    return ! userExistsInDirectory(uuid);
  }

  private boolean userExistsInDirectory(Long uuid) {
    try {
      directory.getUserProperties(uuid);
      return true;
    } catch (NoMatchingElementException e) {
      return false;
    }
  }

  public SingleUser findByUsernameInDatabase(String username) {
    try {
      Query query = getEntityManager().createQuery(
              "select o from " + SingleUser.class.getSimpleName() + " o where o.username = :username");
      query.setParameter("username", username);

      return (SingleUser) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Looks in the database for a {@link SingleUser} with a given uuid.
   *
   * @param uuid uuid to look for
   * @return found user or null.
   */
  public SingleUser findUUIDinDatabase(long uuid) {

    try {
      Query query = getEntityManager().createQuery(
          "select o from " + SingleUser.class.getSimpleName() + " o where o.uuid = :uuid");
      query.setParameter("uuid", uuid);

      return (SingleUser) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Sets the fields of the given {@link SingleUser} object corresponding to the given properties.
   *
   * @param singleUser - The user object to set up.
   * @param userProps  Properties to fill into the user object.
   */
  protected void initUser(SingleUser singleUser, Properties userProps) {

    if (singleUser == null) {
      throw new IllegalArgumentException("singleUser must not be null");
    } else if (userProps == null) {
      throw new IllegalArgumentException("user properties must not be null");
    }

    singleUser.setUsername(userProps.getProperty(IAbstractDirectory.LOGIN));
    singleUser.setName(userProps.getProperty(IAbstractDirectory.NAME));
    singleUser.setFaculty(userProps.getProperty(IAbstractDirectory.FACULTY));
    singleUser.setMail(userProps.getProperty(IAbstractDirectory.EMAIL));
    singleUser.setUuid(Long.valueOf(userProps.getProperty(IAbstractDirectory.UUID)));

    if (singleUser instanceof Student) {
      Student student = (Student) singleUser;

      String matriculationNumber = userProps.getProperty(IAbstractDirectory.MATRICULATIONNUMBER);
      if (matriculationNumber == null) {
        logger.warn("student login lacking matriculation number: " + student);
      }
      student.setMatriculationNumber(Integer.parseInt(matriculationNumber));

      String term = userProps.getProperty(IAbstractDirectory.TERM);
      // TODO Jojo: Workaround for term field = null
      if (term == null) {
        term = "0";
        logger.warn("student login lacking term: " + student);
      }
      student.setTerm(Integer.parseInt(term));


      String studyCourseName = userProps.getProperty(IAbstractDirectory.STUDYCOURSE);
      if (studyCourseName != null) {
        student.setStudyCourse(getOrCreateStudyCourseIfNotPersistent(studyCourseName));
      }
    }

    initRoles(singleUser);
  }

  public StudyCourse getOrCreateStudyCourseIfNotPersistent(String studyCourseName) {

    if (studyCourseName == null) {
      throw new IllegalArgumentException("no studyCourse name given");
    }
    StudyCourse studyCourse = studyCourseDao.findByName(studyCourseName);
    if (studyCourse == null) {
      studyCourse = StudyCourse.getInstance(studyCourseName);
      studyCourseDao.persist(studyCourse);
    }
    return studyCourse;
  }

  /**
   * Initializes the user roles REGISTERED and TEACHER. Other roles will be taken directly from database.
   *
   * @param singleUser user to initialize.
   */
  private void initRoles(SingleUser singleUser) {

    removeTeacherRole(singleUser);

    singleUser.addRoleMapping(getAllMandatorRoleMapping(Role.REGISTERED, singleUser));

    if (singleUser.isTeacher()) {
      singleUser.addRoleMapping(getAllMandatorRoleMapping(Role.TEACHER, singleUser));
    }
  }

  private RoleMapping getAllMandatorRoleMapping(Role role, SingleUser singleUser) {
    RoleMapping roleMapping = roleMappingDao.findByExample(singleUser, Mandator.allMandator, role);

    if (roleMapping == null) {

      Mandator mandator = mandatorDao.findByName(Mandator.allMandator);

      if (mandator == null) {
        mandator = mandatorDao.persist(Mandator.getInstance(Mandator.allMandator));
      }

      roleMapping = RoleMapping.getInstance(role, mandator, singleUser);
      roleMapping = roleMappingDao.persist(roleMapping);
    }
    return roleMapping;
  }

  private void removeTeacherRole(SingleUser singleUser) {
    if (singleUser.hasRole(Role.TEACHER)) {
      singleUser.removeRoleMapping(singleUser.roleMappingForRole(Role.TEACHER));
    }
  }

  private Mandator getAllMandator() {

    Mandator mandator = mandatorDao.findByName(Mandator.allMandator);

    if (mandator == null) {
      mandator = Mandator.getInstance(Mandator.allMandator);
      mandator = mandatorDao.persist(mandator);
    }
    return mandator;
  }

  /**
   * Sets the directory object.
   *
   * @param directory Directory object.
   */
  public void setDirectory(IAbstractDirectory directory) {

    logger.trace("Injected directory: {}", directory);
    this.directory = directory;
  }

  /**
   * @param studyCourseDao the studyCourseDao to set
   */
  public void setStudyCourseDao(IStudyCourseDao studyCourseDao) {

    this.studyCourseDao = studyCourseDao;
  }

  public void setMandatorDao(IMandatorDao mandatorDao) {
    this.mandatorDao = mandatorDao;
  }

  public void setRoleMappingDao(IRoleMappingDao roleMappingDao) {
    this.roleMappingDao = roleMappingDao;
  }
}
