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

package hsa.awp.usergui.util;

import hsa.awp.usergui.prioritylistselectors.AbstractPriorityListSelector;
import hsa.awp.usergui.util.DragAndDrop.AbstractDropAndSortableBox;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.scriptaculous.dragdrop.DraggableTarget;

/**
 * Priolistitem which can recieve items.
 * 
 * @author basti
 */
public class DraggablePrioListTarget extends Panel {
	/**
	 * generated UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * reference to partent box.
	 */
	private AbstractDropAndSortableBox box;

	/**
	 * draggableTarget which can recieve {@link DragableElement}.
	 */
	private DraggableTarget draggableTarget;

	/**
	 * element which is hold by this container.
	 */
	private DragableElement element;

	/**
	 * index of this slot in the priolist.
	 */
	private int index;

	private boolean isActive;

	/**
	 * Constructor which creates a new instance.
	 * 
	 * @param id
	 *            wicket id
	 * @param box
	 *            reference to partent box.
	 * @param index
	 *            index of this slot in the priolist.
	 */
	public DraggablePrioListTarget(String id, AbstractDropAndSortableBox box, int index) {

		this(id, box, index, null);
	}

	/**
	 * Constructor which creates a new instance.
	 * 
	 * @param id
	 *            wicket id
	 * @param box
	 *            reference to partent box.
	 * @param index
	 *            index of this slot in the priolist.
	 * @param element
	 *            element to display.
	 */
	public DraggablePrioListTarget(String id, AbstractDropAndSortableBox box,
			int index, DragableElement element) {

		this(id, box, index, element, true);
	}

	/**
	 * Constructor which creates a new instance.
	 * 
	 * @param id
	 *            wicket id
	 * @param box
	 *            reference to partent box.
	 * @param index
	 *            index of this slot in the priolist.
	 * @param element
	 *            element to display.
	 * @param isActive
	 *            true if draggabiliy is given
	 */
	public DraggablePrioListTarget(String id, AbstractDropAndSortableBox box,
			int index, DragableElement element, boolean isActive) {

		super(id);
		this.box = box;
		this.index = index;
		this.element = element;
		this.isActive = isActive;

		draggableTarget = new DropTarget("DraggablePrioListTarget.target");
		if (element != null) {
			draggableTarget.add(new DragableElement(
					"DraggablePrioListTarget.element", element.getEvent(),
					isActive));
		} else {

			draggableTarget.add(addDummyLabel(index));
		}

		draggableTarget.setOutputMarkupId(true);

		add(draggableTarget);
	}

	/**
	 * Getter for element.
	 * 
	 * @return the element
	 */
	public DragableElement getElement() {

		return element;
	}

	/**
	 * Setter for element.
	 * 
	 * @param element
	 *            the element to set
	 */
	public void setElement(DragableElement element) {

		this.element = element;
	}

	/**
	 * updates this component with {@link AjaxRequestTarget}.
	 * 
	 * @param target
	 *            target which will be used for update.
	 */
	public void update(AjaxRequestTarget target) {

		DraggableTarget contentBox = new DropTarget(
				"DraggablePrioListTarget.target");

		if (element != null) {
			contentBox.add(new DragableElement(
					"DraggablePrioListTarget.element", element.getEvent(),
					isActive));
		} else {
			contentBox.add(addDummyLabel(index));
		}

		draggableTarget.replaceWith(contentBox);
		draggableTarget = (DraggableTarget) contentBox;

		target.addComponent(draggableTarget);
	}

	/**
	 * Implementation of {@link DraggableTarget}.
	 * 
	 * @author basti
	 */
	private class DropTarget extends DraggableTarget {
		/**
		 * generated UID.
		 */
		private static final long serialVersionUID = 3224838902620487634L;

		/**
		 * default wicket constructor.
		 * 
		 * @param id
		 *            wicket id
		 */
		public DropTarget(String id) {

			super(id);
		}

		@Override
		protected void onDrop(Component component, AjaxRequestTarget target) {

			DragableElement element = null;
			try {
				element = (DragableElement) component
						.findParent(DragableElement.class);

				DragAndDropableBox ddb = element
						.findParent(DragAndDropableBox.class);

				if (ddb != null) {
					ddb.removeElementFromList(element, target);
				}

				DraggablePrioListTarget.this.box.itemDropped(element,
						DraggablePrioListTarget.this, target);

				AbstractDropAndSortableBox dsb = element
						.findParent(AbstractDropAndSortableBox.class);

				if (dsb != null) {
					if (!dsb.listContainsElement(element))
						dsb.removeItem(element, target);
				}

				AbstractPriorityListSelector pls = findParent(AbstractPriorityListSelector.class);
				if (pls != null) {
					pls.updateLists(target);
				}
			} catch (ClassCastException e) {
				// TODO exception handling
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO repair this
				return;
			}
		}
	}
	
	private Label addDummyLabel(int index){
		Label label = new Label("DraggablePrioListTarget.element", ""
				+ (index + 1) + ". Priorität");
		label.add(new SimpleAttributeModifier("style",
				"font-size: 24px; font-style: bold; text-align: center; margin-top: auto; margin-buttom: auto"));
		return label;
	}
}
