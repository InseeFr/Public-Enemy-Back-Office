package fr.insee.publicenemy.api.configuration;

import fr.insee.publicenemy.api.application.web.auth.AuthenticationHelper;
import fr.insee.publicenemy.api.configuration.rest.WebClientTokenInterceptor;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;


@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = { "fr.insee.publicenemy.api" })
@EnableTransactionManagement
@EnableCaching
@Slf4j
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationHelper authenticationHelper;


    /**
     * 
     * @param proxyUrl proxy url
     * @param proxyPort proxy port
     * @param builder webclient builder
     * @return webclient configured with proxy
     */
    @Bean
    @ConditionalOnProperty(name="feature.proxy.enabled", havingValue="true")
    public WebClient webClientProxy(
            @Value("${feature.proxy.url}") String proxyUrl,
            @Value("${feature.proxy.port}") Integer proxyPort,
            @Value("${feature.debug.webclient}") boolean debug,
            @Value("${feature.oidc.enabled}") boolean oidcEnabled,
            WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                .type(ProxyProvider.Proxy.HTTP)
                .host(proxyUrl)
                .port(proxyPort));

        if(debug) {
            httpClient = httpClient.wiretap("reactor.netty.http.client.HttpClient",
                    LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        }

        builder
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if(oidcEnabled) builder.filter(new WebClientTokenInterceptor(authenticationHelper));
        return builder.build();
    }

    /**
     * 
     * @param builder webclient builder
     * @return webclient with json default headers
     */
    @Bean
    @ConditionalOnProperty(name="feature.proxy.enabled", havingValue="false")
    public WebClient webClient(
            @Value("${feature.oidc.enabled}") boolean oidcEnabled,
            WebClient.Builder builder) {
        builder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if(oidcEnabled)  builder.filter(new WebClientTokenInterceptor(authenticationHelper));
        return builder.build();
    }
}
