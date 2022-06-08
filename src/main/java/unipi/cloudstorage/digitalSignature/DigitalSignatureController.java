package unipi.cloudstorage.digitalSignature;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.requests.DigitalSignatureValidationRequest;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.shared.security.FileEncoder;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;

import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class DigitalSignatureController extends ResponseHandler {

    private final FileEncoder fileEncoder;

    @CrossOrigin
    @PostMapping("validate-signature" )
    public ResponseEntity<?> validateDigitalSignature(@RequestBody DigitalSignatureValidationRequest request) {
        String fileBytesString = request.getFile();
        String digitalSignature = request.getDigitalSignature();
        if (Validate.isEmpty(fileBytesString) || Validate.isEmpty(digitalSignature)) {
            return createErrorResponse("Please fill in all fields" );
        }

        // Decode hex to bytes
        byte[] digitalSignatureBytes;
        byte[] fileBytes;
        try{
            digitalSignatureBytes = fileEncoder.hexStringToByteArray(digitalSignature);
            fileBytes = fileEncoder.hexStringToByteArray(fileBytesString);
        }catch(Exception e){
            return createErrorResponse("Invalid digital signature format");
        }

        // Singature length validation
        if (digitalSignatureBytes.length != 256) {
            return createErrorResponse("Invalid digital signature" );
        }

        // Generate digital signature
        if (!fileEncoder.validateDigitalSignature(fileBytes, digitalSignatureBytes)) {
            return createErrorResponse("Invalid digital signature" );
        }

        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("fileBytes", fileBytes);
        return createSuccessResponse(responseBody);
    }
}
