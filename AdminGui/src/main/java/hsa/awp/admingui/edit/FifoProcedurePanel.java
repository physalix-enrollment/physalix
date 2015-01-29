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

package hsa.awp.admingui.edit;

import hsa.awp.admingui.controller.IAdminGuiController;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.FifoDisplayMode;
import hsa.awp.campaign.model.FifoProcedure;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Calendar;
import java.util.Date;

import static hsa.awp.campaign.model.FifoDisplayMode.FULL;
import static hsa.awp.campaign.model.FifoDisplayMode.SIGNAL;

/**
 * Class which allows to edit a FifoProdecure.
 *
 * @author Rico Lieback
 */
public class FifoProcedurePanel extends Panel {
  /**
   * generated UID.
   */
  private static final long serialVersionUID = 6759298091464910049L;

  /**
   * Controller which feeds the class with data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;

  /**
   * endDate represents a date picker for the {@link DrawProcedure}.
   */
  private DateTimeField endDate = new DateTimeField("endDate", new Model<Date>());

  /**
   * drawName represents the name of the {@link DrawProcedure}.
   */
  private TextField<String> fifoName = new TextField<String>("fifoName");

  /**
   * drawName represents the name of the {@link hsa.awp.campaign.model.DrawProcedure}.
   */
  private CheckBox displayMode = new CheckBox("showParticipantDetails", new Model<Boolean>());

  /**
   * startDate represents a date picker for the {@link DrawProcedure}.
   */
  private DateTimeField startDate = new DateTimeField("startDate", new Model<Date>());

  /**
   * the text which will be shown in the panel.
   */
  private Label panelLabel = new Label("panelLabel", new Model<String>());

  /**
   * gives feedback to the user.
   */
  private FeedbackPanel feedbackPanel = new FeedbackPanel("fifo.feedback");

  /**
   * Default constructor for the FifoProcedurePanel.
   *
   * @param id the markup id for the panel
   */
  public FifoProcedurePanel(String id) {

    this(id, null);

    Calendar currentData = Calendar.getInstance();
    Date currentTime = currentData.getTime();
    startDate.setModelObject(currentTime);
    endDate.setModelObject(currentTime);
    //TODO Sprache:
    add(panelLabel.setDefaultModel(new Model<String>("Fifo-Prozedur erstellen")));
  }

  /**
   * Constructor for {@link FifoProcedurePanel}. This is the setup for the panel and here are all components registered.
   *
   * @param id       wicket:id which connects markup with code.
   * @param fifoProc the existing FifoProcedure which have to be edit
   */
  public FifoProcedurePanel(String id, final FifoProcedure fifoProc) {

    super(id);

    final FifoProcedure fifoProcedure;

    if (fifoProc == null) {
      fifoProcedure = FifoProcedure.getInstance(controller.getActiveMandator(getSession()));
    } else {
      fifoProcedure = fifoProc;
    }

    CompoundPropertyModel<FifoProcedure> cpm = new CompoundPropertyModel<FifoProcedure>(fifoProcedure);
    final Form<Object> form = new Form<Object>("form");
    form.setDefaultModel(cpm);
    form.setOutputMarkupId(true);
    feedbackPanel.setOutputMarkupId(true);
    // add validators
    fifoName.setRequired(true);
    startDate.setRequired(true);
    endDate.setRequired(true);

    // get&add default values
    displayMode.setModelObject(fifoProcedure.getDisplayMode() != SIGNAL);
    fifoName.setDefaultModel(cpm.bind("name"));
    startDate.setModelObject(fifoProcedure.getStartDate().getTime());
    endDate.setModelObject(fifoProcedure.getEndDate().getTime());
    //TODO Sprache:
    add(panelLabel.setDefaultModel(new Model<String>("Fifo-Prozedur bearbeiten")));

    form.add(fifoName);

    form.add(startDate);

    form.add(endDate);

    form.add(displayMode);

    MarkupContainer add = form.add(new AjaxButton("submit") {
      private static final long serialVersionUID = -6537464906539587006L;

      @Override
      protected void onError(final AjaxRequestTarget target, final Form<?> form) {

        target.addComponent(feedbackPanel);
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

        FifoProcedure refreshedProcedure = fifoProcedure;
        if (fifoProcedure.getId() != 0L) {
          refreshedProcedure = controller.getFifoProcedureById(fifoProcedure.getId());
        }
        refreshedProcedure.setName(fifoProcedure.getName());

        Calendar sD = Calendar.getInstance();
        sD.setTime(startDate.getModelObject());

        Calendar eD = Calendar.getInstance();
        eD.setTime(endDate.getModelObject());

        Boolean displayFullDetails = displayMode.getModelObject();
        FifoDisplayMode fifoDisplayMode = displayFullDetails ? FULL : SIGNAL;

        try {
          refreshedProcedure.setInterval(sD, eD);
          refreshedProcedure.setDisplayMode(fifoDisplayMode);
          controller.writeFifoProcedure(refreshedProcedure);
//                    setResponsePage(new OnePanelPage(new ConfirmedEditPanel(OnePanelPage.getPanelIdOne())));
          //TODO Sprache:
          feedbackPanel.info("Eingaben \u00e4bernommen.");
          this.setVisible(false);

          target.addComponent(this);
          target.addComponent(feedbackPanel);
        } catch (IllegalArgumentException iae) {
          //TODO Sprache:
          feedbackPanel.fatal("Prozedur kann nicht bearbeitet werden.");
        }
      }
    });
    add(feedbackPanel);
    add(form);
  }
}
