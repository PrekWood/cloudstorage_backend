package unipi.cloudstorage.user;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.file.requests.UpdateUserFileRequest;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.user.exceptions.EmailAlreadyBeingUsedUserException;
import unipi.cloudstorage.user.exceptions.UserNotFoundException;
import unipi.cloudstorage.user.requests.RegistrationRequest;
import unipi.cloudstorage.user.requests.UserUpdateRequest;
import unipi.cloudstorage.user.responses.PresentedUserResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/")
public class UserController extends ResponseHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final  UserFileService userFileService;
    private final FileManager fileManager;

    @PostMapping("registration/is-loged-in")
    public ResponseEntity<?> isLoggedIn() {
        // Search for user
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null || !loggedInUser.isPhoneValidated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((new HashMap<>()).put("error", "You are not loged in"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("");
    }

    @GetMapping("user")
    public ResponseEntity<?> getUserDetails(
            @RequestParam(required = false) String email
    ) {
        // Search for user
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null || !loggedInUser.isPhoneValidated()) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in");
        }

        // Return details of loged in user
        if (Validate.isEmpty(email)) {
            return createSuccessResponse(HttpStatus.ACCEPTED, userService.present(loggedInUser));
        }

        // Search user
        User userFromSearch;
        try {
            userFromSearch = userService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            return createErrorResponse("User not found");
        }
        return createSuccessResponse(HttpStatus.ACCEPTED, userService.present(userFromSearch));
    }

    @PostMapping("user")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {

        // Check for empty fields
        if (
                request.getEmail() == null || request.getEmail().equals("") ||
                        request.getPassword() == null || request.getPassword().equals("") ||
                        request.getFirstName() == null || request.getFirstName().equals("") ||
                        request.getLastName() == null || request.getLastName().equals("")
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Please fill in all the nesessary fields"));
        }

        // Create new object
        User newUser = new User(
            request.getEmail(),
            request.getPassword(),
            null,
            request.getFirstName(),
            request.getLastName(),
            UserRole.USER,
            false
        );

        // Try to sign in
        try {
            userService.signUpUser(newUser);
        } catch (EmailAlreadyBeingUsedUserException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body((new HashMap<>()).put("error", "E-mail already exists"));
        }

        // Create user root folder
        Folder rootFolder = new Folder();
        rootFolder.setUser(newUser);
        rootFolder.setName("Home");
        rootFolder.setFolder(null);
        folderService.save(rootFolder);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.present(newUser));
    }

    @CrossOrigin
    @PutMapping(value = "user")
    public ResponseEntity<?> updateUser(
        @RequestBody UserUpdateRequest request
    ) {
        // Get user from token
        User loggedInUser = userService.loadUserFromJwt();
        if (loggedInUser == null) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        loggedInUser.setFirstName(request.getFirstName());
        loggedInUser.setLastName(request.getLastName());
        loggedInUser.setPhoneNumber(request.getPhoneNumber());
        try {
            userService.updateUser(loggedInUser);
        } catch (UserNotFoundException e) {
            return createErrorResponse("Could not update user");
        }

        return createSuccessResponse(userService.present(loggedInUser));
    }

    @PutMapping(value = "user/image")
    @CrossOrigin
    public ResponseEntity<?> updateUserImage(
        @RequestParam MultipartFile file
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

        String imageFilePath = fileManager.getUserFilesPath()+"/users/"+loggedInUser.getId()+"/";
        (new File(imageFilePath)).mkdirs();

        // If already has an image delete it
        File oldImage = new File(fileManager.getUserFilesPath()+loggedInUser.getImagePath());
        if (oldImage.exists()) {
            oldImage.delete();
        }

        // Move file to its correct folder
        String imageName = loggedInUser.getId() + "." + fileExtension;
        File treeLikeFile = new File(imageFilePath + "/" + imageName);
        (new File(tmpFilePath)).renameTo(treeLikeFile);

        // Update db
        try {
            loggedInUser.setImagePath("/users/"+loggedInUser.getId()+"/"+imageName);
            userService.updateUser(loggedInUser);
        } catch (UserNotFoundException userFileNotFound) {
            return createErrorResponse("Couldn't update the user" );
        }

        HashMap<String,String> responseBody = new HashMap<>();
        responseBody.put("imagePath","/users/"+loggedInUser.getId()+"/"+imageName);
        return createSuccessResponse(responseBody);
    }
    @GetMapping(value = "user/{idUser}/image")
    @CrossOrigin
    public ResponseEntity<?> getImage(
        @PathVariable Long idUser
    ) {
        User user = null;
        try {
            user = userService.getUserById(idUser);
            if (user == null) {
                throw new UserNotFoundException("");
            }
        } catch (UserNotFoundException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "Cannot get user image" );
        }

        // Read file bytes
        String imagePath = fileManager.getUserFilesPath()+user.getImagePath();
        Path filePath = Paths.get(imagePath);
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return createErrorResponse("Something went wrong please try again" );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        if(user.getImagePath().contains(".jpg")){
            headers.setContentType(MediaType.IMAGE_JPEG);
        }else if(user.getImagePath().contains(".png")){
            headers.setContentType(MediaType.IMAGE_PNG);
        }else{
            headers.setContentType(MediaType.IMAGE_PNG);
        }
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        return responseEntity;
    }



}

