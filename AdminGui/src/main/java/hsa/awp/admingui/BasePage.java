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

package hsa.awp.admingui;

import hsa.awp.admingui.controller.IAdminGuiController;
import hsa.awp.admingui.edit.*;
import hsa.awp.admingui.edit.event.EventPanel;
import hsa.awp.admingui.report.view.ReportPrintingPanel;
import hsa.awp.admingui.rule.RulePanel;
import hsa.awp.admingui.usermanagement.MandatorDetailPanel;
import hsa.awp.admingui.usermanagement.MandatorPanel;
import hsa.awp.admingui.usermanagement.UserManagement;
import hsa.awp.admingui.util.AccessUtil;
import hsa.awp.admingui.util.StudyCourseImportPanel;
import hsa.awp.admingui.view.*;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static hsa.awp.admingui.util.AccessUtil.allowRender;

/**
 * This ist the {@link BasePage} which holds the main layout and the css style of all pages.
 *
 * @author Basti
 * @author Rico
 */
@AuthorizeInstantiation(value = "enterAdminGui")
public class BasePage extends WebPage {

  /**
   * GuiController which feeds the Gui with Data.
   */
  @SpringBean(name = "admingui.controller")
  private transient IAdminGuiController controller;

  /**
   * Default constructor with default data.
   */
  public BasePage() {

    addTimestamp();
    addCssResources();
    addPageLinks();
    addUserInfo();
  }

  private void addUserInfo() {
    add(new UserInfoPanel("userInfo"));

//    ((AuthenticatedSpringSecuritySession) getSession()).setMandatorId(controller.getActiveMandator(getSession()));
  }

  private void addPageLinks() {
    addCreateCategoryLink();
    addCreateSubjectLink();
    addCreateEventLink();
    addCreateTermLink();
    addEventListLink();
    addCreateCampaignLink();
    addSubjectListLink();
    addProcedureListLink();
    addCreateDrawProcedureLink();
    addCreateFifoProcedureLink();
    addCampaignListLink();
    addRulePanelLink();
    addUserManagementLink();
    addUserRegistrationLink();
    addReportPrintingPanel();
    addStudyCourseImportLink();
    addMandatorLink();
    addSeperators();
  }

  private void addUserRegistrationLink() {
    Link<RegistrationManagementPanel> registrationManagementPanelLink = new Link<RegistrationManagementPanel>("registrations") {

      private static final long serialVersionUID = 3112868747232220835L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new RegistrationManagementPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(registrationManagementPanelLink, "editRegistrations");
    add(registrationManagementPanelLink);
  }

  private void addMandatorLink() {

    Link<MandatorPanel> mandatorPanelLink = new Link<MandatorPanel>("mandators") {

      private static final long serialVersionUID = 2511266333811057735L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new MandatorPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(mandatorPanelLink, "mandators");
    add(mandatorPanelLink);

    Link<MandatorDetailPanel> mandatorDetailPanelLink = new Link<MandatorDetailPanel>("mandatorDetail") {

      private static final long serialVersionUID = 2511266333811057735L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new MandatorDetailPanel(OnePanelPage.getPanelIdOne(), controller.getMandatorById(controller.getActiveMandator(this.getSession())))));
      }
    };
    allowRender(mandatorDetailPanelLink, "mandatorDetail");
    add(mandatorDetailPanelLink);
  }

  private void addSeperators() {
    Label categorySeperator = new Label("categorySeperator");
    Label subjectSeperator = new Label("subjectSeperator");
    Label termSeperator = new Label("termSeperator");
    Label eventSeperator = new Label("eventSeperator");
    Label procedureSeperator = new Label("procedureSeperator");
    Label campaignSeperator = new Label("campaignSeperator");
    Label ruleSeperator = new Label("ruleSeperator");
    Label utilSeperator = new Label("utilSeperator");

    AccessUtil.allowRender(categorySeperator, "createCategories");
    AccessUtil.allowRender(subjectSeperator, "createSubjects", "viewSubjects");
    AccessUtil.allowRender(termSeperator, "createTerms");
    AccessUtil.allowRender(eventSeperator, "createEvents", "viewEvents");
    AccessUtil.allowRender(procedureSeperator, "createFifoProcedure", "createDrawProcedure", "viewProcedures");
    AccessUtil.allowRender(campaignSeperator, "createCampaign", "viewCampaigns");
    AccessUtil.allowRender(ruleSeperator, "ruleManagement");
    AccessUtil.allowRender(utilSeperator, "reportPrinting", "userManagement");


    add(categorySeperator);
    add(subjectSeperator);
    add(termSeperator);
    add(eventSeperator);
    add(procedureSeperator);
    add(campaignSeperator);
    add(ruleSeperator);
    add(utilSeperator);
  }

  private void addCssResources() {
    add(CSSPackageResource.getHeaderContribution(HomePage.class, "style.css"));
  }

  private void addTimestamp() {
    DateFormat df = new SimpleDateFormat("HH:mm:ss");
    add(new Label("timestamp", df.format(new Date())));
  }

  private void addCreateCategoryLink() {

    Link<SubjectPanel> createCategoryLink = new Link<SubjectPanel>("createCategory") {

      private static final long serialVersionUID = 2511266333811057735L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new CategoryPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createCategoryLink, "createCategories");
    add(createCategoryLink);
  }

  private void addCreateSubjectLink() {

    Link<SubjectPanel> createSubjectLink = new Link<SubjectPanel>("createSubject") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 2511266333811057735L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new SubjectPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createSubjectLink, "createSubjects");
    add(createSubjectLink);
  }

  private void addCreateEventLink() {

    Link<EventPanel> createEventLink = new Link<EventPanel>("createEvent") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = -7698021140752722168L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new EventPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createEventLink, "createEvents");
    add(createEventLink);
  }

  private void addCreateTermLink() {

    Link<TermPanel> createTermLink = new Link<TermPanel>("createTerm") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = -7698021140752722178L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new TermPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createTermLink, "createTerms");
    add(createTermLink);
  }

  private void addEventListLink() {

    Link<EventListPanel> eventListLink = new Link<EventListPanel>("eventList") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = -769802143452722168L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new EventListPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(eventListLink, "viewEvents");
    add(eventListLink);
  }

  private void addCreateCampaignLink() {

    Link<CreateCampaignPanel> createCampaignLink = new Link<CreateCampaignPanel>("createCampaign") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 3837467123649300329L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new CreateCampaignPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createCampaignLink, "createCampaign");
    add(createCampaignLink);
  }

  private void addSubjectListLink() {

    Link<EventListPanel> subjectListLink = new Link<EventListPanel>("subjectList") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = -769802143452722168L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new SubjectListPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(subjectListLink, "viewSubjects");
    add(subjectListLink);
  }

  private void addProcedureListLink() {

    Link<ProcedureListPanel> procedureListLink = new Link<ProcedureListPanel>("procView") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 3837467123649300329L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new ProcedureListPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(procedureListLink, "viewProcedures");
    add(procedureListLink);
  }

  private void addCreateDrawProcedureLink() {

    Link<DrawProcedurePanel> createDrawProcedureLink = new Link<DrawProcedurePanel>("createDrawProcedure") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151952344L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new DrawProcedurePanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createDrawProcedureLink, "createDrawProcedure");
    add(createDrawProcedureLink);
  }

  private void addCreateFifoProcedureLink() {

    Link<FifoProcedurePanel> createFifoProcedureLink = new Link<FifoProcedurePanel>("createFifoProcedure") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151952344L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new FifoProcedurePanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(createFifoProcedureLink, "createFifoProcedure");
    add(createFifoProcedureLink);
  }

  private void addCampaignListLink() {

    Link<CampaignListPanel> campaignListLink = new Link<CampaignListPanel>("campaignList") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151951345L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new CampaignListPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(campaignListLink, "viewCampaigns");
    add(campaignListLink);
  }

  private void addRulePanelLink() {

    Link<RulePanel> rulePanelLink = new Link<RulePanel>("rulePanel") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151951345L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new RulePanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(rulePanelLink, "ruleManagement");
    add(rulePanelLink);
  }

  private void addUserManagementLink() {

    Link<UserManagement> userManagementLink = new Link<UserManagement>("userManagement") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151951345L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new UserManagement(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(userManagementLink, "userManagement");
    add(userManagementLink);
  }

  private void addStudyCourseImportLink() {

    Link<StudyCourseImportPanel> studyCourseImport = new Link<StudyCourseImportPanel>("studyCourseImport") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151951345L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new StudyCourseImportPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(studyCourseImport, "studyCourseImport");
    add(studyCourseImport);
  }

  private void addReportPrintingPanel() {

    Link<ReportPrintingPanel> reportPrintingLink = new Link<ReportPrintingPanel>("reportPrinting") {
      /**
       * Generated serialization id.
       */
      private static final long serialVersionUID = 73001852151951345L;

      @Override
      public void onClick() {

        setResponsePage(new OnePanelPage(new ReportPrintingPanel(OnePanelPage.getPanelIdOne())));
      }
    };
    allowRender(reportPrintingLink, "reportPrinting");
    add(reportPrintingLink);
  }


}
