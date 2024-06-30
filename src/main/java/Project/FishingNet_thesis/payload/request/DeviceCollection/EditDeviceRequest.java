package Project.FishingNet_thesis.payload.request.DeviceCollection;

import lombok.Data;

@Data
public class EditDeviceRequest {
    private String deviceID;
    private String newDeviceName;
}
