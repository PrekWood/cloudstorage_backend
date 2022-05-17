package unipi.cloudstorage.folder;


import lombok.AllArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Component;
import unipi.cloudstorage.shared.ModelPresenter;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.responses.PresentedUserResponse;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@AllArgsConstructor
public class FolderPresenter implements ModelPresenter<Folder> {

    @Override
    public HashMap<String, Object> present(Folder folder) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        HashMap<String, Object> response = new HashMap();
        response.put("id",folder.getId());
        response.put("name",folder.getName());
        response.put("idUser",folder.getUser().getId());
        response.put("parentFolderId",folder.getFolder() == null ? null : folder.getFolder().getId());
        response.put("breadcrumb",folder.getBreadcrumb());
        LocalDateTime dateAdd = folder.getDateAdd();
        if(dateAdd != null){
            response.put("dateAdd",dateTimeFormatter.format(dateAdd));
        }
        // Shared with
        List<HashMap<String, Object>> sharedWithList = new ArrayList<>();
        for (UserWithPrivileges userWithPrivileges : folder.getSharedWith()) {
            HashMap<String, Object> sharedWith = new HashMap<>();
            sharedWith.put("userId",userWithPrivileges.getUser().getId());
            sharedWith.put("userEmail",userWithPrivileges.getUser().getEmail());
            sharedWith.put("userName",userWithPrivileges.getUser().getFirstName()+" "+userWithPrivileges.getUser().getLastName());
            sharedWith.put("userImg",userWithPrivileges.getUser().getImagePath());
            sharedWith.put("privileges",userWithPrivileges.getPrivileges());
            sharedWithList.add(sharedWith);
        }
        if(sharedWithList.size() > 0){
            response.put("sharedWith",sharedWithList);
        }
        return response;
    }

    @Override
    public List<HashMap<String, Object>> presentMultiple(List<Folder> folderList) {
        List<HashMap<String, Object>> folderListToReturn = new ArrayList<>();
        for (Folder folder : folderList) {
            folderListToReturn.add(present(folder));
        }
        return folderListToReturn;
    }
}
