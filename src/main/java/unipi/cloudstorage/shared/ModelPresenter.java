package unipi.cloudstorage.shared;

import unipi.cloudstorage.file.UserFile;

import java.util.HashMap;
import java.util.List;

public interface ModelPresenter<T> {
    public HashMap<String, Object> present(T object);
    public List<HashMap<String, Object>> presentMultiple(List<T> objectList);
}
