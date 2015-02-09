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

package hsa.awp.admingui.edit.event.tabs;

import hsa.awp.admingui.OnePanelPage;
import hsa.awp.admingui.controller.IAdminGuiController;
import hsa.awp.admingui.rule.RulePanel;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Exam;
import hsa.awp.gui.util.LoadableDetachedModel;
import hsa.awp.rule.model.RegistrationRuleSet;
import hsa.awp.rule.model.Rule;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Panel consisting of methods and gui elements to apply a rule to an event.
 *
 * @author klassm
 */
public class TabRule extends Panel {
  /**
   * unique serialization id.
   */
  private static final long serialVersionUID = 5005580086544106598L;

  /**
   * Selector for choosing a campaign.
   */
  private DropDownChoice<Campaign> campaignDropDownChoice;

  /**
   * GuiController which feeds the Gui with Data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;

  /**
   * Event to edit.
   */
  private Event event;

  /**
   * Panel for performing feedback.
   */
  private FeedbackPanel feedbackPanel;

  /**
   * Selector for choosing a rule.
   */
  private DropDownChoice<Rule> ruleDropDownChoice;

  /**
   * Constructor.
   *
   * @param id    wicket-id.
   * @param event event to edit.
   */
  public TabRule(String id, final Event event) {

    super(id);

    if (event == null) {
      throw new IllegalArgumentException("no event given");
    }
    this.event = event;

    feedbackPanel = new FeedbackPanel("feedbackPanel");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    add(new Link<RulePanel>("event.rule.ruleLink") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = -4330022747603538362L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new RulePanel(OnePanelPage.getPanelIdOne())));
      }
    });

    final WebMarkupContainer viewContainer = new WebMarkupContainer("event.rule.viewContainer");
    add(viewContainer);
    viewContainer.setOutputMarkupId(true);

    final LoadableDetachedModel<List<MappingCampaignRule>> appliedRuleSets = new LoadableDetachedModel<List<MappingCampaignRule>>() {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = -2124579745330512796L;

      @Override
      protected List<MappingCampaignRule> load() {

        return getCampaignRuleSetMappingUsingRegistrationRuleSet();
      }
    };

    ListView<MappingCampaignRule> campaigns = new ListView<MappingCampaignRule>("event.rule.viewContainer.campaigns",
        appliedRuleSets) {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = -8838977200090913199L;

      @Override
      protected void populateItem(ListItem<MappingCampaignRule> item) {

        Campaign campaign = item.getModelObject().campaign;
        final Long campaignId = campaign.getId();
        item.add(new Label("event.rule.viewContainer.campaigns.campaignName", campaign.getName()));

        item.add(new ListView<Rule>("event.rule.viewContainer.campaigns.rules", item.getModelObject().rules) {
          /**
           * unique serialization id.
           */
          private static final long serialVersionUID = 3744568568875419040L;

          @Override
          protected void populateItem(final ListItem<Rule> item) {

            final String ruleName = item.getModelObject().getName();
            item.add(new Label("event.rule.viewContainer.campaigns.rules.rulename", ruleName));

            item.add(new AjaxFallbackLink<Exam>("event.rule.viewContainer.campaigns.rules.delete") {
              private static final long serialVersionUID = 1L;

              @Override
              public void onClick(AjaxRequestTarget target) {
                target.addComponent(feedbackPanel);
                controller.deleteRuleFromRegistrationRuleSet(campaignId, event.getId(), item.getModelObject());

                item.modelChanging();

                // Remove item and invalidate listView
                getList().remove(item.getModelObject());

                modelChanged();
                removeAll();

                target.addComponent(viewContainer);
                info(String.format("Regelverknüpfung für [%s] wurde gelöscht", ruleName)); // TODO Sprache
              }
            });
          }
        });
      }
    };
    campaigns.setOutputMarkupId(true);
    viewContainer.add(campaigns);

    Form<Object> form = new Form<Object>("event.rule.selectForm");
    add(form);

    LoadableDetachedModel<List<Rule>> ruleChoiceModel = new LoadableDetachedModel<List<Rule>>() {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = 8248346584785484372L;

      @Override
      protected List<Rule> load() {

        return getAllAvailableCampaignRules();
      }
    };

    campaignDropDownChoice = new DropDownChoice<Campaign>("event.rule.campaignSelect", new Model<Campaign>(),
        getAllCampaigns(), new ChoiceRenderer<Campaign>("name"));
    form.add(campaignDropDownChoice);
    campaignDropDownChoice.add(new OnChangeAjaxBehavior() {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = 3121098439171790920L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {

        target.addComponent(ruleDropDownChoice);
      }
    });
    campaignDropDownChoice.setRequired(true);

    ruleDropDownChoice = new DropDownChoice<Rule>("event.rule.ruleSelect", new Model<Rule>(), ruleChoiceModel,
        new ChoiceRenderer<Rule>("name"));
    form.add(ruleDropDownChoice);
    ruleDropDownChoice.setRequired(true);
    ruleDropDownChoice.setOutputMarkupId(true);

    form.add(new AjaxButton("event.rule.submit") {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = 3023000043184393346L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

        target.addComponent(viewContainer);
        target.addComponent(feedbackPanel);
        controller.addRule(campaignDropDownChoice.getModelObject(), event, ruleDropDownChoice.getModelObject(), getSession());
        info("Regel wurde zugeordnet.");
      }
    });
  }

  private List<MappingCampaignRule> getCampaignRuleSetMappingUsingRegistrationRuleSet() {

    List<RegistrationRuleSet> ruleSets = controller.getRegistrationRuleSetsByEvent(event);
    List<MappingCampaignRule> mapping = new LinkedList<MappingCampaignRule>();

    for (RegistrationRuleSet ruleSet : ruleSets) {
      Campaign campaign = controller.getCampaignById(ruleSet.getCampaign());
      List<Rule> rules = new LinkedList<Rule>(ruleSet.getRules());
      Collections.sort(rules, new Comparator<Rule>() {
        @Override
        public int compare(Rule o1, Rule o2) {

          return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
      });
      mapping.add(new MappingCampaignRule(campaign, rules));
    }
    Collections.sort(mapping);

    return mapping;
  }

  /**
   * Looks for all rules that can be applied to a {@link Campaign}. The {@link Campaign} is taken directly out of the
   * campaignSelector.
   *
   * @return List of available rules.
   */
  private List<Rule> getAllAvailableCampaignRules() {

    Campaign campaign = campaignDropDownChoice.getModelObject();
    if (campaign == null) {
      return new LinkedList<Rule>();
    }

    List<Rule> rules = controller.getRulesByMandator(getSession());
    List<RegistrationRuleSet> ruleSets = controller.getRegistrationRuleSetsByEvent(event);

    for (RegistrationRuleSet ruleSet : ruleSets) {
      if (!campaign.getId().equals(ruleSet.getCampaign())) {
        continue;
      }

      for (Rule rule : ruleSet.getRules()) {
        rules.remove(rule);
      }
    }

    Collections.sort(rules, new Comparator<Rule>() {
      @Override
      public int compare(Rule arg0, Rule arg1) {

        return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
      }
    });
    return rules;
  }

  /**
   * Looks for all Campaigns applied to an Event. List will be sorted by name.
   *
   * @return sorted list of applied {@link Campaign}s.
   */
  private List<Campaign> getAllCampaigns() {

    List<Campaign> campaigns = controller.getCampaignsByEvent(event);
    Collections.sort(campaigns, new Comparator<Campaign>() {
      @Override
      public int compare(Campaign arg0, Campaign arg1) {

        return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
      }
    });

    return campaigns;
  }

  /**
   * Assignment of a Campaign to its rules. Won't work with a Map because wicket's ListView does only like Lists.
   *
   * @author klassm
   */
  private class MappingCampaignRule implements Comparable<MappingCampaignRule> {
    /**
     * Campaign as key.
     */
    private Campaign campaign;

    /**
     * Applied rules.
     */
    private List<Rule> rules;

    /**
     * Constructor.
     *
     * @param campaign campaign as key.
     * @param rules    applied rules.
     */
    public MappingCampaignRule(Campaign campaign, List<Rule> rules) {

      this.campaign = campaign;
      this.rules = rules;
    }

    @Override
    public int compareTo(MappingCampaignRule o) {

      return campaign.getName().compareTo(o.campaign.getName());
    }
  }
}
