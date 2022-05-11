package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unipi.cloudstorage.file.exceptions.UserFileCouldNotBeUploaded;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.shared.FileManager;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.io.File;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

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
                return userFileRepository.findAllByUserIdAndFileNameContains(
                        idUser,
                        searchQuery,
                        Sort.by(
                                orderWay.equals("asc") ? ASC : DESC,
                                orderBy
                        )
                );
            }
            return userFileRepository.findAllByUserIdAndFolderIdAndFileNameContains(
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
            return userFileRepository.findAllByUserIdAndFavoriteAndFileNameContains(
                    idUser,
                    favorite,
                    searchQuery,
                    Sort.by(
                            orderWay.equals("asc") ? ASC : DESC,
                            orderBy
                    )
            );
        }
        return userFileRepository.findAllByUserIdAndFolderIdAndFavoriteAndFileNameContains(
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
        String filePath = fileManager.getUserFilesPath() + file.getFilePath();
        File fileObj = new File(filePath);
        if (!fileObj.exists() || !fileObj.delete()) {
            throw new UserFileNotFound();
        }
        userFileRepository.delete(file);
    }

    public String getRealFilePath(UserFile file) {
        return getTreeLikeFilePath(file.getId()) + "/" + file.getId() + "." + file.getExtension();
    }

    public HashMap<String, Object> present(UserFile userFile) {
        return userFilePresenter.present(userFile);
    }

    public List<HashMap<String, Object>> present(List<UserFile> filesList) {
        return userFilePresenter.presentMultiple(filesList);
    }

}
