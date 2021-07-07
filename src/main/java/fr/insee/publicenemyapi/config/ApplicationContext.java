package fr.insee.publicenemyapi.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Configuration
@ComponentScan("fr.insee.publicenemyapi.*")
@PropertySource(value = {"classpath:/publicenemybo.properties", "file:${properties.path}/publicenemybo.properties"}, ignoreResourceNotFound = true)
public class ApplicationContext {

    @Value("${fr.insee.publicenemyapi.persistence.database.host}")
    String dbHost;

    @Value("${fr.insee.publicenemyapi.persistence.database.port}")
    String dbPort;

    @Value("${fr.insee.publicenemyapi.persistence.database.schema}")
    String dbSchema;

    @Value("${fr.insee.publicenemyapi.persistence.database.user}")
    private String dbUser;

    @Value("${fr.insee.publicenemyapi.persistence.database.password}")
    private String dbPassword;

    @Value("${fr.insee.publicenemyapi.persistence.database.driver}")
    private String dbDriver;
    @Value("${fr.insee.publicenemyapi.env}")
    String environment;



    /***
     * This method create a new Datasource object
     * @return new Datasource
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbSchema));
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    /***
     * This method return datasource connection
     * @param dataSource
     * @return Connection
     * @throws SQLException
     */
    @Bean
    public Connection connection(DataSource dataSource) throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    /***
     * Create a new JdbcTemplate with a datasource passed in parameter
     * @param dataSource
     * @return JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = null;
        try {
            jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection(dataSource), false));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        return jdbcTemplate;
    }
    @Bean(name="liquibase")
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setContexts(environment);
        liquibase.setChangeLog("classpath:db/master.xml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
