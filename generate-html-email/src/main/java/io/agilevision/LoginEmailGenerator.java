package io.agilevision;

import io.agilevision.model.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;

public class LoginEmailGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(LoginEmailGenerator.class);

  public String generate(String email) {
    Map<String, Object> templateContext = generateTemplateContext(email);

    LOG.info("Configure TemplateRenderer");
    var templateRenderer = new TemplateRenderer();
    LOG.info("Render template");
    return templateRenderer.render(templateContext);
  }

  private Map<String, Object> generateTemplateContext(String email) {
    Map<String, Object> templateContext = new HashMap<>();
    templateContext.put("title", "Invitation");
    templateContext.put("timestamp", LocalDateTime.now());
    templateContext.put("textBody", "Your credentials:");
    Map<String, Object> actionBlock = new HashMap<>();

    actionBlock.put(
        "information",
        Arrays.asList(
            new InformationBlock("Email", email),
            new InformationBlock("Password", "XXXXXXX")));

    Map<String, String> action = new HashMap<>();
    action.put("text", "Login");
    action.put("url", "https://agilevision.io");
    action.put("contactUs", "mailto:contact@agilevision.io");

    actionBlock.put("action", action);
    templateContext.put("actionBlock", actionBlock);
    return templateContext;
  }
}
