package com.example.asanturagent.security.config;

import com.example.asanturagent.services.AgentRegistrationService;
import com.example.asanturagent.util.jwt.JwtRequestFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtRequestFilter jwtRequestFilter;
    private final AgentRegistrationService agentRegistrationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable().authorizeRequests()
//                .antMatchers("/api/v1/registration/confirm/token/*").permitAll()
                .antMatchers("/api/v1/registration/*/*").permitAll()
                .antMatchers("/api/v1/forgot/*").permitAll().anyRequest().authenticated()
                .and().exceptionHandling()
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//antMatchers("/api/v1/forgot/*").permitAll()
//
//        http.csrf().disable().authorizeRequests().antMatchers("/api/v1/registration/confirm/token/*").permitAll().anyRequest().authenticated().antMatchers("/api/v*/*/*")
//                .permitAll()
//                .anyRequest()
//                .authenticated()
//                .and().exceptionHandling()
//                .and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);


//        http
//                .csrf().disable()
//                .authorizeRequests()
////                .antMatchers("/api/v*/registration/**")
//                .antMatchers("api/v*/*/*").permitAll().anyRequest()

//                .antMatchers("/api/*/*/*/*").permitAll().anyRequest()
//                .antMatchers("/api/*/*/*")
//                .permitAll()
//                .antMatchers("/api/forgot/*").permitAll().anyRequest()
//                .authenticated();

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//                .authenticated().and()
//                .formLogin();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(agentRegistrationService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
