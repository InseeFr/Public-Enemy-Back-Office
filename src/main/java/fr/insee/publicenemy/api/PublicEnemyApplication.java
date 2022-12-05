package fr.insee.publicenemy.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import fr.insee.publicenemy.api.configuration.PropertiesLogger;

@SpringBootApplication
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
