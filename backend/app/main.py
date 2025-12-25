from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import requests

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # fine for local dev
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/goalie/{player_id}")
def goalie_stats(player_id: int):
    url = f"https://api-web.nhle.com/v1/player/{player_id}/landing"

    res = requests.get(url)
    if res.status_code != 200:
        raise HTTPException(status_code=404, detail="Player not found")

    return res.json()

@app.get("/ping")
def ping():
    return {"status": "ok"}
