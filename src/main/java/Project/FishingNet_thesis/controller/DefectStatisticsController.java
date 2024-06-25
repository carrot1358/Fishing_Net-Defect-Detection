package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.payload.response.APIResponse;
import Project.FishingNet_thesis.repository.DefectStatisticsRepository;
import Project.FishingNet_thesis.repository.FishingDefectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/defect_statistics")
@CrossOrigin(origins = "*")
public class DefectStatisticsController {
    @Autowired
    private DefectStatisticsRepository defectStatisticsRepository;
    @Autowired
    private FishingDefectRepository fishingDefectRepository;

    @PostMapping("/get-all-statistics")
    public APIResponse getAllStatistics() {
        APIResponse res = new APIResponse();
        res.setStatus(0);
        res.setMessage("Get all statistics success");
        res.setData(defectStatisticsRepository.findAll());
        return res;
    }

    @PostMapping("/get-statistics-by-date")
    public APIResponse getStatisticsByDate(@RequestParam Date date) {
        APIResponse res = new APIResponse();
        res.setStatus(0);
        res.setMessage("Get statistics by date success");
        res.setData(defectStatisticsRepository.findByDate(date));
        return res;
    }

    @PostMapping("/get-today-statistics")
    public APIResponse getTodayStatistics() {
        APIResponse res = new APIResponse();
        res.setStatus(0);
        res.setMessage("Get today statistics success");
        res.setData(defectStatisticsRepository.findByDate(new Date()));
        return res;
    }

    @PostMapping("/get-today-data")
    public APIResponse getTodayData() {
        APIResponse res = new APIResponse();
        res.setStatus(0);
        res.setMessage("Get today data success");
        res.setData(fishingDefectRepository.findByTimestamp(new Date()));
        return res;
    }
}
