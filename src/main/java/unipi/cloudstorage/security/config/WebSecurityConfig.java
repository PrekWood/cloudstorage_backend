package unipi.cloudstorage.security.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import unipi.cloudstorage.security.jwt.JWTLoginFilter;
import unipi.cloudstorage.security.jwt.JWTValidationFilter;
import unipi.cloudstorage.user.*;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /*@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // Cross site request forgery disable
            .csrf().disable()

            .authorizeRequests()

            // Allow registration requests
            .antMatchers("/registration/","/registration/confirm/").permitAll()

            // Any other request must be authenticated
            .anyRequest().authenticated()

            // Basic authentication
            .and().httpBasic();
    }*/

    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }*/



    @Override
    protected void configure(HttpSecurity http) throws Exception {
       /* http
                .authorizeRequests()

                // Allow registration post when not loged in
                .antMatchers("/registration/").permitAll()

                .anyRequest().authenticated()

                .and().formLogin()

                // Basic auth
                .and().httpBasic()

                // Cross site request forgery disable
                .and().csrf().disable()

                // Disable default session managenment

        ;*/
        JWTLoginFilter jwtFilter = new JWTLoginFilter(authenticationManagerBean());
        jwtFilter.setFilterProcessesUrl("/api/login");


        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/registration/").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(jwtFilter);
        http.addFilterBefore(new JWTValidationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // In memory storing
        /*auth.inMemoryAuthentication()
                .withUser("prekas")
                .password(passwordEncoder.encode("password"))
                .authorities("USER", "ADMIN");*/

        // In database storing
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
