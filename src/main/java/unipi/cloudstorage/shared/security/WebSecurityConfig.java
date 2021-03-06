package unipi.cloudstorage.shared.security;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import unipi.cloudstorage.shared.security.jwt.JWTLoginFilter;
import unipi.cloudstorage.shared.security.jwt.JWTValidationFilter;
import unipi.cloudstorage.user.*;
import unipi.cloudstorage.userToken.UserToken;
import unipi.cloudstorage.userToken.UserTokenService;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@Component
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserTokenService userTokenService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JWTLoginFilter jwtFilter = new JWTLoginFilter(authenticationManagerBean(), userTokenService);
        jwtFilter.setFilterProcessesUrl("/api/login");

        http.cors();
        http.csrf().disable();
        //http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/imgs/**").permitAll();
        http.authorizeRequests().antMatchers("/user_files/**").permitAll();
        http.authorizeRequests().antMatchers("/api/user/**/image").permitAll();
        http.authorizeRequests().antMatchers("/api/share").permitAll();
        http.authorizeRequests().antMatchers("/api/validate-signature").permitAll();
        http.authorizeRequests().antMatchers("/api/login","/api/user","/api/country-codes","/api/registration/addPhoneNumber").permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(jwtFilter);
        http.addFilterBefore(new JWTValidationFilter(userTokenService), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



}
