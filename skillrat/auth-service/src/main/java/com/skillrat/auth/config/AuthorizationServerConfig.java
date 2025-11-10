package com.skillrat.auth.config;

import com.skillrat.auth.password.SkillratPasswordAuthenticationConverter;
import com.skillrat.auth.password.SkillratPasswordAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http,
                                                             OAuth2AuthorizationService authorizationService,
                                                             RegisteredClientRepository registeredClientRepository,
                                                             OAuth2TokenGenerator<?> tokenGenerator) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(new SkillratPasswordAuthenticationConverter())
                        .authenticationProvider(new SkillratPasswordAuthenticationProvider(authorizationService, registeredClientRepository, tokenGenerator))
                );
        return http.build();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/actuator/**"),
                                 new AntPathRequestMatcher("/oauth/check_token")).permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(resource -> resource.opaqueToken(Customizer.withDefaults()))
            .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource) {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    public DataSourceInitializer authSchema(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public CommandLineRegisteredClientLoader commandLineRegisteredClientLoader(RegisteredClientRepository repo) {
        return new CommandLineRegisteredClientLoader(repo);
    }

    public static class CommandLineRegisteredClientLoader implements org.springframework.boot.CommandLineRunner {
        private final RegisteredClientRepository repo;
        public CommandLineRegisteredClientLoader(RegisteredClientRepository repo) { this.repo = repo; }
        @Override
        public void run(String... args) {
            // Ensure a default gateway client exists
            String clientId = "gateway";
            RegisteredClient existing = ((JdbcRegisteredClientRepository) repo).findByClientId(clientId);
            if (existing == null) {
                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(clientId)
                        .clientSecret("{noop}gateway-secret")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:skillrat-password"))
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofMinutes(30))
                                .reuseRefreshTokens(false)
                                .build())
                        .clientSettings(ClientSettings.builder().requireProofKey(false).build())
                        .build();
                repo.save(client);
            }
        }
    }
}
