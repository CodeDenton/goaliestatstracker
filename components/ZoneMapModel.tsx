
interface ZoneMapModelProps {
  isOpen: boolean
  onClose: () => void
  shotData: any[]
  goalieInfo: {
    firstName: String
    lastName: String
    headshot: String
    teamLogo: String
  }
}
const ZoneMapModel = ({ isOpen, onClose, shotData, goalieInfo }: ZoneMapModelProps) => {
  if (!isOpen) return null;


  const zonePos: { [key: string]: { x: number; y: number; width: number; height: number; savePctg: number; saves: number; savePercentile: number } } = {
    // Top zones (defensive zone entry)
    "Beyond Red Line": { x: 110, y: 0, width: 180, height: 60, savePctg: Number(Number(shotData[1].savePctg).toFixed(3)), saves: shotData[1].saves, savePercentile: shotData[1].savePctgPercentile },
    "Offensive Neutral Zone": { x: 115, y: 62.5, width: 170, height: 60, savePctg: Number(Number(shotData[10].savePctg).toFixed(3)), saves: shotData[10].saves, savePercentile: shotData[10].savePctgPercentile },

    // Blue line / Point areas
    "L Point": { x: 10, y: 70, width: 100, height: 70, savePctg: Number(Number(shotData[8].savePctg).toFixed(3)), saves: shotData[8].saves, savePercentile: shotData[8].savePctgPercentile },
    "Center Point": { x: 130, y: 125, width: 140, height: 50, savePctg: Number(Number(shotData[2].savePctg).toFixed(3)), saves: shotData[2].saves, savePercentile: shotData[2].savePctgPercentile },
    "R Point": { x: 290, y: 70, width: 100, height: 70, savePctg: Number(Number(shotData[16].savePctg).toFixed(3)), saves: shotData[16].saves, savePercentile: shotData[16].savePctgPercentile },

    // Wing/outside areas
    "Outside L": { x: 0, y: 150, width: 90, height: 100, savePctg: Number(Number(shotData[11].savePctg).toFixed(3)), saves: shotData[11].saves, savePercentile: shotData[11].savePctgPercentile },
    "Outside R": { x: 310, y: 150, width: 90, height: 100, savePctg: Number(Number(shotData[12].savePctg).toFixed(3)), saves: shotData[12].saves, savePercentile: shotData[12].savePctgPercentile },

    // Face-off dots
    "L Circle": { x: 50, y: 255, width: 80, height: 70, savePctg: Number(Number(shotData[5].savePctg).toFixed(3)), saves: shotData[5].saves, savePercentile: shotData[5].savePctgPercentile },
    "R Circle": { x: 270, y: 255, width: 80, height: 70, savePctg: Number(Number(shotData[13].savePctg).toFixed(3)), saves: shotData[13].saves, savePercentile: shotData[13].savePctgPercentile },

    // Slot areas (high danger)
    "High Slot": { x: 140, y: 180, width: 120, height: 90, savePctg: Number(Number(shotData[4].savePctg).toFixed(3)), saves: shotData[4].saves, savePercentile: shotData[4].savePctgPercentile },
    "Low Slot": { x: 140, y: 275, width: 120, height: 70, savePctg: Number(Number(shotData[9].savePctg).toFixed(3)), saves: shotData[9].saves, savePercentile: shotData[9].savePctgPercentile },

    // Net-side areas
    "L Net Side": { x: 80, y: 350, width: 80, height: 50, savePctg: Number(Number(shotData[7].savePctg).toFixed(3)), saves: shotData[7].saves, savePercentile: shotData[7].savePctgPercentile },
    "R Net Side": { x: 240, y: 350, width: 80, height: 50, savePctg: Number(Number(shotData[15].savePctg).toFixed(3)), saves: shotData[15].saves, savePercentile: shotData[15].savePctgPercentile },

    // Corner areas
    "L Corner": { x: 0, y: 330, width: 80, height: 80, savePctg: Number(Number(shotData[6].savePctg).toFixed(3)), saves: shotData[6].saves, savePercentile: shotData[6].savePctgPercentile },
    "R Corner": { x: 320, y: 330, width: 80, height: 80, savePctg: Number(Number(shotData[14].savePctg).toFixed(3)), saves: shotData[14].saves, savePercentile: shotData[14].savePctgPercentile },

    // Behind net and crease
    "Behind the Net": { x: 10, y: 415, width: 380, height: 50, savePctg: Number(Number(shotData[0].savePctg).toFixed(3)), saves: shotData[0].saves, savePercentile: shotData[0].savePctgPercentile },
    "Crease": { x: 160, y: 350, width: 80, height: 50, savePctg: Number(Number(shotData[3].savePctg).toFixed(3)), saves: shotData[3].saves, savePercentile: shotData[3].savePctgPercentile },
  }
  // const test = shotData[0].saves

  const colors = [
    "#34d399", "#4ade80", "#facc15", "#fb923c", "#f87171"
  ];
  return (
      <div className="relative bg-[#495057] rounded-lg p-0 pt-20">
        <svg
          viewBox="0 0 400 400"
          className="w-full h-auto"
          style={{ maxHeight: "700px" }}
        >

          {/* Draw each zone */}
          {Object.entries(zonePos).map(([zoneName, pos], index) => (
            <g key={zoneName}>
              {/* Zone box */}
              <rect
                x={pos.x}
                y={pos.y}
                width={pos.width}
                height={pos.height}
                fill={
                  pos.savePctg >= 0.950 ? colors[0]
                    : pos.savePctg >= 0.900 ? colors[1]
                      : pos.savePctg >= 0.850 ? colors[2]
                        : pos.savePctg >= 0.800 ? colors[3]
                          : colors[4]
                }
                stroke="white"
                strokeWidth="2"
                opacity="0.7"
                rx="4"
              />

              {/* Zone name */}
              <text
                x={pos.x + pos.width / 2}
                y={pos.y + pos.height / 2 - 8}
                textAnchor="middle"
                dominantBaseline="middle"
                fill="white"
                fontSize="9"
                fontWeight="bold"
              >
                {zoneName}
              </text>

              {/* SV% */}
              <text
                x={pos.x + pos.width / 2}
                y={pos.y + pos.height / 2 + 4}
                textAnchor="middle"
                fill="white"
                fontSize="8"
                opacity="0.8"
              >
                SV%: {pos.savePctg}
              </text>

              {/* Saves */}
              <text
                x={pos.x + pos.width / 2}
                y={pos.y + pos.height / 2 + 14}
                textAnchor="middle"
                fill="white"
                fontSize="8"
                opacity="0.8"
              >
                <tspan x={pos.x + pos.width / 2} dy="0">
                  SavePercentile: {Number(pos.savePercentile).toFixed(2)}
                </tspan>
                <tspan x={pos.x + pos.width / 2} dy="1.2em">
                  Saves: {pos.saves}
                </tspan>
              </text>
            </g>
          ))}
        </svg>



      </div>
  )
}

export default ZoneMapModel