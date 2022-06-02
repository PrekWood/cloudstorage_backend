package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.sharing.SharableService;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivilegesService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static unipi.cloudstorage.sharing.enums.FilePrivileges.*;

@Service
@AllArgsConstructor
public class UserFileService{

    private final UserFileRepository userFileRepository;
    private final UserFilePresenter userFilePresenter;
    private final UserWithPrivilegesService userWithPrivilegesService;
    private final FileManager fileManager;

    private final SharableService sharableService;
    private final static int TMP_FILE_NAME_DIGITS = 20;


    public void uploadFile(MultipartFile file, String filePath) throws UserFileCouldNotBeUploaded {
        try {
            byte[] data = file.getBytes();
            Path path = Paths.get(filePath);
            Files.write(path, data);
        } catch (IOException e) {
            throw new UserFileCouldNotBeUploaded("The file couldn't be uplaoded");
        }
    }

    public void save(UserFile userFile) {
        userFileRepository.save(userFile);
    }


    public UserFile getFileById(Long idFile) throws UserFileNotFound {
        Optional<UserFile> file = userFileRepository.findById(idFile);
        if (!file.isPresent() || file.isEmpty()) {
            throw new UserFileNotFound("file not found");
        }
        return file.get();
    }

    public HashMap<String, String> getTmpFilePath(String fileName) {
        String extention = fileManager.getFileExtension(fileName);
        boolean fileAlreadyExists = true;

        Random randomGenerator = new Random();

        String tmpFilePath = "";
        String tmpFileName = "";
        do {
            StringBuilder tmpFileNameBuilder = new StringBuilder();
            for (int randomNumberIndex = 0; randomNumberIndex < TMP_FILE_NAME_DIGITS; randomNumberIndex++) {
                tmpFileNameBuilder.append(randomGenerator.nextInt(10));
            }

            tmpFileName = tmpFileNameBuilder.toString() + "." + extention;
            tmpFilePath = fileManager.getUserFilesPath() + "/tmp/" + tmpFileName;

            fileAlreadyExists = (new File(tmpFilePath)).exists();
        } while (fileAlreadyExists);

        HashMap<String, String> returnMap = new HashMap<>();
        returnMap.put("filePath", tmpFilePath);
        returnMap.put("fileName", tmpFileName);
        return returnMap;
    }

    public void update(UserFile fileToUpdate) throws UserFileNotFound {
        // Search for user
        UserFile existingFile = userFileRepository.findById(fileToUpdate.getId()).orElse(null);
        if (existingFile == null) {
            throw new UserFileNotFound("File was not found while trying to update it");
        }

        existingFile.setFilePath(fileToUpdate.getFilePath());
        existingFile.setExtension(fileToUpdate.getExtension());
        existingFile.setUser(fileToUpdate.getUser());
        existingFile.setSharedWith(fileToUpdate.getSharedWith());
        save(existingFile);
    }

    public String getTreeLikeFilePath(Long idFile) {
        String idFileString = String.valueOf(idFile);
        StringBuilder treeLikeFilePathBuilder = new StringBuilder();
        treeLikeFilePathBuilder.append(fileManager.getUserFilesPath());
        for (int letterPosition = 0; letterPosition < idFileString.length(); letterPosition++) {
            treeLikeFilePathBuilder.append("/");
            treeLikeFilePathBuilder.append(idFileString.charAt(letterPosition));
        }
        return treeLikeFilePathBuilder.toString();
    }

    public List<UserFile> getFiles(
            Long idUser,
            Long folderId,
            Boolean favorite,
            String orderBy,
            String orderWay,
            Boolean allFiles
    ) {

        if (favorite == null) {
            if (allFiles) {
                return userFileRepository.findAllByUserId(
                        idUser,
                        Sort.by(
                                orderWay.equals("asc") ? ASC : DESC,
                                orderBy
                        )
                );
            }
            return userFileRepository.findAllByUserIdAndFolderId(
                    idUser,
                    folderId,
                    Sort.by(
                            orderWay.equals("asc") ? ASC : DESC,
                            orderBy
                    )
            );
        }

        if (allFiles) {
            return userFileRepository.findAllByUserIdAndFavorite(
                    idUser,
                    favorite,
                    Sort.by(
                            orderWay.equals("asc") ? ASC : DESC,
                            orderBy
                    )
            );
        }

        return userFileRepository.findAllByUserIdAndFolderIdAndFavorite(
                idUser,
                folderId,
                favorite,
                Sort.by(
                        orderWay.equals("asc") ? ASC : DESC,
                        orderBy
                )
        );
    }

    public List<UserFile> getFiles(
            Long idUser,
            Long folderId
    ) {
        return userFileRepository.findAllByUserIdAndFolderId(
            idUser,
            folderId,
            Sort.by(
                    DESC,
                    "dateAdd"
            )
        );
    }

    public List<UserFile> search(
            Long idUser,
            Long folderId,
            Boolean favorite,
            String searchQuery,
            String orderBy,
            String orderWay,
            Boolean allFiles
    ) {
        if (favorite == null) {
            if (allFiles) {
                return userFileRepository.findAllByUserIdAndNameContains(
                        idUser,
                        searchQuery,
                        Sort.by(
                                orderWay.equals("asc") ? ASC : DESC,
                                orderBy
                        )
                );
            }
            return userFileRepository.findAllByUserIdAndFolderIdAndNameContains(
                    idUser,
                    folderId,
                    searchQuery,
                    Sort.by(
                            orderWay.equals("asc") ? ASC : DESC,
                            orderBy
                    )
            );
        }
        if (allFiles) {
            return userFileRepository.findAllByUserIdAndFavoriteAndNameContains(
                    idUser,
                    favorite,
                    searchQuery,
                    Sort.by(
                            orderWay.equals("asc") ? ASC : DESC,
                            orderBy
                    )
            );
        }
        return userFileRepository.findAllByUserIdAndFolderIdAndFavoriteAndNameContains(
                idUser,
                folderId,
                favorite,
                searchQuery,
                Sort.by(
                    orderWay.equals("asc") ? ASC : DESC,
                    orderBy
                )
        );
    }

    public void delete(UserFile file) throws UserFileNotFound {

        userWithPrivilegesService.deleteSharableSharedWithRecords(file.getId());

        // Delete shared with
        for (UserWithPrivileges sharedWith : userWithPrivilegesService.findBySharableId(file.getId())) {
            userWithPrivilegesService.delete(sharedWith);
        }

        // delete file from filepath
        String filePath = fileManager.getUserFilesPath() + file.getFilePath();
        File fileObj = new File(filePath);
        if (fileObj.exists()) {
            fileObj.delete();
        }

        userFileRepository.delete(file);
    }

    public String getRealFilePath(UserFile file) {
        return getTreeLikeFilePath(file.getId()) + "/" + file.getId() + "." + file.getExtension();
    }



    public HashMap<String, Object> present(UserFile userFile, Boolean full) {
        if(full){
            userFile = sharableService.loadSharedWith(userFile);
        }
        return userFilePresenter.present(userFile);
    }
    public HashMap<String, Object> present(UserFile userFile) {
        return present(userFile, false);
    }


    public List<HashMap<String, Object>> present(List<UserFile> filesList, Boolean full) {
        if(full){
            filesList = sharableService.loadSharedWith(filesList);
        }
        return userFilePresenter.presentMultiple(filesList);
    }
    public List<HashMap<String, Object>> present(List<UserFile> filesList) {
        return present(filesList,false);
    }



    public List<UserFile> getSharedFiles(
            Long idUser,
            Long folderId,
            String searchQuery,
            String orderBy,
            String orderWay
    ) {

        if (folderId != null) {
            return userFileRepository.getFilesOfSharedFolder(
                folderId,
                Sort.by(
                        orderWay.equals("asc") ? ASC : DESC,
                        orderBy
                )
            );
        }

        if (Validate.isEmpty(searchQuery)) {
            return userFileRepository.searchForSharedFiles(
                idUser,
                Sort.by(
                        orderWay.equals("asc") ? ASC : DESC,
                        orderBy
                )
            );
        }

        return userFileRepository.searchForSharedFiles(
            idUser,
            searchQuery,
            Sort.by(
                orderWay.equals("asc") ? ASC : DESC,
                orderBy
            )
        );
    }

    public boolean checkAccess(User user, UserFile file, String action){

        //file = sharableService.loadSharedWith(file);

        // Check if is the owner
        boolean isUserTheOwner = file.getUser().getId().equals(user.getId());
        if(isUserTheOwner){
            return true;
        }
        System.out.println("isUserTheOwner: "+isUserTheOwner);
        System.out.println(file.getSharedWith().toString());

        // Check if the file is shared with the user
        boolean isSharedWithUser = false;
        for (UserWithPrivileges userThatHasAccess : file.getSharedWith()) {
            if (!user.getId().equals(userThatHasAccess.getUser().getId())) {
                continue;
            }

            System.out.println("mphke");
            System.out.println(action);

            // Check for privileges
            if (Validate.isEmpty(action)){
                isSharedWithUser = true;
            }else if(action.equals("edit")){
                System.out.println(userThatHasAccess.getPrivileges());

                if(
                    userThatHasAccess.getPrivileges().equals(DOWNLOAD_EDIT) ||
                    userThatHasAccess.getPrivileges().equals(ALL)
                ){
                    isSharedWithUser = true;
                }
            }else if(action.equals("delete")){
                if(
                    userThatHasAccess.getPrivileges().equals(DOWNLOAD_DELETE) ||
                    userThatHasAccess.getPrivileges().equals(ALL)
                ){
                    isSharedWithUser = true;
                }
            }
            break;
        }
        System.out.println("isSharedWithUser: "+isSharedWithUser);

        if(isSharedWithUser){
            return true;
        }

        // Check if the file is located in a folder that is shared with the user
        boolean isInsideAFolderThatIsSharedWithUser = false;
        Folder rootFolder = getRootFolder(file);
        for (UserWithPrivileges userThatHasAccess:rootFolder.getSharedWith()) {

            if (!user.getId().equals(userThatHasAccess.getUser().getId())) {
                continue;
            }

            // Check for privileges
            if (Validate.isEmpty(action)){
                isInsideAFolderThatIsSharedWithUser = true;
            }else if(action.equals("edit")){
                if(
                    userThatHasAccess.getPrivileges().equals(DOWNLOAD_EDIT) ||
                    userThatHasAccess.getPrivileges().equals(ALL)
                ){
                    isInsideAFolderThatIsSharedWithUser = true;
                }
            }else if(action.equals("delete")){
                if(
                    userThatHasAccess.getPrivileges().equals(DOWNLOAD_DELETE) ||
                    userThatHasAccess.getPrivileges().equals(ALL)
                ){
                    isInsideAFolderThatIsSharedWithUser = true;
                }
            }
            break;
        }
        System.out.println("isInsideAFolderThatIsSharedWithUser: "+isInsideAFolderThatIsSharedWithUser);
        System.out.println("rootFolder: "+rootFolder.getId());

        return isInsideAFolderThatIsSharedWithUser;
    }

    public boolean checkAccess(User user, UserFile file){
        return checkAccess(user, file, null);
    }


    public Folder getRootFolder(UserFile file){
        Folder currentFolder = file.getFolder();
        if(currentFolder.getFolder() == null){
            return currentFolder;
        }
        while (currentFolder.getFolder().getFolder() != null){
            currentFolder = currentFolder.getFolder();
        }
        return currentFolder;
    }

}
