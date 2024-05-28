package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.service.CountOfdefectRepository;
import Project.FishingNet_thesis.service.FishingDefectRepository;
import Project.FishingNet_thesis.table.CountOfdefect;
import Project.FishingNet_thesis.table.FishingDefect;
import Project.FishingNet_thesis.table.debug.RandomDataRequest;
import Project.FishingNet_thesis.websocket.handler.MyWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.Date;
import java.util.GregorianCalendar;

@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
public class DebugController {
    @Autowired
    FishingDefectRepository fishingDefectRepository;
    @Autowired
    CountOfdefectRepository countOfdefectRepository;
    @Autowired
    FishingDefectController fishingDefectController;

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

    @PostMapping("/random-data-debug")
    public APIResponse randomData(@RequestBody RandomDataRequest randomDataRequest) {
        APIResponse res = new APIResponse();
        Random random = new Random();
        GregorianCalendar gc = new GregorianCalendar();
        int count = randomDataRequest.getCount();
        while (count > 0) {
            try {
                String uuid = UUID.randomUUID().toString();
                String directory = System.getProperty("user.dir") + "/imageDB";
                String filePath = Paths.get(directory, uuid + ".jpg").toString();

                // Create the directory if it does not exist
                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Generate a random image
                String randomImageUrl = "https://picsum.photos/250/200";
                URL url = new URL(randomImageUrl);
                Path dest = Paths.get(filePath);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, dest);
                }

                // Generate a random date
                // Set the start date (e.g., year 2000)
                gc.set(randomDataRequest.getYear(), randomDataRequest.getMonth(), randomDataRequest.getDay());
                long startDate = gc.getTime().getTime();
                // Set the end date (e.g., current date)
                Date currentDate = new Date();
                gc.setTime(currentDate);
                long endDate = gc.getTime().getTime();
                    // Generate a random long value between the start and end dates
                long randomDate = startDate + (long) (random.nextDouble() * (endDate - startDate));
                    // Create a new date using the random long value
                Date randomDateResult = new Date(randomDate);

                FishingDefect fishingDefect = new FishingDefect();
                fishingDefectController.increaseDefectCount(randomDateResult);
                fishingDefect.setId(uuid);
                fishingDefect.setTimestamp(randomDateResult);
                fishingDefect.setFilename(uuid);
                fishingDefect.setIsmanaged(random.nextBoolean());

                // Save the file path to the database instead of the Blob
                fishingDefect.setImage(filePath);

                fishingDefectRepository.save(fishingDefect);
                count--;
            } catch (Exception e) {
                res.setStatus(500);
                res.setMessage(e.getMessage());
            }
            res.setStatus(0);
            res.setMessage("Random data Success");
        }
        return res;
    }
}
