package Project.FishingNet_thesis.payload.request.UserCollection;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String userId;
    private String oldPassword;
    private String newPassword;
}
