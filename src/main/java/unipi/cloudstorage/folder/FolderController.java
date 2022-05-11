package unipi.cloudstorage.folder;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.folder.requests.FolderCreationRequest;
import unipi.cloudstorage.folder.requests.FolderUpdateRequest;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserService;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class FolderController extends ResponseHandler {

    private final FolderService folderService;
    private final UserService userService;
    private final UserFileService fileService;

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
}
