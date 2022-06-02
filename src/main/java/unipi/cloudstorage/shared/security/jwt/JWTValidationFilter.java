package unipi.cloudstorage.shared.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import unipi.cloudstorage.userToken.UserTokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@AllArgsConstructor
public class JWTValidationFilter extends OncePerRequestFilter {

    private final UserTokenService userTokenService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(
            request.getServletPath().equals("/api/registration") ||
            request.getServletPath().equals("/api/country-codes")
        ){
            filterChain.doFilter(request, response);
        }else{
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                try{

                    String jwtToken = authorizationHeader.replace("Bearer ","");
                    String username = this.getUsernameFromJWT(jwtToken);

                    // All users have the same roles
                    Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));

                    // Create authentication token and allow users inside spring
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    filterChain.doFilter(request, response);

                }catch(Exception exception){
                    System.out.println("Authentication error: "+exception.getMessage());

                    response.setStatus(FORBIDDEN.value());
                    response.setHeader("error", exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE);

                    HashMap<String, String> errorMessage = new HashMap<>();
                    errorMessage.put("error",exception.getMessage());
                    new ObjectMapper().writeValue(response.getOutputStream(),errorMessage);
                }
            }else{
                System.out.println("no token");

                filterChain.doFilter(request, response);
            }
        }
    }

    public String getUsernameFromJWT(String jwtToken){
        Algorithm algorithm = Algorithm.HMAC256(JWTSecret.getJWTSecret().getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(jwtToken);
        return decodedJWT.getSubject();
    }
}
