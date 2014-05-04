package hsa.awp.usergui.util.DragAndDrop;

import hsa.awp.event.model.Event;
import hsa.awp.usergui.prioritylistselectors.AbstractPriorityListSelector;
import hsa.awp.usergui.util.DragAndDropableBox;
import hsa.awp.usergui.util.DragableElement;
import hsa.awp.usergui.util.DraggablePrioListTarget;
import hsa.awp.usergui.util.warningPanel.WarningPanel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class AbstractDropAndSortableBox extends Panel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DragableElement[] elements;
	
	private boolean isActive = true;
	private ModalWindow confirmDialog = new ModalWindow("confirmDialog");
	private String color = "background-color: #ffffff";
	private Label title;
	private IModel<String> titleModel;

	/**
	 * Targets representing each slot of the priolist.
	 */
	private List<DraggablePrioListTarget> draggablePrioListTargets;

	/**
	 * ListView which displays all targets.
	 */
	private ListView<DraggablePrioListTarget> itemContainers;

	/**
	 * MarkupBox which contains all elements, in order to support ajax updating.
	 */
	private MarkupContainer markupBox;
	

	public AbstractDropAndSortableBox(String id, int maxItems) {

		this(id, null, maxItems, true);
		initAndAddDialog();
	}

	/**
	 * Constructor which generates a priolist with the given elements.
	 * 
	 * @param id
	 *            wicket id
	 * @param events
	 *            eventlist with events to be displayed. list.size() should not
	 *            be grater than maxItems.
	 * @param maxItems
	 *            number of items the priolist should hold
	 * @param isActive
	 *            true if draggabiliy is given
	 */
	public AbstractDropAndSortableBox(String id, List<Event> events, int maxItems,
			boolean isActive) {
		super(id);
		this.isActive = isActive;
		initAndAddDialog();
		markupBox = new WebMarkupContainer("DropAndSortableBox.box");
		markupBox.setOutputMarkupId(true);
		titleModel = new IModel<String>() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private String title = "Wunschliste";

			@Override
			public void detach() {
				//title = null;
			}

			@Override
			public String getObject() {
				return title;
			}

			@Override
			public void setObject(String object) {
				title = object;
			}
		};
		title = new Label("DropAndSortableBox.title", titleModel);
		title.setOutputMarkupId(true);
		elements = new DragableElement[maxItems];

		if (events != null) {
			if (events.size() > maxItems) {
				throw new IllegalArgumentException(
						"list bigger than allowed items count");
			}
			for (Event event : events) {
				elements[events.indexOf(event)] = new DragableElement(
						"DropAndSortableBox.element", event, isActive);
			}
		}

		draggablePrioListTargets = new ArrayList<DraggablePrioListTarget>(
				maxItems);

		for (int i = 0; i < maxItems; i++) {
			draggablePrioListTargets.add(new DraggablePrioListTarget(
					"DropAndSortableBox.droptarget", AbstractDropAndSortableBox.this,
					i, elements[i], isActive));
		}

		itemContainers = new ListView<DraggablePrioListTarget>(
				"DropAndSortableBox.list", draggablePrioListTargets) {
			/**
       *
       */
			private static final long serialVersionUID = -3426894828457312814L;

			@Override
			protected void populateItem(ListItem<DraggablePrioListTarget> item) {

				draggablePrioListTargets.get(item.getIndex())
						.setOutputMarkupId(true);
				item.add(draggablePrioListTargets.get(item.getIndex()));
			}
		};

		markupBox.add(itemContainers);
		add(markupBox);
		add(title);
	}
	
	public List<Event> getEventList() {

		List<Event> list = new LinkedList<Event>();

		for (DragableElement e : elements) {
			if (e != null) {
				list.add(e.getEvent());
			}
		}

		return list;
	}
	
	
	public void itemDropped(DragableElement element,
			DraggablePrioListTarget prioList, AjaxRequestTarget target) {
		if(isActive() && !listContainsElement(element) && isAddingAllowed(element, target))
			addItemToElementList(element, prioList, target);
		else {
			if(!isActive())
				changeDialogContentAndShow(target, "Das Ziehen von Veranstaltungen in bereits gespeicherte Listen ist nicht erlaubt!"
						+ "<br><br> Drawing of courses into saved lists is not allowed!", false);
			doElseBranch(element, target);
		}		
	}
	
	public abstract boolean isAddingAllowed(DragableElement element, AjaxRequestTarget target);
	
	public void doElseBranch(DragableElement element, AjaxRequestTarget target){
		AbstractPriorityListSelector pls = findParent(AbstractPriorityListSelector.class);
		if(element.findParent(AbstractDropAndSortableBox.class) == null)
			pls.addElementToSourceBox(element);
		pls.updateLists(target);
	};
	
	private void addItemToElementList(DragableElement element,
			DraggablePrioListTarget prioList, AjaxRequestTarget target) {

			int index = draggablePrioListTargets.indexOf(prioList);
			if (elements[index] == null) { /* dropped on empty slot */
				if (index == 0) {
					elements[index] = element; 
					DragAndDropableBox box = element.findParent(DragAndDropableBox.class);
					if(box == null){
						AbstractPriorityListSelector pls = findParent(AbstractPriorityListSelector.class);
						if(pls != null){
							pls.getSourceBox().add(new SimpleAttributeModifier("style", color));
							pls.updateLists(target);
						}
					}
				} else if (elements[index - 1] != null) { /*
														 * dropped in empty slot
														 * && previous slot is
														 * occupied. add element
														 */
					elements[index] = element;
				} else { /*
						 * dropped in empty slot && previous slot is empty. move
						 * element up.
						 */
					for (int i = 0; i < elements.length; i++) {
						if (elements[i] == null) {
							elements[i] = element;
							break;
						}
					}
				}
			} else { /* dropped in occupied slot */
				for (int i = elements.length - 1; i > index; i--) { /*
																	 * move
																	 * items
																	 * down
																	 * (last
																	 * item is
																	 * lost)
																	 * until the
																	 * index
																	 * where the
																	 * element
																	 * was
																	 * dropped
																	 */
					if (i == elements.length - 1 && elements[i] != null) { /*
																			 * last
																			 * element
																			 * have
																			 * to
																			 * be
																			 * moved
																			 * back
																			 * to
																			 * item
																			 * list
																			 */
						AbstractPriorityListSelector pls = findParent(AbstractPriorityListSelector.class);
						pls.addElementToSourceBox(elements[i]);
						pls.updateLists(target);
					}
					elements[i] = elements[i - 1];
				}
				elements[index] = element; /*
											 * set element to position where it
											 * was dropped
											 */
			}
			updateAll(target);
	}
	
	/**
	 * updates all components in this component with target.
	 * 
	 * @param target
	 *            {@link AjaxRequestTarget} for the ajax update
	 */
	public void updateAll(AjaxRequestTarget target) {

		for (DraggablePrioListTarget list : draggablePrioListTargets) {
			updateCell(list, target);
			list.update(target);
			target.addComponent(list);
		}
		target.addComponent(title);
		target.addComponent(markupBox);
	}

	/**
	 * updates only one priolist item.
	 * 
	 * @param list
	 *            item to be updated
	 * @param target
	 *            {@link AjaxRequestTarget} for the ajax update
	 */
	private void updateCell(DraggablePrioListTarget list,
			AjaxRequestTarget target) {

		int index = draggablePrioListTargets.indexOf(list);

		if (elements.length > index) {
			if (list.getElement() != elements[index]) {
				list.setElement(elements[index]);
			}
		}
	}

	/**
	 * Debug method for displaying all elements in this container.
	 */
	@SuppressWarnings("unused")
	private void printElements() {

		System.out.print("[ ");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] != null) {
				System.out.print(elements[i].getEvent().getSubject().getName()
						+ " | ");
			} else {
				System.out.print("null | ");
			}
		}

		System.out.println(" ]");
	}
	
	private void initAndAddDialog(){
		confirmDialog.setTitle("Hinweis");
		confirmDialog.setContent(new WarningPanel(confirmDialog.getContentId(), ""));
		confirmDialog.setInitialHeight(75);
		confirmDialog.setInitialWidth(350);
		add(confirmDialog);
	}
	
	public void changeDialogContentAndShow(AjaxRequestTarget target, String message, boolean escapeModelString) {
		confirmDialog.setContent(new WarningPanel(confirmDialog.getContentId(), message, escapeModelString));
		confirmDialog.show(target);
	}


	/**
	 * handles process when an element is moved out of this container.
	 * 
	 * @param element
	 *            element which is removed
	 * @param target
	 *            {@link AjaxRequestTarget} for the ajax update
	 */
	public abstract boolean removeItem(DragableElement element, AjaxRequestTarget target); 
	
	public DragableElement[] getElements(){
		return elements;
	}
	
	private boolean isActive(){
		return isActive;
	}
	
	public abstract boolean listContainsElement(DragableElement element);

	public void setColor(String color){
		this.color = "background-color: #" + color;
		add(new SimpleAttributeModifier("style", this.color));
	}
	
	public String getColor(){
		return color;
	}
	
	public void setTitle(String title){
		titleModel.setObject("Wunschliste " + title);
	}
	
}
