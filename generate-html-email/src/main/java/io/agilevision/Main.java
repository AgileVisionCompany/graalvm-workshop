package io.agilevision;

import java.io.*;
import java.nio.file.*;
import org.slf4j.*;

public class Main {

  private static final String RES_FILE_PATH = "email.html";

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    var email = System.getenv()
        .getOrDefault("APP_LOGIN_EMAIL", "Please set email to env variable: APP_LOGIN_EMAIL");
    LOG.info("Generate email for: {}", email);
    var html = new LoginEmailGenerator().generate(email);
    LOG.info("Write result to file: {}", RES_FILE_PATH);
    Files.writeString(Path.of(RES_FILE_PATH), html);
  }
}
