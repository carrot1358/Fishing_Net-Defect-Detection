package Project.FishingNet_thesis.controller;

import Project.FishingNet_thesis.models.NotificationDocument;
import Project.FishingNet_thesis.models.UserDocument;
import Project.FishingNet_thesis.payload.request.NotificationCollection.AddNotificationRequest;
import Project.FishingNet_thesis.payload.request.NotificationCollection.DelNotifyRequest;
import Project.FishingNet_thesis.payload.request.NotificationCollection.EditNotifyRequest;
import Project.FishingNet_thesis.payload.request.NotificationCollection.GetByUserIdRequest;
import Project.FishingNet_thesis.payload.response.APIResponse;
import Project.FishingNet_thesis.repository.NotificationRepository;
import Project.FishingNet_thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public APIResponse addNotification(@RequestBody AddNotificationRequest addNotificationRequest) {
        APIResponse res = new APIResponse();
        NotificationDocument notifyDoc = new NotificationDocument();
        if (userRepository.existsById(addNotificationRequest.getUserId())) {
            if (notificationRepository.findByUserDocumentId(addNotificationRequest.getUserId()).size() >= 5) {
                res.setMessage("User has 5 tokens as maximum");
                res.setStatus(400);
                return res;
            }
            if (notificationRepository.existsByToken(addNotificationRequest.getApiKeys())) {
                res.setMessage("This token is already used");
                res.setStatus(400);
            } else {
                String uuid = UUID.randomUUID().toString();
                UserDocument user = userRepository.findById(addNotificationRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("Error: User is not found."));
                notifyDoc.setId(uuid);
                notifyDoc.setUserDocument(user);
                notifyDoc.setName(addNotificationRequest.getName());
                notifyDoc.setToken(addNotificationRequest.getApiKeys());
                notifyDoc.setIsSendLink(addNotificationRequest.getIsSendLink());
                notifyDoc.setIsSendImage(addNotificationRequest.getIsSendImage());
                notificationRepository.save(notifyDoc);
                res.setStatus(200);
                res.setMessage("Add Notification Success");
            }
        } else {
            res.setMessage("User not found");
            res.setStatus(400);
        }
        return res;
    }

    @PostMapping("/delete")
    public APIResponse deleteNotification(@RequestBody DelNotifyRequest delNotifyRequest) {
        APIResponse res = new APIResponse();
        if (notificationRepository.existsByToken(delNotifyRequest.getNotifyToken())) {
            NotificationDocument notifyDoc = notificationRepository.findByToken(delNotifyRequest.getNotifyToken());
            notificationRepository.delete(notifyDoc);
            res.setStatus(200);
            res.setMessage("Delete Notification Success");
        } else {
            res.setStatus(400);
            res.setMessage("Notification not found");
        }
        return res;
    }

    @PostMapping("/edit")
    public APIResponse editNotification(@RequestBody EditNotifyRequest editNotifyRequest) {
        APIResponse res = new APIResponse();
        NotificationDocument notifyDoc = notificationRepository.findByToken(editNotifyRequest.getOldNotifyToken());
        if (notifyDoc != null) {
            if (editNotifyRequest.getNewNotifyToken().equals(editNotifyRequest.getOldNotifyToken())) {
                notifyDoc.setToken(editNotifyRequest.getOldNotifyToken());
            } else {
                if (notificationRepository.existsByToken(editNotifyRequest.getNewNotifyToken())) {
                    res.setStatus(400);
                    res.setMessage("This token is already used");
                    return res;
                } else {
                    notifyDoc.setToken(editNotifyRequest.getNewNotifyToken());
                }
            }
            notifyDoc.setName(editNotifyRequest.getNotifyName());
            notifyDoc.setIsSendLink(editNotifyRequest.getIsSendLink());
            notifyDoc.setIsSendImage(editNotifyRequest.getIsSendImage());
            notificationRepository.save(notifyDoc);
            res.setStatus(200);
            res.setMessage("Edit Notification Success");
        } else {
            res.setStatus(400);
            res.setMessage("Notification not found");
        }
        return res;

    }

    @PostMapping("/getByUserId")
    public APIResponse getNotificationByUserId(@RequestBody GetByUserIdRequest getByUserIdRequest) {
        APIResponse res = new APIResponse();
        if (userRepository.existsById(getByUserIdRequest.getUserId())) {
            UserDocument user = userRepository.findById(getByUserIdRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            res.setStatus(200);
            res.setMessage("Get Notification Success");
//            res.setData(notificationRepository.findByUserDocument(user));
            res.setData(notificationRepository.findByUserDocumentId(getByUserIdRequest.getUserId()).stream()
                    .map(NotificationDocument::WithOutUserDocument)
                    .collect(Collectors.toList()));
        } else {
            res.setStatus(400);
            res.setMessage("User not found");
        }
        return res;
    }

    @PostMapping("/getAll")
    public APIResponse getAllNotification() {
        APIResponse res = new APIResponse();
        res.setStatus(200);
        res.setMessage("Get Notification Success");
        res.setData(notificationRepository.findAll().stream()
                .map(NotificationDocument::WithOutUserDocument)
                .collect(Collectors.toList()));
        return res;
    }
}
