package com.tokenplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // Public pages (no login required)
                .antMatchers("/css/**", "/js/**", "/fonts/**").permitAll()
                .antMatchers("/auth/**", "/error").permitAll()
                .antMatchers("/api/v1/**").permitAll()
                .antMatchers("/", "/tools/**", "/blog/**").permitAll()
                // 🔒 Admin-only pages
                .antMatchers("/dashboard/**", "/recharge/**", "/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                // 🔀 Login redirect depends on role (handled by controller)
                .successHandler((request, response, authentication) -> {
                    // Admin → dashboard, regular logged-in user → tools
                    response.sendRedirect("/tools");
                })
                .permitAll()
            .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/tools")
                .permitAll()
            .and()
            .csrf()
                .ignoringAntMatchers("/api/v1/**", "/auth/register", "/auth/login")
            .and()
            .rememberMe()
                .key("token-platform-remember-me")
                .tokenValiditySeconds(604800);
    }
}
