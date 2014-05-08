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

package hsa.awp.common.dao;

import hsa.awp.common.IGenericDomainModel;
import hsa.awp.common.exception.*;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.id.IdentifierGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;

import static java.lang.String.format;

/**
 * Abstract class for Accessing domain objects in the database.
 *
 * @param <T> Type of domain object that is accessed.
 * @param <K> Type of id that is used (i.e. Integer or Long).
 * @author klassm
 */
public abstract class AbstractDao<T extends IGenericDomainModel<K>, K> implements IAbstractDao<T, K> {
  /**
   * Logger used for logging.
   */
  private Logger logger = LoggerFactory.getLogger(AbstractDao.class);

  /**
   * {@link EntityManager} to use for accessing the database.
   */
  private EntityManager entityManager;

  /**
   * Type of the class that is accessed.
   */
  private Class<T> structure;

  /**
   * Creates an {@link AbstractDao}.
   *
   * @param structure Type of the class that is accessed.
   */
  public AbstractDao(Class<T> structure) {

    if (structure == null) {
      logger.info("Cannot create AbstractDao : no structure class given");
      throw new IllegalArgumentException("no structure class given.");
    }
    logger.trace("Dao for '{}' created", structure.getSimpleName());
    this.structure = structure;
  }

  @Override
  public long countAll() {

    logger.trace("call of countAll()");
    return (Long) getEntityManager().createQuery("select count(o) from " + structure.getSimpleName() + " o").getSingleResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> findAll() {

    logger.trace("call of findAll()");
    List<T> list = getEntityManager().createQuery("select o from " + structure.getSimpleName() + " o").getResultList();
    return list;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> findAll(int firstResult, int maxResults) {

    logger.trace("call of findAll(firstResult, maxResults");
    return getEntityManager().createQuery("select o from " + structure.getSimpleName() + " o").setFirstResult(firstResult)
        .setMaxResults(maxResults).getResultList();
  }

  @Override
  public T findById(K id) {

    logger.trace("call of findById()");
    if (id == null) {
      logger.debug("cannot find id: no id given");
      throw new IllegalArgumentException("no id given");
    }

    try {
      T element = getEntityManager().find(structure, id);
      if (element == null) {
        String message = format("could not find [%s] with id [%s]", structure.getSimpleName(), id);
        logger.warn(message);
        throw new NoMatchingElementException(message);
      }
      return element;
    } catch (IllegalArgumentException e) {
      logger.warn("invalid id");
      throw new IllegalArgumentException("invalid id");
    } catch (IllegalStateException e) {
      logger.warn("invalid id");
      throw new IllegalArgumentException("invalid id");
    }
  }

  @Override
  public T merge(T item) {

    if (item == null) {
      logger.warn("cannot merge item: no item given");
      throw new IllegalArgumentException("no item given");
    }

    try {
      findById(item.getId());
      return getEntityManager().merge(item);
    } catch (NoMatchingElementException e) {
      logger.warn("cannot merge item: element not yet saved");
      throw new ItemNotSavedException("Item with id [" + item.getId() + "] is not saved.", e);
    }
  }

  @Override
  public T persist(T item) {

    if (item == null) {
      logger.warn("cannot persist item: no item given");
      throw new IllegalArgumentException("no item given");
    }

    if (getEntityManager().find(structure, ((IGenericDomainModel<K>) item).getId()) != null) {
      logger.warn("cannot persist item: item already exists");
      throw new ItemAlreadyExistsException();
    }

    try {
      getEntityManager().persist(item);
      flush();
      return item;
    } catch (IllegalArgumentException e) {
      throw new DataAccessException(item.getClass() + " is not an entity.", e);
    } catch (PersistenceException e) {
      if (e.getCause() instanceof PropertyValueException) {
        throw new PropertyViolatedException(e);
      } else if (e.getCause() instanceof ConstraintViolationException) {
        throw new PropertyViolatedException(e);
      } else if (e.getCause() instanceof IdentifierGenerationException) {
        throw new PropertyViolatedException("Probably id (primary key, cause) not set. Id was: " + item.getId(), e);
      } else {
        throw new DataAccessException("error executing database access.", e);
      }
    }
  }

  @Override
  public void remove(T item) {

    if (item == null) {
      throw new IllegalArgumentException("no item given");
    }
    logger.trace("removing item " + item.toString());
    try {
      item = merge(item);
      getEntityManager().remove(item);
    } catch (IllegalArgumentException e) {
      throw new NoMatchingElementException("no matching element", e);
    }
  }

  @Override
  public void removeAll() {

    logger.trace("removeAll items");
    for (T element : findAll()) {
      remove(element);
    }
  }

  /**
   * Writes all temporary changes to the database.
   */
  protected void flush() {

    logger.trace("flushing");
    getEntityManager().flush();
  }

  /**
   * Getter for the {@link EntityManager}.
   *
   * @return {@link EntityManager}.
   */
  public EntityManager getEntityManager() {

    if (entityManager == null) {
      logger.warn("cannot get entityManager (no entityManager set)");
      throw new IllegalArgumentException("no entity manager set.");
    }
    logger.trace("getting entityManager");
    return entityManager;
  }

  @Override
  @PersistenceContext
  public void setEntityManager(EntityManager entityManager) {

    logger.trace("setting entityManager " + entityManager);
    this.entityManager = entityManager;
  }
}
