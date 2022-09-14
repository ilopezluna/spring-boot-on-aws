package io.ilopezluna.application.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

import java.util.Properties;

@RequiredArgsConstructor
@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseConfig {

    private final ConfigurableEnvironment environment;

    @Value("${spring.datasource.aws.database.secret}")
    private String awsSecret;

    @Primary
    @Bean
    public DataSourceProperties customDataSourceProperties(DataSourceProperties dataSourceProperties) {
        boolean usedAwsSecret = false;
        try {
            if (StringUtils.hasText(awsSecret)) {
                final JSONObject dbCredentials = new JSONObject(awsSecret);
                dataSourceProperties.setUsername(dbCredentials.getString("username"));
                dataSourceProperties.setPassword(dbCredentials.getString("password"));

                // Needed since Hikari reads properties directly
                final Properties properties = new Properties();
                properties.setProperty("spring.datasource.username", dataSourceProperties.getUsername());
                properties.setProperty("spring.datasource.password", dataSourceProperties.getPassword());
                environment.getPropertySources().addFirst(new PropertiesPropertySource("aws-custom-datasource-properties", properties));

                usedAwsSecret = true;
            }
        } catch (JSONException e) {
            log.error("Datasource AWS secret property was set but an error occurred parsing the value");
        }

        if (!usedAwsSecret) {
            log.info("No AWS credentials secret was configured. Falling back to properties set for username/password.");
        }

        return dataSourceProperties;
    }
}
