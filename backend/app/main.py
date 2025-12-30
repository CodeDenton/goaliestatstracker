import requests
from fastapi import FastAPI, HTTPException

app = FastAPI()

# --- CONFIG ---
SEASON = "20252026"
SITUATION = "2"

# --- HELPERS ---

def fetch_goalie_detail(player_id: int):
    url = f"https://api-web.nhle.com/v1/edge/goalie-detail/{player_id}/{SEASON}/{SITUATION}"
    res = requests.get(url, timeout=5)

    if res.status_code != 200:
        raise Exception("NHL API failed")

    return res.json()

# --- ROUTES ---

@app.get("/goalies")
def get_goalie_ids():
    """
    TEMP: hardcode known-working goalie IDs
    (later you can auto-fetch this)
    """
    return [
        {"id": "8476412"},  # example
        {"id": "8476945"},
        {"id": "8477992"},
        {"id": "8482661"},
    ]

@app.get("/goalies/full")
def get_all_goalies():
    goalie_ids = get_goalie_ids()
    results = []

    for g in goalie_ids:
        try:
            data = fetch_goalie_detail(int(g["id"]))
            results.append(data)
        except Exception as e:
            print(f"Skipping goalie {g['id']}:", e)
            continue

    return results

@app.get("/goalies/{player_id}")
def get_goalie(player_id: int):
    try:
        return fetch_goalie_detail(player_id)
    except:
        raise HTTPException(status_code=500, detail="Failed to fetch goalie")



