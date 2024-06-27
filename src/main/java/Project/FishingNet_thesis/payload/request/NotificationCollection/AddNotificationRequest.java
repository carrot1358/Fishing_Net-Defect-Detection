package Project.FishingNet_thesis.payload.request.NotificationCollection;

import lombok.Data;

@Data
public class AddNotificationRequest {
    private String name;
    private String apiKeys;
    private Boolean isSendLink;
    private Boolean isSendImage;
    private String userId;
}
