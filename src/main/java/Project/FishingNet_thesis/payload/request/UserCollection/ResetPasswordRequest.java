package Project.FishingNet_thesis.payload.request.UserCollection;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}
