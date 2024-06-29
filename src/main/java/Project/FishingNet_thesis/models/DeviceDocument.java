package Project.FishingNet_thesis.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class DeviceDocument {
    @Id
    private String deviceID;
    private String deviceName;
    @NotBlank
    private Boolean deviceStatus; // 1:online, 0:offline
    private String ws_sessionID;
}
