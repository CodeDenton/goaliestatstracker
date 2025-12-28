import requests
from fastapi import FastAPI, HTTPException

app = FastAPI()

@app.get("/goalies/{player_id}")
def get_goalie(player_id: int):
    url = f"https://api-web.nhle.com/v1/edge/goalie-detail/{player_id}/20252026/2"
    response = requests.get(url)

    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="Failed to fetch NHL API")

    # Return the full JSON as-is
    return response.json()
