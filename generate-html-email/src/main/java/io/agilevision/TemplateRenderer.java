package io.agilevision;

import com.github.jknack.handlebars.*;
import java.io.*;
import java.util.*;

public class TemplateRenderer {

  private static final String PATH_TO_COGNITO_TEMPLATE = "hbs/general-template.hbs";

  private final Template template;

  public TemplateRenderer() {
    Handlebars handlebars = new Handlebars();
    handlebars.getLoader().setSuffix("");
    try {
      template = handlebars.compile(PATH_TO_COGNITO_TEMPLATE);
    } catch (IOException ioe) {
      throw new IllegalStateException("Cannot instantiate TemplateRenderer", ioe);
    }
  }

  public String render(Map<String, Object> context) {
    Context templateContext = Context.newBuilder(context).build();
    try {
      return template.apply(templateContext);
    } catch (IOException ioe) {
      throw new UncheckedIOException("Cannot render template", ioe);
    }
  }
}
