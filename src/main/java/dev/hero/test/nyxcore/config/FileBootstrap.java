package dev.hero.test.nyxcore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBootstrap {

    private static final Logger log = LoggerFactory.getLogger(FileBootstrap.class);
    private static final String CONFIG_DIR = "./config";
    private static final String LOGS_DIR = "./logs";

    public static void ensureConfig() {
        ensureDirectory(CONFIG_DIR);
        ensureDirectory(LOGS_DIR);

        seedMissingFile("defaults/application.properties", CONFIG_DIR + "/application.properties");
        seedMissingFile("defaults/hosts.json", CONFIG_DIR + "/hosts.json");
        seedMissingFile("defaults/commands.json", CONFIG_DIR + "/commands.json");
        seedMissingFile("defaults/dashboard.json", CONFIG_DIR + "/dashboard.json");
    }

    private static void ensureDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Created missing directory: {}", path);
            } else {
                log.error("Failed to create directory: {}", path);
            }
        }
    }

    private static void seedMissingFile(String resourcePath, String targetPath) {
        File targetFile = new File(targetPath);

        if (targetFile.exists()) {
            return;
        }

        try (InputStream in = FileBootstrap.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                log.warn("Template {} not found inside the JAR.", resourcePath);
                return;
            }
            Files.copy(in, Path.of(targetPath));
            log.info("Seeded missing configuration file: {}", targetPath);
        } catch (Exception e) {
            log.error("Failed to seed file: {}", targetPath, e);
        }
    }
}
