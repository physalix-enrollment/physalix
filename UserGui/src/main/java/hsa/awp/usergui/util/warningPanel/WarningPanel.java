package hsa.awp.usergui.util.warningPanel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class WarningPanel extends Panel {

  private static final long serialVersionUID = 1810597523360348356L;

  public WarningPanel(String id, String message) {
    super(id);
    add(new Label("message", message));
  }

  public WarningPanel(String id, String message, boolean escapeModelString) {
    super(id);
    add(new Label("message", message).setEscapeModelStrings(escapeModelString));
  }

}
