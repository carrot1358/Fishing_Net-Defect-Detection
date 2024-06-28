package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.models.NotificationDocument;
import Project.FishingNet_thesis.payload.response.APIResponse;
import Project.FishingNet_thesis.repository.DefectStatisticsRepository;
import Project.FishingNet_thesis.models.DefectStatisticsDocument;
import Project.FishingNet_thesis.repository.NotificationRepository;
import Project.FishingNet_thesis.repository.UserRepository;
import Project.FishingNet_thesis.security.service.websocket.handler.MyWebSocketHandler;
import Project.FishingNet_thesis.repository.FishingDefectRepository;
import Project.FishingNet_thesis.models.FishingDefectDocument;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fishing-defect")
public class FishingDefectController {
    private static final Logger logger = LoggerFactory.getLogger(FishingDefectController.class);

    @Autowired
    MyWebSocketHandler myWebSocketHandler;
    @Autowired
    FishingDefectRepository fishingDefectRepository;
    @Autowired
    DefectStatisticsRepository defectStatisticsRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;

    public void managedToWebSocket(String sendTo,String status,String message,String imageId, Date date){
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("sendTo", sendTo);
            messageMap.put("status", status);
            messageMap.put("message", message);
            messageMap.put("id_image", imageId);
            messageMap.put("date", date);
            // Convert the message to JSON
            String messageJson = objectMapper.writeValueAsString(messageMap);
            // Send the JSON to all clients websocket
            Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
            for (WebSocketSession session : sessions) {
                if (session != null && session.isOpen()) {
                    myWebSocketHandler.sendMessage(session, messageJson);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void increaseDefectCount(Date date, String id) {
        //get only year, month, day
        date = new Date(date.getYear(), date.getMonth(), date.getDate());

        DefectStatisticsDocument defectStatisticsDocument = defectStatisticsRepository.findByDate(date);
        if (defectStatisticsDocument == null) {
            defectStatisticsDocument = new DefectStatisticsDocument();
            defectStatisticsDocument.setDate(date);
            defectStatisticsDocument.setDefectCount(1);
        } else {
            defectStatisticsDocument.setDefectCount(defectStatisticsDocument.getDefectCount() + 1);
        }
        defectStatisticsDocument.addDefect_id(id);
        defectStatisticsRepository.save(defectStatisticsDocument);
    }

    public void increaseActivateCount(Date date) {
        date = new Date(date.getYear(), date.getMonth(), 1);
        DefectStatisticsDocument defectStatisticsDocument = defectStatisticsRepository.findByDate(date);
        if (defectStatisticsDocument == null) {
            defectStatisticsDocument = new DefectStatisticsDocument();
            defectStatisticsDocument.setDate(date);
            defectStatisticsDocument.setActivateCount(1);
        } else {
            defectStatisticsDocument.setActivateCount(defectStatisticsDocument.getActivateCount() + 1);
        }
        defectStatisticsRepository.save(defectStatisticsDocument);
    }

    public void increaseDeactivateCount(Date date) {
        date = new Date(date.getYear(), date.getMonth(), 1);
        DefectStatisticsDocument defectStatisticsDocument = defectStatisticsRepository.findByDate(date);
        if (defectStatisticsDocument == null) {
            defectStatisticsDocument = new DefectStatisticsDocument();
            defectStatisticsDocument.setDate(date);
            defectStatisticsDocument.setDeactivateCount(1);
        } else {
            defectStatisticsDocument.setDeactivateCount(defectStatisticsDocument.getDeactivateCount() + 1);
        }
        defectStatisticsRepository.save(defectStatisticsDocument);
    }

    public void sendToLineNotify(String lineNotifyToken, String message, File file) {
        logger.info("sendToLineNotify() called with token: {}, message: {}, file: {}", lineNotifyToken, message, file != null ? file.getName() : "null");

        String lineNotifyApiUrl = "https://notify-api.line.me/api/notify";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(lineNotifyApiUrl);
        httpPost.addHeader("Authorization", "Bearer " + lineNotifyToken);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("message", message);
        if (file != null) {
            try {
                builder.addBinaryBody("imageFile", new FileInputStream(file), ContentType.create("image/jpeg"), file.getName());
            } catch (FileNotFoundException e) {
                logger.error("File not found: {}", file.getName(), e);
            }
        }
        org.apache.http.HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Log the status of the response
            logger.info("Response status: {}", response.getStatusLine().getStatusCode());
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error executing HTTP request", e);
        }
    }

    @PostMapping("/upload-image")
    public APIResponse uploadFishingDefect(@RequestParam("file") MultipartFile file) {
        APIResponse res = new APIResponse();
        try {
            String uuid = UUID.randomUUID().toString();
            String directory = System.getProperty("user.dir") + "/imageDB";
            String filePath = Paths.get(directory, uuid + ".jpg").toString();

            // Create the directory if it does not exist
            File dir = new File(directory);
            if (!dir.exists()) {
                boolean isCreated = dir.mkdirs();
                if (!isCreated) {
                    throw new IOException("Failed to create directory " + directory);
                }
            }

            // Save the image to the directory
            File dest = new File(filePath);
            file.transferTo(dest);

            // Use the destination file from now on
            File newFile = new File(filePath);

            FishingDefectDocument fishingDefectDocument = new FishingDefectDocument();
            Date currentDate = new Date();
            increaseDefectCount(currentDate, uuid);
            fishingDefectDocument.setId(uuid);
            fishingDefectDocument.setTimestamp(currentDate);
            fishingDefectDocument.setFilename(uuid);
            fishingDefectDocument.setIsmanaged(false);

            // Save the file path to the database instead of the Blob
            fishingDefectDocument.setImage(filePath);

            fishingDefectRepository.save(fishingDefectDocument);
            res.setStatus(200);
            res.setMessage("Success");

            // Convert the FishingDefect object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(fishingDefectDocument);

            // Send message and image to Line Notify
            List<NotificationDocument> listNotify = notificationRepository.findAll();
            for (NotificationDocument notify : listNotify) {
                String lineNotifyToken = notify.getToken();
                String userId = notify.getUserDocument().getId();
                // Create URLs for "Activate" and "Deactivate" messages
                String activateUrl = "http://yourserver.com/api/fishing-defect/activate?id=" + uuid + "&userId=" + userId;
                String deactivateUrl = "http://yourserver.com/api/fishing-defect/deactivate?id=" + uuid + "&userId=" + userId;
                // Send URLs to Line Notify
                try {
                    if (notify.getIsSendImage())
                        sendToLineNotify(lineNotifyToken, "New fishingNet defect uploaded: ", newFile);
                    if (notify.getIsSendLink()) {
                        sendToLineNotify(lineNotifyToken, "Activate URL: " + activateUrl, null);
                        sendToLineNotify(lineNotifyToken, "Deactivate URL: " + deactivateUrl, null);
                    }
                } catch (Exception e) {
                    logger.error("Error sending to Line Notify", e);
                }
            }

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

    @GetMapping("/activate")
    public APIResponse activate(@RequestParam("id") String id, @RequestParam("userId") String userId) {
        APIResponse res = new APIResponse();
        fishingDefectRepository.findById(id).ifPresent(fishingDefectDocument -> {
            if (!fishingDefectDocument.isIsmanaged()) {
                Date date = fishingDefectRepository.findById(id).get().getTimestamp();
                fishingDefectDocument.setUserDocument(userRepository.findById(userId).get());
                fishingDefectDocument.setIsmanaged(true);
                fishingDefectDocument.setStatus("Activated");
                fishingDefectRepository.save(fishingDefectDocument);
                increaseActivateCount(date);

                managedToWebSocket("frontend","success","Activated",id,date);
                res.setStatus(0);
                res.setMessage("Send  Activate  Success");
            } else {
                managedToWebSocket("frontend","error","This defect is already been managed.",id,fishingDefectDocument.getTimestamp());
                res.setStatus(1);
                res.setMessage("This defect is already been managed.");

            }
        });
        return res;
    }

    @GetMapping("/deactivate")
    public APIResponse deactivate(@RequestParam("id") String id, @RequestParam("userId") String userId) {
        APIResponse res = new APIResponse();
        fishingDefectRepository.findById(id).ifPresent(fishingDefectDocument -> {
            if (!fishingDefectDocument.isIsmanaged()) {
                Date date = fishingDefectRepository.findById(id).get().getTimestamp();
                fishingDefectDocument.setUserDocument(userRepository.findById(userId).get());
                fishingDefectDocument.setIsmanaged(true);
                fishingDefectDocument.setStatus("Deactivated");
                fishingDefectRepository.save(fishingDefectDocument);
                increaseDeactivateCount(date);

                managedToWebSocket("frontend","success","Deactivated",id,date);
                res.setStatus(0);
                res.setMessage("Send  Deactivated  Success");
            } else {
                managedToWebSocket("frontend","error","This defect is already been managed.",id,fishingDefectDocument.getTimestamp());
                res.setStatus(1);
                res.setMessage("This defect is already been managed.");

            }
        });
        return res;
    }

    @GetMapping("/get_dataById/{id}")
    public FishingDefectDocument getDataById(@PathVariable String id) {
        FishingDefectDocument fishingDefectDocument = fishingDefectRepository.findById(id).orElse(null);
        if (fishingDefectDocument != null) {
            try {
                Path path = Paths.get(fishingDefectDocument.getImage());
                byte[] fileContent = Files.readAllBytes(path);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                fishingDefectDocument.setImage(encodedString);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        return fishingDefectDocument;
    }

    @GetMapping("/get_alldata")
    public APIResponse getAllData() {
        APIResponse res = new APIResponse();
        List<FishingDefectDocument> fishingDefectdatumDocuments = fishingDefectRepository.findAll();
        for (FishingDefectDocument fishingDefectDocument : fishingDefectdatumDocuments) {
            try {
                Path path = Paths.get(fishingDefectDocument.getImage());
                byte[] fileContent = Files.readAllBytes(path);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                fishingDefectDocument.setImage(encodedString);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        res.setStatus(0);
        res.setMessage("Success");
        res.setData(fishingDefectdatumDocuments);
        return res;
    }

    @GetMapping("/get_lastdata")
    public APIResponse getLastData() {
        APIResponse res = new APIResponse();
        List<FishingDefectDocument> fishingDefectdatumDocuments = fishingDefectRepository.findAll();
        if (!fishingDefectdatumDocuments.isEmpty()) {
            FishingDefectDocument fishingDefectDocument = fishingDefectdatumDocuments.getLast();
            try {
                Path path = Paths.get(fishingDefectDocument.getImage());
                byte[] fileContent = Files.readAllBytes(path);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                fishingDefectDocument.setImage(encodedString);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            res.setStatus(0);
            res.setMessage("Success");
            res.setData(fishingDefectDocument);
        } else {
            res.setStatus(1);
            res.setMessage("No data found");
        }
        return res;
    }


}