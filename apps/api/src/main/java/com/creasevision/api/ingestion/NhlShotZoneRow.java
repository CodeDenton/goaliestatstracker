package com.creasevision.api.ingestion;
public record NhlShotZoneRow(Long gameId,String seasonId,Long goalieId,String team,String zoneCode,int shots,int saves,int goals) { }
