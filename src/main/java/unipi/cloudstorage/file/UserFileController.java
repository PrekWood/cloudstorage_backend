package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.file.requests.DigitalSignatureValidationRequest;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.shared.security.FileEncoder;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class UserFileController extends ResponseHandler {

    private final UserFileService userFileService;
    private final UserService userService;
    private final FileManager fileManager;
    private final FileEncoder fileEncoder;

    @CrossOrigin
    @PostMapping("file/upload" )
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return createErrorResponse("Couldn't find the file's name" );
        }

        // first upload image to tmp
        HashMap<String, String> tmpFile = userFileService.getTmpFilePath(fileName);
        String tmpFilePath = tmpFile.get("filePath" );
        String tmpFileName = tmpFile.get("fileName" );
        try {
            userFileService.uploadFile(file, tmpFilePath);
        } catch (UserFileCouldNotBeUploaded userFileCouldNotBeUploaded) {
            return createErrorResponse("Couldn't upload file" );
        }

        // get file extention
        String fileExtension = fileManager.getFileExtension(fileName);

        // create img in db
        UserFile userFile = new UserFile();
        userFile.setUser(logedInUser);
        userFile.setFilePath("tmp/" + tmpFileName);
        userFile.setExtension(fileExtension);
        userFile.setFileName(fileName);
        userFile.setDateAdd(LocalDateTime.now());
        userFileService.save(userFile);

        // Each image is stored in a tree like file path that is the concat of the id
        // For example the image with id:12345 will be stored like so /1/2/3/4/5/12345.pdf
        String treeLikeFilePath = userFileService.getTreeLikeFilePath(userFile.getId());

        // Create the file path if it doesn't exist
        (new File(treeLikeFilePath)).mkdirs();

        // Move file to the tree like folder structure
        String idFileString = String.valueOf(userFile.getId());
        String treeLikeFileName = idFileString + "." + fileExtension;
        File treeLikeFile = new File(treeLikeFilePath + "/" + treeLikeFileName);
        (new File(tmpFilePath)).renameTo(treeLikeFile);

        // Update db
        try {
            String treeLikeFileTruncated = (treeLikeFilePath + "/" + treeLikeFileName).replace(
                    fileManager.getUserFilesPath(),
                    ""
            );
            userFile.setFilePath(treeLikeFileTruncated);
            userFileService.update(userFile);
        } catch (UserFileNotFound userFileNotFound) {
            return createErrorResponse("Couldn't update the file" );
        }

        return createSuccessResponse();
    }

    @CrossOrigin
    @PostMapping("file/{idFile}/favorite" )
    public ResponseEntity<?> toggleFavorite(@PathVariable Long idFile) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();

        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        // Search file by id
        UserFile file = null;
        try {
            file = userFileService.getFileById(idFile);
            if (file == null) {
                throw new UserFileNotFound();
            }
        } catch (UserFileNotFound e) {
            return createErrorResponse("File not found" );
        }

        // Block if this file doesn't belong to the user
        if (!file.getUser().getId().equals(logedInUser.getId())) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to this file" );
        }

        // Update favorite
        try {
            file.setFavorite(!file.isFavorite());
            userFileService.update(file);
        } catch (UserFileNotFound userFileNotFound) {
            return createErrorResponse("Something went wrong please try again" );
        }

        return createSuccessResponse(userFileService.present(file));
    }

    @CrossOrigin
    @GetMapping(value = {"files", "files/{orderBy}", "files/{orderBy}/{orderWay}"})
    public ResponseEntity<?> getFiles(
            @PathVariable(required = false) String orderBy,
            @PathVariable(required = false) String orderWay,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean onlyFavorites
    ) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        if (onlyFavorites==null || !onlyFavorites) {
            onlyFavorites = false;
        }

        List<UserFile> filesList = new ArrayList<>();
        if(Validate.isEmpty(searchQuery)){
            if(onlyFavorites){
                System.out.println("getFavoriteFilesByUserId");
                filesList = userFileService.getFavoriteFilesByUserId(logedInUser.getId(), "date_add","desc");
            }else{
                System.out.println("getFilesByUserId");
                filesList = userFileService.getFilesByUserId(logedInUser.getId(), orderBy, orderWay);
            }
        }else{
            if(onlyFavorites){
                System.out.println("searchInFavorites");
                filesList = userFileService.searchInFavorites(logedInUser.getId(), searchQuery, orderBy, orderWay);
            }else{
                System.out.println("search");
                filesList = userFileService.search(logedInUser.getId(), searchQuery, orderBy, orderWay);
            }
        }

        return createSuccessResponse(userFileService.present(filesList));
    }

    @CrossOrigin
    @GetMapping("file/{idFile}/download" )
    public ResponseEntity<?> downloadFile(@PathVariable Long idFile) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        // Search file by id
        UserFile file = null;
        try {
            file = userFileService.getFileById(idFile);
            if (file == null) {
                throw new UserFileNotFound();
            }
        } catch (UserFileNotFound e) {
            return createErrorResponse("File not found" );
        }

        // Block if this file doesn't belong to the user
        if (!file.getUser().getId().equals(logedInUser.getId())) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to this file" );
        }

        // Read file bytes
        String filePathString = fileManager.getUserFilesPath() + file.getFilePath();
        Path filePath = Paths.get(filePathString);
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return createErrorResponse("Something went wrong please try again" );
        }

        // Generate digital signature
        byte[] digitalSignature = fileEncoder.generateDigitalSignature(fileBytes);

        // Transform bytes to hex
        String digitalSignatureHex = fileEncoder.byteArrayToHexString(digitalSignature);

        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("digitalSignature", digitalSignatureHex);
        responseBody.put("file", fileBytes);
        return createSuccessResponse(responseBody);
    }


    @CrossOrigin
    @PostMapping("file/{idFile}/validate-signature" )
    public ResponseEntity<?> validateDigitalSignature(@PathVariable Long idFile, @RequestBody DigitalSignatureValidationRequest request) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        // Search file by id
        UserFile file = null;
        try {
            file = userFileService.getFileById(idFile);
            if (file == null) {
                throw new UserFileNotFound();
            }
        } catch (UserFileNotFound e) {
            return createErrorResponse("File not found" );
        }

        // Block if this file doesn't belong to the user
        if (!file.getUser().getId().equals(logedInUser.getId())) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to this file" );
        }

        String fileBytesString = request.getFile();
        String digitalSignature = request.getDigitalSignature();
        if (Validate.isEmpty(fileBytesString) || Validate.isEmpty(digitalSignature)) {
            return createErrorResponse("Please fill in all fields" );
        }

        // Decode hex to bytes
        byte[] digitalSignatureBytes = fileEncoder.hexStringToByteArray(digitalSignature);
        byte[] fileBytes = fileEncoder.hexStringToByteArray(fileBytesString);

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


    @CrossOrigin
    @DeleteMapping("file/{idFile}/" )
    public ResponseEntity<?> deleteFile(@PathVariable Long idFile) {

        // Get user from token
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        // Search file by id
        UserFile file = null;
        try {
            file = userFileService.getFileById(idFile);
            if (file == null) {
                throw new UserFileNotFound();
            }
        } catch (UserFileNotFound e) {
            return createErrorResponse("File not found" );
        }

        // Block if this file doesn't belong to the user
        if (!file.getUser().getId().equals(logedInUser.getId())) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to this file" );
        }

        // Try to delete file
        try {
            userFileService.delete(file);
        } catch (UserFileNotFound userFileNotFound) {
            return createErrorResponse("File not found" );
        }

        return createSuccessResponse();
    }




}
