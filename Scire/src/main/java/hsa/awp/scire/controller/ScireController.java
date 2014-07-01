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

package hsa.awp.scire.controller;

import hsa.awp.campaign.facade.CampaignFacade;
import hsa.awp.campaign.facade.ICampaignFacade;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.common.exception.DataAccessException;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.common.util.GenericUtil;
import hsa.awp.common.util.ITimerTaskFactory;
import hsa.awp.scire.procedureLogic.DrawProcedureLogic;
import hsa.awp.scire.procedureLogic.FifoProcedureLogic;
import hsa.awp.scire.procedureLogic.IProcedureLogic;
import hsa.awp.scire.procedureLogic.IProcedureLogicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Controller class managing currently active {@link Procedure}s, looking for new and finalizing expired ones.
 *
 * @author klassm
 * @author johannes
 */
public class ScireController implements IScireController {
  /**
   * {@link CampaignFacade}.
   */
  private ICampaignFacade campaignFacade;

  /**
   * time interval the {@link ScireController} checks for newly active {@link Procedure}s.
   */
  private int checkInterval = 500;

  /**
   * Calendar object symbolizing the time when the Controller has last checked the state of its {@link Procedure}s.
   */
  private Calendar lastCheck;

  /**
   * Default logger.
   */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * {@link IProcedureLogicFactory}.
   */
  private IProcedureLogicFactory procedureLogicFactory;

  /**
   * ProcedureLogic types.
   */
  private Set<Class<? extends IProcedureLogic<?>>> procedureTypes = new HashSet<Class<? extends IProcedureLogic<?>>>();

  /**
   * Currently active {@link Procedure}s.
   */
  private Set<IProcedureLogic<? extends Procedure>> runningProcedures;

  /**
   * Timer object calling the check method in a periodical time period.
   */
  private Timer timer;

  /**
   * The TimerTaskFactory which wraps a {@link TimerTask} into an open {@link EntityManager}.
   */
  private ITimerTaskFactory timerTaskFactory;

  /**
   * Boolean variable being true if the timer checking for updated states of the {@link Procedure}s is running.
   */
  private boolean timerIsRunning = false;

  /**
   * Default constructor.
   */
  public ScireController() {

    logger.info("creating Scire controller");

    this.runningProcedures = new HashSet<IProcedureLogic<? extends Procedure>>();

    procedureTypes.add(FifoProcedureLogic.class);
    procedureTypes.add(DrawProcedureLogic.class);
  }

  @Override
  public synchronized Collection<Campaign> findActiveCampaigns() {

    if (!isCheckingForProcedureStates()) {
      throw new IllegalStateException("ScireController is not checking for new Procedures");
    }

    Collection<Campaign> active = campaignFacade.findActiveCampaigns();

    if (active.size() != runningProcedures.size()) {
      check();
    }

    return active;
  }

  @Override
  public synchronized IProcedureLogic<? extends Procedure> findActiveLogicByCampaign(Campaign campaign) {

    if (campaign == null) {
      throw new IllegalArgumentException("no campaign given");
    }

    logger.trace("searching for campaign {}", campaign.getName());

    for (IProcedureLogic<? extends Procedure> logic : runningProcedures) {
      Procedure proc = logic.getProcedure();
      logger.trace("campaign of {} logic is {}", proc.getName(), proc.getCampaign().getName());
      if (proc.getCampaign().equals(campaign)) {
        return logic;
      }
    }
    throw new NoMatchingElementException("No matching campaign found");
  }

  @Override
  public synchronized IProcedureLogic<? extends Procedure> findActiveLogicByProcedure(Long id) {

    if (id == null) {
      throw new IllegalArgumentException("no id given");
    }

    for (IProcedureLogic<? extends Procedure> logic : runningProcedures) {
      if (logic.getProcedure().getId().equals(id)) {
        return logic;
      }
    }
    return null;
  }

  @Override
  public synchronized IProcedureLogic<? extends Procedure> findActiveLogicByProcedure(Procedure proc) {

    if (proc == null) {
      throw new IllegalArgumentException("no Procedure given");
    }
    return findActiveLogicByProcedure(proc.getId());
  }

  @Override
  public Set<IProcedureLogic<? extends Procedure>> getRunningProcedures() {
    checkForDeletedCampaignsOrProcedures();

    return new HashSet<IProcedureLogic<? extends Procedure>>(runningProcedures);
  }

  @Override
  public int getTimerInterval() {

    return checkInterval;
  }

  @Override
  public boolean isCheckingForProcedureStates() {

    return timerIsRunning;
  }

  @Override
  public void setProcedureLogicTypeList(Set<Class<? extends IProcedureLogic<?>>> types) {

    if (types == null) {
      throw new IllegalArgumentException("no set given");
    }
    this.procedureTypes = types;
  }

  @Override
  public synchronized void setTimerInterval(int interval) {

    if (interval < 0) {
      throw new IllegalArgumentException("argument must be at least 1 minute (60.000 millisecods)");
    }

    logger.debug("setting refresh interval to {}. Old value was {}", interval, this.checkInterval);
    this.checkInterval = interval;

    if (timerIsRunning) {
      stopTimer();
      startTimer();
    }
  }

  /**
   * Starts the timer if it is currently running.
   */
  @Override
  public synchronized void startTimer() {

    if (timerIsRunning) {
      throw new IllegalStateException("timer is already running.");
    }
    logger.debug("starting timer");

    lastCheck = Calendar.getInstance();
    lastCheck.add(Calendar.YEAR, -30);

    timer = new Timer();
    timer.scheduleAtFixedRate(timerTaskFactory.getTask(new Runnable() {
      @Override
      public void run() {
        // TODO remove that one!!!
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "password"));
        try {
          check();
        } catch (Throwable e) {
          logger.error(e.toString(), e);
        }
      }
    }), 0, checkInterval);
    timerIsRunning = true;
  }

  @Override
  public synchronized void stopTimer() {

    if (!timerIsRunning) {
      throw new IllegalStateException("cannot stop timer - it is not running at the moment");
    }
    logger.debug("stopping timer");

    timer.cancel();
    timerIsRunning = false;
  }

  /**
   * Adds a new procedure logic type.
   *
   * @param type procedure logic type.
   */
  public synchronized void addProcedureLogicType(Class<? extends IProcedureLogic<?>> type) {

    procedureTypes.add(type);
  }

  /**
   * Checks the {@link Procedure}s whether their states have changed.
   */
  private synchronized void check() {

    logger.debug("starting check for procedure states");

    logger.debug("looking for deleted campaigns/procedures");

    checkForDeletedCampaignsOrProcedures();


    Set<IProcedureLogic<? extends Procedure>> procedures = new HashSet<IProcedureLogic<? extends Procedure>>(runningProcedures);
    Calendar now = Calendar.getInstance();


    logger.debug("looking for no longer active procedures");
    for (IProcedureLogic<? extends Procedure> logic : procedures) {
      Procedure proc = logic.getProcedure();

      logger.trace("checking {}", proc.getName());

      // is over?
      if (proc.getEndDate().compareTo(now) <= 0) {
        logger.debug("finish {}", logic);
        logic.afterActive();
        runningProcedures.remove(logic);
      } else {
        // notify running
        logic.whileActive();
      }
    }

    logger.debug("looking for newly active procedures");

    List<Campaign> activeCampaigns = campaignFacade.findActiveCampaigns();
    logger.debug("active campaigns: {}", activeCampaigns);

    for (Campaign campaign : activeCampaigns) {
      Procedure activeProcedure = campaign.findCurrentProcedure();
      if (activeProcedure != null && !isProcedureKnown(activeProcedure)) {
        logger.debug("found new Procedure: {}", activeProcedure.getName());

        for (Class<? extends IProcedureLogic<? extends Procedure>> logicClass : procedureTypes) {
          Class<? extends Procedure> typeClass = GenericUtil
              .searchForTypeArgument(logicClass, activeProcedure.getClass());
          if (typeClass != null) {
            IProcedureLogic<?> logic = procedureLogicFactory.getInstance(logicClass);
            logic.setProcedure(activeProcedure);

            logic.beforeActive();

            runningProcedures.add(logic);

            logger.debug("added procedure as active: {} : {}", activeProcedure.getName(), logic.getClass());
          }
        }
      }
    }
  }

  private void checkForDeletedCampaignsOrProcedures() {

    Set<IProcedureLogic<? extends Procedure>> procedureLogicsForDeletion = new HashSet<IProcedureLogic<? extends Procedure>>();

    for (IProcedureLogic<? extends Procedure> procedureLogic : runningProcedures) {
      Procedure procedure = procedureLogic.getProcedure();

      if (procedure instanceof DrawProcedure) {
        try {
          procedure = campaignFacade.getDrawProcedureById(procedure.getId());
        } catch (DataAccessException ex) {
          procedureLogicsForDeletion.add(procedureLogic);
        }
      } else if (procedure instanceof FifoProcedure) {
        try {
          procedure = campaignFacade.getFifoProcedureById(procedure.getId());
        } catch (DataAccessException ex) {
          procedureLogicsForDeletion.add(procedureLogic);
        }
      }

      try {
        Campaign campaign = procedure.getCampaign();
        if (campaign != null) {
          campaignFacade.getCampaignById(campaign.getId());
        } else {
          procedureLogicsForDeletion.add(procedureLogic);
        }
      } catch (DataAccessException ex) {
        procedureLogicsForDeletion.add(procedureLogic);
      }
    }

    for (IProcedureLogic<? extends Procedure> procedureLogic : procedureLogicsForDeletion) {
      logger.debug("removing procedure '" + procedureLogic.getProcedure().getName() + "' from running procedures");
      runningProcedures.remove(procedureLogic);
    }

  }

  /**
   * This method will return true if the given {@link Procedure} is already known as running {@link Procedure}.
   *
   * @param procedure {@link Procedure} to check.
   * @return true if the {@link Procedure} is given.
   */
  private boolean isProcedureKnown(Procedure procedure) {

    if (procedure == null) {
      throw new IllegalArgumentException("no Procedure given");
    }

    for (IProcedureLogic<? extends Procedure> logic : runningProcedures) {
      if (logic.getProcedure().equals(procedure)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Setter for campaignFacade.
   *
   * @param campaignFacade the campaignFacade to set
   */
  public void setCampaignFacade(ICampaignFacade campaignFacade) {

    if (campaignFacade == null) {
      throw new IllegalArgumentException("null given, ICampaignFacade expected");
    }

    this.campaignFacade = campaignFacade;
  }

  /**
   * Setter for procedureLogicFactory.
   *
   * @param procedureLogicFactory the procedureLogicFactory to set
   */
  public void setProcedureLogicFactory(IProcedureLogicFactory procedureLogicFactory) {

    this.procedureLogicFactory = procedureLogicFactory;
  }

  public void setTimerTaskFactory(ITimerTaskFactory factory) {

    timerTaskFactory = factory;
  }
}
