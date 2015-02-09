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

package hsa.awp.rule.facade;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.rule.ICampaignRuleChecker;
import hsa.awp.event.model.Event;
import hsa.awp.rule.dao.IRegistrationRuleSetDao;
import hsa.awp.rule.dao.IRuleDao;
import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.Rule;
import hsa.awp.user.model.SingleUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RuleFacade implements IRuleFacade, ICampaignRuleChecker {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private IRegistrationRuleSetDao registrationRuleSetDao;

  private IRuleDao ruleDao;

  @Override
  @Transactional
  public boolean isRegistrationAllowed(SingleUser user, Campaign campaign, Event event) {

    if (user == null) {
      throw new IllegalArgumentException("user must not be null");
    } else if (campaign == null) {
      throw new IllegalArgumentException("campaign must not be null");
    } else if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }

    // first get the RuleSet for the given parameters
    RegistrationRuleSet set = registrationRuleSetDao.findByCampaignAndEvent(campaign.getId(), event.getId());

    if (set == null) {
      logger.trace("no rule set found for campaign '{}' and event '{}'", campaign.getName(), event.getEventId());
      return true;
    } else {
      return set.check(user, campaign, event);
    }
  }

  @Override
  @Transactional
  public RegistrationRuleSet addRuleToRegistrationRuleSet(Long campaign, Long event, Long mandator, Rule rule) {

    RegistrationRuleSet set = registrationRuleSetDao.findByCampaignAndEvent(campaign, event);

    // if not found, we have to create one.
    if (set == null) {
      set = RegistrationRuleSet.getInstance(mandator);
      set.setCampaign(campaign);
      set.setEvent(event);
      set = registrationRuleSetDao.persist(set);
    }

    set.addRule(rule);
    set = registrationRuleSetDao.merge(set);

    return set;
  }

  @Override
  @Transactional
  public void removeRuleFromRegistrationRuleSet(Long campaign, Long event, Rule rule) {
    RegistrationRuleSet ruleSet = registrationRuleSetDao.findByCampaignAndEvent(campaign, event);

    // if not found ignore this request.
    if (ruleSet == null)
      return;

    ruleSet.removeRule(rule);
    if (ruleSet.getRules().isEmpty()) {
      registrationRuleSetDao.remove(ruleSet);
    } else {
      registrationRuleSetDao.merge(ruleSet);
    }
  }

  @Override
  @Transactional
  public List<Rule> findAllRules() {

    return ruleDao.findAll();
  }

  @Override
  @Transactional
  public List<RegistrationRuleSet> findRegistrationRuleSetsByEventId(Long id) {

    return registrationRuleSetDao.findByEventId(id);
  }

  @Override
  @Transactional
  public Rule findRuleById(Long id) {

    return ruleDao.findById(id);
  }

  @Override
  @Transactional
  public Rule findRuleByName(String name) {

    return ruleDao.findByName(name);
  }

  @Transactional
  @Override
  public Rule findRuleByNameAndMandator(String value, Long mandatorId) {
    return ruleDao.findByNameAndMandatorId(value, mandatorId);
  }

  @Override
  public Set<Rule> findRulesByCampaignAndEvent(Long campaignId, Long eventId) {

    RegistrationRuleSet ruleSet = registrationRuleSetDao.findByCampaignAndEvent(campaignId, eventId);
    if (ruleSet == null) {
      return null;
    }
    return ruleSet.getRules();
  }

  @Override
  @Transactional
  public void removeRule(Rule rule) {

    List<RegistrationRuleSet> sets = registrationRuleSetDao.findAll();
    for (RegistrationRuleSet set : sets) {
      if (set.removeRule(rule)) {
        registrationRuleSetDao.merge(set);
      }
    }
    ruleDao.remove(rule);
  }

  @Override
  @Transactional
  public void removeRulesConnection(Long campaignId, Long eventId) {

    RegistrationRuleSet ruleSet = registrationRuleSetDao.findByCampaignAndEvent(campaignId, eventId);
    if (ruleSet == null) {
      return;
    }

    for (Rule rule : new LinkedList<Rule>(ruleSet.getRules())) {
      ruleSet.removeRule(rule);
    }
    registrationRuleSetDao.remove(ruleSet);
  }

  @Override
  @Transactional
  public RegistrationRuleSet saveRegistrationRuleSet(RegistrationRuleSet set) {

    this.registrationRuleSetDao.persist(set);
    return set;
  }

  @Override
  @Transactional
  public <R extends Rule> R saveRule(R rule) {

    ruleDao.persist(rule);
    return rule;
  }

  @Override
  @Transactional
  @SuppressWarnings("unchecked")
  public <R extends Rule> R updateRule(R rule) {

    return (R) ruleDao.merge(rule);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RegistrationRuleSet> findByCampaign(Long campaign) {
    return registrationRuleSetDao.findByCampaign(campaign);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Rule> findRuleByMandator(Long mandator) {
    return ruleDao.findByMandator(mandator);
  }

  public void setRegistrationRuleSetDao(IRegistrationRuleSetDao registrationRuleSetDao) {

    this.registrationRuleSetDao = registrationRuleSetDao;
  }

  public void setRuleDao(IRuleDao ruleDao) {

    this.ruleDao = ruleDao;
  }
}
