package com.creasevision.api.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "nhl.import-game-id")
public class GameImportCommand implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(GameImportCommand.class);
    private final GameImportService importer; private final NhlImportProperties properties;
    public GameImportCommand(GameImportService importer, NhlImportProperties properties) { this.importer=importer; this.properties=properties; }
    @Override public void run(ApplicationArguments args) { log.info("Game import {} wrote {} goalie stat lines.", properties.getImportGameId(), importer.importGame(properties.getImportGameId())); }
}
