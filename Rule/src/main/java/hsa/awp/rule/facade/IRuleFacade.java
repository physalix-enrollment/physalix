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

import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.Rule;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface IRuleFacade {
  RegistrationRuleSet addRuleToRegistrationRuleSet(Long campaign, Long event, Long mandator, Rule rule);

  void removeRuleFromRegistrationRuleSet(Long campaign, Long event, Rule rule);

  List<Rule> findAllRules();

  List<RegistrationRuleSet> findRegistrationRuleSetsByEventId(Long id);

  Rule findRuleById(Long id);

  Rule findRuleByName(String name);

  Set<Rule> findRulesByCampaignAndEvent(Long campaignId, Long eventId);

  void removeRule(Rule rule);

  void removeRulesConnection(Long campaignId, Long eventId);

  RegistrationRuleSet saveRegistrationRuleSet(RegistrationRuleSet set);

  <R extends Rule> R saveRule(R rule);

  <R extends Rule> R updateRule(R rule);

  List<Rule> findRuleByMandator(Long mandator);

  Rule findRuleByNameAndMandator(String value, Long mandatorId);

  List<RegistrationRuleSet> findByCampaign(Long campaign);
}
