interface ZoneMapModelProps {
  isOpen: boolean
  onClose: () => void
  shotData: any[]
  goalieInfo: {
    firstName: string
    lastName: string
    headshot: string
    teamLogo: string
  }
}

const ZoneMapModel = ({ isOpen, onClose, shotData, goalieInfo }: ZoneMapModelProps) => {
  if (!isOpen) return null

  const zonePos: {
    [key: string]: {
      x: number
      y: number
      width: number
      height: number
      savePctg: number
      saves: number
      savePercentile: number
    }
  } = {
    "Beyond Red Line": { x:110,y:0,width:180,height:60,savePctg:Number(Number(shotData[1].savePctg).toFixed(3)),saves:shotData[1].saves,savePercentile:shotData[1].savePctgPercentile },
    "Offensive Zone": { x:115,y:62.5,width:170,height:60,savePctg:Number(Number(shotData[10].savePctg).toFixed(3)),saves:shotData[10].saves,savePercentile:shotData[10].savePctgPercentile },
    "L Point": { x:10,y:70,width:100,height:70,savePctg:Number(Number(shotData[8].savePctg).toFixed(3)),saves:shotData[8].saves,savePercentile:shotData[8].savePctgPercentile },
    "Center Point": { x:130,y:125,width:140,height:50,savePctg:Number(Number(shotData[2].savePctg).toFixed(3)),saves:shotData[2].saves,savePercentile:shotData[2].savePctgPercentile },
    "R Point": { x:290,y:70,width:100,height:70,savePctg:Number(Number(shotData[16].savePctg).toFixed(3)),saves:shotData[16].saves,savePercentile:shotData[16].savePctgPercentile },
    "Outside L": { x:0,y:150,width:90,height:100,savePctg:Number(Number(shotData[11].savePctg).toFixed(3)),saves:shotData[11].saves,savePercentile:shotData[11].savePctgPercentile },
    "Outside R": { x:310,y:150,width:90,height:100,savePctg:Number(Number(shotData[12].savePctg).toFixed(3)),saves:shotData[12].saves,savePercentile:shotData[12].savePctgPercentile },
    "L Circle": { x:50,y:255,width:80,height:70,savePctg:Number(Number(shotData[5].savePctg).toFixed(3)),saves:shotData[5].saves,savePercentile:shotData[5].savePctgPercentile },
    "R Circle": { x:270,y:255,width:80,height:70,savePctg:Number(Number(shotData[13].savePctg).toFixed(3)),saves:shotData[13].saves,savePercentile:shotData[13].savePctgPercentile },
    "High Slot": { x:140,y:180,width:120,height:90,savePctg:Number(Number(shotData[4].savePctg).toFixed(3)),saves:shotData[4].saves,savePercentile:shotData[4].savePctgPercentile },
    "Low Slot": { x:140,y:275,width:120,height:70,savePctg:Number(Number(shotData[9].savePctg).toFixed(3)),saves:shotData[9].saves,savePercentile:shotData[9].savePctgPercentile },
    "L Net Side": { x:80,y:350,width:80,height:50,savePctg:Number(Number(shotData[7].savePctg).toFixed(3)),saves:shotData[7].saves,savePercentile:shotData[7].savePctgPercentile },
    "R Net Side": { x:240,y:350,width:80,height:50,savePctg:Number(Number(shotData[15].savePctg).toFixed(3)),saves:shotData[15].saves,savePercentile:shotData[15].savePctgPercentile },
    "L Corner": { x:0,y:330,width:80,height:80,savePctg:Number(Number(shotData[6].savePctg).toFixed(3)),saves:shotData[6].saves,savePercentile:shotData[6].savePctgPercentile },
    "R Corner": { x:320,y:330,width:80,height:80,savePctg:Number(Number(shotData[14].savePctg).toFixed(3)),saves:shotData[14].saves,savePercentile:shotData[14].savePctgPercentile },
    "Behind the Net": { x:10,y:415,width:380,height:50,savePctg:Number(Number(shotData[0].savePctg).toFixed(3)),saves:shotData[0].saves,savePercentile:shotData[0].savePctgPercentile },
    "Crease": { x:160,y:350,width:80,height:50,savePctg:Number(Number(shotData[3].savePctg).toFixed(3)),saves:shotData[3].saves,savePercentile:shotData[3].savePctgPercentile },
  }

  const getZoneColor = (pctg: number) => {
    if (pctg >= 0.96) return "#22c55e"
    if (pctg >= 0.94) return "#4ade80"
    if (pctg >= 0.92) return "#86efac"
    if (pctg >= 0.9) return "#fbbf24"
    if (pctg >= 0.87) return "#f97316"
    if (pctg >= 0.84) return "#ef4444"
    return "#dc2626"
  }

  const getTextColor = (pctg: number) => (pctg >= 0.92 ? "#111827" : "#ffffff")

  return (
    <div className="w-full h-full flex flex-col items-center">

      <svg
        viewBox="0 0 400 470"
        className="w-full max-h-[calc(92vh-160px)]"
      >
        <rect x="0" y="0" width="400" height="470" fill="#18191c" rx="10" />

        {Object.entries(zonePos).map(([zoneName, pos]) => {
          const fill = getZoneColor(pos.savePctg)
          const textCol = getTextColor(pos.savePctg)
          const cx = pos.x + pos.width / 2
          const lineH = pos.height
          const compact = lineH < 56

          return (
            <g key={zoneName}>
              <rect
                x={pos.x + 1}
                y={pos.y + 1}
                width={pos.width - 2}
                height={pos.height - 2}
                fill={fill}
                opacity={0.82}
                rx={6}
              />

              {compact ? (
                <>
                  <text
                    x={cx}
                    y={pos.y + lineH / 2 - 5}
                    textAnchor="middle"
                    fill={textCol}
                    fontSize="7.5"
                    fontWeight="600"
                  >
                    {zoneName}
                  </text>

                  <text
                    x={cx}
                    y={pos.y + lineH / 2 + 6}
                    textAnchor="middle"
                    fill={textCol}
                    fontSize="7"
                    opacity="0.8"
                  >
                    .{String(Math.round(pos.savePctg * 1000)).padStart(3, "0")} · {pos.saves} sv
                  </text>
                </>
              ) : (
                <>
                  <text x={cx} y={pos.y + lineH * 0.28} textAnchor="middle" fill={textCol} fontSize="9" fontWeight="600">
                    {zoneName}
                  </text>

                  <text x={cx} y={pos.y + lineH * 0.52} textAnchor="middle" fill={textCol} fontSize="11" fontWeight="700">
                    .{String(Math.round(pos.savePctg * 1000)).padStart(3, "0")}
                  </text>

                  <text x={cx} y={pos.y + lineH * 0.7} textAnchor="middle" fill={textCol} fontSize="7" opacity="0.72">
                    {pos.saves} saves
                  </text>

                  <text x={cx} y={pos.y + lineH * 0.85} textAnchor="middle" fill={textCol} fontSize="6.5" opacity="0.6">
                    {Number(pos.savePercentile).toFixed(0)}th pct
                  </text>
                </>
              )}
            </g>
          )
        })}
      </svg>

      {/* Legend */}
      <div className="mt-4 flex flex-wrap items-center justify-center gap-3 text-[10px] text-slate-500">
        {[
          { label: "Elite", color: "#22c55e" },
          { label: "Above Avg", color: "#86efac" },
          { label: "Average", color: "#fbbf24" },
          { label: "Below Avg", color: "#f97316" },
          { label: "Poor", color: "#ef4444" },
        ].map(({ label, color }) => (
          <div key={label} className="flex items-center gap-1.5">
            <div
              className="w-2 h-2 rounded-sm"
              style={{ background: color, opacity: 0.85 }}
            />
            <span>{label}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default ZoneMapModel