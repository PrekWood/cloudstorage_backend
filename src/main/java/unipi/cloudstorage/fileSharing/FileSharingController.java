package unipi.cloudstorage.fileSharing;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.fileSharing.enums.ShareableType;
import unipi.cloudstorage.fileSharing.requests.CreateShareableLinkRequest;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.folder.exceptions.FolderZipCouldNotBeCreated;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.shared.security.FileEncoder;
import unipi.cloudstorage.shared.security.jwt.JWTSecret;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

import static unipi.cloudstorage.fileSharing.enums.ShareableType.FILE;
import static unipi.cloudstorage.fileSharing.enums.ShareableType.FOLDER;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class FileSharingController extends ResponseHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final UserFileService userFileService;
    private final FileManager fileManager;
    private final FileEncoder fileEncoder;
    public static final int TOKEN_TIME_TO_LIVE = 60 * 60 * 1000 * 24 * 14;

    @CrossOrigin
    @PostMapping("share" )
    public ResponseEntity<?> createShareableLink(
            @RequestBody CreateShareableLinkRequest request
    ) {
        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        if(!request.getType().equals(FOLDER) && !request.getType().equals(FILE)){
            return createErrorResponse("Wrong sharable type");
        }

        String tokenBodyJsonString;
        if(request.getType().equals(FOLDER)){
            // Search file by id
            Long idFolder = request.getObjectId();
            Folder folder = null;
            try {
                folder = folderService.getFolderById(idFolder);
                if (folder == null) {
                    throw new FolderNotFoundException("Folder not found");
                }
            } catch (FolderNotFoundException e) {
                return createErrorResponse(e.getMessage());
            }

            // Create zip
            String zipLocation;
            File zipFile = null;
            try {
                zipLocation = folderService.createZip(folder);
                zipFile = new File(zipLocation);
                if(!zipFile.exists()){
                    throw new FolderZipCouldNotBeCreated("");
                }
            } catch (FolderZipCouldNotBeCreated e) {
                return createErrorResponse(e.getMessage());
            }

            // Access token creation
            HashMap<String, Object> tokenBody = new HashMap<>();
            tokenBody.put("type","folder");
            tokenBody.put("idObject",idFolder);
            try {
                tokenBodyJsonString = new ObjectMapper().writeValueAsString(tokenBody);
            } catch (JsonProcessingException e) {
                return createErrorResponse("Could not create token");
            }

        }else{

            // Search file by id
            Long idFile = request.getObjectId();
            UserFile file = null;
            try {
                file = userFileService.getFileById(idFile);
                if (file == null) {
                    throw new UserFileNotFound();
                }
            } catch (UserFileNotFound e) {
                return createErrorResponse("File not found" );
            }

            // Access token creation
            HashMap<String, Object> tokenBody = new HashMap<>();
            tokenBody.put("type","file");
            tokenBody.put("idObject",file.getId());
            try {
                tokenBodyJsonString = new ObjectMapper().writeValueAsString(tokenBody);
            } catch (JsonProcessingException e) {
                return createErrorResponse("Could not create token");
            }

        }

        // Create a jwt accessToken
        Algorithm algorithm = Algorithm.HMAC256(JWTSecret.getJWTSecret().getBytes());
        String accessToken = JWT.create()
                .withSubject(tokenBodyJsonString)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_TIME_TO_LIVE))
                .withIssuer(loggedInUser.getEmail())
                .sign(algorithm);

        // Format response
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("accessToken", accessToken);
        return createSuccessResponse(responseBody);
    }

    @CrossOrigin
    @GetMapping("share" )
    public ResponseEntity<?> downloadFileByLink(
            @RequestParam String accessToken
    ) {

        if(Validate.isEmpty(accessToken)){
            return createErrorResponse("Invalid token");
        }

        // Decode JWT
        DecodedJWT decodedJWT = decodeJWT(accessToken);
        if(decodedJWT == null || decodedJWT.getExpiresAt().before(new Date())) {
            return createErrorResponse("Invalid token");
        }
        String decodedTokenJson = decodedJWT.getSubject();

        // Parse body to Hashmap
        HashMap<String, String> tokenBody = null;
        try {
            tokenBody = new ObjectMapper().readValue(decodedTokenJson, HashMap.class);
            if(tokenBody == null){
                throw new Exception("");
            }
        } catch (Exception e) {
            return createErrorResponse("Invalid token");
        }

        if(!tokenBody.get("type").equals("file") && !tokenBody.get("type").equals("folder")){
            return createErrorResponse("Invalid token");
        }

        // Get sender info
        User user = userService.loadUserByUsername(decodedJWT.getIssuer());
        if(user == null){
            return createErrorResponse("Sender couldn't be specified");
        }

        // Get file info
        String filePathString =null;
        HashMap<String, Object> presentedFile = null;
        if(tokenBody.get("type").equals("file")){

            // Search for file
            Long idFile = Long.parseLong(String.valueOf(tokenBody.get("idObject")));
            UserFile file = null;
            try {
                file = userFileService.getFileById(idFile);
                if (file == null) {
                    throw new UserFileNotFound();
                }
            } catch (UserFileNotFound e) {
                return createErrorResponse("File not found" );
            }
            presentedFile = userFileService.present(file);
            filePathString = userFileService.getRealFilePath(file);

        }else if(tokenBody.get("type").equals("folder")){

            // Search folder by id
            Long idFolder = Long.parseLong(String.valueOf(tokenBody.get("idObject")));
            Folder folder = null;
            try {
                folder = folderService.getFolderById(idFolder);
                if (folder == null) {
                    throw new FolderNotFoundException("");
                }
            } catch (FolderNotFoundException e) {
                return createErrorResponse("File not found" );
            }

            presentedFile = folderService.present(folder);
            filePathString = folderService.getFolderZipLocation(folder)+folder.getName()+".zip";
        }

        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(filePathString));
        } catch (IOException e) {
            return createErrorResponse("Something went wrong please try again" );
        }

        // Generate digital signature
        byte[] digitalSignature = fileEncoder.generateDigitalSignature(fileBytes);

        // Transform bytes to hex
        String digitalSignatureHex = fileEncoder.byteArrayToHexString(digitalSignature);

        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("type", tokenBody.get("type"));
        responseBody.put("file", presentedFile);
        responseBody.put("sender", userService.present(user));
        responseBody.put("fileBytes", fileBytes);
        responseBody.put("digitalSignature", digitalSignatureHex);
        return createSuccessResponse(responseBody);

    }

    private DecodedJWT decodeJWT(String accessToken){
        Algorithm algorithm = Algorithm.HMAC256(JWTSecret.getJWTSecret().getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        try{
            return verifier.verify(accessToken);
        } catch (Exception e){
            return null;
        }
    }

}
