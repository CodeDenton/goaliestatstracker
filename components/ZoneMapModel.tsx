"use client"

import { useState } from "react"

interface ZoneMapModelProps {
  mode: 'save' | 'shot'
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

const ZoneMapModel = ({ mode, isOpen, onClose, shotData, goalieInfo }: ZoneMapModelProps) => {
  const [hoveredZone, setHoveredZone] = useState<string | null>(null)
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 })

  const shots = (d: any) => {
    if (d.savePctg === 0 || d.saves === 0) return 0
    return Math.round(d.saves / d.savePctg)
  }

  const modeStat = mode === 'save' ? 'savePctg' : 'shots'

  if (!isOpen) return null

  const ordinal = (n: number) => {
    const s = ["th", "st", "nd", "rd"]
    const v = n % 100
    return n + (s[(v - 20) % 10] || s[v] || s[0])
  }

  const zonePos: {
    [key: string]: {
      x: number
      y: number
      width: number
      height: number
      savePctg: number
      saves: number
      savePercentile: number
      shotNum: number
    }
  } = {
    "Beyond Red Line": { x: 8, y: 8, width: 584, height: 50, savePctg: Number(shotData[1].savePctg.toFixed(3)), saves: shotData[1].saves, savePercentile: shotData[1].savePctgPercentile, shotNum: shots(shotData[1]) },
    "Offensive Neutral Zone": { x: 8, y: 60, width: 584, height: 50, savePctg: Number(shotData[10].savePctg.toFixed(3)), saves: shotData[10].saves, savePercentile: shotData[10].savePctgPercentile, shotNum: shots(shotData[10]) },
    "Left Point": { x: 8, y: 114, width: 185, height: 90, savePctg: Number(shotData[8].savePctg.toFixed(3)), saves: shotData[8].saves, savePercentile: shotData[8].savePctgPercentile, shotNum: shots(shotData[8]) },
    "Center Point": { x: 205, y: 114, width: 190, height: 90, savePctg: Number(shotData[2].savePctg.toFixed(3)), saves: shotData[2].saves, savePercentile: shotData[2].savePctgPercentile, shotNum: shots(shotData[2]) },
    "Right Point": { x: 407, y: 114, width: 185, height: 90, savePctg: Number(shotData[16].savePctg.toFixed(3)), saves: shotData[16].saves, savePercentile: shotData[16].savePctgPercentile, shotNum: shots(shotData[16]) },
    "Outside Left": { x: 8, y: 206, width: 185, height: 110, savePctg: Number(shotData[11].savePctg.toFixed(3)), saves: shotData[11].saves, savePercentile: shotData[11].savePctgPercentile, shotNum: shots(shotData[11]) },
    "High Slot": { x: 205, y: 206, width: 190, height: 110, savePctg: Number(shotData[4].savePctg.toFixed(3)), saves: shotData[4].saves, savePercentile: shotData[4].savePctgPercentile, shotNum: shots(shotData[4]) },
    "Outside Right": { x: 407, y: 206, width: 185, height: 110, savePctg: Number(shotData[12].savePctg.toFixed(3)), saves: shotData[12].saves, savePercentile: shotData[12].savePctgPercentile, shotNum: shots(shotData[12]) },
    "Left Circle": { x: 8, y: 318, width: 185, height: 110, savePctg: Number(shotData[5].savePctg.toFixed(3)), saves: shotData[5].saves, savePercentile: shotData[5].savePctgPercentile, shotNum: shots(shotData[5]) },
    "Low Slot": { x: 205, y: 318, width: 190, height: 110, savePctg: Number(shotData[9].savePctg.toFixed(3)), saves: shotData[9].saves, savePercentile: shotData[9].savePctgPercentile, shotNum: shots(shotData[9]) },
    "Right Circle": { x: 407, y: 318, width: 185, height: 110, savePctg: Number(shotData[13].savePctg.toFixed(3)), saves: shotData[13].saves, savePercentile: shotData[13].savePctgPercentile, shotNum: shots(shotData[13]) },
    "Left Corner": { x: 8, y: 430, width: 130, height: 110, savePctg: Number(shotData[6].savePctg.toFixed(3)), saves: shotData[6].saves, savePercentile: shotData[6].savePctgPercentile, shotNum: shots(shotData[6]) },
    "Left Net Side": { x: 140, y: 430, width: 90, height: 110, savePctg: Number(shotData[7].savePctg.toFixed(3)), saves: shotData[7].saves, savePercentile: shotData[7].savePctgPercentile, shotNum: shots(shotData[7]) },
    "Crease": { x: 240, y: 430, width: 120, height: 110, savePctg: Number(shotData[3].savePctg.toFixed(3)), saves: shotData[3].saves, savePercentile: shotData[3].savePctgPercentile, shotNum: shots(shotData[3]) },
    "Right Net Side": { x: 370, y: 430, width: 90, height: 110, savePctg: Number(shotData[15].savePctg.toFixed(3)), saves: shotData[15].saves, savePercentile: shotData[15].savePctgPercentile, shotNum: shots(shotData[15]) },
    "Right Corner": { x: 460, y: 430, width: 130, height: 110, savePctg: Number(shotData[14].savePctg.toFixed(3)), saves: shotData[14].saves, savePercentile: shotData[14].savePctgPercentile, shotNum: shots(shotData[14]) },
    "Behind the Net": { x: 8, y: 542, width: 584, height: 50, savePctg: Number(shotData[0].savePctg.toFixed(3)), saves: shotData[0].saves, savePercentile: shotData[0].savePctgPercentile, shotNum: shots(shotData[0]) },
  }

  const getZoneColor = (pctg: number, noData = false) => {
    if (noData) return "#27272a"
    if (pctg >= 0.96) return "#10b981"
    if (pctg >= 0.94) return "#34d399"
    if (pctg >= 0.92) return "#6ee7b7"
    if (pctg >= 0.90) return "#fbbf24"
    if (pctg >= 0.87) return "#f97316"
    if (pctg >= 0.84) return "#ef4444"
    return "#dc2626"
  }

  const getPercentileLabel = (p: number) => {
    if (p >= 0.9) return "Elite"
    if (p >= 0.7) return "Above Avg"
    if (p >= 0.4) return "Average"
    if (p >= 0.2) return "Below Avg"
    return "Poor"
  }

  const hoveredData = hoveredZone ? zonePos[hoveredZone] : null

  const handleMouseMove = (e: React.MouseEvent<SVGGElement>) => {
    const svg = (e.currentTarget as SVGGElement).ownerSVGElement!
    const rect = svg.getBoundingClientRect()
    setTooltipPos({
      x: (e.clientX - rect.left) * (600 / rect.width),
      y: (e.clientY - rect.top) * (600 / rect.height),
    })
  }

  return (
    <div className="w-full flex flex-col items-center select-none">
      <div className="relative w-full">
        <svg
          viewBox="0 0 600 600"
          className="w-full"
          onMouseLeave={() => setHoveredZone(null)}
        >
          {/* Background */}
          <rect x="0" y="0" width="600" height="600" fill="#0a0b0d" rx="14" />

          {/* Rink outline */}
          <rect x="4" y="4" width="592" height="592" fill="none" stroke="#1d2d44" strokeWidth="2" rx="12" />

          {/* Blue line */}
          <line x1="4" y1="112" x2="596" y2="112" stroke="#3b82f6" strokeWidth="2" opacity="0.5" />

          {/* Red goal line */}
          <line x1="4" y1="538" x2="596" y2="538" stroke="#ef4444" strokeWidth="2" opacity="0.5" />

          {/* Faceoff circles — behind cells, subtle */}
          <circle cx="100" cy="390" r="50" fill="none" stroke="#ffffff08" strokeWidth="1" />
          <circle cx="500" cy="390" r="50" fill="none" stroke="#ffffff08" strokeWidth="1" />
          <circle cx="100" cy="390" r="2.5" fill="#ffffff15" />
          <circle cx="500" cy="390" r="2.5" fill="#ffffff15" />

          {/* Crease arc */}
          <path d="M 240 538 A 60 45 0 0 1 360 538" fill="none" stroke="#60a5fa" strokeWidth="1" opacity="0.3" />

          {/* Net */}
          <rect x="262" y="546" width="76" height="18" fill="none" stroke="#ffffff20" strokeWidth="1" rx="2" />

          {/* Zone cells */}
          {Object.entries(zonePos).map(([zoneName, pos]) => {
            const noData = pos.saves === 0
            const fill = getZoneColor(pos.savePctg, noData)
            const isHovered = hoveredZone === zoneName
            const cx = pos.x + pos.width / 2
            const cy = pos.y + pos.height / 2
            const compact = pos.height < 70
            const darkText = !noData && pos.savePctg >= 0.92

            return (
              <g
                key={zoneName}
                onMouseEnter={(e) => { setHoveredZone(zoneName); handleMouseMove(e) }}
                onMouseMove={handleMouseMove}
                onMouseLeave={() => setHoveredZone(null)}
                style={{ cursor: "pointer" }}
              >
                <rect
                  x={pos.x + 1.5}
                  y={pos.y + 1.5}
                  width={pos.width - 3}
                  height={pos.height - 3}
                  fill={fill}
                  opacity={isHovered ? 1 : noData ? 0.6 : 0.78}
                  rx={6}
                  style={{ transition: "opacity 0.12s ease" }}
                />

                {/* Zone label */}
                {!compact && (
                  <text
                    x={cx}
                    y={cy - 12}
                    textAnchor="middle"
                    fill={noData ? "#52525b" : darkText ? "#11182788" : "#ffffff77"}
                    fontSize="8.5"
                    fontWeight="600"
                    letterSpacing="0.5"
                  >
                    {zoneName.toUpperCase()}
                  </text>
                )}

                {/* Save % or N/A */}
                <text
                  x={cx}
                  y={compact ? cy + 5 : cy + 10}
                  textAnchor="middle"
                  fill={noData ? "#52525b" : darkText ? "#111827" : "#ffffff"}
                  fontSize={compact ? "11" : "17"}
                  fontWeight="800"
                  fontFamily="monospace"
                >
                  {mode === 'save'
                    ? noData ? "N/A" : `.${String(Math.round(pos.savePctg * 1000)).padStart(3, "0")}`
                    : pos.shotNum === 0 ? "N/A" : `${pos.shotNum}`
                  }
                </text>

                {/* Saves count */}
                {!compact && (
                  <text
                    x={cx}
                    y={cy + 26}
                    textAnchor="middle"
                    fill={noData ? "#3f3f46" : darkText ? "#11182755" : "#ffffff44"}
                    fontSize="8.5"
                  >
                    {mode === 'save'
                    ? noData ? "no data": `${pos.saves} sv`
                    : pos.shotNum === 0 ? "N/A" : `${pos.shotNum - pos.saves} goals`
                  }
                    
                  </text>
                )}
              </g>
            )
          })}

          {/* Tooltip */}
          {hoveredZone && hoveredData && (() => {
            const noData = hoveredData.saves === 0
            const tw = 135
            const th = noData ? 46 : 62
            const tx = Math.min(tooltipPos.x + 14, 600 - tw - 8)
            const ty = Math.max(tooltipPos.y - th - 10, 8)

            return (
              <g style={{ pointerEvents: "none" }}>
                <rect x={tx} y={ty} width={tw} height={th} rx={6} fill="#111318" stroke="#ffffff18" strokeWidth="1" />

                {/* Zone name */}
                <text x={tx + 12} y={ty + 18} fill="#6b7280" fontSize="7.5" fontWeight="600" letterSpacing="1">
                  {hoveredZone.toUpperCase()}
                </text>

                {noData ? (
                  <text x={tx + 12} y={ty + 34} fill="#52525b" fontSize="10" fontWeight="600">
                    No data recorded
                  </text>
                ) : (
                  <>
                    <text x={tx + 12} y={ty + 38} fill="#f9fafb" fontSize="18" fontWeight="700" fontFamily="monospace">
                      .{String(Math.round(hoveredData.savePctg * 1000)).padStart(3, "0")}
                    </text>
                    <line x1={tx + 12} y1={ty + 44} x2={tx + tw - 12} y2={ty + 44} stroke="#ffffff10" strokeWidth="1" />
                    <text x={tx + 12} y={ty + 56} fill="#4b5563" fontSize="7.5">
                      {hoveredData.saves} sv · {getPercentileLabel(hoveredData.savePercentile)} · {ordinal(Math.round(hoveredData.savePercentile * 100))} pct
                    </text>
                  </>
                )}
              </g>
            )
          })()}
        </svg>
      </div>

      {/* Legend */}
      <div className="mt-4 flex flex-wrap items-center justify-center gap-5 text-[11px] text-neutral-500">
        {[
          { label: "Elite (96+)", color: "#10b981" },
          { label: "Above Avg (92–96)", color: "#6ee7b7" },
          { label: "Average (90–92)", color: "#fbbf24" },
          { label: "Below Avg (87–90)", color: "#f97316" },
          { label: "Poor (<87)", color: "#ef4444" },
          { label: "No Data", color: "#27272a" },
        ].map(({ label, color }) => (
          <div key={label} className="flex items-center gap-2">
            <div className="w-2.5 h-2.5 rounded-sm" style={{ background: color }} />
            <span>{label}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default ZoneMapModel