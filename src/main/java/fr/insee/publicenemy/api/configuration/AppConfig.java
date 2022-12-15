package fr.insee.publicenemy.api.configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = { "fr.insee.publicenemy.api" })
@EnableTransactionManagement
@EnableCaching
public class AppConfig implements WebMvcConfigurer {

    /**
     * 
     * @param proxyUrl
     * @param proxyPort
     * @param builder
     * @return webclient configured with proxy
     */
    @Bean
    @ConditionalOnProperty(name="application.proxy.enable", havingValue="true")
    public WebClient webClientProxy(@Value("${application.proxy.url}") String proxyUrl, 
            @Value("${application.proxy.port}") Integer proxyPort, 
            WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress(proxyUrl, proxyPort)))
            .build();
        
        builder
            .clientConnector(new JdkClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return builder.build();
    }

    /**
     * 
     * @param builder
     * @return webclient with json default headers
     */
    @Bean
    @ConditionalOnProperty(name="application.proxy.enable", havingValue="false")
    public WebClient webClient(WebClient.Builder builder) {
        builder
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return builder.build();
    }
}
