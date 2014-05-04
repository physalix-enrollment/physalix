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
import hsa.awp.admingui.edit.event.EventPanel;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.Subject;
import hsa.awp.event.model.Term;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.MinimumValidator;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Tab for tabbedpanel with the event informations.
 *
 * @author Basti
 * @author Matze
 */
public class TabGeneral extends Panel {

  /**
   * unique serialization id.
   */
  private static final long serialVersionUID = -2317854893451974153L;

  /**
   * GuiController which feeds the Gui with Data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;

  /**
   * Textfield for eventID.
   */
  private TextField<Integer> eventIdField;

  /**
   * Textfield for the subject field in {@link Event}.
   */
  private TextField<String> subjectField;

  private TextField<String> detailInfoField;

  /**
   * DropDown for term
   */
  private DropDownChoice<Term> termChoice;

  /**
   * textfield for maxParticipants field in {@link Event}.
   */
  private TextField<Integer> maxParticipantsField;

  /**
   * feedbackpanel for user information.
   */
  private FeedbackPanel feedbackPanel;

  /**
   * event which is edited or created.
   */
  private Event event;

  /**
   * Constructor for tab.
   *
   * @param id         wicket id
   * @param evt        event which is edited oder created.
   * @param eventPanel panel to update the details of the EventPanel.
   */
  public TabGeneral(String id, Event evt, final EventPanel eventPanel) {

    super(id);

    this.event = evt;

    Form<Event> form = new Form<Event>("event.tabGeneral.form");
    add(form);

    feedbackPanel = new FeedbackPanel("event.tabGeneral.feedbackPanel");
    feedbackPanel.setOutputMarkupId(true);
    form.add(feedbackPanel);

    eventIdField = new TextField<Integer>("event.tabGeneral.eventId", new Model<Integer>(event.getEventId()), Integer.class);
    form.add(eventIdField);
    eventIdField.add(new IValidator<Integer>() {

      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = 5605293764293745302L;

      @Override
      public void validate(IValidatable<Integer> validatable) {

        Integer val = validatable.getValue();

        if (val < 1) {
          validatable.error(new IValidationError() {

            @Override
            public String getErrorMessage(IErrorMessageSource messageSource) {

              return "Veranstaltungsnummer muss größer 0 sein.";
            }
          });
        } else if (val > 9999) {

          validatable.error(new IValidationError() {

            @Override
            public String getErrorMessage(IErrorMessageSource messageSource) {

              return "Veranstaltungsnummer muss kleiner 10000 sein.";
            }
          });
        }

      }

    });

    eventIdField.add(new MinimumValidator<Integer>(1));


    maxParticipantsField = new TextField<Integer>("event.tabGeneral.maxParticipants", new Model<Integer>(event
        .getMaxParticipants()), Integer.class);
    form.add(maxParticipantsField);
    maxParticipantsField.add(new MinimumValidator<Integer>(1));

    termChoice = new DropDownChoice<Term>("event.tabGeneral.term", new Model<Term>(event.getTerm()), controller.getTermsByMandator(getSession()), new ChoiceRenderer<Term>());
    form.add(termChoice);

    detailInfoField = new TextField<String>("event.tabGeneral.informationDetail", new Model<String>(event.getDetailInformation()));
    detailInfoField.setRequired(true);
    form.add(detailInfoField);

    String subjectName = "";
    if (event.getSubject() != null) {
      subjectName = event.getSubject().getName();
    }

    subjectField = new TextField<String>("event.tabGeneral.subject", new Model<String>(subjectName));
    form.add(subjectField);
    subjectField.add(new AutoCompleteBehavior<String>(new AbstractAutoCompleteRenderer<String>() {

      /**
       * generated UID.
       */
      private static final long serialVersionUID = 5609814253082290571L;

      @Override
      protected String getTextValue(String sub) {

        return sub;
      }

      @Override
      protected void renderChoice(String sub, Response response, String criteria) {

        response.write(getTextValue(sub));
      }
    }) {

      /**
       * generated serialization id.
       */
      private static final long serialVersionUID = 1220805798637698821L;

      @Override
      protected Iterator<String> getChoices(String input) {

        input = input.toLowerCase();
        List<String> names = new LinkedList<String>();
        for (Subject subject : controller.getSubjectsByMandator(getSession())) {
          if (subject.getName().toLowerCase().startsWith(input)) {
            names.add(subject.getName());
          }
        }
        Collections.sort(names);

        return names.iterator();
      }
    });

    form.add(new AjaxButton("event.tabGeneral.submit") {

      /**
       * generated serialization id.
       */
      private static final long serialVersionUID = -6537464906539587006L;

      @Override
      protected void onError(final AjaxRequestTarget target, final Form<?> form) {

        target.addComponent(feedbackPanel);
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

        int eventId = eventIdField.getModelObject();
        Subject subject = controller.getSubjectByNameAndMandator(subjectField.getModelObject(), getSession());

        // TODO Sprache:
        // TODO basti refactoring
        if (subject == null) {
          feedbackPanel.error("Kein gültiges Fach angegeben");
        } else {
          boolean isNew = event.getId() == 0L;

          event.setEventId(eventId);
          event.setMaxParticipants(new Integer(maxParticipantsField.getModelObject()));
          event.setSubject(subject);
          event.setTerm(termChoice.getModelObject());
          event.setDetailInformation(detailInfoField.getModelObject() == null ? "" : detailInfoField.getModelObject());

          event = controller.writeEvent(event);

          if (isNew) {
            setResponsePage(new OnePanelPage(new EventPanel(OnePanelPage.getPanelIdOne(), event)));
          } else {
            feedbackPanel.info("Eingaben übernommen");
          }
          eventPanel.updateEventDetails(target);
        }

        target.addComponent(form);
        target.addComponent(feedbackPanel);
      }

    });
  }

}
