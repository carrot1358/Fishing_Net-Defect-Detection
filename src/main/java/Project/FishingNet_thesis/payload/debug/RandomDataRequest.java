package Project.FishingNet_thesis.payload.debug;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@Data
@Document("random_data_request")
public class RandomDataRequest {
    private Integer count;
    private Integer year;
    private Integer month;
    private Integer day;

    public int defineMonth(){
        return switch (this.month) {
            case 1 -> Calendar.JANUARY;
            case 2 -> Calendar.FEBRUARY;
            case 3 -> Calendar.MARCH;
            case 4 -> Calendar.APRIL;
            case 5 -> Calendar.MAY;
            case 6 -> Calendar.JUNE;
            case 7 -> Calendar.JULY;
            case 8 -> Calendar.AUGUST;
            case 9 -> Calendar.SEPTEMBER;
            case 10 -> Calendar.OCTOBER;
            case 11 -> Calendar.NOVEMBER;
            case 12 -> Calendar.DECEMBER;
            default -> throw new IllegalStateException("Unexpected value: " + this.month);
        };
    }

    public Date getFromDate() {
        LocalDate localDate = LocalDate.of(year, defineMonth() + 1, day);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
