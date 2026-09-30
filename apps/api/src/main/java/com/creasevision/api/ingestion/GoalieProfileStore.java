package com.creasevision.api.ingestion;

import com.creasevision.api.model.Goalie;

public interface GoalieProfileStore { void saveProfile(Goalie importedGoalie); }
