package fr.insee.publicenemy.api;

import fr.insee.publicenemy.api.configuration.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "fr.insee.publicenemy")
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class PublicEnemyApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
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
