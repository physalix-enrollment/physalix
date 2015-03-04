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

package hsa.awp.scire.procedureLogic;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.event.model.Event;
import hsa.awp.scire.exception.MaximumAllowedRegistrationsExceededException;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.User;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logic class being used to realize FIFO strategy when registering with events.
 *
 * @author klassm
 */
public class FifoProcedureLogic extends AbstractProcedureLogic<FifoProcedure> implements IFifoProcedureLogic,
    IProcedureLogic<FifoProcedure> {
  public FifoProcedureLogic() {

    super(FifoProcedure.class);
    logger = LoggerFactory.getLogger(this.getClass());
  }

  @Override
  @Transactional
  public synchronized void register(Event event, User participant, SingleUser initiator, boolean examOnly) {

    Campaign campaign = getProcedure().getCampaign();
    int maximumAllowedRegistrations = campaign.getMaximumConfirmedRegistrationsOrDefault();

    if (maximumAllowedRegistrations > 0)
    {
      long registrationCount = campaignFacade.countConfirmedRegistrationsByParticipantIdAndCampaignId(participant.getId(), campaign.getId());
      if (registrationCount > maximumAllowedRegistrations)
      {
        throw new MaximumAllowedRegistrationsExceededException();
      }
    }

    ConfirmedRegistration confirmedRegistration = singleRegistration(event, participant, initiator, examOnly);
    sendMail(confirmedRegistration);
  }

  @Override
  public void beforeActive() {

  }

  @Override
  public void afterActive() {

  }

  @Override
  public void whileActive() {

  }

  @Override
  public FifoProcedure getProcedure() {

    logger.debug("get procedure");
    return procedure;
  }
}
