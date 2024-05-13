package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.handler.MyWebSocketHandler;
import Project.FishingNet_thesis.service.FishingDefectRepository;
import Project.FishingNet_thesis.table.FishingDefect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Paths;
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
    public APIResponse uploadFishingDefect(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename) {
    APIResponse res = new APIResponse();
    try {
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

        FishingDefect fishingDefect = new FishingDefect();
        fishingDefect.setId(UUID.randomUUID().toString());
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