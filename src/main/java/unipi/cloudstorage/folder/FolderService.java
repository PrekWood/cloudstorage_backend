package unipi.cloudstorage.folder;

import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.folder.exceptions.FolderZipCouldNotBeCreated;
import unipi.cloudstorage.shared.FileManager;
import unipi.cloudstorage.shared.Validate;
import unipi.cloudstorage.user.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@AllArgsConstructor
@Service
public class FolderService {
    private final FolderRepository repository;
    private final FolderPresenter presenter;
    private final UserFileService fileService;
    private final FileManager fileManager;


    public void save(Folder f){
        repository.save(f);
    }

    public Folder getRootFolderOfUser(User user){
        return repository.findFirstByUserIdAndParentFolderIsNull(user.getId());
    }

    public List<Folder> getChildFolders(Long userId, Long parentFolderId){
        return repository.findAllByUserIdAndParentFolderId(userId, parentFolderId);
    }

    public Folder getFolderById(Long userId, Long folderId){
        return repository.findFirstByUserIdAndId(userId, folderId);
    }

    public void update(Folder folderToUpdate) throws FolderNotFoundException {
        // Search for user
        Folder existingFile = repository.findById(folderToUpdate.getId()).orElse(null);
        if (existingFile == null) {
            throw new FolderNotFoundException("File was not found while trying to update it" );
        }

        existingFile.setName(folderToUpdate.getName());
        existingFile.setParentFolder(folderToUpdate.getParentFolder());
        existingFile.setUser(folderToUpdate.getUser());
        save(existingFile);
    }

    public List<HashMap<String, Object>> getBreadcrumb(Folder f){
        List<HashMap<String, Object>> breadcrumb = new ArrayList<>();
        Folder currentFolder = f;
        while(currentFolder != null){
            HashMap<String, Object> breadcrumbItem = new HashMap<>();
            breadcrumbItem.put("name",currentFolder.getName());
            breadcrumbItem.put("id",currentFolder.getId());
            currentFolder = currentFolder.getParentFolder();
            breadcrumb.add(breadcrumbItem);
        }

        return breadcrumb;
    }

    public HashMap<String, Object> present(Folder f){
        f.setBreadcrumb(this.getBreadcrumb(f));
        return presenter.present(f);
    }
    public List<HashMap<String, Object>> present(List<Folder> folderList){
        for (Folder f : folderList) {
            f.setBreadcrumb(getBreadcrumb(f));
        }
        return presenter.presentMultiple(folderList);
    }

    public void delete(User user, Folder folder) throws UserFileNotFound{

        List<Folder> subFolderList = getChildFolders(user.getId(),folder.getId());

        // Delete folder's files
        List<UserFile> filesList = fileService.getFiles(
            user.getId(),
            folder.getId()
        );
        for (UserFile file: filesList) {
            fileService.delete(file);
        }

        // Delete subfolder's files recursively
        for (Folder subFolder: subFolderList) {
            delete(user, subFolder);
        }

        // And then delete folders recursively one by one
        repository.delete(folder);
    }

    public Folder findFilesAndSubFolders(Folder folder){
        folder.setFiles(fileService.getFiles(
            folder.getUser().getId(),
            folder.getId()
        ));
        folder = fillSubFolders(folder);
        folder = fillSubFolderFiles(folder);
        return folder;
    }
    public Folder fillSubFolders(Folder folder){
        List<Folder> subfolderList = getChildFolders(
            folder.getUser().getId(),
            folder.getId()
        );
        for (Folder subfolder: subfolderList){
            subfolder = fillSubFolders(subfolder);
        }
        folder.setSubfolders(subfolderList);
        return folder;
    }
    public Folder fillSubFolderFiles(Folder folder){
        List<Folder> subfolderList = getChildFolders(
            folder.getUser().getId(),
            folder.getId()
        );
        for (Folder subfolder: subfolderList){
            subfolder = fillSubFolderFiles(subfolder);
        }
        folder.setFiles(fileService.getFiles(
            folder.getUser().getId(),
            folder.getId()
        ));
        return folder;
    }

    /**
     * Prints the folder subdirs in a tree like structure
     * ONLY FOR DEBUGGING
     *
     * Creates something like the following:
     *
     * Summer 2022
     *   folder-svgrepo-com (1).png
     *   New Folder
     *     New Folder
     *       folder.svg
     *       New Folder
     *         gunaikeio-kolie-p-d-paola-co01-272-u-apo-tin-seira-letters (2) (2).jpg
     *
     * @param folder
     * @param level Should be 0 on the first call
     */
    public void printFolders(Folder folder, int level){
        List<Folder> subfolderList = getChildFolders(
            folder.getUser().getId(),
            folder.getId()
        );

        StringBuilder folderName = new StringBuilder();
        for(int i=0;i<level;i++){
            folderName.append("  ");
        }
        folderName.append(folder.getName());
        System.out.println(folderName.toString());



        List<UserFile> filesList = fileService.getFiles(
                folder.getUser().getId(),
                folder.getId()
        );

        for (UserFile file: filesList){
            StringBuilder fileName = new StringBuilder();
            for(int i=0;i<level;i++){
                fileName.append("  ");
            }
            fileName.append("  ");
            fileName.append(file.getFileName());
            System.out.println(fileName.toString());
        }

        for (Folder subfolder: subfolderList){
            printFolders(subfolder, (level+1));
        }
    }
    public void printFolders(Folder folder){
        printFolders(folder,0);
    }

    /**
     * Recursive function that moves the files and
     * creates the folder like so
     *
     * cloudstorage_backend/user_files/compressed_folders_tmp/{id_folder}/{folder_name}
     *
     * @param folder
     * @param tmpFolderPath
     * @return
     * @throws IOException
     */
    public void createFolderInTmp(Folder folder, String tmpFolderPath) throws IOException {
        // Create tmp folder directory
        new File(tmpFolderPath).mkdirs();

        // Make a copy of the files
        for (UserFile file: folder.getFiles()) {
            Path fileLocation = Paths.get(fileService.getRealFilePath(file));
            Path locationToBeCopied = Paths.get(tmpFolderPath+"/"+file.getFileName());
            Files.copy(fileLocation, locationToBeCopied, StandardCopyOption.REPLACE_EXISTING);
        }

        for (Folder subfolder: folder.getSubfolders()){
            createFolderInTmp(subfolder, tmpFolderPath+"/"+subfolder.getName());
        }
    }
    public String createFolderInTmp(Folder folder) throws IOException {
        String tmpFolderPath = fileManager.getUserFilesPath()+"/compressed_folders_tmp/"+folder.getId()+"/";
        createFolderInTmp(folder, tmpFolderPath+folder.getName());
        return tmpFolderPath;
    }

    public String createZip(Folder folder) throws FolderZipCouldNotBeCreated {

        Folder folderFull = findFilesAndSubFolders(folder);

        // Open only for debug to know that the folders and files are loaded correctly
        //folderService.printFolders(folderFull);

        // Create folder in tmp
        String tmpFolderPath;
        try {
            tmpFolderPath = createFolderInTmp(folderFull);
            if(Validate.isEmpty(tmpFolderPath)){
                throw new Exception();
            }
        } catch (Exception e) {
            throw new FolderZipCouldNotBeCreated("Temporary file could not be created");
        }

        // Zip the folder
        String zipFilePath = tmpFolderPath+"/"+folderFull.getName()+".zip";
        File tmpFolder = new File(tmpFolderPath+folder.getName());
        try {
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            fileManager.zipFile(tmpFolder, folder.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            throw new FolderZipCouldNotBeCreated("Error while zipping the file");
        }

        // Delete the folder and only keep the zip
        try {
            FileUtils.deleteDirectory(new File(tmpFolderPath+folderFull.getName()));
        } catch (IOException e) {
            throw new FolderZipCouldNotBeCreated("Error while deleting the tmp folder");
        }

        return zipFilePath;
    }
}
