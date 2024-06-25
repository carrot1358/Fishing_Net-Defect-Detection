package Project.FishingNet_thesis.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document("count_of_defect")
public class DefectStatisticsDocument {
    private @Id String id;
    private Date date;
    private int defectCount;
    private int activateCount;
    private int deactivateCount;

    private List<String> defect_id = new ArrayList<>();

    public void addDefect_id(String id){
        this.defect_id.add(id);
    }
}
