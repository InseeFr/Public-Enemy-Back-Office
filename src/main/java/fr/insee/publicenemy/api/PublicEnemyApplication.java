package fr.insee.publicenemy.api;

import fr.insee.publicenemy.api.configuration.PropertiesLogger;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "fr.insee.publicenemy")
@EnableTransactionManagement
@EnableAsync
@ConfigurationPropertiesScan
@Slf4j
public class PublicEnemyApplication {

	@Value("${application.timezoneId}")
	private String applicationTimeZoneId;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
	}

	@PostConstruct
	public void executeAfterMain() {
		log.info("Timezone is set to '{}'", applicationTimeZoneId);
		TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZoneId));
	}

	/**
	 * @param springApplicationBuilder
	 * @return SpringApplicationBuilder
	 */
	public static SpringApplicationBuilder configureApplicationBuilder(
			SpringApplicationBuilder springApplicationBuilder) {
		return springApplicationBuilder.sources(PublicEnemyApplication.class)
				.listeners(new PropertiesLogger());
	}

}
