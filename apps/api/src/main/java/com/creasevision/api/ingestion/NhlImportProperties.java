package com.creasevision.api.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nhl")
public class NhlImportProperties {

    private String season = "20262027";
    private String situation = "2";
    private boolean refreshOnStartup = true;

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

    public boolean isRefreshOnStartup() {
        return refreshOnStartup;
    }

    public void setRefreshOnStartup(boolean refreshOnStartup) {
        this.refreshOnStartup = refreshOnStartup;
    }
}
