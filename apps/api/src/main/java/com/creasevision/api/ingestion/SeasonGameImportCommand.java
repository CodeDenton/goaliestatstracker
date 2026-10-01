package com.creasevision.api.ingestion;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="nhl.import-game-season") public class SeasonGameImportCommand implements ApplicationRunner {
 private final SeasonGameImportService importer; private final NhlImportProperties properties;
 public SeasonGameImportCommand(SeasonGameImportService importer,NhlImportProperties properties){this.importer=importer;this.properties=properties;}
 @Override public void run(ApplicationArguments args){importer.importSeason(properties.getImportGameSeason());}
}
