package unipi.cloudstorage.folder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileService;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.folder.exceptions.FolderNotFoundException;
import unipi.cloudstorage.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@Service
public class FolderService {
    private final FolderRepository repository;
    private final FolderPresenter presenter;
    private final UserFileService fileService;

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
}
