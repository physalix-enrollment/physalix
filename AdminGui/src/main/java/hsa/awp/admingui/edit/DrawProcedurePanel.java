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
import hsa.awp.campaign.model.PriorityList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class which allows to edit a DrawProcedures.
 *
 * @author Rico Lieback
 */
public class DrawProcedurePanel extends Panel {
  /**
   * generated UID.
   */
  private static final long serialVersionUID = 6759298091464910049L;

  /**
   * Controller which feeds the class with data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;
  
  private static final List<String> TYPES = Arrays.asList(
      "Keinerlei Beschränkung bei Erstellung der Wunschlisten",
      "Nur Veranstaltungen aus einem Fach pro Wunschliste möglich"
  );
  
  private String selected = TYPES.get(0);

  /**
   * drawDate represents a date picker for the {@link DrawProcedure}.
   */
  private DateTimeField drawDate = new DateTimeField("drawDate",
      new Model<Date>());

  /**
   * drawName represents the name of the {@link DrawProcedure}.
   */
  private TextField<String> drawName = new TextField<String>("drawName");

  /**
   * Maximum number of {@link PriorityList}s.
   */
  private TextField<String> maximumPriorityLists = new TextField<String>(
      "maximumPriorityLists", new Model<String>());

  /**
   * Maximum number of {@link PriorityList}s.
   */
  private TextField<String> maximumPriorityListItems = new TextField<String>(
      "maximumPriorityListItems", new Model<String>());

  /**
   * endDate represents a date picker for the {@link DrawProcedure}.
   */
  private DateTimeField endDate = new DateTimeField("endDate",
      new Model<Date>());

  /**
   * startDate represents a date picker for the {@link DrawProcedure}.
   */
  private DateTimeField startDate = new DateTimeField("startDate",
      new Model<Date>());

  /**
   * the text which will be shown in the panel.
   */
  private Label panelLabel = new Label("panelLabel", new Model<String>());

  /**
   * gives feedback to the user.
   */
  private FeedbackPanel feedbackPanel = new FeedbackPanel("drawpanel.feedback");

  /**
   * Default constructor for the DrawProcedurePanel.
   *
   * @param id the markup id for the Panel
   */
  
  private RadioChoice<String> ruleBasedTypes = new RadioChoice<String>("ruleBasedChoice", 
		  new PropertyModel<String>(this, "selected"),
		  TYPES);
  
  public DrawProcedurePanel(String id) {

    this(id, null);

    Calendar currentData = Calendar.getInstance();
    Date currentTime = currentData.getTime();
    startDate.setModelObject(currentTime);
    endDate.setModelObject(currentTime);
    drawDate.setModelObject(currentTime);
    maximumPriorityListItems.setDefaultModel(new Model<Integer>(0));
    maximumPriorityLists.setDefaultModel(new Model<Integer>(0));
    // TODO Sprache:
    add(panelLabel.setDefaultModel(new Model<String>(
        "Los-Prozedur erstellen")));
  }

  /**
   * Constructor for {@link DrawProcedurePanel}. This is the setup for the
   * panel and here are all components registered.
   *
   * @param id       wicket:id which connects markup with code.
   * @param drawProc the draw procedure which have to be edit
   */
  public DrawProcedurePanel(String id, final DrawProcedure drawProc) {

    super(id);

    final DrawProcedure drawProcedure;

    if (drawProc == null) {
      drawProcedure = DrawProcedure.getInstance(controller.getActiveMandator(getSession()));
    } else {
      drawProcedure = drawProc;
    }

    CompoundPropertyModel<DrawProcedure> cpm = new CompoundPropertyModel<DrawProcedure>(drawProcedure);
    final Form<Object> form = new Form<Object>("form");
    form.setDefaultModel(cpm);
    feedbackPanel.setOutputMarkupId(true);
    form.setOutputMarkupId(true);
    // TODO Sprache:
    add(panelLabel.setDefaultModel(new Model<String>("Los-Prozedur bearbeiten")));

    // add validators
    drawName.setRequired(true);
    startDate.setRequired(true);
    endDate.setRequired(true);
    drawDate.setRequired(true);
    maximumPriorityListItems.setRequired(true);
    maximumPriorityLists.setRequired(true);

    // get&add default values
    drawName.setDefaultModel(cpm.bind("name"));
    startDate.setModelObject(drawProcedure.getStartDate().getTime());
    endDate.setModelObject(drawProcedure.getEndDate().getTime());
    drawDate.setModelObject(drawProcedure.getDrawDate().getTime());
    maximumPriorityListItems.setModelObject(Integer.toString(drawProcedure
        .getMaximumPriorityListItems()));
    maximumPriorityLists.setModelObject(Integer.toString(drawProcedure
        .getMaximumPriorityLists()));

    if(drawProcedure.getRuleBased() == 0)
    	selected = TYPES.get(0);
    else if(drawProcedure.getRuleBased() == 1)
    	selected = TYPES.get(1);

    form.add(drawName);

    form.add(startDate);

    form.add(endDate);

    form.add(drawDate);

    form.add(maximumPriorityListItems);

    form.add(maximumPriorityLists);

    form.add(ruleBasedTypes);

    form.add(new AjaxButton("submit") {
      private static final long serialVersionUID = -6537464906539587006L;

      @Override
      protected void onError(final AjaxRequestTarget target, final Form<?> form) {

        target.addComponent(feedbackPanel);
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

        target.addComponent(form);
        target.addComponent(feedbackPanel);

        Calendar sD = Calendar.getInstance();
        sD.setTime(startDate.getModelObject());

        Calendar eD = Calendar.getInstance();
        eD.setTime(endDate.getModelObject());

        Calendar dD = Calendar.getInstance();
        dD.setTime(drawDate.getModelObject());

        try {
          DrawProcedure draw = controller.getDrawProcedureById(drawProcedure.getId());
          if (draw == null) {
            draw = drawProcedure;
          }
          
          if(selected == TYPES.get(0))
        	  draw.setRuleBased(0);
          else if(selected == TYPES.get(1))
        	  draw.setRuleBased(1);
          
          draw.setInterval(sD, eD);
          draw.setDrawDate(dD);
          draw.setMaximumPriorityListItems(Integer.valueOf(maximumPriorityListItems.getModelObject()));
          draw.setMaximumPriorityLists(Integer.valueOf(maximumPriorityLists.getModelObject()));

          // controller writeDrawProcedure to DB

          controller.writeDrawProcedure(draw);
          // setResponsePage(new OnePanelPage(new
          // ConfirmedEditPanel(OnePanelPage.getPanelIdOne())));
          // TODO Sprache:
          feedbackPanel.info("Eingaben übernommen.");
          this.setVisible(false);
        } catch (NumberFormatException nf) {
          // TODO Sprache:
          feedbackPanel.error("Zahl erwartet.");
        } catch (IllegalArgumentException iae) {
          // TODO Sprache:
          feedbackPanel.fatal("Prozedur kann nicht bearbeitet werden.");
        }
      }
    });
    add(feedbackPanel);
    add(form);
  }
}
