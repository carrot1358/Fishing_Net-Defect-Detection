package Project.FishingNet_thesis.payload.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String userId;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String address;
    private String phone;
}
