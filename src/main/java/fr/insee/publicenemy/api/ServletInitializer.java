package fr.insee.publicenemy.api;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * To launch application if deployed as war in tomcat
 *
 * @deprecated : Prefer deploying app as a fat jar
 */
@Deprecated
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return PublicEnemyApplication.configureApplicationBuilder(application);
	}

}
