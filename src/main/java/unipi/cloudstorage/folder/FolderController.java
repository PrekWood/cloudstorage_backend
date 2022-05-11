package unipi.cloudstorage.folder;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.file.requests.DigitalSignatureValidationRequest;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.folder.exceptions.FolderZipCouldNotBeCreated;
import unipi.cloudstorage.folder.requests.FolderCreationRequest;
import unipi.cloudstorage.folder.requests.FolderUpdateRequest;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.shared.security.FileEncoder;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class FolderController extends ResponseHandler {

    private final FolderService folderService;
    private final UserService userService;
    private final UserFileService fileService;
    private final FileManager fileManager;
    private final FileEncoder fileEncoder;

    @CrossOrigin
    @GetMapping("folders" )
    public ResponseEntity<?> getFolders(
        @RequestParam(required = false) Long folderId
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not logged in" );
        }

        if(folderId == null){
            folderId = folderService.getRootFolderOfUser(loggedInUser).getId();
        }

        List<Folder> folderList = folderService.getChildFolders(loggedInUser.getId(), folderId);

        return createSuccessResponse(folderService.present(folderList));
    }

    @CrossOrigin
    @PostMapping("folder" )
    public ResponseEntity<?> createFolder(
            @RequestBody FolderCreationRequest request
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        String name = request.getName();
        Long parentFolderId = request.getParentFolderId();

        // Get parent folder
        Folder parentFolder;
        if(parentFolderId == null){
            parentFolder = folderService.getRootFolderOfUser(loggedInUser);
        }else{
            parentFolder = folderService.getFolderById(loggedInUser.getId(),parentFolderId);
        }

        // Create new folder
        Folder newFolder = new Folder();
        newFolder.setParentFolder(parentFolder);
        newFolder.setUser(loggedInUser);
        newFolder.setName(name);
        newFolder.setDateAdd(LocalDateTime.now());
        folderService.save(newFolder);

        return createSuccessResponse();
    }

    @CrossOrigin
    @PutMapping("folder/{idFolder}/" )
    public ResponseEntity<?> updateFolder(
            @RequestBody FolderUpdateRequest request,
            @PathVariable Long idFolder
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not logged in" );
        }

        // Search file by id
        Folder folder = null;
        try {
            folder = folderService.getFolderById(loggedInUser.getId(),idFolder);
            if (folder == null) {
                throw new FolderNotFoundException("Folder not found");
            }
        } catch (FolderNotFoundException e) {
            return createErrorResponse(e.getMessage());
        }

        // Try to update folder
        if(!Validate.isEmpty(request.getName())){
            folder.setName(request.getName());
        }
        try {
            folderService.update(folder);
        } catch (FolderNotFoundException e) {
            return createErrorResponse(e.getMessage());
        }

        return createSuccessResponse(folderService.present(folder));
    }

    @CrossOrigin
    @GetMapping("folder/{idFolder}/" )
    public ResponseEntity<?> getFolderDetails(
        @PathVariable Long idFolder
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not logged in" );
        }

        // Search file by id
        Folder folder = null;
        try {
            folder = folderService.getFolderById(loggedInUser.getId(), idFolder);
            if (folder == null) {
                throw new FolderNotFoundException("Folder not found");
            }
        } catch (FolderNotFoundException e) {
            return createErrorResponse(e.getMessage());
        }

        return createSuccessResponse(folderService.present(folder));
    }

    @CrossOrigin
    @DeleteMapping("folder/{idFolder}/" )
    public ResponseEntity<?> deleteFolder(
        @PathVariable Long idFolder
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not logged in" );
        }

        // Search file by id
        Folder folder = null;
        try {
            folder = folderService.getFolderById(loggedInUser.getId(), idFolder);
            if (folder == null) {
                throw new FolderNotFoundException("Folder not found");
            }
        } catch (FolderNotFoundException e) {
            return createErrorResponse(e.getMessage());
        }

        // Delete folder
        try {
            folderService.delete(loggedInUser, folder);
        } catch (UserFileNotFound e) {
            return createErrorResponse(e.getMessage());
        }

        return createSuccessResponse(folderService.present(folder));
    }

    @CrossOrigin
    @GetMapping("folder/{idFolder}/download" )
    public ResponseEntity<?> downloadFolder(
        @PathVariable Long idFolder
    ) {

        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not logged in" );
        }

        // Search file by id
        Folder folder = null;
        try {
            folder = folderService.getFolderById(loggedInUser.getId(), idFolder);
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

        // Read zip file bytes
        Path filePath = Paths.get(zipLocation);
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

}
