// let data;
const Goalie = ({ data}: { data: any}) => {
//   try {
//      const res = await fetch(`http://localhost:8000/goalies/${playerId}`, {
//   cache: "no-store",
// });

// console.log("STATUS:", res.status);
// console.log("OK:", res.ok);

// // console.log("RAW RESPONSE:", text);

// if (!res.ok) {
//   throw new Error(`API failed with ${res.status}`);
// }
// const text = await res.text();

// data = JSON.parse(text);
// console.log("PARSED:", data);

//   } catch {
//       return <p>Failed to load goalie data.</p>;
//   }


//stats
const firstName = data?.player?.firstName?.default
const lastName = data?.player?.lastName?.default
const headshot = data?.player?.headshot
const highDanger = data?.shotLocationSummary[1]?.savePctg ?? 0
const highGoalsAgainst = data?.shotLocationSummary[1]?.goalsAgainst ?? 0
const highSaves = data?.shotLocationSummary[1]?.saves ?? 0
const highShotsAgainst = highGoalsAgainst + highSaves

const teamLogo = data?.player?.team?.teamLogo.dark

const midDanger = data?.shotLocationSummary[3]?.savePctg ?? 0
const midGoalsAgainst = data?.shotLocationSummary[3]?.goalsAgainst ?? 0
const midSaves = data?.shotLocationSummary[3]?.saves ?? 0
const midShotsAgainst = midGoalsAgainst + midSaves

const longDanger = data?.shotLocationSummary[2]?.savePctg ?? 0
const longGoalsAgainst = data?.shotLocationSummary[2]?.goalsAgainst ?? 0
const longSaves = data?.shotLocationSummary[2]?.saves ?? 0
const longShotsAgainst = longGoalsAgainst + longSaves

const allDanger = data?.shotLocationSummary[0]?.savePctg ?? 0
const allGoalsAgainst = data?.shotLocationSummary[0]?.goalsAgainst ?? 0
const allSaves = data?.shotLocationSummary[0]?.saves ?? 0
const allShotsAgainst = allGoalsAgainst + allSaves

//weak point func
const shotLocoDetails = data?.shotLocationDetails
let weakPoint = 0
for(let i=0; i<shotLocoDetails.length-1; i++) {
  if(shotLocoDetails[i].savePctg < shotLocoDetails[weakPoint].savePctg && shotLocoDetails[i].saves > 5) {
    weakPoint = i;
  }
}
const weakPointName = shotLocoDetails[weakPoint].area
const weakPointSv = shotLocoDetails[weakPoint].savePctg
const weakPointSaves = shotLocoDetails[weakPoint].saves

const allSvColor = allDanger >= 0.900 ? "text-emerald-400" : "text-red-400"


  return (
     <>
     <div className="flex flex-wrap justify-center gap-2">
    <div className="w-64 bg-slate-800 rounded-xl p-4 hover:bg-slate-700 transition transform hover:scale-105 text-center border border-slate-700">
      <img src={teamLogo} alt="" className="mx-auto w-32 h-32 rounded-full"/>
      <img src={headshot} alt="" className="mx-auto w-32 h-32 rounded-full"/>

  <h1 className="text-lg font-semibold text-center mb-1">
    {firstName + " " + lastName}
  </h1>

  <h2 className="text-lg">Overall</h2>
    <p className={`text-slate-400 text-lg`}>
      Sv%: <strong className={allSvColor}>{Number(allDanger).toFixed(3)}%</strong> <br></br>
      Shots Faced: <strong>{allShotsAgainst}</strong> <br></br>
    </p>

  <h2>High Danger</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(highDanger).toFixed(3)}%</strong> <br></br>
      Shots: <strong>{highShotsAgainst}</strong> <br></br>
    </p>

    <h2>Mid Range</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(midDanger).toFixed(3)}%</strong> <br></br>
      Shots: <strong>{midShotsAgainst}</strong> <br></br>
    </p>

    <h2>Long Range</h2>
    <p className="text-slate-400">
      Sv%: <strong>{Number(longDanger).toFixed(3)}%</strong> <br></br>
      Shots: <strong>{longShotsAgainst}</strong> <br></br>
    </p>

    <h2>Weakest Area</h2>
    <p className="text-slate-400">{weakPointName} <br />
     Sv%: <strong>{Number(weakPointSv).toFixed(3)}%</strong> <br />
     Total Saves: <strong>{weakPointSaves}</strong>
    </p>
</div>

{/* <div>
  <h1>{firstName} {lastName}</h1>
  <img src={headshot} alt="" />
</div> */}

     </div>
    
    </>
  )
}

export default Goalie