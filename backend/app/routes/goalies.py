@app.get("/goalies/{player_id}")
def get_goalie(player_id: int):
    data = fetch_player_json(player_id)

    # safety check
    if data["position"] != "G":
        return {"error": "Player is not a goalie"}

    career = data["careerTotals"]["regularSeason"]
    season = data["featuredStats"]["regularSeason"]["subSeason"]

    return {
        "id": data["playerId"],
        "name": f'{data["firstName"]["default"]} {data["lastName"]["default"]}',
        "team": data["currentTeamAbbrev"],
        "position": "G",
        "headshot": data["headshot"],
        "heightInches": data["heightInInches"],
        "weight": data["weightInPounds"],
        "career": {
            "gamesPlayed": career["gamesPlayed"],
            "wins": career["wins"],
            "losses": career["losses"],
            "otLosses": career["otLosses"],
            "savePctg": career["savePctg"],
            "goalsAgainstAvg": career["goalsAgainstAvg"],
            "shutouts": career["shutouts"]
        },
        "season": {
            "gamesPlayed": season["gamesPlayed"],
            "wins": season["wins"],
            "losses": season["losses"],
            "otLosses": season["otLosses"],
            "savePctg": season["savePctg"],
            "goalsAgainstAvg": season["goalsAgainstAvg"],
            "shutouts": season["shutouts"]
        }
    }
