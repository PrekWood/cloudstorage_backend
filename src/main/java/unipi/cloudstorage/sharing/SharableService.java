package unipi.cloudstorage.sharing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.UserFileRepository;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;

import java.util.List;

import static unipi.cloudstorage.sharing.enums.FilePrivileges.ALL;

@Service
@AllArgsConstructor
public class SharableService {

    private final UserFileRepository userFileRepository;

    public List<UserWithPrivileges> loadSharedWithById(Long id){
        return userFileRepository.loadSharedWith(id);
    }

    public UserFile loadSharedWith(UserFile file){
        // Load shared with users
        List<UserWithPrivileges> sharedWith = loadSharedWithById(file.getId());
        for (UserWithPrivileges up : sharedWith){
            up.setOwner(false);
        }

        // Add another record for the owner
        UserWithPrivileges owner = new UserWithPrivileges();
        owner.setUser(file.getUser());
        owner.setSharable(file);
        owner.setOwner(true);
        owner.setPrivileges(ALL);
        sharedWith.add(owner);

        file.setSharedWith(sharedWith);
        return file;
    }

    public Folder loadSharedWith(Folder folder){
        // Load shared with users
        List<UserWithPrivileges> sharedWith = loadSharedWithById(folder.getId());
        for (UserWithPrivileges up : sharedWith){
            up.setOwner(false);
        }

        // Add another record for the owner
        UserWithPrivileges owner = new UserWithPrivileges();
        owner.setUser(folder.getUser());
        owner.setSharable(folder);
        owner.setOwner(true);
        owner.setPrivileges(ALL);
        sharedWith.add(owner);

        folder.setSharedWith(sharedWith);
        return folder;
    }

    public List<UserFile> loadSharedWith(List<UserFile> filesList){
        for (int fileIndex = 0; fileIndex < filesList.size(); fileIndex++){
            UserFile file = filesList.get(fileIndex);
            file = loadSharedWith(file);
            filesList.set(fileIndex,file);
        }
        return filesList;
    }
    public List<Folder> loadSharedWithFolder(List<Folder> folderList){
        for (int folderIndex = 0; folderIndex < folderList.size(); folderIndex++){
            Folder folder = folderList.get(folderIndex);
            folder = loadSharedWith(folder);
            folderList.set(folderIndex,folder);
        }
        return folderList;
    }
}
