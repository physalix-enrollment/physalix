package hsa.awp.admingui.priolists;

import hsa.awp.admingui.controller.IAdminGuiController;
import hsa.awp.campaign.model.Campaign;
import hsa.awp.event.model.Event;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class PrioListItemsPanel extends Panel {

	private static final long serialVersionUID = -6320751336684964627L;
	@SpringBean(name = "admingui.controller")
	private transient IAdminGuiController controller;
	private EventModel eventModel;
	private WebMarkupContainer updateContainer;
	private LoadableDetachableModel<List<Campaign>> campaignsModel;

	public PrioListItemsPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		updateContainer = new WebMarkupContainer("updateContainer");
		updateContainer.setOutputMarkupId(true);
		eventModel = new EventModel();
		campaignsModel = new LoadableDetachableModel<List<Campaign>>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected List<Campaign> load() {
				return controller.getAllCampaigns();
			}
		};
		final ListView<Event> eventList = new ListView<Event>("events", eventModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Event> item) {
				Event e = item.getModelObject();
				item.add(new Label("events.name", e.getSubject().getName()));
				item.add(new Label("events.desc", e.getDetailInformation()));
			}
		};
		final DropDownChoice<Campaign> campaignDropDown = new DropDownChoice<Campaign>("campaigns", new Model<Campaign>(), campaignsModel);
		campaignDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			
			private static final long serialVersionUID = 4598489035621218371L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Campaign camp = campaignDropDown.getModelObject();
				eventModel.setCampaign(camp);
				target.addComponent(updateContainer);
			}
		});
		add(campaignDropDown);
		updateContainer.add(eventList);
		add(updateContainer);
	}
	
	private class EventModel extends LoadableDetachableModel<List<Event>>{
		
		private static final long serialVersionUID = 1L;
		private Campaign campaign = null;

		@Override
		protected List<Event> load() {
			List<Event> events = new ArrayList<Event>();
			if(campaign != null)
				events = controller.getEventsByCampaign(campaign);
			return events;
		}
		
		public void setCampaign(Campaign camp){
			campaign = camp;
		}
	}

}
