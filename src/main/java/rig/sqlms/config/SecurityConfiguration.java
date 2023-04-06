package rig.sqlms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Value("${headers.content-security-policy}")
    private String contentSecurityPolicy;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .contentSecurityPolicy(contentSecurityPolicy);
        return http.build();
    }
}
