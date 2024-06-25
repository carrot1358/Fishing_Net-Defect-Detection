package Project.FishingNet_thesis.payload.debug;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("random_data_request")
public class RandomDataRequest {
    private Integer count;
    private Integer year;
    private Integer month;
    private Integer day;
}
