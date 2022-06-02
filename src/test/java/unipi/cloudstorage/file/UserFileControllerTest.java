package unipi.cloudstorage.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import unipi.cloudstorage.shared.CloudStorageTest;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.requests.RegistrationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserFileControllerTest extends CloudStorageTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void itUploadsAFile() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create a file
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml" .getBytes());
        mvc.perform(
                        multipart("/api/file")
                                .file(file).param("folderId", String.valueOf(-1))
                                .param("folderId", String.valueOf(-1))
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    void itUploadsAFileAndChecksTheFileCount() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // Get initial file count
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Format response
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);
        int initialFilesCount = filesList.size();

        // create a file
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml" .getBytes());
        mvc.perform(
                        multipart("/api/file")
                                .file(file).param("folderId", String.valueOf(-1))
                                .header("Authorization", "Bearer " + bearerToken)

                )
                .andExpect(status().isOk());

        // Get file count after file creation
        MvcResult getFilesResponseAfter = mvc.perform(
                        get("/api/files")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Format response
        String filesAfterString = getFilesResponseAfter.getResponse().getContentAsString();
        List<Object> filesListAfter = new ObjectMapper().readValue(filesAfterString, List.class);
        int finalFilesCount = filesListAfter.size();
        System.out.println("finalFilesCount: " + finalFilesCount);

        assertEquals((initialFilesCount + 1), finalFilesCount);
    }

    // Sorting tests

    @Test
    void itCreatesTwoFilesAndTriesSortingThemByDateAddDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/desc")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "1.txt");

    }

    @Test
    void itCreatesTwoFilesAndTriesSortingThemByDateAddAsc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/asc")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "1.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "2.txt");
    }

    @Test
    void itCreatesTwoFilesAndTriesSortingThemByNameDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/name/desc")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "1.txt");
    }

    @Test
    void itCreatesTwoFilesAndTriesSortingThemByNameAsc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/name/asc")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "1.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "2.txt");
    }

    @Test
    void itTestsFavoriteFiltering() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make one of the files favorite
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("favorite", true);
        mvc.perform(
                        put("/api/file/" + fileResponses.get(0).get("id") + "/")
                                .header("Authorization", "Bearer " + bearerToken)
                                .content(asJsonString(requestBody))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files?onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 1);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "1.txt");
    }

    @Test
    void itTestsFavoriteFilteringAndSortingByDateDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make two of the files favorite
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("favorite", true);
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            mvc.perform(
                            put("/api/file/" + fileResponses.get(fileIndex).get("id") + "/")
                                    .header("Authorization", "Bearer " + bearerToken)
                                    .content(asJsonString(requestBody))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/desc?onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "1.txt");
    }


    @Test
    void itTestsFavoriteFilteringAndSortingByDateAsc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make two of the files favorite
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("favorite", true);
            mvc.perform(
                            put("/api/file/" + fileResponses.get(fileIndex).get("id") + "/")
                                    .header("Authorization", "Bearer " + bearerToken)
                                    .content(asJsonString(requestBody))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/asc?onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "1.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "2.txt");
    }


    @Test
    void itTestsFavoriteFilteringAndSortingByNameDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make two of the files favorite
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("favorite", true);
            mvc.perform(
                            put("/api/file/" + fileResponses.get(fileIndex).get("id") + "/")
                                    .header("Authorization", "Bearer " + bearerToken)
                                    .content(asJsonString(requestBody))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/name/desc?onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "1.txt");
    }

    @Test
    void itTestsFavoriteFilteringAndSortingByNameAsc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            MockMultipartFile file = new MockMultipartFile("file", (fileIndex + 1) + ".txt", "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make two of the files favorite
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("favorite", true);
            mvc.perform(
                            put("/api/file/" + fileResponses.get(fileIndex).get("id") + "/")
                                    .header("Authorization", "Bearer " + bearerToken)
                                    .content(asJsonString(requestBody))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/name/asc?onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "1.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "2.txt");
    }

    @Test
    void itTestsSearchingSortingByDateAddDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            String fileName = (fileIndex == 2 ? "" : "<keywordToSearch>") + (fileIndex + 1) + ".txt";
            MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/desc?searchQuery=<keywordToSearch>")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "<keywordToSearch>2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "<keywordToSearch>1.txt");
    }

    @Test
    void itTestsSearchingSortingByDateAddAsc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create two files
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            String fileName = (fileIndex == 2 ? "" : "<keywordToSearch>") + (fileIndex + 1) + ".txt";
            MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // get files order by date add
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/asc?searchQuery=<keywordToSearch>")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "<keywordToSearch>1.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "<keywordToSearch>2.txt");
    }

    @Test
    void itTestsSearchingWithFavoritesAndSortingByDateAddDesc() throws Exception {
        String bearerToken = createTestUserAndLogin(mvc);

        // create 4 files where the <keywordToSearch> is only on 3 of them
        List<HashMap<String, Object>> fileResponses = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < 4; fileIndex++) {
            String fileName = (fileIndex == 3 ? "" : "<keywordToSearch>") + (fileIndex + 1) + ".txt";
            MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "text file" .getBytes());
            MvcResult response = mvc.perform(
                            multipart("/api/file")
                                    .file(file).param("folderId", String.valueOf(-1))
                                    .header("Authorization", "Bearer " + bearerToken)

                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // parse as a list
            String responseString = response.getResponse().getContentAsString();
            HashMap<String, Object> fileResponse = new ObjectMapper().readValue(responseString, HashMap.class);
            fileResponses.add(fileResponse);
        }

        // Make 2 of the files favorite
        for (int fileIndex = 0; fileIndex < 2; fileIndex++) {
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("favorite", true);
            mvc.perform(
                            put("/api/file/" + fileResponses.get(fileIndex).get("id") + "/")
                                    .header("Authorization", "Bearer " + bearerToken)
                                    .content(asJsonString(requestBody))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
        }

        // get files
        MvcResult getFilesResponse = mvc.perform(
                        get("/api/files/dateAdd/desc?searchQuery=<keywordToSearch>&onlyFavorites=true")
                                .param("folderId", "-1")
                                .header("Authorization", "Bearer " + bearerToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // parse as a list
        String filesString = getFilesResponse.getResponse().getContentAsString();
        List<Object> filesList = new ObjectMapper().readValue(filesString, List.class);

        assertEquals(filesList.size(), 2);
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(0)).get("name")), "<keywordToSearch>2.txt");
        assertEquals(String.valueOf(((LinkedHashMap) filesList.get(1)).get("name")), "<keywordToSearch>1.txt");
    }


}