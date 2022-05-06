package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.ModelPresenter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@Component
public class UserFilePresenter implements ModelPresenter<UserFile> {

    private final FileManager fileManager;

    @Override
    public HashMap<String, Object> present(UserFile file){
        String fileExtension = file.getExtension().toLowerCase();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        HashMap<String, Object> fileToReturn = new HashMap<>();
        fileToReturn.put("id",file.getId());
        fileToReturn.put("extension",fileExtension);
        fileToReturn.put("name",file.getFileName());
        fileToReturn.put("path",file.getFilePath());
        fileToReturn.put("idUser",file.getUser().getId());
        fileToReturn.put("favorite",file.isFavorite());
        fileToReturn.put("dateAdd",file.getDateAdd());
        LocalDateTime dateAdd = file.getDateAdd();
        if(dateAdd != null){
            fileToReturn.put("dateAdd",dateTimeFormatter.format(dateAdd));
        }else{
            fileToReturn.put("dateAdd",dateAdd);
        }

        // Search for file type icon
        String fileTypeIconLink = "/imgs/file_type_icons/"+fileExtension+".png";
        Boolean fileTypeIconExists = (new File(fileManager.getRootDir()+"/src/main/resources/static"+fileTypeIconLink))
                .exists();
        fileToReturn.put("fileTypeIconLink",fileTypeIconLink);
        fileToReturn.put("fileTypeIconExists",fileTypeIconExists);

        // add file size
        Long fileSize;
        try {
            fileSize = Files.size(Paths.get(fileManager.getUserFilesPath()+file.getFilePath()));
        } catch (IOException e) {
            fileSize = 0L;
        }
        fileToReturn.put("size",fileManager.formatBytes(fileSize));

        return fileToReturn;
    }

    @Override
    public List<HashMap<String, Object>> presentMultiple(List<UserFile> filesList){

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<HashMap<String, Object>> filesListToReturn = new ArrayList<>();
        HashMap<String, Boolean> fileTypeIconsChecked = new HashMap<>();
        for (UserFile file:filesList) {

            String fileExtension = file.getExtension().toLowerCase();

            HashMap<String, Object> fileMap = new HashMap<>();
            fileMap.put("id",file.getId());
            fileMap.put("extension",fileExtension);
            fileMap.put("name",file.getFileName());
            fileMap.put("path",file.getFilePath());
            fileMap.put("idUser",file.getUser().getId());
            fileMap.put("favorite",file.isFavorite());

            LocalDateTime dateAdd = file.getDateAdd();
            if(dateAdd != null){
                fileMap.put("dateAdd",dateTimeFormatter.format(dateAdd));
            }else{
                fileMap.put("dateAdd",dateAdd);
            }

            // Search for file type icon
            String fileTypeIconLink = "/imgs/file_type_icons/"+fileExtension+".png";
            Boolean fileTypeIconExists = fileTypeIconsChecked.get(fileExtension);
            if(fileTypeIconExists == null){
                fileTypeIconExists = (new File(fileManager.getRootDir()+"/src/main/resources/static"+fileTypeIconLink))
                        .exists();
                fileTypeIconsChecked.put(fileExtension, fileTypeIconExists);
            }
            fileMap.put("fileTypeIconLink",fileTypeIconLink);
            fileMap.put("fileTypeIconExists",fileTypeIconExists);

            // add file size
            Long fileSize;
            try {
                fileSize = Files.size(Paths.get(fileManager.getUserFilesPath()+file.getFilePath()));
            } catch (IOException e) {
                fileSize = 0L;
            }
            fileMap.put("size",fileManager.formatBytes(fileSize));

            filesListToReturn.add(fileMap);
        }

        return filesListToReturn;
    }

}
