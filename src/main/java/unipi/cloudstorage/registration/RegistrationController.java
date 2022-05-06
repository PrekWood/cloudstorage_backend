package unipi.cloudstorage.registration;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unipi.cloudstorage.countryCodes.CountryCode;
import unipi.cloudstorage.countryCodes.CountryCodeService;
import unipi.cloudstorage.countryCodes.exceptions.CountryCodeNotFoundException;
import unipi.cloudstorage.otp.Otp;
import unipi.cloudstorage.otp.OtpService;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.shared.sms.SmsSender;
import unipi.cloudstorage.shared.sms.Twilio;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserRole;
import unipi.cloudstorage.user.UserService;
import unipi.cloudstorage.user.exceptions.EmailAlreadyBeingUsedUserException;
import unipi.cloudstorage.user.exceptions.UserNotFoundException;
import unipi.cloudstorage.user.requests.OtpRequest;
import unipi.cloudstorage.user.requests.OtpValidationRequest;
import unipi.cloudstorage.user.requests.RegistrationRequest;
import unipi.cloudstorage.userToken.UserToken;
import unipi.cloudstorage.userToken.UserTokenService;
import unipi.cloudstorage.userToken.exceptions.UserTokenNotFound;

import java.io.IOException;
import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/" )
public class RegistrationController extends ResponseHandler {

    private final UserService userService;
    private final CountryCodeService countryCodeService;
    private final OtpService otpService;
    private final UserTokenService userTokenService;

    @PostMapping("registration" )
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {

        // Check for empty fields
        if (
                request.getEmail() == null || request.getEmail().equals("" ) ||
                        request.getPassword() == null || request.getPassword().equals("" ) ||
                        request.getFirstName() == null || request.getFirstName().equals("" ) ||
                        request.getLastName() == null || request.getLastName().equals("" )
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Please fill in all the nesessary fields" ));
        }

        // Create new object
        User newUser = new User(
                request.getEmail(),
                request.getPassword(),
                null,
                request.getFirstName(),
                request.getLastName(),
                UserRole.USER,
                false
        );

        // Try to sign in
        try {
            userService.signUpUser(newUser);
        } catch (EmailAlreadyBeingUsedUserException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body((new HashMap<>()).put("error", "E-mail already exists" ));
        }

        // Build response
        HashMap<String, String> responseBody = new HashMap<>();
        responseBody.put("idUser", String.valueOf(newUser.getId()));
        responseBody.put("email", newUser.getEmail());
        responseBody.put("firstName", newUser.getFirstName());
        responseBody.put("lastName", newUser.getLastName());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @CrossOrigin
    @PostMapping("registration/otp" )
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {

        String phoneNumber = request.getPhoneNumber();
        Long idCountryCode = request.getIdCountryCode();

        // Phone number validation
        if (phoneNumber.equals("" ) || phoneNumber.length() != 10) {
            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Invalid phone number" );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        // Country Code check
        CountryCode countryCode = null;
        try {
            countryCode = countryCodeService.findById(idCountryCode);
        } catch (CountryCodeNotFoundException e) {
            System.out.println(e);

            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Invalid county code" );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "You are not loged in" );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
        }

        // update fields
        logedInUser.setPhoneNumber(phoneNumber);
        logedInUser.setCountryCode(countryCode);
        try {
            userService.updateUser(logedInUser);
        } catch (UserNotFoundException e) {
            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "User not found while updating" );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        // Generate Otp
        Otp registerOtp = new Otp();
        registerOtp.setUser(logedInUser);
        otpService.save(registerOtp);

        // Send sms
        try {
            SmsSender smsSender = new Twilio();
            smsSender.sendSms(countryCode.getCode(), phoneNumber, registerOtp.getPinCode());
        } catch (IOException e) {
            HashMap<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "SMS Could not be sent" );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        return ResponseEntity.status(HttpStatus.OK).body("" );
    }

    @PostMapping("registration/otp-resend" )
    public ResponseEntity<?> resendOtp() {

        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((new HashMap<>()).put("error", "You are not loged in" ));
        }

        // Generate Otp
        Otp registerOtp = new Otp();
        registerOtp.setUser(logedInUser);
        otpService.save(registerOtp);

        // Send sms
        try {
            SmsSender smsSender = new Twilio();
            smsSender.sendSms(
                    logedInUser.getCountryCode().getCode(),
                    logedInUser.getPhoneNumber(),
                    registerOtp.getPinCode()
            );
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "SMS Could not be sent" ));
        }

        return ResponseEntity.status(HttpStatus.OK).body("" );
    }


    @PostMapping("registration/otp-validation" )
    public ResponseEntity<?> validateOtp(@RequestBody OtpValidationRequest request) {

        String otpCode = request.getOtpCode();

        // Phone number validation
        if (otpCode.equals("" ) || otpCode.length() != 5) {
            System.out.println("Invalid otpCode" );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Invalid otpCode" ));
        }

        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((new HashMap<>()).put("error", "You are not loged in" ));
        }

        // Get last otp and compare it with the user's
        Otp lastOtp = otpService.getLastOtpOfUser(logedInUser);
        if (!lastOtp.getPinCode().equals(otpCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Invalid PIN" ));
        }

        // Set user as validated
        try {
            logedInUser.setPhoneValidated(true);
            userService.updateUser(logedInUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Error while updating the user" ));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("" );
    }

    @PostMapping("logout" )
    public ResponseEntity<?> logout(@RequestHeader("Authorization" ) String bearerToken) {
        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if (logedInUser == null || !logedInUser.isPhoneValidated()) {
            return createErrorResponse(HttpStatus.FORBIDDEN, "You are not loged in" );
        }

        String tokenString = bearerToken.replace("Bearer ", "" );

        UserToken token = null;
        try {
            token = userTokenService.findById(tokenString);
            if (token == null) {
                throw new UserTokenNotFound();
            }
        } catch (UserTokenNotFound userFileNotFound) {
            return createErrorResponse("Invalid token" );
        }

        // Set token invalid
        token.setValid(false);
        try {
            userTokenService.update(token);
        } catch (UserTokenNotFound userTokenNotFound) {
            return createErrorResponse("Something went wrong please try again" );
        }

        return createSuccessResponse();
    }
}
