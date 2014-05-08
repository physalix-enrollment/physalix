package hsa.awp.scire.procedureLogic.util;

import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.campaign.model.PriorityListItem;
import hsa.awp.event.facade.IEventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Collection;

public class XmlDrawLogUtil {

  private static final Logger log = LoggerFactory.getLogger(XmlDrawLogUtil.class);

  private JAXBContext context;

  private IEventFacade eventFacade;

  public String transformMailContentsToXml(Collection<MailContent> contentList) {
    XmlDrawLog xmlDrawLog = transformMailContentsToXmlDrawLog(contentList);
    return marshalXmlDrawLogToString(xmlDrawLog);
  }

  public String marshalXmlDrawLogToString(XmlDrawLog xmlDrawLog) {
    StringWriter writer = new StringWriter();

    try {
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(xmlDrawLog, writer);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

  public XmlDrawLog transformMailContentsToXmlDrawLog(Collection<MailContent> contentList) {
    XmlDrawLog xmlDrawLog = new XmlDrawLog();
    for (MailContent content : contentList) {
      XmlDrawLog.LogEntry entry = transformMailContentToLogEntry(content);
      xmlDrawLog.logEntries.add(entry);
    }
    return xmlDrawLog;
  }

  public XmlDrawLog.LogEntry transformMailContentToLogEntry(MailContent content) {
    XmlDrawLog.LogEntry entry = new XmlDrawLog.LogEntry();
    try {
      DrawProcedure procedure = content.getDrawProcedure();
      SingleUser user = content.getUser();

      entry.campaign = procedure.getCampaign().getName();
      entry.procedure = procedure.getName();
      entry.fullname = user.getName();
      entry.username = user.getUsername();
      entry.mail = user.getMail();

      if (user instanceof Student)
        entry.matnr = ((Student) user).getMatriculationNumber();
      else
        entry.matnr = 0;

      int priorityListIndex = 1;
      for (PriorityList priorityList : content.getPrioLists()) {
        XmlDrawLog.PrioList prioList = new XmlDrawLog.PrioList();
        prioList.nr = priorityListIndex++;

        for (PriorityListItem item : priorityList.getItems()) {
          XmlDrawLog.PrioListEntry prioListEntry = new XmlDrawLog.PrioListEntry();
          prioListEntry.id = item.getEvent();
          prioListEntry.priority = item.getPriority();

          try {
            Event event = eventFacade.getEventById(item.getEvent());
            prioListEntry.eventId = event.getEventId();
            prioListEntry.subject = event.getSubject().getName();
            prioListEntry.text = event.getDetailInformation();
          } catch (Exception e) {
            log.warn("caught exception. skipping detail information for priolist item.", e);
          }
          prioList.priorityListEntries.add(prioListEntry);
        }
        entry.priorityLists.add(prioList);
      }

      for (ConfirmedRegistration registration : content.getRegistrations()) {
        XmlDrawLog.Ticket ticket = new XmlDrawLog.Ticket();
        ticket.id = registration.getEventId();

        try {
          Event event = eventFacade.getEventById(registration.getEventId());
          ticket.eventId = event.getEventId();
          ticket.subject = event.getSubject().getName();
          ticket.text = event.getDetailInformation();
        } catch (Exception e) {
          log.warn("caught exception. skipping detail information for registration.", e);
        }
        entry.tickets.add(ticket);
      }
    } catch (Exception e) {
      log.warn("caught exception. returning partial log entry", e);
    }

    return entry;
  }

  public void setContext(JAXBContext context) {
    this.context = context;
  }

  public void setEventFacade(IEventFacade eventFacade) {
    this.eventFacade = eventFacade;
  }
}
