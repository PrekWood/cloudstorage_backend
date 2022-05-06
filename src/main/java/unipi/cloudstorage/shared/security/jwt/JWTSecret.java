package unipi.cloudstorage.shared.security.jwt;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JWTSecret {
    private static final String JWT_SECRET = "YXP5Ra&EF3!DLuvoAuyzURoO&kpD%z9vqgeh1Cc#@HxyHBof#234xo*2jzo0KR8l%FOAz";

    public static String getJWTSecret(){
        return JWT_SECRET;
    }
}
