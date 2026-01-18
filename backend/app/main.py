import requests
import asyncio
import aiohttp
from fastapi import FastAPI, HTTPException
from typing import List, Dict

app = FastAPI()

SEASON = "20252026"
SITUATION = "2"

def fetch_team_roster(team_abbrev: str) -> Dict:
    """Fetch roster for a specific team"""
    url = f"https://api-web.nhle.com/v1/roster/{team_abbrev}/current"
    res = requests.get(url, timeout=5)
    
    if res.status_code != 200:
        return {}
    
    return res.json()

def fetch_all_goalie_ids() -> List[int]:
    """Automatically fetch all goalie IDs from current rosters"""
    goalie_ids = set()
    
    teams = [
        "ANA", "BOS", "BUF", "CAR", "CBJ", "CGY", "CHI", "COL", "DAL", "DET",
        "EDM", "FLA", "LAK", "MIN", "MTL", "NJD", "NSH", "NYI", "NYR", "OTT",
        "PHI", "PIT", "SEA", "SJS", "STL", "TBL", "TOR", "UTA", "VAN", "VGK",
        "WPG", "WSH"
    ]
    
    for team in teams:
        try:
            roster = fetch_team_roster(team)
            goalies = roster.get("goalies", [])
            
            for goalie in goalies:
                player_id = goalie.get("id")
                if player_id:
                    goalie_ids.add(player_id)
                    
        except Exception as e:
            print(f"Error fetching roster for {team}:", e)
            continue
    
    return sorted(list(goalie_ids))

def fetch_goalie_detail(player_id: int):
    """Fetch detailed stats for a single goalie (synchronous)"""
    url = f"https://api-web.nhle.com/v1/edge/goalie-detail/{player_id}/{SEASON}/{SITUATION}"
    res = requests.get(url, timeout=5)

    if res.status_code != 200:
        raise Exception("NHL API failed")

    return res.json()

async def fetch_goalie_detail_async(session, player_id: int):
    """Fetch detailed stats for a single goalie (asynchronous for parallel requests)"""
    url = f"https://api-web.nhle.com/v1/edge/goalie-detail/{player_id}/{SEASON}/{SITUATION}"
    try:
        async with session.get(url, timeout=aiohttp.ClientTimeout(total=10)) as response:
            if response.status != 200:
                print(f"Failed to fetch goalie {player_id}: status {response.status}")
                return None
            return await response.json()
    except Exception as e:
        print(f"Error fetching goalie {player_id}: {e}")
        return None

@app.get("/goalies")
def get_goalie_ids():
    """
    Get all goalie IDs from current NHL rosters
    Returns: List of objects with goalie IDs
    """
    try:
        goalie_ids = fetch_all_goalie_ids()
        return [{"id": str(gid)} for gid in goalie_ids]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to fetch goalie IDs: {str(e)}")

@app.get("/goalies/full")
async def get_all_goalies(limit: int = None):
    """
    Fetch full details for all goalies using async/parallel requests
    Use ?limit=10 to limit results for testing
    Returns: Array of goalie detail objects
    """
    goalie_ids = get_goalie_ids()
    
    if limit:
        goalie_ids = goalie_ids[:limit]
    
    results = []
    
    print(f"Fetching {len(goalie_ids)} goalies in parallel...")
    
    async with aiohttp.ClientSession() as session:
        tasks = []
        for g in goalie_ids:
            task = fetch_goalie_detail_async(session, int(g["id"]))
            tasks.append(task)
        
        responses = await asyncio.gather(*tasks, return_exceptions=True)
        
        for i, response in enumerate(responses):
            if response and not isinstance(response, Exception):
                results.append(response)
            elif isinstance(response, Exception):
                print(f"Exception for goalie {goalie_ids[i]['id']}: {response}")
    
    print(f"Successfully fetched {len(results)} out of {len(goalie_ids)} goalies")
    
    return results

@app.get("/goalies/{player_id}")
def get_goalie(player_id: int):
    """
    Get detailed stats for a specific goalie by ID
    """
    try:
        return fetch_goalie_detail(player_id)
    except:
        raise HTTPException(status_code=500, detail="Failed to fetch goalie")