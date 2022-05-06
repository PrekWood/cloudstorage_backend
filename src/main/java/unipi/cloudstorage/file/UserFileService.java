package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.enums.UserFileField;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.enums.OrderWay;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.exceptions.UserNotFoundException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.io.File;

@Service
@AllArgsConstructor
public class UserFileService {

    private final UserFileRepository userFileRepository;
    private final UserFilePresenter userFilePresenter;
    private final FileManager fileManager;
    private final static int TMP_FILE_NAME_DIGITS = 20;

    public void uploadFile(MultipartFile file, String filePath) throws UserFileCouldNotBeUploaded {
        try {
            byte[] data = file.getBytes();
            Path path = Paths.get(filePath);
            Files.write(path, data);
        } catch (IOException e) {
            throw new UserFileCouldNotBeUploaded("The file couldn't be uplaoded" );
        }
    }

    public void save(UserFile userFile) {
        userFileRepository.save(userFile);
    }


    public UserFile getFileById(Long idFile) throws UserFileNotFound {
        Optional<UserFile> file = userFileRepository.findById(idFile);
        if (!file.isPresent() || file.isEmpty()) {
            throw new UserFileNotFound("file not found" );
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
            throw new UserFileNotFound("File was not found while trying to update it" );
        }

        existingFile.setFilePath(fileToUpdate.getFilePath());
        existingFile.setExtension(fileToUpdate.getExtension());
        existingFile.setUser(fileToUpdate.getUser());
        save(existingFile);
    }

    public String getTreeLikeFilePath(Long idFile) {
        String idFileString = String.valueOf(idFile);
        StringBuilder treeLikeFilePathBuilder = new StringBuilder();
        treeLikeFilePathBuilder.append(fileManager.getUserFilesPath());
        for (int letterPosition = 0; letterPosition < idFileString.length(); letterPosition++) {
            treeLikeFilePathBuilder.append("/" );
            treeLikeFilePathBuilder.append(idFileString.charAt(letterPosition));
        }
        return treeLikeFilePathBuilder.toString();
    }

    public List<UserFile> getFilesByUserId(Long idUser) {
        return userFileRepository.findAllByUserId(idUser);
    }
    public List<UserFile> getFilesByUserId(Long idUser, String orderBy, String orderWay) {
        List<UserFile> filesList = new ArrayList<>();
        if(orderBy == null){
            return userFileRepository.findAllByUserId(idUser);
        }
        switch (orderBy) {
            case "date_add" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdOrderByDateAddDesc(idUser);
                }
                return userFileRepository.findAllByUserIdOrderByDateAddAsc(idUser);
            }
            case "name" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    System.out.println("name desc");
                    return userFileRepository.findAllByUserIdOrderByFileNameDesc(idUser);
                }
                System.out.println("name asc");
                return userFileRepository.findAllByUserIdOrderByFileNameAsc(idUser);
            }
            default -> {
                System.out.println("default");
                return userFileRepository.findAllByUserId(idUser);
            }
        }
    }
    public List<UserFile> getFavoriteFilesByUserId(Long idUser, String orderBy, String orderWay) {
        List<UserFile> filesList = new ArrayList<>();
        if(orderBy == null){
            return userFileRepository.findAllByUserIdAndFavoriteIsTrue(idUser);
        }
        switch (orderBy) {
            case "date_add" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdAndFavoriteIsTrueOrderByDateAddDesc(idUser);
                }
                return userFileRepository.findAllByUserIdAndFavoriteIsTrueOrderByDateAddAsc(idUser);
            }
            case "name" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    System.out.println("name desc");
                    return userFileRepository.findAllByUserIdAndFavoriteIsTrueOrderByFileNameDesc(idUser);
                }
                System.out.println("name asc");
                return userFileRepository.findAllByUserIdAndFavoriteIsTrueOrderByFileNameAsc(idUser);
            }
            default -> {
                System.out.println("default");
                return userFileRepository.findAllByUserIdAndFavoriteIsTrue(idUser);
            }
        }
    }


    public void delete(UserFile file) throws UserFileNotFound {
        String filePath = fileManager.getUserFilesPath() + file.getFilePath();
        File fileObj = new File(filePath);
        if (!fileObj.exists() || !fileObj.delete()) {
            throw new UserFileNotFound();
        }
        userFileRepository.delete(file);
    }

    public List<UserFile> search(Long idUser, String searchQuery, String orderBy, String orderWay) {
        List<UserFile> filesList = new ArrayList<>();
        if(orderBy == null){
            return userFileRepository.findAllByUserIdAndFileNameContains(idUser, searchQuery);
        }
        switch (orderBy) {
            case "date_add" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdAndFileNameContainsOrderByDateAddDesc(idUser, searchQuery);
                }
                return userFileRepository.findAllByUserIdAndFileNameContainsOrderByDateAddAsc(idUser, searchQuery);
            }
            case "name" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdAndFileNameContainsOrderByFileNameDesc(idUser, searchQuery);
                }
                return userFileRepository.findAllByUserIdAndFileNameContainsOrderByFileNameAsc(idUser, searchQuery);
            }
            default -> {
                return userFileRepository.findAllByUserIdAndFileNameContains(idUser, searchQuery);
            }
        }
    }
    public List<UserFile> searchInFavorites(Long idUser, String searchQuery, String orderBy, String orderWay) {
        List<UserFile> filesList = new ArrayList<>();
        if(orderBy == null){
            return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContains(idUser, searchQuery);
        }
        switch (orderBy) {
            case "date_add" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByDateAddDesc(idUser, searchQuery);
                }
                return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByDateAddAsc(idUser, searchQuery);
            }
            case "name" -> {
                if(orderWay == null || orderWay.equals("desc")){
                    return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByFileNameDesc(idUser, searchQuery);
                }
                return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByFileNameAsc(idUser, searchQuery);
            }
            default -> {
                return userFileRepository.findAllByUserIdAndFavoriteIsTrueAndFileNameContains(idUser, searchQuery);
            }
        }
    }

    public HashMap<String, Object> present(UserFile userFile) {
        return userFilePresenter.present(userFile);
    }

    public List<HashMap<String, Object>> present(List<UserFile> filesList) {
        return userFilePresenter.presentMultiple(filesList);
    }

}
