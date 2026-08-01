package com.dawak.api.catalogue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("local")
public class LocalCatalogueDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(LocalCatalogueDataInitializer.class);

    private final DataSource dataSource;

    public LocalCatalogueDataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        var populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/local/sample_catalogue.sql"));
        populator.setSeparator(";");
        populator.setContinueOnError(false);
        populator.execute(dataSource);
        log.info("Local sample catalogue data is ready");
    }
}
