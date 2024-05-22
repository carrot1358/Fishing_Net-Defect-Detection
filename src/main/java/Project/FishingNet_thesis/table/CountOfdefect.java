package Project.FishingNet_thesis.table;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("count_of_defect")
public class CountOfdefect {
    private @Id String id;
    private Date date;
    private int defectCount;
    private int activateCount;
    private int deactivateCount;


}
