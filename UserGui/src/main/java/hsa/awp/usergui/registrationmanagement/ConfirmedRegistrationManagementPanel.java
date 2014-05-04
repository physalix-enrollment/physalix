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

import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.event.model.Event;
import hsa.awp.gui.util.LoadableDetachedModel;
import hsa.awp.user.model.SingleUser;
import hsa.awp.usergui.controller.IUserGuiController;
import hsa.awp.usergui.util.JavascriptEventConfirmation;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static hsa.awp.event.util.EventFormattingUtils.formatIdSubjectNameAndDetailInformation;

public class ConfirmedRegistrationManagementPanel extends Panel {
  /**
   * generated UID.
   */
  private static final long serialVersionUID = -5931875471824320053L;

  @SpringBean(name = "usergui.controller")
  private transient IUserGuiController controller;

  public ConfirmedRegistrationManagementPanel(String id, final List<ConfirmedRegistration> registrations) {

    super(id);

    final MarkupContainer box = new WebMarkupContainer("FifoManagementBox");
    box.setOutputMarkupId(true);

    final LoadableDetachedModel<List<ConfirmedRegistration>> regModel = new LoadableDetachedModel<List<ConfirmedRegistration>>() {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = -5771746782326674752L;

      @Override
      protected List<ConfirmedRegistration> load() {

        SingleUser singleUser = controller.getUserById(SecurityContextHolder.getContext().getAuthentication().getName());
        return controller.findConfirmedRegistrationsByParticipantId(singleUser.getId());
      }
    };

    box.add(new ListView<ConfirmedRegistration>("registrationList", regModel) {
      /**
       * generated uid.
       */
      private static final long serialVersionUID = 5261679476830400718L;

      @Override
      protected void populateItem(final ListItem<ConfirmedRegistration> item) {

        ConfirmedRegistration registration = item.getModelObject();
        Event event = controller.getEventById(registration.getEventId());

        item.add(new Label("regName", formatIdSubjectNameAndDetailInformation(event)));

        if (registration.getProcedure() == null) {
          item.add(new Label("procedureName", "direkt"));
        } else {
          item.add(new Label("procedureName", registration.getProcedure().getName()));
        }

        AjaxLink<String> deleteLink = new AjaxLink<String>("regCancelLink") {
          /**
           * generated UID.
           */
          private static final long serialVersionUID = 7720300709649429249L;

          @Override
          public void onClick(AjaxRequestTarget target) {

            controller.deleteConfirmedRegistration(item.getModelObject());

            RegistrationManagementPanel regPan = findParent(RegistrationManagementPanel.class);
            if (regPan != null) {
              regPan.update(target);
            }

            target.addComponent(box);
            regModel.detach();
          }
        };

        deleteLink.setVisible(controller.isCampaignOpen(item.getModelObject()));

        item.add(deleteLink);
        deleteLink.add(new JavascriptEventConfirmation("onclick", "Sind Sie sicher?/Are you sure?"));
      }
    });

    add(box);
  }
}
