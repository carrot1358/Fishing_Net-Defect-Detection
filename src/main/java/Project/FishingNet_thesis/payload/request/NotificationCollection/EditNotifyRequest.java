package Project.FishingNet_thesis.payload.request.NotificationCollection;

import lombok.Data;

@Data
public class EditNotifyRequest {
    private String oldNotifyToken;
    private String newNotifyToken;
    private String notifyName;
    private Boolean isSendLink;
    private Boolean isSendImage;
}
