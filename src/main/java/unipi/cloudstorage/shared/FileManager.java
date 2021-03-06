package unipi.cloudstorage.shared;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileManager {

    public String getRootDir() {
        return new FileSystemResource("").getFile().getAbsolutePath();
    }

    public String getUserFilesPath() {
        return getRootDir() + "/user_files";
    }

    public String formatBytes(Long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public String getFileExtension(String fileName) {
        String[] fileNameParts = fileName.split("\\.", -1);
        return fileNameParts[fileNameParts.length - 1];
    }

    public byte[] getFileBytes(String filepath, ClassLoader classLoader) {
        byte[] byteArray = null;
        try {
            File file = getFileFromResource(filepath, classLoader);
            FileInputStream fl = new FileInputStream(file);
            byteArray = new byte[(int) file.length()];
            fl.read(byteArray);
            fl.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return byteArray;
    }

    public InputStream getFileFromResourceAsStream(String fileName, ClassLoader classLoader) {

        // The class loader that loaded the class
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    private File getFileFromResource(String fileName, ClassLoader classLoader) throws URISyntaxException {
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    /**
     * https://www.baeldung.com/java-compress-and-uncompress
     * @param fileToZip
     * @param fileName
     * @param zipOut
     * @throws IOException
     */
    public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
