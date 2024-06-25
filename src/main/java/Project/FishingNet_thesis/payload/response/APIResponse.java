package Project.FishingNet_thesis.payload.response;

import lombok.Data;

@Data
public class APIResponse {
    private Object data;
    private String message;
    private int status;
}
