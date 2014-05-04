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

package hsa.awp.usergui;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.FifoProcedure;
import hsa.awp.campaign.model.Procedure;
import hsa.awp.usergui.controller.IUserGuiController;
import hsa.awp.usergui.prioritylistselectors.NewPriorityListSelector;
import hsa.awp.usergui.prioritylistselectors.PriorityListSelector;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CampaignPreviewPanel extends Panel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 3158911913605383361L;
/**
   * {@link hsa.awp.usergui.controller.IUserGuiController}.
   */
  @SpringBean(name = "usergui.controller")
  private IUserGuiController controller;

  public CampaignPreviewPanel(String id, final Campaign campaign) {
    super(id);
    Link<String> campaignDetail = createCampaignDetail(campaign);
    campaignDetail.setEnabled(isCampaignActive(campaign.findCurrentProcedure()));
    add(campaignDetail);
    final ModalWindow detailWindow = new ModalWindow("campaignListPanel.detailWindow");
    detailWindow.setContent(new AjaxLazyLoadPanel(detailWindow.getContentId()) {
      /**
       *
       */
      private static final long serialVersionUID = -822132746613326567L;

      @Override
      public Component getLazyLoadComponent(String markupId) {

        return new CampaignDetailPanel(markupId, campaign.getDetailText());
      }
    });
    detailWindow.setTitle(new Model<String>("Kampagnendetails"));
    detailWindow.setInitialWidth(450);

    add(detailWindow);

    add(new AjaxFallbackLink<Object>("campaignListPanel.infoLink") {
      /**
       * unique serialization id.
       */
      private static final long serialVersionUID = 543607735730300949L;

      @Override
      public void onClick(AjaxRequestTarget target) {

        detailWindow.show(target);
      }
    });

  }

  private boolean isCampaignActive(Procedure currentProcedure) {
    if (currentProcedure instanceof DrawProcedure) {

      DrawProcedure drawProcedure = controller.getDrawProcedureById(currentProcedure.getId());

      if (controller.isAlreadyDrawn(drawProcedure)) {
        return false;
      } else {
        return drawProcedure.isActive();
      }

    } else if (currentProcedure instanceof FifoProcedure) {
      FifoProcedure fifoProcedure = (FifoProcedure) currentProcedure;
      return fifoProcedure.isActive();
    }
    throw new IllegalStateException("undefinded state");
  }

  private Link<String> createCampaignDetail(final Campaign camp) {

    final Procedure currentProcedure = camp.findCurrentProcedure();

    Link<String> link = new Link<String>("campaignListPanel.link") {
      /**
       * generated UID.
       */
      private static final long serialVersionUID = 3425455511638833341L;

      @Override
      public void onClick() {

        if (camp.findCurrentProcedure() instanceof FifoProcedure) {
          setResponsePage(new OnePanelPage(new FlatListPanel("p1", camp)));
        } 
        else if (camp.findCurrentProcedure() instanceof DrawProcedure
            && !controller.isAlreadyDrawn((DrawProcedure) currentProcedure)) {
        	if(camp.findCurrentProcedure().getRuleBased() == 0)
        		setResponsePage(new OnePanelPage(new PriorityListSelector("p1", (DrawProcedure) camp
        				.findCurrentProcedure())));
        	else if(camp.findCurrentProcedure().getRuleBased() == 1)
        		setResponsePage(new OnePanelPage(new NewPriorityListSelector("p1", camp
        				.findCurrentProcedure().getId())));
        }
      }
    };
    link.add(new Label("campaignListPanel.name", camp.getName()));
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    link.add(new Label("campaignListPanel.timeSpan", "vom "
        + df.format(camp.getStartShow().getTime())
        + " bis "
        + df.format(camp.getEndShow().getTime())));

    ListView<Procedure> procedureListView = new ListView<Procedure>("campaignListPanel.procedureList", createProcedureList(camp)) {
      /**
		 * 
		 */
		private static final long serialVersionUID = -6278807028608347383L;

	@Override
      protected void populateItem(ListItem<Procedure> procedureListItem) {
        procedureListItem.add(new ProcedureDetailView("campaignListPanel.procedure", procedureListItem.getModelObject()));
      }
    };

    link.add(procedureListView);
    return link;
  }

  private List<Procedure> createProcedureList(Campaign campaign) {

    List<Procedure> procedures = new ArrayList<Procedure>(campaign.getAppliedProcedures());

    Collections.sort(procedures, new Comparator<Procedure>() {
      @Override
      public int compare(Procedure procedure, Procedure procedure1) {
        return procedure.getStartDate().compareTo(procedure1.getStartDate());
      }
    });

    return procedures;
  }


}
