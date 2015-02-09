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

package hsa.awp.rule.model;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.common.AbstractMandatorableDomainObject;
import hsa.awp.event.model.Event;
import hsa.awp.user.model.SingleUser;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "`ruleset`")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RuleSet extends AbstractMandatorableDomainObject<Long> implements IRule {
  /**
   * serial version UID used for serialization.
   */
  private static final long serialVersionUID = -847847314461876923L;

  /**
   * Auto-generated database identifier. This id represents the {@link Rule} in the database.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = 0L;

  /**
   * The {@link Set} of {@link Rule}s which are combined with this {@link RuleSet}.
   */
  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
  private Set<Rule> rules;


  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof RuleSet)) {
      return false;
    }
    RuleSet other = (RuleSet) obj;

    // if item is persisted use only the id for checking
    if (id != null && id != 0L) {
      return id.equals(other.id);
    }

    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    // if item is persisted use only the id for calculating hash code
    if (id != null && id != 0L) {
      return id.hashCode();
    }

    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public Long getId() {

    return id;
  }

  @Override
  public boolean check(SingleUser user, Campaign campaign, Event event) {

    for (Rule rule : rules) {
      if (!rule.check(user, campaign, event)) {
        return false;
      }
    }
    return true;
  }

  public void addRule(Rule rule) {

    rules.add(rule);
  }

  public Set<Rule> getRules() {

    return this.rules;
  }

  protected void setRules(Set<Rule> rules) {

    this.rules = rules;
  }

  public boolean removeRule(Rule rule) {

    return rules.remove(rule);
  }

  /**
   * Sets the id of this {@link Rule}. This method should only be used by tests and hibernate!
   *
   * @param id the new id
   */
  void setId(Long id) {

    this.id = id;
  }


}
