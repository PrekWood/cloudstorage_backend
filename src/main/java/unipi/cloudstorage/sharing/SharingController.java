package unipi.cloudstorage.sharing;

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
import unipi.cloudstorage.sharing.enums.FilePrivileges;
import unipi.cloudstorage.sharing.enums.ShareableType;
import unipi.cloudstorage.sharing.requests.CreateShareableLinkRequest;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.folder.exceptions.FolderZipCouldNotBeCreated;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.shared.security.FileEncoder;
import unipi.cloudstorage.shared.security.jwt.JWTSecret;
import unipi.cloudstorage.sharing.requests.ShareFileRequest;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;
import unipi.cloudstorage.user.exceptions.UserNotFoundException;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivilegesService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static unipi.cloudstorage.sharing.enums.FilePrivileges.*;
import static unipi.cloudstorage.sharing.enums.ShareableType.FILE;
import static unipi.cloudstorage.sharing.enums.ShareableType.FOLDER;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class SharingController extends ResponseHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final UserFileService userFileService;
    private final UserWithPrivilegesService userWithPrivilegesService;
    private final FileEncoder fileEncoder;
    public static final int TOKEN_TIME_TO_LIVE = 60 * 60 * 1000 * 24 * 14;

    @CrossOrigin
    @PostMapping("share/link" )
    public ResponseEntity<?> createShareableLink(
            @RequestBody CreateShareableLinkRequest request
    ) {
        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        ShareableType shareableType = request.getType();
        if(!shareableType.equals(FOLDER) && !shareableType.equals(FILE)){
            return createErrorResponse("Wrong sharable type");
        }

        String tokenBodyJsonString;
        if(shareableType.equals(FOLDER)){
            // Search folder by id
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
            tokenBody.put("idObject",idFile);
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
    @PostMapping("share/users" )
    public ResponseEntity<?> shareFileWithUsers(
            @RequestBody ShareFileRequest request
    ){
        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        if(!request.getType().equals(FOLDER) && !request.getType().equals(FILE)){
            return createErrorResponse("Invalid sharable type");
        }

        // Search for sharable
        Sharable file = null;
        Long idFile = request.getObjectId();

        try{
            if(request.getType().equals(FOLDER)){
                file = folderService.getFolderById(idFile);
            }else{
                file = userFileService.getFileById(idFile);
            }
        } catch (UserFileNotFound | FolderNotFoundException userFileNotFound) {
            return createErrorResponse("Invalid id");
        }



        List<UserWithPrivileges> sharedWithList = new ArrayList<>();
        for (HashMap<String, Object> userToShareWith : request.getUsers()) {
            /*
            JS constants

            const ONLY_DOWNLOAD = -1;
            const DOWNLOAD_EDIT = -2;
            const DOWNLOAD_DELETE = -3;
            const ALL = -4;
            */
            FilePrivileges privileges;
            System.out.println("privileges bef");

            switch (String.valueOf(userToShareWith.get("privileges"))){
                case "-1":
                    privileges = ONLY_DOWNLOAD;
                    break;
                case "-2":
                    privileges = DOWNLOAD_EDIT;
                    break;
                case "-3":
                    privileges = DOWNLOAD_DELETE;
                    break;
                case "-4":
                    privileges = ALL;
                    break;
                default:
                    return createErrorResponse("Invalid privileges");
            }


            // Search file by id
            Long idUser = ((Number) userToShareWith.get("id")).longValue();

            User userToShareWithObj = null;
            try {
                userToShareWithObj = userService.getUserById(idUser);
                if (userToShareWithObj == null) {
                    throw new UserNotFoundException("User not found");
                }
            } catch (UserNotFoundException e) {
                return createErrorResponse("Something went wrong please try again");
            }

            // Object creation
            UserWithPrivileges sharedWith = new UserWithPrivileges();
            sharedWith.setPrivileges(privileges);
            sharedWith.setUser(userToShareWithObj);
            sharedWith.setSharable(file);
            userWithPrivilegesService.save(sharedWith);

            sharedWithList.add(sharedWith);
        }

        // Update file
        file.setSharedWith(sharedWithList);
        try{
            if(file instanceof UserFile){
                userFileService.update((UserFile)file);
            }else{
                folderService.update((Folder)file);
            }
        } catch (UserFileNotFound | FolderNotFoundException userFileNotFound) {
            return createErrorResponse("Something went wrong please try again");
        }

        return createSuccessResponse();
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
