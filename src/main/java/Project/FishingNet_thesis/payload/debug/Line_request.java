package Project.FishingNet_thesis.payload.debug;

import lombok.Data;

import java.io.File;

@Data
public class Line_request {
    private String token;
    private String message;
    private File image;
}
