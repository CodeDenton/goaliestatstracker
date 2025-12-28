
const Goalie = async ({ playerId }: { playerId: string }) => {
    const res = await fetch(`http://localhost:8000/goalies/${playerId} `, {
  cache: "no-store",
});

console.log("STATUS:", res.status);
console.log("OK:", res.ok);

const text = await res.text();
// console.log("RAW RESPONSE:", text);

if (!res.ok) {
  throw new Error(`API failed with ${res.status}`);
}

const data = JSON.parse(text);
console.log("PARSED:", data);

//stats
const firstName = data?.player?.firstName?.default
const lastName = data?.player?.lastName?.default
const headshot = data?.player?.headshot
const highDanger = data?.shotLocationSummary[1]?.savePctg
const highGoalsAgainst = data?.shotLocationSummary[1]?.goalsAgainst
const highSaves = data?.shotLocationSummary[1]?.saves
const highShotsAgainst = highGoalsAgainst + highSaves

const midDanger = data?.shotLocationSummary[3]?.savePctg
const midGoalsAgainst = data?.shotLocationSummary[3]?.goalsAgainst
const midSaves = data?.shotLocationSummary[3]?.saves
const midShotsAgainst = midGoalsAgainst + midSaves

const longDanger = data?.shotLocationSummary[2]?.savePctg
const longGoalsAgainst = data?.shotLocationSummary[2]?.goalsAgainst
const longSaves = data?.shotLocationSummary[2]?.saves
const longShotsAgainst = longGoalsAgainst + longSaves

const allDanger = data?.shotLocationSummary[0]?.savePctg
const allGoalsAgainst = data?.shotLocationSummary[0]?.goalsAgainst
const allSaves = data?.shotLocationSummary[0]?.saves
const allShotsAgainst = allGoalsAgainst + allSaves

//weak point func
const shotLocoDetails = data?.shotLocationDetails
let weakPoint = 0
for(let i=0; i<shotLocoDetails.length-1; i++) {
  if(shotLocoDetails[i].savePctg < shotLocoDetails[weakPoint].savePctg) {
    weakPoint = i;
  }
}
const weakPointName = shotLocoDetails[weakPoint].area
const weakPointSv = shotLocoDetails[weakPoint].savePctg

  return (
     <>
    <div className="w-64 bg-slate-800 rounded-xl p-4 hover:bg-slate-700 transition text-center">
      <img src={headshot} alt="" className="mx-auto w-32 h-32 rounded-full mb-2"/>
  <h1 className="text-lg font-semibold text-center mb-0">
    {firstName + " " + lastName}
  </h1>
  <h2>High Danger</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(highDanger).toFixed(3)}</strong> <br></br>
      Shots Faced <strong>{highShotsAgainst}</strong> <br></br>
    </p>

    <h2>Mid Range</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(midDanger).toFixed(3)}</strong> <br></br>
      Shots Faced <strong>{midShotsAgainst}</strong> <br></br>
    </p>

    <h2>Long Range</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(longDanger).toFixed(3)}</strong> <br></br>
      Shots Faced <strong>{longShotsAgainst}</strong> <br></br>
    </p>

    <h2>Overall</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(allDanger).toFixed(3)}</strong> <br></br>
      Shots Faced <strong>{allShotsAgainst}</strong> <br></br>
    </p>

    <h2>Weakest Area</h2>
    <p className="text-slate-400">{weakPointName} <br />
     Sv%: <strong>{Number(weakPointSv).toFixed(3)}</strong>
    </p>



  
</div>
    </>
  )
}

export default Goalie