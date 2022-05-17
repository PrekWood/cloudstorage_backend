package unipi.cloudstorage.userWithPrivileges;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.folder.Folder;

import java.util.List;

@Service
@AllArgsConstructor
public class UserWithPrivilegesService {

    private final UserWithPrivilegesRepository repository;

    public void save(UserWithPrivileges userWithPrivileges){
        repository.save(userWithPrivileges);
    }

    public void delete(UserWithPrivileges userWithPrivileges){
        repository.delete(userWithPrivileges);
    }

    public void deleteSharableSharedWithRecords(Long idFile){
        repository.deleteSharableSharedWithRecords(idFile);
    }

    public UserWithPrivileges findById(Long id){
        return repository.getById(id);
    }
    public List<UserWithPrivileges> findBySharableId(Long id){
        return repository.findAllBySharableId(id);
    }

}
