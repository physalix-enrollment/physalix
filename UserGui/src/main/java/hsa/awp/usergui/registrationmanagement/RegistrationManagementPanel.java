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

import hsa.awp.user.model.SingleUser;
import hsa.awp.usergui.controller.IUserGuiController;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;

public class RegistrationManagementPanel extends Panel {
  private static final String KEINE_ANMELDUNGEN = "Keine Buchungen verf\u00fcgbar./No enrollments available.";

  private static final String ANMELDUNGEN = "Ihre Buchungen/Your enrollments";

  /**
   * unique serialization id.
   */
  private static final long serialVersionUID = 3830771486180474427L;

  @SpringBean(name = "usergui.controller")
  private transient IUserGuiController controller;


  private MarkupContainer box;

  public RegistrationManagementPanel(String id) {

    super(id);

    final SingleUser singleUser = controller.getUserById(SecurityContextHolder.getContext().getAuthentication().getName());

    final Label message = new Label("RegistrationManagementPanel.message", KEINE_ANMELDUNGEN);
    ConfirmedRegistrationManagementPanel confirmedPanel = new ConfirmedRegistrationManagementPanel(
        "RegistrationManagementPanel.ConfirmedRegistrationsManagement", controller
        .findConfirmedRegistrationsByParticipantId(singleUser.getId()));

    box = new WebMarkupContainer("RegistrationManagementPanel.box");

    Label regTitel = new Label("RegistrationManagementPanel.regTitle", ANMELDUNGEN);

    boolean confirmedRegistrationsExisting = controller.findConfirmedRegistrationsByParticipantId(singleUser.getId()).size() > 0;

    confirmedPanel.setVisible(confirmedRegistrationsExisting);
    message.setVisible(!confirmedRegistrationsExisting);
    regTitel.setVisible(confirmedRegistrationsExisting);


    box.setOutputMarkupId(true);

    box.add(message);
    box.add(confirmedPanel);
    box.add(regTitel);

    add(box);
		
  }

  public void update(AjaxRequestTarget target) {

    target.addComponent(box);
  }
}
