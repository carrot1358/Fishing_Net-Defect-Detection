package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.service.CountOfdefectRepository;
import Project.FishingNet_thesis.table.CountOfdefect;
import Project.FishingNet_thesis.websocket.handler.MyWebSocketHandler;
import Project.FishingNet_thesis.service.FishingDefectRepository;
import Project.FishingNet_thesis.table.FishingDefect;

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
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@RestController
@RequestMapping("/api/fishing-defect")
@CrossOrigin(origins = "*")
public class FishingDefectController {

    private final MyWebSocketHandler myWebSocketHandler;
    private final FishingDefectRepository fishingDefectRepository;
    private final CountOfdefectRepository countOfdefectRepository;

    @Autowired
    public FishingDefectController(MyWebSocketHandler myWebSocketHandler, FishingDefectRepository fishingDefectRepository, CountOfdefectRepository countOfdefectRepository) {
        this.myWebSocketHandler = myWebSocketHandler;
        this.fishingDefectRepository = fishingDefectRepository;
        this.countOfdefectRepository = countOfdefectRepository;
    }

    private void increaseDefectCount(Date date) {
        date = new Date(date.getYear(), date.getMonth(), 1);
        CountOfdefect countOfdefect = countOfdefectRepository.findByDate(date);
        if (countOfdefect == null) {
            countOfdefect = new CountOfdefect();
            countOfdefect.setDate(date);
            countOfdefect.setDefectCount(1);
        } else {
            countOfdefect.setDefectCount(countOfdefect.getDefectCount() + 1);
        }
        countOfdefectRepository.save(countOfdefect);
    }
    private void increaseActivateCount(Date date) {
        date = new Date(date.getYear(), date.getMonth(), 1);
        CountOfdefect countOfdefect = countOfdefectRepository.findByDate(date);
        if (countOfdefect == null) {
            countOfdefect = new CountOfdefect();
            countOfdefect.setDate(date);
            countOfdefect.setActivateCount(1);
        } else {
            countOfdefect.setActivateCount(countOfdefect.getActivateCount() + 1);
        }
        countOfdefectRepository.save(countOfdefect);
    }
    private void increaseDeactivateCount(Date date) {
        date = new Date(date.getYear(), date.getMonth(), 1);
        CountOfdefect countOfdefect = countOfdefectRepository.findByDate(date);
        if (countOfdefect == null) {
            countOfdefect = new CountOfdefect();
            countOfdefect.setDate(date);
            countOfdefect.setDeactivateCount(1);
        } else {
            countOfdefect.setDeactivateCount(countOfdefect.getDeactivateCount() + 1);
        }
        countOfdefectRepository.save(countOfdefect);
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

//        CloseableHttpResponse response = httpClient.execute(httpPost);
        httpClient.close();
    }

    @PostMapping("/upload-image")
    public APIResponse uploadFishingDefect(@RequestParam("file") MultipartFile file) {
        APIResponse res = new APIResponse();
        try {
            String uuid = UUID.randomUUID().toString();
            String directory = System.getProperty("user.dir") + "/imageDB";
            String filePath = Paths.get(directory, uuid + ".jpg").toString();

            // Create the directory if it does not exist
//            File dir = new File(directory);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }

            // Save the image to the directory
            File dest = new File(filePath);
            file.transferTo(dest);

            // Use the destination file from now on
            File newFile = new File(filePath);

            FishingDefect fishingDefect = new FishingDefect();
            Date currentDate = new Date();
            increaseDefectCount(currentDate);
            fishingDefect.setId(uuid);
            fishingDefect.setTimestamp(currentDate);
            fishingDefect.setFilename(uuid);
            fishingDefect.setIsmanaged(false);

            // Save the file path to the database instead of the Blob
            fishingDefect.setImage(filePath);

            fishingDefectRepository.save(fishingDefect);
            res.setStatus(200);
            res.setMessage("Success");

            // Convert the FishingDefect object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(fishingDefect);

            // Send message and image to Line Notify
            String lineNotifyToken = "5sOmbqMoui5pDCdI3pAxFGV5z88sGuVbn17ArYOEcJ0";
            String message = "New fishingNet defect uploaded: ";
            sendToLineNotify(lineNotifyToken, message, newFile);

            // Create URLs for "Activate" and "Deactivate" messages
            String activateUrl = "http://yourserver.com/api/fishing-defect/activate?id=" + uuid;
            String deactivateUrl = "http://yourserver.com/api/fishing-defect/deactivate?id=" + uuid;

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

    // Remove all data for debugging purposes
    @PostMapping("/removeAll-data-debug")
    public APIResponse removeAllData() {
        APIResponse res = new APIResponse();
        // remove all image in imageDB
        File file = new File(System.getProperty("user.dir") + "/imageDB");
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        fishingDefectRepository.deleteAll();
        res.setStatus(0);
        res.setMessage("Remove all data Success");
        return res;
    }

    @GetMapping("/activate")
    public APIResponse activate(@RequestParam("id") String id) {
        APIResponse res = new APIResponse();
        fishingDefectRepository.findById(id).ifPresent(fishingDefect -> {
            if (!fishingDefect.isIsmanaged()) {
                Date date = fishingDefectRepository.findById(id).get().getTimestamp();
                increaseActivateCount(date);
                fishingDefect.setIsmanaged(true);
                fishingDefectRepository.save(fishingDefect);
                String Message = "{" +
                        "\"message\": \"Activated\"," +
                        "\"id_image\": \"" + id + "\"," +
                        "\"date\": \"" + fishingDefect.getTimestamp() + "\"" +
                        "}";
                Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        myWebSocketHandler.sendMessage(session, Message);
                    }
                }
                res.setStatus(0);
                res.setMessage("Send  Activate  Success");
            } else {
                String Mesage = "{" +
                        "\"message\": \"This defect is already been managed.\"," +
                        "\"id_image\": \"" + id + "\"," +
                        "\"date\": \"" + fishingDefect.getTimestamp() + "\"" +
                        "}";
                Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        myWebSocketHandler.sendMessage(session, Mesage);
                    }
                }
                res.setStatus(1);
                res.setMessage("This defect is already been managed.");

            }
        });
        return res;
    }

    @GetMapping("/deactivate")
    public APIResponse deactivate(@RequestParam("id") String id) {
        APIResponse res = new APIResponse();
        fishingDefectRepository.findById(id).ifPresent(fishingDefect -> {
            if (!fishingDefect.isIsmanaged()) {
                Date date = fishingDefectRepository.findById(id).get().getTimestamp();
                increaseDeactivateCount(date);
                fishingDefect.setIsmanaged(true);
                fishingDefectRepository.save(fishingDefect);
                String Message = "{" +
                        "\"message\": \"Deactivated\"," +
                        "\"id_image\": \"" + id + "\"," +
                        "\"date\": \"" + fishingDefect.getTimestamp() + "\"" +
                        "}";
                Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        myWebSocketHandler.sendMessage(session, Message);
                    }
                }
                res.setStatus(0);
                res.setMessage("Send  Deactivated  Success");
            } else {
                String Mesage = "{" +
                        "\"message\": \"This defect is already been managed.\"," +
                        "\"id_image\": \"" + id + "\"," +
                        "\"date\": \"" + fishingDefect.getTimestamp() + "\"" +
                        "}";
                Collection<WebSocketSession> sessions = myWebSocketHandler.getSessions();
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        myWebSocketHandler.sendMessage(session, Mesage);
                    }
                }
                res.setStatus(1);
                res.setMessage("This defect is already been managed.");

            }
        });
        return res;
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
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        return fishingDefect;
    }

    @GetMapping("/get_alldata")
    public APIResponse getAllData() {
        APIResponse res = new APIResponse();
        List<FishingDefect> fishingDefectdata = fishingDefectRepository.findAll();
        for (FishingDefect fishingDefect : fishingDefectdata) {
            try {
                Path path = Paths.get(fishingDefect.getImage());
                byte[] fileContent = Files.readAllBytes(path);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                fishingDefect.setImage(encodedString);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        res.setStatus(0);
        res.setMessage("Success");
        res.setData(fishingDefectdata);
        return res;
    }


}