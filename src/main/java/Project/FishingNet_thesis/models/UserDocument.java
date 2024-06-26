package Project.FishingNet_thesis.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashSet;
import java.util.Set;

@Data
@Document("user_document")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserDocument {
    @Id
    private String id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    private String imageProfileName;
    private String firstname;
    private String lastname;
    private String address;
    private String phone;
    @DBRef
    private Set<Role> roles = new HashSet<>();

    public UserDocument(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UserDocument withoutPassword() {
        UserDocument user = new UserDocument(this.getId(), this.getUsername(), this.getEmail(), null);
        // add all other fields
        user.setImageProfileName(this.getImageProfileName());
        user.setFirstname(this.getFirstname());
        user.setLastname(this.getLastname());
        user.setAddress(this.getAddress());
        user.setPhone(this.getPhone());
        user.setRoles(this.getRoles());
        return user;
    }
}
