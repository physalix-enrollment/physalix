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

import hsa.awp.event.model.Event;
import hsa.awp.usergui.prioritylistselectors.AbstractPriorityListSelector;
import hsa.awp.usergui.util.DragAndDrop.AbstractDropAndSortableBox;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.wicketstuff.scriptaculous.dragdrop.DraggableTarget;

/**
 * Box which contains {@link DragableElement}.
 *
 * @author basti
 */
public class DragAndDropableBox extends Panel {
  public static final String DRAG_AND_DROPABLE_BOX_ITEM = "dragAndDropableBox.item";

  /**
   * generated UID.
   */
  private static final long serialVersionUID = 2274850184692999190L;

  /**
   * List of DragableElement which are currently in this box.
   */
  private List<DragableElement> elements;
  
  private long componentId = -1;

  /**
   * Maximum of elements which this box can hold.
   */
  private int maxEntries;

  /**
   * Plain constructor only with Wicket id. It can be used if there is no limit of elements and in the beginning the box is empty.
   *
   * @param id Wicket id
   */
  public DragAndDropableBox(String id) {

    this(id, null, -1);
  }

  /**
   * Constructor which provides an empty box with an element limit.
   *
   * @param id         Wicket id
   * @param maxEntries element limit
   */
  public DragAndDropableBox(String id, int maxEntries) {

    this(id, null, maxEntries);
  }

  /**
   * Constructor which has no limit but an initial list of events.
   *
   * @param id     Wicket id
   * @param events initial list of event
   */
  public DragAndDropableBox(String id, List<Event> events) {

    this(id, events, -1);
  }

  /**
   * General construcktor which has a limit and an initial list.
   *
   * @param id         Wicket id
   * @param events     initial list of events
   * @param maxEntries element limit.
   */
  public DragAndDropableBox(String id, List<Event> events, final int maxEntries) {

    super(id);
    this.setOutputMarkupId(true);

    if (maxEntries > 0) {
      this.maxEntries = maxEntries;
    } else {
      this.maxEntries = Integer.MAX_VALUE;
    }
    if (events != null) {
      if (events.size() >= this.maxEntries) {
        throw new RuntimeException("wrong DragAndDropableBox usage eventlist bigger than displayable elements");
      } else {
        elements = new LinkedList<DragableElement>();
        for (Event event : events) {
          elements.add(new DragableElement(DRAG_AND_DROPABLE_BOX_ITEM, event, true));
        }
      }
    } else {
      this.elements = new LinkedList<DragableElement>();
    }

    DraggableTarget box = new DraggableTarget("dragAndDropableBox.box") {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      protected void onDrop(Component comp, AjaxRequestTarget target) {

        DragableElement element = null;
        try {
          element = (DragableElement) comp.findParent(DragableElement.class);
          AbstractPriorityListSelector prioListSel = findParent(AbstractPriorityListSelector.class);
          if (!listContainsElement(elements, element) && elements.size() < DragAndDropableBox.this.maxEntries) {
            DragAndDropableBox ddb = prioListSel.getSourceBox();
            if(ddb.getComponentId() == prioListSel.getDropBoxElementId(element))
            	elements.add(element);

//            if (ddb != null) {
//              ddb.removeElementFromList(element, target);
//            }
            AbstractDropAndSortableBox dsb = element.findParent(AbstractDropAndSortableBox.class);

            if (dsb != null) {
              boolean isLastElement = dsb.removeItem(element, target);
              if(isLastElement){
            	  DragAndDropableBox.this.add(new SimpleAttributeModifier("style", "background-color: #ffffff"));
              }
            }
          }
          prioListSel.updateLists(target);
        } catch (ClassCastException e) {
          e.printStackTrace();
        } catch (NullPointerException e) {
          e.printStackTrace();
        }
      }
    };
    if (this.maxEntries != Integer.MAX_VALUE) {
      box.add(new AttributeAppender("class", new Model<String>("priolist"), " "));
    } else {
      box.add(new AttributeAppender("class", new Model<String>("selector"), " "));
    }

    box.add(new ListView<DragableElement>("dragAndDropableBox.list", elements) {
      /**
       *
       */
      private static final long serialVersionUID = 8736299534177286367L;

      @Override
      protected void populateItem(ListItem<DragableElement> item) {

        item.add(new DragableElement(DRAG_AND_DROPABLE_BOX_ITEM, item.getModelObject().getEvent()));
      }
    });

    add(box);
  }

  /**
   * Checks whether the element is already in the list or not.
   *
   * @param list    list which will be searched
   * @param element element which should be searched
   * @return true if found false if not
   */

  private boolean listContainsElement(List<DragableElement> list, DragableElement element) {

    boolean found = false;
    for (DragableElement dragableElement : list) {
      if (dragableElement.getEvent().getId() == element.getEvent().getId()) {
        found = true;
        return found;
      }
    }

    return found;
  }

  /**
   * Removes an element from the list. Is called when an element starts dragging.
   *
   * @param e      element which should be removed.
   * @param target AjaxRequestTarget for updating component
   */
  public void removeElementFromList(DragableElement e, AjaxRequestTarget target) {

    for (int i = 0; i < elements.size(); i++) {
      if (elements.get(i).getEvent().getId() == e.getEvent().getId()) {
        elements.remove(i);
      }
    }

    AbstractPriorityListSelector prioListSel = findParent(AbstractPriorityListSelector.class);
    prioListSel.updateLists(target);
  }

  public void addAllEvents(Collection<Event> events) {

    for (Event e : events) {
      elements.add(new DragableElement(DRAG_AND_DROPABLE_BOX_ITEM, e));
    }
  }

  /**
   * Adds an element to the box.
   *
   * @param element element to be added.
   */
  public void addElement(DragableElement element) {

    elements.add(element);
  }

  /**
   * Generates List of all Events, which are currently in the box.
   *
   * @return list of events
   */
  public List<Event> getEventList() {

    List<Event> list = new LinkedList<Event>();

    for (DragableElement e : elements) {
      list.add(e.getEvent());
    }

    return list;
  }

  public void removeAllElements() {

    if (elements.size() > 0) {
      elements.clear();
    }
  }

  public long getComponentId() {
	return componentId;
  }

  public void setComponentId(long componentId) {
	this.componentId = componentId;
  }

}
