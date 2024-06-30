package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.models.UserDocument;
import Project.FishingNet_thesis.payload.response.APIResponse;
import Project.FishingNet_thesis.repository.*;
import Project.FishingNet_thesis.models.FishingDefectDocument;
import Project.FishingNet_thesis.payload.debug.Line_request;
import Project.FishingNet_thesis.payload.debug.RandomDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/debug")
public class DebugController {
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    @Autowired
    FishingDefectRepository fishingDefectRepository;
    @Autowired
    DefectStatisticsRepository defectStatisticsRepository;
    @Autowired
    NotificationController notificationController;
    @Autowired
    DeviceRepository deviceRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FishingDefectController fishingDefectController;

    public Date randomDate(Date fromDate, Date toDate) {
        long start = fromDate.getTime();
        long end = toDate.getTime();
        long random = start + (long) (Math.random() * (end - start));
        return new Date(random);
    }

    public String generateImage(String nameImage, String path, int width, int height) {
        String directory = System.getProperty("user.dir") + path;
        String filePath = Paths.get(directory, nameImage + ".jpg").toString();
        // Create the directory if it does not exist
        File dir = new File(directory);
        if (!dir.exists()) {
            boolean fileCreated = dir.mkdirs();
            if (!fileCreated) {
                System.out.println("Can't create directory");
            } else {
                System.out.println("Create directory success");
            }
        }
        // Generate a random image
        String randomImageUrl = "https://picsum.photos/" + width + "/" + height;
        try {
            URL url = new URL(randomImageUrl);
            Path dest = Paths.get(filePath);
            try (InputStream in = url.openStream()) {
                Files.copy(in, dest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/removeAll-data-debug")
    public APIResponse removeAllData() {
        APIResponse res = new APIResponse();
        // remove all image in imageDB
        File file = new File(System.getProperty("user.dir") + "/imageDB");
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                boolean delSuccess = f.delete();
                if (!delSuccess) {
                    System.out.println("Can't delete file: " + f.getName());
                }
            }
        }
        // remove all image in imageProfile
        File fileProfile = new File(System.getProperty("user.dir") + "/imageProfile");
        File[] filesProfile = fileProfile.listFiles();
        if (filesProfile != null) {
            for (File f : filesProfile) {
                boolean delSuccess = f.delete();
                if (!delSuccess) {
                    System.out.println("Can't delete file: " + f.getName());
                }
            }
        }
        // remove all data in database
        fishingDefectRepository.deleteAll();
        defectStatisticsRepository.deleteAll();
        notificationRepository.deleteAll();
        deviceRepository.deleteAll();
        userRepository.deleteAll();

        res.setStatus(0);
        res.setMessage("Remove all data Success");
        return res;
    }

    @PostMapping("/removeDefect-data-debug")
    public APIResponse removeDefectData() {
        APIResponse res = new APIResponse();
        // remove all image in imageDB
        File file = new File(System.getProperty("user.dir") + "/imageDB");
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                boolean delSuccess = f.delete();
                if (!delSuccess) {
                    System.out.println("Can't delete file: " + f.getName());
                }
            }
        }
        // remove all data in database
        fishingDefectRepository.deleteAll();
        defectStatisticsRepository.deleteAll();

        res.setStatus(0);
        res.setMessage("Remove all defect data Success");
        return res;
    }

    @PostMapping("/random-data-debug")
    public APIResponse randomData(@RequestBody RandomDataRequest randomDataRequest) {
        APIResponse res = new APIResponse();
        Random random = new Random();
        int count = randomDataRequest.getCount();
        while (count > 0) {
            try {
                String uuid = UUID.randomUUID().toString();

                // Generate a random image
                /*String filePath = Paths.get(directory, uuid + ".jpg").toString();

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
                }*/
                String filePath = generateImage(uuid, "/imageDB", 250, 200);

                // Generate a random date
                /*// Set the start date (e.g., year 2000)
                gc.set(randomDataRequest.getYear(),
                        randomDataRequest.getMonth(),
                        randomDataRequest.getDay());
                long startDate = gc.getTime().getTime();
                // Set the end date (e.g., current date)
                Date currentDate = new Date();
                gc.setTime(currentDate);
                long endDate = gc.getTime().getTime();
                // Generate a random long value between the start and end dates
                long randomDate = startDate + (long) (random.nextDouble() * (endDate - startDate));
                // Create a new date using the random long value
                Date randomDateResult = new Date(randomDate);

                System.out.println("Random data ...");
                System.out.println("Start Date: " + startDate);
                System.out.println("End Date: " + endDate);
                System.out.println("Random Date: " + randomDateResult);*/
                Date randomDateResult = randomDate(randomDataRequest.getFromDate(), new Date());

                FishingDefectDocument fishingDefectDocument = new FishingDefectDocument();
                fishingDefectController.increaseDefectCount(randomDateResult, uuid);
                fishingDefectDocument.setId(uuid);
                fishingDefectDocument.setTimestamp(randomDateResult);
                fishingDefectDocument.setFilename(uuid);
                boolean isManaged = random.nextBoolean();
                fishingDefectDocument.setIsmanaged(isManaged);
                if (isManaged) {
                    boolean status = random.nextBoolean();
                    fishingDefectDocument.setStatus(status ? "Activated" : "Deactivated");
                    try {
                        fishingDefectDocument.setUserDocument(userRepository.findAll().get(random.nextInt(userRepository.findAll().size())));
                    } catch (Exception e) {
                        //create new user
                        UserDocument userDocument = new UserDocument(UUID.randomUUID().toString(), "user" + random.nextInt(1000), "user" + random.nextInt(1000) + "@gmail.com", "123456");
                        userRepository.save(userDocument);
                        fishingDefectDocument.setUserDocument(userDocument);
                    }
                }
                fishingDefectDocument.setImage(filePath);

                fishingDefectRepository.save(fishingDefectDocument);
                count--;
                logger.info(fishingDefectDocument.toString());
            } catch (Exception e) {
                res.setStatus(500);
                res.setMessage(e.getMessage());
            }
            res.setStatus(0);
            res.setMessage("Random data Success");

        }
        return res;
    }

    @PostMapping("/Line-send-debug")
    public APIResponse lineSend(@RequestBody Line_request lineRequest) {
        APIResponse res = new APIResponse();
        try {
            fishingDefectController.sendToLineNotify(lineRequest.getToken(), lineRequest.getMessage(), lineRequest.getImage());
            res.setStatus(0);
            res.setMessage("Line send Success");
        } catch (Exception e) {
            res.setStatus(500);
            res.setMessage(e.getMessage());
        }
        return res;
    }

}
