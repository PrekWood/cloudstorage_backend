package unipi.cloudstorage.shared;

import unipi.cloudstorage.file.UserFile;

import java.util.HashMap;
import java.util.List;

public interface ModelPresenter<T> {
    public Object present(T object);
    public Object presentMultiple(List<T> objectList);
}
