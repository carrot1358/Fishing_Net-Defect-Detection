package Project.FishingNet_thesis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class FishingNetThesisApplication {

 public static void main(String[] args) {
  SpringApplication.run(FishingNetThesisApplication.class, args);

  // Auto open Swagger UI in default browser
  if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    try {
      Desktop.getDesktop().browse(new URI("http://localhost:8080/swagger-ui.html"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
 }
}