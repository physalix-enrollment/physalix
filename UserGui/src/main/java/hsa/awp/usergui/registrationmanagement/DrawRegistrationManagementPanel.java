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

package hsa.awp.usergui.registrationmanagement;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.model.PriorityListItem;
import hsa.awp.event.model.Event;
import hsa.awp.usergui.controller.IUserGuiController;
import hsa.awp.usergui.prioritylistselectors.PriorityListSelector;
import hsa.awp.usergui.util.DropAndSortableBox;
import hsa.awp.usergui.util.JavascriptEventConfirmation;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author basti
 */
public class DrawRegistrationManagementPanel extends Panel {
  /**
   * unique serialization id.
   */
  private static final long serialVersionUID = -856328622581131607L;

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean(name = "usergui.controller")
  private transient IUserGuiController controller;

  private Campaign campaign;

  /**
   * List of all priolistBoxes.
   */
  private List<DropAndSortableBox> dropBoxList;

  private MarkupContainer box;

  public DrawRegistrationManagementPanel(String id, final IModel<List<PriorityList>> priolistModel) {

    super(id);

    for (PriorityList priorityList : priolistModel.getObject()) {
      if (campaign != null && !campaign.equals(priorityList.getProcedure().getCampaign())) {
        throw new IllegalArgumentException("priolists belong to different campaigns");
      }
      campaign = priorityList.getProcedure().getCampaign();
    }

    dropBoxList = new LinkedList<DropAndSortableBox>();

    box = new WebMarkupContainer("DrawRegistrationManagemantPanel.box");
    box.setOutputMarkupId(true);

    IModel<Integer> iterations = new LoadableDetachableModel<Integer>() {
      private static final long serialVersionUID = 1L;

      @Override
      protected Integer load() {

        int iterations = priolistModel.getObject().size();

        DrawRegistrationManagementPanel.this.setVisible(iterations > 0);

        return iterations;
      }
    };

    /*
    * render priolists dynamically dependent on the attribute in drawProcedure.
    */
    Loop prioListLoop = new Loop("DrawRegistrationManagemantPanel.listsList", iterations) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(final LoopItem item) {

        List<PriorityList> prioListList = priolistModel.getObject();
        DropAndSortableBox list = new DropAndSortableBox("DrawRegistrationManagemantPanel.prioList",
            getEventListFromPrioList(prioListList.get(item.getIteration())), prioListList
            .get(item.getIteration()).getProcedure().getMaximumPriorityListItems(), false);
        list.setOutputMarkupId(true);

        list.add(new AttributeAppender("class", new Model<String>("deactive"), " "));
        item
            .add(new Label("DrawRegistrationManagemantPanel.listName", "Wunschliste Kurs "
                + (item.getIteration() + 1)));
        dropBoxList.add(list);
        item.add(list);

        AjaxFallbackLink<String> deleteLink = new AjaxFallbackLink<String>("DrawRegistrationManagemantPanel.delete") {
          /**
           * unique serialization id.
           */
          private static final long serialVersionUID = -5932722911932385381L;

          @Override
          public void onClick(AjaxRequestTarget target) {

            priolistModel.detach();
            List<PriorityList> prioListList = priolistModel.getObject();
            controller.removePriolist(prioListList.get(item.getIteration()));

            PriorityListSelector prioListSelector = findParent(PriorityListSelector.class);
            if (prioListSelector != null) {
              prioListSelector.update(target, prioListList.get(item.getIteration()));
            }

            prioListList.remove(item.getIteration());
            target.addComponent(box);
          }
        };
        item.add(deleteLink);
        deleteLink.add(new JavascriptEventConfirmation("onclick", "Sind Sie sicher?"));
      }
    };

    box.add(new Label("DrawRegistrationManagemantPanel.titel", "Gespeicherte Wunschlisten"));
    box.add(prioListLoop);
    add(box);
  }

  private List<Event> getEventListFromPrioList(PriorityList list) {

    List<Event> eventList = new LinkedList<Event>();
    for (int priority = 0; priority < list.getItems().size(); priority++) {
      PriorityListItem item = list.getItem(priority + 1);
      if (item != null) {
        eventList.add(controller.getEventById(item.getEvent()));
      } else {
        logger.warn("Malformed priority list with id '{}'", list.getId());
        break;
      }
    }

    return eventList;
  }
}
