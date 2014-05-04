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

package hsa.awp.usergui.util.DragAndDrop;

import hsa.awp.event.model.Event;
import hsa.awp.usergui.util.DragableElement;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Panel which displays a sortable list of events.
 * 
 * @author basti
 */
public class DropAndSortableBox extends AbstractDropAndSortableBox {
	/**
	 * unique serialization id.
	 */
	private static final long serialVersionUID = 1929005508618747799L;

	public DropAndSortableBox(String id, int maxItems) {

		this(id, null, maxItems, true);
	}

	public DropAndSortableBox(String id, List<Event> events, int maxItems,
			boolean isActive) {

		super(id, events, maxItems, isActive);
	}

	@Override
	public boolean isAddingAllowed(DragableElement element, AjaxRequestTarget target) {
		return true;
	}
	
	@Override
	public boolean listContainsElement(DragableElement element){
		return false;
	}

	@Override
	public boolean removeItem(DragableElement element, AjaxRequestTarget target) {

		boolean deleted = false;
		DragableElement [] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (deleted) {
				elements[i - 1] = elements[i]; /* move all following items up */
				elements[i] = null;
			} else if (elements[i] != null
					&& elements[i].getEvent().equals(element.getEvent())) {
				elements[i] = null; /* delete item */
				deleted = true;
			}
		}
		updateAll(target); /* update component */
		return false;
	}
}
