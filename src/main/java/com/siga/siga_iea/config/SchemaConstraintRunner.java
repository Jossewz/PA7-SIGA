package com.siga.siga_iea.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaConstraintRunner implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            try (InputStream is = getClass().getResourceAsStream("/schema-constraints.sql")) {
                if (is != null) {
                    String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    stmt.execute(sql);
                    log.info("CHECK constraints aplicados correctamente.");
                } else {
                    log.warn("No se encontró el archivo /schema-constraints.sql");
                }
            }

        } catch (Exception e) {
            log.warn("No se pudieron aplicar CHECK constraints: {}", e.getMessage());
        }
    }
}
