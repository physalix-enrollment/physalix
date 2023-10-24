package hsa.awp.scire.procedureLogic.util;

import hsa.awp.campaign.model.Campaign;
import hsa.awp.campaign.model.ConfirmedRegistration;
import hsa.awp.campaign.model.DrawProcedure;
import hsa.awp.campaign.model.PriorityList;
import hsa.awp.common.exception.NoMatchingElementException;
import hsa.awp.event.facade.EventFacade;
import hsa.awp.event.model.Event;
import hsa.awp.event.model.EventBuilder;
import hsa.awp.event.model.Subject;
import hsa.awp.event.model.SubjectBuilder;
import hsa.awp.user.model.SingleUser;
import hsa.awp.user.model.Student;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.bind.JAXBContext;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XmlDrawLogUtilTest {

  @InjectMocks XmlDrawLogUtil xmlDrawLogUtil = new XmlDrawLogUtil();

  @Mock EventFacade eventFacade;
  private SingleUser user;
  private DrawProcedure drawProcedure;
  private Event event;
  private MailContent content;

  @Before
  public void setUp() throws Exception {
    JAXBContext context = JAXBContext.newInstance(XmlDrawLog.class);
    xmlDrawLogUtil.setContext(context);

    user = Student.getInstance("test-user", 123456);
    user.setName("Test User");
    user.setMail("test@physalix");
    Campaign campaign = Campaign.getInstance(3L);
    campaign.setName("testCampaign");
    drawProcedure = DrawProcedure.getInstance(1337L);
    drawProcedure.setName("testDraw");
    campaign.addProcedure(drawProcedure);
    Subject subject = new SubjectBuilder().build();
    event = new EventBuilder().withSubject(subject).build();
    event.setId(1L);

    when(eventFacade.getEventById(1L)).thenReturn(event);

    content = new MailContent(user);
    content.setDrawProcedure(drawProcedure);
    content.setPrioLists(createPriorityListsFromEvent(2, event.getId()));
    content.setRegistrations(createConfirmedRegistrationsFromEvent(2, event.getId()));
  }

  @Test
  public void testTransformMailContentsToXml() {

    List<MailContent> contents = asList(content, content);

    String xml = xmlDrawLogUtil.transformMailContentsToXml(contents);

    assertThat(xml, containsString("testCampaign"));
    assertThat(xml, containsString("testDraw"));
    assertThat(xml, containsString("Test User"));
    assertThat(xml, containsString("test@physalix"));
    assertThat(xml, containsString("123456"));
    assertThat(xml, containsString("test-user"));
    assertThat(xml, containsString("<drawLog>"));
  }

  @Test
  public void testTransformMailContentsToXmlWithAbsentEvent() {

    long nonExistingEventId = 123456789L;
    when(eventFacade.getEventById(nonExistingEventId)).thenThrow(new NoMatchingElementException());
    ReflectionTestUtils.setField(event, "subject", null);

    MailContent partial = new MailContent(user);
    partial.setDrawProcedure(drawProcedure);
    partial.setPrioLists(createPriorityListsFromEvent(2, nonExistingEventId));
    partial.setRegistrations(createConfirmedRegistrationsFromEvent(2, event.getId()));

    List<MailContent> contents = asList(partial);

    String xml = xmlDrawLogUtil.transformMailContentsToXml(contents);
    assertThat(xml, containsString("123456789"));
    assertThat(xml, containsString("<id>1</id>"));
    assertThat(xml, containsString("<eventId>42</eventId>"));
  }

  private List<ConfirmedRegistration> createConfirmedRegistrationsFromEvent(int amount, Long eventId) {
    List<ConfirmedRegistration> registrations = new ArrayList<ConfirmedRegistration>();
    for (int i = 0; i < amount; i++) {
      ConfirmedRegistration registration = ConfirmedRegistration.getInstance(eventId, 3L);
      registrations.add(registration);
    }
    return registrations;
  }

  private List<PriorityList> createPriorityListsFromEvent(int amount, Long eventId) {
    List<PriorityList> lists = new ArrayList<PriorityList>();
    for (int i = 0; i < amount; i++) {
      Long[] eventIds = new Long[amount];
      Arrays.fill(eventIds, eventId);
      PriorityList priorityList = PriorityList.getInstance(1L, 2L, asList(eventIds), 3L);
      lists.add(priorityList);
    }
    return lists;
  }
}
