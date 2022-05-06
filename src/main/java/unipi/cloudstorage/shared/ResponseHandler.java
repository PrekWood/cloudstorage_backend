package unipi.cloudstorage.shared;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public class ResponseHandler {
    public ResponseEntity<?> createErrorResponse(HttpStatus status, String error){
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", error);
        return ResponseEntity.status(status).body(responseBody);
    }
    public ResponseEntity<?> createErrorResponse(String error){
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }


    public ResponseEntity<?> createSuccessResponse(){
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
    public ResponseEntity<?> createSuccessResponse(HttpStatus status){
        return ResponseEntity.status(status).body("");
    }
    public ResponseEntity<?> createSuccessResponse(String message){
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
    public ResponseEntity<?> createSuccessResponse(HttpStatus status, String message){
        HashMap<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        return ResponseEntity.status(status).body(responseBody);
    }
    public ResponseEntity<?> createSuccessResponse(HttpStatus status, Object responseBody){
        return ResponseEntity.status(status).body(responseBody);
    }
    public ResponseEntity<?> createSuccessResponse(Object responseBody){
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

}
