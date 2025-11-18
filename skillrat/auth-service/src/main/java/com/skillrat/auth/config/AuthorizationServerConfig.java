package com.skillrat.auth.config;

import java.time.Duration;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
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
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.skillrat.auth.password.SkillratPasswordAuthenticationConverter;
import com.skillrat.auth.password.SkillratPasswordAuthenticationProvider;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder) {
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        org.springframework.security.oauth2.server.authorization.token.JwtGenerator jwtGenerator = new org.springframework.security.oauth2.server.authorization.token.JwtGenerator(jwtEncoder);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            RSAKey rsa = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyID(java.util.UUID.randomUUID().toString())
                    .build();
            JWKSet set = new JWKSet(rsa);
            return new ImmutableJWKSet<>(set);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    @Bean
    public org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http,
                                                             OAuth2AuthorizationService authorizationService,
                                                             RegisteredClientRepository registeredClientRepository,
                                                             OAuth2TokenGenerator<?> tokenGenerator,
                                                             org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.cors(cors -> {});
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(new SkillratPasswordAuthenticationConverter())
                        .authenticationProvider(new SkillratPasswordAuthenticationProvider(authorizationService, registeredClientRepository, tokenGenerator, jwtEncoder))
                );
        return http.build();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/**", "/oauth/check_token", "/oauth/dev/**").permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/oauth/check_token", "/oauth/dev/**"))
            .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource) {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    public DataSourceInitializer authSchema(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public CommandLineRegisteredClientLoader commandLineRegisteredClientLoader(RegisteredClientRepository repo, DataSource dataSource) {
        return new CommandLineRegisteredClientLoader(repo, dataSource);
    }

    public static class CommandLineRegisteredClientLoader implements CommandLineRunner {
        private final RegisteredClientRepository repo;
        private final JdbcTemplate jdbcTemplate;

        public CommandLineRegisteredClientLoader(RegisteredClientRepository repo, DataSource dataSource) {
            this.repo = repo;
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Override
        public void run(String... args) {
            String clientId = "gateway";

            // Delete existing
            jdbcTemplate.update("delete from oauth2_registered_client where client_id = ?", clientId);

            RegisteredClient desired = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret("{noop}gateway-secret")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:skillrat-password"))
                    .scope("gateway")
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(30))
                            .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                            .reuseRefreshTokens(false)
                            .build())
                    .clientSettings(ClientSettings.builder().requireProofKey(false).build())
                    .build();

            ((JdbcRegisteredClientRepository) repo).save(desired);
        }
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of("*"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
