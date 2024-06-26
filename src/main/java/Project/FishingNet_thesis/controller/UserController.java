package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.config.ConfigProperties;
import Project.FishingNet_thesis.models.ERole;
import Project.FishingNet_thesis.models.Role;
import Project.FishingNet_thesis.models.TokenResetDocument;
import Project.FishingNet_thesis.payload.request.*;
import Project.FishingNet_thesis.payload.response.APIResponse;
import Project.FishingNet_thesis.repository.RoleRepository;
import Project.FishingNet_thesis.repository.TokenResetRepository;
import Project.FishingNet_thesis.repository.UserRepository;
import Project.FishingNet_thesis.security.service.EmailService.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import Project.FishingNet_thesis.models.UserDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    TokenResetRepository tokenResetRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    private ConfigProperties configProperties;

    @PostMapping("/signup")
    public APIResponse registerUser(@RequestBody SignUpRequest signUpRequest) {
        APIResponse res = new APIResponse();
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            res.setStatus(400);
            res.setMessage("Error: Username is already taken!");
            return res;
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            res.setStatus(400);
            res.setMessage("Error: Email is already in use!");
            return res;
        }
        if (signUpRequest.getUsername().length() < 3 || signUpRequest.getUsername().length() > 20) {
            res.setStatus(400);
            res.setMessage("Error: Username must be between 3 and 20 characters!");
            return res;
        }
        if (signUpRequest.getPassword().length() < 6 || signUpRequest.getPassword().length() > 40) {
            res.setStatus(400);
            res.setMessage("Error: Password must be between 6 and 40 characters!");
            return res;
        }
        // Check if the email is valid
        if (!signUpRequest.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            res.setStatus(400);
            res.setMessage("Error: Email is invalid!");
            return res;
        }
        // Create new user's account
        UserDocument user = new UserDocument(
                UUID.randomUUID().toString(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword());

        // Set user role to ROLE_ANONYMOUS
        Role anonymousRole = roleRepository.findByName(ERole.ROLE_ANONYMOUS)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ROLE_ANONYMOUS);
                    return roleRepository.save(role);
                });

        user.getRoles().add(anonymousRole);

        userRepository.save(user);

        res.setStatus(200);
        res.setMessage("User registered successfully!");
        return res;


    }

    @PostMapping("/singin")
    public APIResponse loginUser(@RequestBody LoginRequest loginRequest) {
        APIResponse res = new APIResponse();
        if (!userRepository.existsByUsername(loginRequest.getUsername())) {
            res.setStatus(400);
            res.setMessage("Error: Username does not exist!");
            return res;
        }
        if (!userRepository.existsByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword())) {
            res.setStatus(400);
            res.setMessage("Error: Password is incorrect!");
        } else {
            Optional<UserDocument> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
            if (optionalUser.isPresent()) {
                UserDocument user = optionalUser.get().withoutPassword();
                res.setStatus(200);
                res.setMessage("Login successfully!");
                res.setData(user);
            } else {
                res.setStatus(400);
                res.setMessage("Error: User not found!");
            }
        }
        return res;
    }

    @PostMapping("/logout")
    public APIResponse logoutUser() {
        APIResponse res = new APIResponse();
        res.setStatus(200);
        res.setMessage("Logout successfully!");
        return res;
    }

    @PostMapping("/forgot-password")
    public APIResponse forgotPassword(@RequestBody ForgetPasswordRequest forgetRequest) {
        APIResponse res = new APIResponse();
        String email = forgetRequest.getEmail();
        Optional<UserDocument> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            res.setStatus(400);
            res.setMessage("Error: Email does not exist!");
        } else {
            UserDocument user = optionalUser.get();
            String token = UUID.randomUUID() + userRepository.findByEmail(email).get().getId();
            // Set the expiration time for the token
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(new Date()); // sets calendar time/date
            cal.add(Calendar.MINUTE, 15); // adds 15 minutes
            Date expirationDate = cal.getTime(); // returns new date object, 15 minutes in the future
            // Create a TokenResetDocument object and set its fields
            TokenResetDocument tokenResetDocument = new TokenResetDocument();
            tokenResetDocument.setToken(token);
            tokenResetDocument.setUser(user);
            tokenResetDocument.setExpirationDate(expirationDate);
            // Save the TokenResetDocument object to the database
            tokenResetRepository.save(tokenResetDocument);

            // send an email to the user with the reset link that includes the token
            String resetLink = configProperties.getFEbaseUrl() + "/reset-password?token=" + token;
            emailService.sendMail(user.getEmail(), "Password Reset Request",
                    "To reset your password, click the following link: " + resetLink);


            res.setStatus(200);
            res.setMessage("Sent an email to the user with the reset link");
            res.setData("Expiration Date : " + expirationDate);
        }
        return res;
    }

    @PostMapping("/reset-password")
    public APIResponse resetPassword(@RequestBody ResetPasswordRequest requestPass) {
        APIResponse res = new APIResponse();

        // find the token in the database
        TokenResetDocument tokenResetDocument = tokenResetRepository.findByToken(requestPass.getToken());
        // if the token is found and it has not expired, get the user's email
        if (tokenResetDocument != null && tokenResetDocument.getExpirationDate().after(new Date())) {
            UserDocument user = tokenResetDocument.getUser();
            // update the user's password
            user.setPassword(requestPass.getNewPassword());
            userRepository.save(user);
            // delete the token from the database
            tokenResetRepository.delete(tokenResetDocument);
            res.setStatus(200);
            res.setMessage("Password reset successfully!");
        } else {
            res.setStatus(400);
            res.setMessage("Error: Token not found or expired!");
        }
        return res;
    }

    @PostMapping("/change-password")
    public APIResponse changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        APIResponse res = new APIResponse();
        UserDocument user = userRepository.findById(changePasswordRequest.getUserId()).orElse(null);
        if (user == null) {
            res.setStatus(400);
            res.setMessage("Error: User not found!");
        } else {
            if (user.getPassword().equals(changePasswordRequest.getOldPassword())) {
                user.setPassword(changePasswordRequest.getNewPassword());
                userRepository.save(user);
                res.setStatus(200);
                res.setMessage("Password changed successfully!");
            } else {
                res.setStatus(400);
                res.setMessage("Error: Old password is incorrect!");
            }
        }
        return res;
    }

    @PostMapping("/update-profile")
    public APIResponse updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        APIResponse res = new APIResponse();
        UserDocument user = userRepository.findById(updateProfileRequest.getUserId()).orElse(null);
        if (user == null) {
            res.setStatus(400);
            res.setMessage("Error: User not found!");
        } else {
            user.setUsername(updateProfileRequest.getUsername());
            user.setEmail(updateProfileRequest.getEmail());
            user.setFirstname(updateProfileRequest.getFirstname());
            user.setLastname(updateProfileRequest.getLastname());
            user.setAddress(updateProfileRequest.getAddress());
            user.setPhone(updateProfileRequest.getPhone());
            userRepository.save(user);
            res.setStatus(200);
            res.setMessage("Profile updated successfully!");
        }
        return res;
    }

    @PostMapping("/update-image-profile")
    public APIResponse updateImageProfile(@RequestParam("userId") String userId, @RequestParam("imageProfile") MultipartFile imageProfile) {
        APIResponse res = new APIResponse();
        UserDocument user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            res.setStatus(400);
            res.setMessage("Error: User not found!");
        } else {
            try {
                String directory = System.getProperty("user.dir") + "/imageProfile";
                String filePath = Paths.get(directory, user.getId() + ".jpg").toString();
                File dest = new File(filePath);
                imageProfile.transferTo(dest);
                user.setImageProfileName(user.getId() + ".jpg");
                userRepository.save(user);
                res.setStatus(200);
                res.setMessage("Image profile updated successfully!");
            } catch (Exception e) {
                res.setStatus(400);
                res.setMessage("Error: " + e.getMessage());
            }
        }
        return res;
    }

    @GetMapping("/get-image-profile")
    public APIResponse getImageProfile(@RequestBody String userId) {
        APIResponse res = new APIResponse();
        UserDocument user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            res.setStatus(400);
            res.setMessage("Error: User not found!");
        } else {
            String directory = System.getProperty("user.dir") + "/imageProfile";
            String filePath = Paths.get(directory, user.getId() + ".jpg").toString();
            File file = new File(filePath);
            if (file.exists()) {
                res.setStatus(200);
                res.setMessage("Image profile found!");
                res.setData(file);
            } else {
                res.setStatus(400);
                res.setMessage("Error: Image profile not found!");
            }
        }
        return res;
    }
}
