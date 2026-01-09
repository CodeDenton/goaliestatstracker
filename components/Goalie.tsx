"use client"
import { useState } from "react"
import ZoneMapModel from "./ZoneMapModel"

const Goalie = ({ data }: { data: any }) => {
  const [showZoneMap, setShowZoneMap] = useState(false)

  //stats
  const teamLogo = data?.player?.team?.teamLogo.dark
  const firstName = data?.player?.firstName?.default
  const lastName = data?.player?.lastName?.default
  const headshot = data?.player?.headshot

  const highDanger = data?.shotLocationSummary[1]?.savePctg ?? 0
  const highDangerSavePercentile = data?.shotLocationSummary[1].savePctgPercentile ?? 0
  const highGoalsAgainst = data?.shotLocationSummary[1]?.goalsAgainst ?? 0
  const highSaves = data?.shotLocationSummary[1]?.saves ?? 0
  const highShotsAgainst = highGoalsAgainst + highSaves

  const midDanger = data?.shotLocationSummary[3]?.savePctg ?? 0
  const midDangerSavePercentile = data?.shotLocationSummary[3].savePctgPercentile ?? 0
  const midGoalsAgainst = data?.shotLocationSummary[3]?.goalsAgainst ?? 0
  const midSaves = data?.shotLocationSummary[3]?.saves ?? 0
  const midShotsAgainst = midGoalsAgainst + midSaves

  const longDanger = data?.shotLocationSummary[2]?.savePctg ?? 0
  const longDangerSavePercentile = data?.shotLocationSummary[2].savePctgPercentile ?? 0
  const longGoalsAgainst = data?.shotLocationSummary[2]?.goalsAgainst ?? 0
  const longSaves = data?.shotLocationSummary[2]?.saves ?? 0
  const longShotsAgainst = longGoalsAgainst + longSaves

  const allDanger = data?.shotLocationSummary[0]?.savePctg ?? 0
  const allDangerSavePercentile = data?.shotLocationSummary[0].savePctgPercentile ?? 0
  const allGoalsAgainst = data?.shotLocationSummary[0]?.goalsAgainst ?? 0
  const allSaves = data?.shotLocationSummary[0]?.saves ?? 0
  const allShotsAgainst = allGoalsAgainst + allSaves

  //weak point helper func
  const shotLocoDetails = data?.shotLocationDetails
  let weakPoint = 0
  for (let i = 0; i < shotLocoDetails.length - 1; i++) {
    if (shotLocoDetails[i].savePctg < shotLocoDetails[weakPoint].savePctg && shotLocoDetails[i].saves > 5) {
      weakPoint = i;
    }
  }
  const weakPointName = shotLocoDetails[weakPoint].area ?? 0
  const weakPointSv = shotLocoDetails[weakPoint].savePctg ?? 0
  const weakPointSavePercentile = shotLocoDetails[weakPoint].savePctgPercentile ?? 0
  const weakPointSaves = shotLocoDetails[weakPoint].saves ?? 0

  const allSvColor = allDanger >= 0.900 ? "text-emerald-400" : "text-red-400"


  return (
    <>
    {!showZoneMap ? (
      // Standard Cards View
       <div className="flex flex-wrap justify-left gap-2" onClick={() => setShowZoneMap(true)}>
        <div className="w-64 bg-[#343A40] rounded-xl p-4 hover:bg-[#6C757D] transition transform hover:scale-105 text-center">
          <img src={teamLogo} alt="" className="mx-auto w-32 h-32 rounded-full" />
          <img src={headshot} alt="" className="mx-auto w-32 h-32 rounded-full" />

          <h1 className="text-lg font-semibold text-center mb-1">
            {firstName + " " + lastName}
          </h1>

          <h2 className="text-lg">Overall</h2>
          <p className={`text-slate-400 text-lg`}>
            Sv%: <strong className={allSvColor}>{Number(allDanger).toFixed(3)}%</strong> <br></br>
            Shots Faced: <strong>{allShotsAgainst}</strong> <br></br>
          </p>
        </div>
      </div>
    ) : (
      // Card Clicked Zone View
      <div className="fixed inset-0 z-50 bg-[#495057] flex">
         <div className="w-80 bg-[#343A40] p-4 overflow-y-auto border-r border-[#6C757D]">
            <button 
              onClick={() => setShowZoneMap(false)}
              className="mb-4 text-slate-400 hover:text-white"
            >
              ← Back
            </button>
            <div className="text-center">
               <img src={teamLogo} alt="" className="mx-auto w-32 h-32 rounded-full" />
          <img src={headshot} alt="" className="mx-auto w-32 h-32 rounded-full" />

          <h1 className="text-lg font-semibold text-center mb-1">
            {firstName + " " + lastName}
          </h1>

          <h2 className="text-lg">Overall</h2>
          <p className={`text-slate-400 text-lg`}>
            Sv%: <strong className={allSvColor}>{Number(allDanger).toFixed(3)}%</strong><br></br>
            Save Percentile: <>{allDangerSavePercentile}</> <br />
            Shots Faced: <strong>{allShotsAgainst}</strong> <br></br>
          </p>

          <h2>High Danger</h2>
          <p className="text-slate-400">
            Sv%: <strong>{Number(highDanger).toFixed(3)}%</strong> <br></br>
            Save Percentile: <>{highDangerSavePercentile}</> <br />
            Shots: <strong>{highShotsAgainst}</strong> <br></br>
          </p>

          <h2>Mid Range</h2>
          <p className="text-slate-400">
            Sv%: <strong>{Number(midDanger).toFixed(3)}%</strong> <br></br>
            Save Percentile: <>{midDangerSavePercentile}</> <br />
            Shots: <strong>{midShotsAgainst}</strong> <br></br>
          </p>

          <h2>Long Range</h2>
          <p className="text-slate-400">
            Sv%: <strong>{Number(longDanger).toFixed(3)}%</strong> <br></br>
            Save Percentile: <>{longDangerSavePercentile}</> <br />
            Shots: <strong>{longShotsAgainst}</strong> <br></br>
          </p>

          <h2>Weakest Area</h2>
          <p className="text-slate-400">{weakPointName} <br />
            Sv%: <strong>{Number(weakPointSv).toFixed(3)}%</strong> <br />
            Save Percentile: <>{weakPointSavePercentile}</> <br />
            Total Saves: <strong>{weakPointSaves}</strong>
          </p>
            </div>
            </div>
         <div className="flex-1 p-8">
      <ZoneMapModel
        isOpen={showZoneMap}
        onClose={() => setShowZoneMap(false)}
        shotData={shotLocoDetails}
        goalieInfo={{ firstName, lastName, headshot, teamLogo }}>
      </ZoneMapModel>
      </div>
      </div>
    )
    }
    </>
  )
}

export default Goalie