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

package hsa.awp.usergui.prioritylistselectors;

import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.event.model.Category;
import hsa.awp.event.model.Event;
import hsa.awp.gui.util.LoadableDetachedModel;
import hsa.awp.user.model.SingleUser;
import hsa.awp.usergui.OnePanelPage;
import hsa.awp.usergui.controller.IUserGuiController;
import hsa.awp.usergui.registrationmanagement.DrawRegistrationManagementPanel;
import hsa.awp.usergui.util.DragAndDropableBox;
import hsa.awp.usergui.util.DragableElement;
import hsa.awp.usergui.util.DragAndDrop.DropAndSortableBox;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Prioritylistselector which let the user choose what element he wants in which
 * order.
 * 
 * @author basti
 */
public class PriorityListSelector extends AbstractPriorityListSelector {
	/**
	 * generated UID.
	 */
	private static final long serialVersionUID = 4902775122982841093L;

	private static Label messageEmpty = new Label("prioListSelector.messageEmpty",
			"Die maximale Anzahl der erlaubten Wunschlisten ist erreicht/<br>Limit of allowed preference list reached");
	private static Label messageTitle = new Label("prioListSelector.messageTitle",
			"Neue Wunschlisten/New preference lists");
	private static Label messageSubtitle = new Label(
			"prioListSelector.messageSubtitle",
			"Listen sind nicht gespeichert/Lists are not saved!");

	private static Button submitButton;

	/**
	 * {@link IUserGuiController}.
	 */
	@SpringBean(name = "usergui.controller")
	private IUserGuiController controller;

	/**
	 * List of all priolistBoxes.
	 */
	private List<DropAndSortableBox> dropBoxList;

	/**
	 * Box which displays all available events.
	 */
	private DragAndDropableBox sourceBox;

	/**
	 * gives feedback to the user.
	 */
	private FeedbackPanel feedbackPanel = new FeedbackPanel("prio.feedback");

	private MarkupContainer box;

	private Form<String> form;

	private final IModel<DrawProcedure> drawProcedureModel;

	private final SingleUser singleUser;

	private IModel<List<Category>> categoryListModel;

	/**
	 * Constructor for PriorityListSelector.
	 * 
	 * @param id
	 *            Wicket id
	 * @param drawProc
	 *            DrawProcedure to be edited
	 */
	public PriorityListSelector(String id, final DrawProcedure drawProc) {

		super(id);
		messageEmpty.setEscapeModelStrings(false);
		singleUser = controller.getUserById(SecurityContextHolder.getContext()
				.getAuthentication().getName());

		box = new WebMarkupContainer("prioListSelector.box");
		box.setOutputMarkupId(true);
		form = new Form<String>("prioListSelector.form");
		this.setOutputMarkupId(true);
		form.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupId(true);

		drawProcedureModel = new LoadableDetachedModel<DrawProcedure>() {
			/**
       *
       */
			private static final long serialVersionUID = 1L;

			@Override
			protected DrawProcedure load() {

				return controller.getDrawProcedureById(drawProc.getId());
			}
		};

		final IModel<List<Event>> eventlistModel = new LoadableDetachableModel<List<Event>>() {
			/**
       *
       */
			private static final long serialVersionUID = 1509181846335682010L;

			@Override
			protected List<Event> load() {

				List<Event> eventList = controller
						.convertToEventList(new ArrayList<Long>(
								drawProcedureModel.getObject().getCampaign()
										.getEventIds()));

				Collections.sort(eventList, new Comparator<Event>() {
					@Override
					public int compare(Event o1, Event o2) {

						if (o1.getEventId() > o2.getEventId()) {
							return 1;
						} else if (o1.getEventId() < o2.getEventId()) {
							return -1;
						} else {
							return 0;
						}
					}
				});

				return filterEventListForSourcebox(eventList);
			}
		};

		categoryListModel = new LoadableDetachableModel<List<Category>>() {
			/**
       *
       */
			private static final long serialVersionUID = -6326470345409063111L;

			@Override
			protected List<Category> load() {

				Set<Category> categories = new TreeSet<Category>(
						new Comparator<Category>() {
							@Override
							public int compare(Category o1, Category o2) {

								return o1.getName().compareTo(o2.getName());
							}
						});

				eventlistModel.detach();
				for (Event e : eventlistModel.getObject()) {
					categories.add(e.getSubject().getCategory());
				}

				return new ArrayList<Category>(categories);
			}
		};

		IModel<Integer> categoryIterations = new LoadableDetachableModel<Integer>() {
			/**
		 * 
		 */
			private static final long serialVersionUID = -1513427284596362424L;

			@Override
			protected Integer load() {

				return categoryListModel.getObject().size();
			}
		};

		Loop categoryList = new Loop("prioListSelector.categories",
				categoryIterations) {
			/**
			 * generated uid.
			 */
			private static final long serialVersionUID = -6820774021151549325L;

			@Override
			protected void populateItem(LoopItem item) {

				final Category category = categoryListModel.getObject().get(
						item.getIteration());
				AjaxLink<Category> link = new AjaxLink<Category>(
						"prioListSelector.categoriesLink", new Model<Category>(
								category)) {
					/**
					 * generated uid.
					 */
					private static final long serialVersionUID = -3667864064733586820L;

					@Override
					public void onClick(AjaxRequestTarget target) {

						sourceBox.removeAllElements();
						sourceBox.setComponentId(category.getId());
						this.addEventsToSourcebox();
						target.addComponent(sourceBox);
					}

					private void addEventsToSourcebox() {

						eventlistModel.detach();
						for (Event e : eventlistModel.getObject()) {
							if (e.getSubject().getCategory()
									.equals(this.getModel().getObject())) {
								sourceBox
										.addElement(new DragableElement(
												DragAndDropableBox.DRAG_AND_DROPABLE_BOX_ITEM,
												e));
							}
						}
					}
				};
				link.add(new Label("prioListSelector.categoryName", category
						.getName()));
				item.add(link);
			}
		};

		categoryList.setOutputMarkupId(true);

		sourceBox = new DragAndDropableBox("prioListSelector.selectableObjects");
		form.add(sourceBox);

		dropBoxList = new ArrayList<DropAndSortableBox>(drawProcedureModel
				.getObject().getMaximumPriorityLists());

		submitButton = new Button("prioListSelector.submit") {
			/**
			 * generated UID.
			 */
			private static final long serialVersionUID = -1440808750941977688L;

			@Override
			public void onSubmit() {

				Set<List<Event>> lists = new HashSet<List<Event>>();

				for (DropAndSortableBox box : dropBoxList) {
					if (box.getEventList().size() > 0) {
						lists.add(box.getEventList());
					}
				}

				if (lists.size() != 0) {
					drawProcedureModel.detach();
					try {
						DrawProcedure drawProcedure = drawProcedureModel
								.getObject();
						controller.createPrioList(SecurityContextHolder
								.getContext().getAuthentication().getName(),
								SecurityContextHolder.getContext()
										.getAuthentication().getName(), lists,
								drawProcedure.getCampaign());
						setResponsePage(new OnePanelPage(
								new PriorityListSelector(
										OnePanelPage.getPanelIdOne(),
										drawProcedure)));
					} catch (IllegalArgumentException e) {
						moveElementsBackToSource();
						feedbackPanel
								.error("Bitte Eingaben \u00dcberpr\u00fcfen.");
					} catch (IllegalStateException e) {
						feedbackPanel
								.error("Leider zu spät, die Verlosung hat schon stattgefunden.");
					}
				} else {
					feedbackPanel
							.error("Wunschlisten d\u00dcrfen nicht leer sein!");
				}
			}
		};

		IModel<Integer> prioListIterations = new LoadableDetachableModel<Integer>() {
			/**
       *
       */
			private static final long serialVersionUID = -2446977182797089682L;

			@Override
			protected Integer load() {

				DrawProcedure drawProcedure = drawProcedureModel.getObject();
				int i = drawProcedure.getMaximumPriorityLists()
						- controller.findPriorityListsByUserAndProcedure(
								singleUser.getId(), drawProcedure).size();

				PriorityListSelector.submitButton.setVisible(i > 0);
				PriorityListSelector.messageEmpty.setVisible(!(i > 0));
				PriorityListSelector.messageTitle.setVisible((i > 0));
				PriorityListSelector.messageSubtitle.setVisible((i > 0));

				return i;
			}
		};

		/*
		 * render priolists dynamically dependent on the attribute in
		 * drawProcedure.
		 */
		final Loop priolists = new Loop("prioListSelector.listsList",
				prioListIterations) {
			/**
       *
       */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(LoopItem item) {

				DrawProcedure drawProcedure = drawProcedureModel.getObject();
				DropAndSortableBox list = new DropAndSortableBox(
						"prioListSelector.prioList",
						drawProcedure.getMaximumPriorityListItems());
				list.setOutputMarkupId(true);
//				item.add(new Label("prioListSelector.listName",
//						"Wunschliste Kurs "
//								+ (item.getIteration() + 1 + controller
//										.findPriorityListsByUserAndProcedure(
//												singleUser.getId(),
//												drawProcedure).size())));
				dropBoxList.add(list);
				item.add(list);
			}
		};

		priolists.setOutputMarkupId(true);

		form.add(categoryList);
		form.add(messageEmpty);
		form.add(messageTitle);
		form.add(messageSubtitle);
		form.add(priolists);
		form.add(submitButton);

		/*
		 * Priolist management
		 */

		IModel<List<PriorityList>> prioListsModel = new LoadableDetachableModel<List<PriorityList>>() {
			/**
			 * generated UID.
			 */
			private static final long serialVersionUID = 8833064897441919997L;

			@Override
			protected List<PriorityList> load() {

				List<PriorityList> list = controller
						.findPriorityListsByUserAndProcedure(
								singleUser.getId(),
								drawProcedureModel.getObject());

				return list;
			}
		};

		DrawRegistrationManagementPanel drawRegistrationManagementPanel = new DrawRegistrationManagementPanel(
				"prioListSelector.managementPanel", prioListsModel);

		form.add(drawRegistrationManagementPanel);

		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		box.add(new Label("prioListSelector.heading", drawProcedureModel
				.getObject().getCampaign().getName()
				+ ": Phase: "
				+ drawProcedureModel.getObject().getName()
				+ " vom "
				+ df.format(drawProcedureModel.getObject().getStartDate()
						.getTime())
				+ " bis "
				+ (drawProcedureModel.getObject() instanceof DrawProcedure ? df
						.format(drawProcedureModel.getObject().getDrawDate()
								.getTime()) : df.format((drawProcedureModel
						.getObject().getEndDate().getTime())))));

		box.add(feedbackPanel);
		box.add(form);
		add(box);
	}

	public long getDropBoxElementId(DragableElement element) {
		long eventId = element.getEvent().getId();
		return controller.findCategoryIdByEventId(eventId);
	};

	public List<Event> filterEventListForSourcebox(List<Event> events) {

		List<Event> eventBlackList = new LinkedList<Event>();
		/*
		 * check if event is in an uncommited priolist
		 */
		if (dropBoxList != null && dropBoxList.size() > 0) {
			for (DropAndSortableBox box : dropBoxList) {
				eventBlackList.addAll(box.getEventList());
			}
		}
		return controller.filterEventList(events, singleUser,
				drawProcedureModel.getObject(), eventBlackList);
	}

	public void moveElementsBackToSource() {

		for (DropAndSortableBox dropBox : dropBoxList) {
			for (Event event : dropBox.getEventList()) {
				sourceBox.addElement(new DragableElement(
						DragAndDropableBox.DRAG_AND_DROPABLE_BOX_ITEM, event));
			}
		}
	}

	/**
	 * Add element to sourceBox.
	 * 
	 * @param element
	 *            element to be added.
	 */
	public void addElementToSourceBox(DragableElement element) {

		sourceBox.addElement(element);
	}

	/**
	 * Updates the component after a priolist is deleted.
	 * 
	 * @param target
	 *            ajax target
	 * @param list
	 *            priolist which is deleted, needed to add the elements back to
	 *            source box.
	 */
	public void update(AjaxRequestTarget target, PriorityList list) {

		moveElementsBackToSource();
		sourceBox.removeAllElements();

		categoryListModel.detach();

		target.addComponent(box);
	}

	/**
	 * Ajax update for the whole component.
	 * 
	 * @param target
	 *            ajaxrequesttarget
	 */
	public void updateLists(AjaxRequestTarget target) {

		target.addComponent(sourceBox);
	}

	@Override
	public DragAndDropableBox getSourceBox() {
		return sourceBox;
	}
}
