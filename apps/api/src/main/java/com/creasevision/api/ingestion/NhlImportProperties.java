package com.creasevision.api.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nhl")
public class NhlImportProperties {

    private String season = "20262027";
    private String situation = "2";
    private String importSeason;
    private String importSeasons;
    private Long importGameId;
    private boolean refreshOnStartup = true;
    private String refreshSecret;

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public String getImportSeason() {
        return importSeason;
    }

    public void setImportSeason(String importSeason) {
        this.importSeason = importSeason;
    }

    public String getImportSeasons() {
        return importSeasons;
    }

    public void setImportSeasons(String importSeasons) {
        this.importSeasons = importSeasons;
    }

    public Long getImportGameId() { return importGameId; }

    public void setImportGameId(Long importGameId) { this.importGameId = importGameId; }

    public boolean isRefreshOnStartup() {
        return refreshOnStartup;
    }

    public void setRefreshOnStartup(boolean refreshOnStartup) {
        this.refreshOnStartup = refreshOnStartup;
    }

    public String getRefreshSecret() { return refreshSecret; }
    public void setRefreshSecret(String refreshSecret) { this.refreshSecret = refreshSecret; }
}
