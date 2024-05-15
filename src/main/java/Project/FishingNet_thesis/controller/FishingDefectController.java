package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.websocket.handler.MyWebSocketHandler;
import Project.FishingNet_thesis.service.FishingDefectRepository;
import Project.FishingNet_thesis.table.FishingDefect;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/fishing-defect")
@CrossOrigin(origins = "*")
public class FishingDefectController {
    private final MyWebSocketHandler myWebSocketHandler;
    private final FishingDefectRepository fishingDefectRepository;

    @Autowired
    public FishingDefectController(MyWebSocketHandler myWebSocketHandler, FishingDefectRepository fishingDefectRepository) {
        this.myWebSocketHandler = myWebSocketHandler;
        this.fishingDefectRepository = fishingDefectRepository;
    }

    @PostMapping("/upload-image")
    public APIResponse uploadFishingDefect(@RequestParam("file") MultipartFile file) {
        APIResponse res = new APIResponse();
        try {
            String uuid = UUID.randomUUID().toString();
            String filename = uuid;
            String directory = System.getProperty("user.dir") + "/imageDB";
            String filePath = Paths.get(directory, filename + ".jpg").toString();

            // Create the directory if it does not exist
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Save the image to the directory
            File dest = new File(filePath);
            file.transferTo(dest);

            // Use the destination file from now on
            File newFile = new File(filePath);

            FishingDefect fishingDefect = new FishingDefect();

            fishingDefect.setId(uuid);
            fishingDefect.setTimestamp(new Date().toString());
            fishingDefect.setFilename(filename);

            // Save the file path to the database instead of the Blob
            fishingDefect.setImage(filePath);

            fishingDefectRepository.save(fishingDefect);
            res.setStatus(200);
            res.setMessage("Success");

            // Convert the FishingDefect object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(fishingDefect);

            // Send message and image to Line Notify
            String lineNotifyToken = "XLkCg5xlhohfmIGBX7H0X8sh61bbxkmnTBygvu58DOP";
            String message = "New fishing defect uploaded: " + filename;
            sendToLineNotify(lineNotifyToken, message, newFile);

            // Create URLs for "Activate" and "Deactivate" messages
            String activateUrl = "http://yourserver.com/api/fishing-defect/activate";
            String deactivateUrl = "http://yourserver.com/api/fishing-defect/deactivate";

            // Send URLs to Line Notify
            sendToLineNotify(lineNotifyToken, "Activate URL: " + activateUrl, null);
            sendToLineNotify(lineNotifyToken, "Deactivate URL: " + deactivateUrl, null);

            // Send the JSON to all clients websocket
            Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
            for (WebSocketSession session : sessions) {
                if (session != null && session.isOpen()) {
                    myWebSocketHandler.sendMessage(session, json);
                }
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.setMessage(e.getMessage());
        }
        return res;
    }

    private void sendToLineNotify(String lineNotifyToken, String message, File file) throws IOException {
    String lineNotifyApiUrl = "https://notify-api.line.me/api/notify";
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(lineNotifyApiUrl);
    httpPost.addHeader("Authorization", "Bearer " + lineNotifyToken);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addTextBody("message", message);
    if (file != null) {
        builder.addBinaryBody("imageFile", new FileInputStream(file), ContentType.create("image/jpeg"), file.getName());
    }
    org.apache.http.HttpEntity multipart = builder.build();
    httpPost.setEntity(multipart);

    CloseableHttpResponse response = httpClient.execute(httpPost);
    httpClient.close();
}


    @GetMapping("/get_dataById/{id}")
    public FishingDefect getDataById(@PathVariable String id) {
        FishingDefect fishingDefect = fishingDefectRepository.findById(id).orElse(null);
        if (fishingDefect != null) {
            try {
                Path path = Paths.get(fishingDefect.getImage());
                byte[] fileContent = Files.readAllBytes(path);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                fishingDefect.setImage(encodedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fishingDefect;
    }
}