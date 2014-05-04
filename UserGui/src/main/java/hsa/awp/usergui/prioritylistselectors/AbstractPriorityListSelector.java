package hsa.awp.usergui.prioritylistselectors;

import hsa.awp.campaign.model.PriorityList;
import hsa.awp.event.model.Event;
import hsa.awp.usergui.util.DragAndDropableBox;
import hsa.awp.usergui.util.DragableElement;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class AbstractPriorityListSelector extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8680568014838734271L;
	
	public AbstractPriorityListSelector(String id){
		super(id);
	}

	public abstract List<Event> filterEventListForSourcebox(List<Event> events);

	public abstract void moveElementsBackToSource();

	/**
	 * Add element to sourceBox.
	 * 
	 * @param element
	 *            element to be added.
	 */
	public abstract void addElementToSourceBox(DragableElement element);

	/**
	 * Updates the component after a priolist is deleted.
	 * 
	 * @param target
	 *            ajax target
	 * @param list
	 *            priolist which is deleted, needed to add the elements back to
	 *            source box.
	 */
	public abstract void update(AjaxRequestTarget target, PriorityList list);

	/**
	 * Ajax update for the whole component.
	 * 
	 * @param target
	 *            ajaxrequesttarget
	 */
	public abstract void updateLists(AjaxRequestTarget target);
	
	public abstract long getDropBoxElementId(DragableElement element);
	
	public abstract DragAndDropableBox getSourceBox();
	
}
