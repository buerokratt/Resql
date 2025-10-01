package rig.sqlms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfiguration {
    @Value("${headers.content-security-policy}")
    private String contentSecurityPolicy;

    public SecurityConfiguration() {
        super();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(header -> header
                        .contentSecurityPolicy(csp -> csp.policyDirectives(contentSecurityPolicy)))
                .authorizeHttpRequests(authorize -> authorize
                                .anyRequest().permitAll()
                ).csrf(csrf -> csrf.disable())
        ;
        return http.build();
    }
}
