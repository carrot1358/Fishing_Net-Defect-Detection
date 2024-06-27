package Project.FishingNet_thesis.payload.request.UserCollection;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String id;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String address;
    private String phone;
}
