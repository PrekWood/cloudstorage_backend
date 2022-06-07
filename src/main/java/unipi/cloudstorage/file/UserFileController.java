package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.file.requests.UpdateUserFileRequest;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
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
import java.util.HashMap;
import java.util.List;

import static unipi.cloudstorage.folder.Folder.ROOT_FOLDER;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class UserFileController extends ResponseHandler {

    private final UserFileService userFileService;
    private final UserService userService;
    private final FileManager fileManager;
    private final FileEncoder fileEncoder;
    private final FolderService folderService;

    @CrossOrigin
    @PostMapping("file" )
    public ResponseEntity<?> uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long folderId
    ) {


        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return createErrorResponse("Couldn't get the file's name" );
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

        // get file extension
        String fileExtension = fileManager.getFileExtension(fileName);

        // get root folder
        Folder folder;
        System.out.println(folderId);
        if(folderId.equals(ROOT_FOLDER)){
            folder = folderService.getRootFolderOfUser(loggedInUser);
        }else{
            try {
                folder = folderService.getFolderById(folderId);
            } catch (FolderNotFoundException e) {
                return createErrorResponse("Couldn't find folder" );
            }
        }

        // create img in db
        UserFile userFile = new UserFile();
        userFile.setUser(loggedInUser);
        userFile.setFilePath("tmp/" + tmpFileName);
        userFile.setExtension(fileExtension);
        userFile.setName(fileName);
        userFile.setFolder(folder);
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

        return createSuccessResponse(userFileService.present(userFile));
    }

    @CrossOrigin
    @GetMapping("file/{idFile}/download" )
    public ResponseEntity<?> downloadFile(@PathVariable Long idFile) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
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
        if (!userFileService.checkAccess(loggedInUser, file)) {
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
    @GetMapping("file/{idFile}/" )
    public ResponseEntity<?> getFileDetails(@PathVariable Long idFile) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
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
        if (!userFileService.checkAccess(loggedInUser, file)) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to this file" );
        }

        return createSuccessResponse(userFileService.present(file));
    }

    @CrossOrigin
    @DeleteMapping("file/{idFile}/" )
    public ResponseEntity<?> deleteFile(@PathVariable Long idFile) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
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
        if (!userFileService.checkAccess(loggedInUser, file, "delete")) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to delete this file" );
        }



        // Try to delete file
        try {
            userFileService.delete(file);
        } catch (UserFileNotFound userFileNotFound) {
            return createErrorResponse("File not found" );
        }

        return createSuccessResponse();
    }

    @CrossOrigin
    @PutMapping("file/{idFile}/" )
    public ResponseEntity<?> updateFile(
            @RequestBody UpdateUserFileRequest request,
            @PathVariable Long idFile
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
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
        if (!userFileService.checkAccess(loggedInUser, file, "edit")) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You don't have access to edit this file" );
        }

        // Try to update file
        if(request.getFavorite() != null){
            file.setFavorite(request.getFavorite());
        }
        if(!Validate.isEmpty(request.getFileName())){
            file.setName(request.getFileName());
        }

        try {
            userFileService.update(file);
        } catch (UserFileNotFound userFileNotFound) {
            return createErrorResponse("File not found" );
        }

        return createSuccessResponse(userFileService.present(file));
    }

    @CrossOrigin
    @GetMapping(value = {"files", "files/{orderBy}", "files/{orderBy}/{orderWay}"})
    public ResponseEntity<?> getFiles(
            @PathVariable(required = false) String orderBy,
            @PathVariable(required = false) String orderWay,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) Boolean onlyFavorites,
            @RequestParam(required = false) Boolean allFiles,
            @RequestParam(required = false) Boolean onlyShared
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        // Assign default values
        if (onlyFavorites==null || !onlyFavorites) {
            onlyFavorites = null;
        }
        boolean isFolderRoot = false;
        if (folderId.equals(ROOT_FOLDER)) {
            isFolderRoot = true;
            folderId = folderService.getRootFolderOfUser(loggedInUser).getId();
        }
        if(orderBy == null){
            orderBy = "dateAdd";
        }
        if(orderWay == null){
            orderWay = "desc";
        }
        if(allFiles == null){
            allFiles = false;
        }
        if(onlyShared == null){
            onlyShared = false;
        }

        List<UserFile> filesList;
        if(onlyShared){
            filesList = userFileService.getSharedFiles(
                loggedInUser.getId(),
                isFolderRoot ? null : folderId,
                searchQuery,
                orderBy,
                orderWay
            );
            return createSuccessResponse(userFileService.present(filesList, true));
        }

        if(Validate.isEmpty(searchQuery)){
            filesList = userFileService.getFiles(
                loggedInUser.getId(),
                folderId,
                onlyFavorites,
                orderBy,
                orderWay,
                allFiles
            );
        }else{
            filesList = userFileService.search(
                loggedInUser.getId(),
                folderId,
                onlyFavorites,
                searchQuery,
                orderBy,
                orderWay,
                allFiles
            );
        }

        return createSuccessResponse(userFileService.present(filesList, true));
    }


}
