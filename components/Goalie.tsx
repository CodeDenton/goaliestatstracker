"use client"
import { useState } from "react"
import ZoneMapModel from "./ZoneMapModel"

const svColor = (v: number) =>
  v >= 0.94 ? "text-green-500"
  : v >= 0.91 ? "text-green-300"
  : v >= 0.89 ? "text-amber-400"
  : v >= 0.86 ? "text-orange-400"
  : "text-red-500"

const fmtSv = (v: number) =>
  `.${String(Math.round(v * 1000)).padStart(3, "0")}`

const StatRow = ({ label, value, accent }: any) => (
  <div className="flex justify-between items-baseline py-1.5 border-b border-white/5">
    <span className="text-[10px] text-gray-500 tracking-widest uppercase">
      {label}
    </span>
    <span className={`text-xs font-semibold tracking-tight ${accent ?? "text-neutral-100"}`}>
      {value}
    </span>
  </div>
)

const SectionLabel = ({ children }: any) => (
  <div className="text-[9px] font-bold tracking-[0.12em] uppercase text-gray-600 mt-4 mb-1">
    {children}
  </div>
)

const Goalie = ({ data}: any) => {  
  
  const [showZoneMap, setShowZoneMap] = useState(false)

  const teamLogo  = data?.player?.team?.teamLogo?.dark
  const firstName = data?.player?.firstName?.default
  const lastName  = data?.player?.lastName?.default
  const headshot  = data?.player?.headshot

  const build = (idx: number) => {
    const d = data?.shotLocationSummary[idx]
    const ga = d?.goalsAgainst ?? 0
    const sv = d?.saves ?? 0
    return { sv: d?.savePctg ?? 0, pct: d?.savePctgPercentile ?? 0, shots: ga + sv, saves: sv }
  }

  const all  = build(0)
  const high = build(1)
  const long = build(2)
  const mid  = build(3)

  const shotLocoDetails = data?.shotLocationDetails ?? []

  let weakIdx = 0
  for (let i = 0; i < shotLocoDetails.length - 1; i++) {
    if (
      shotLocoDetails[i].savePctg <
      shotLocoDetails[weakIdx].savePctg &&
      shotLocoDetails[i].saves > 5
    ) weakIdx = i
  }

  const weak = {
    name: shotLocoDetails[weakIdx]?.area ?? "—",
    sv: shotLocoDetails[weakIdx]?.savePctg ?? 0,
    pct: shotLocoDetails[weakIdx]?.savePctgPercentile ?? 0,
    saves: shotLocoDetails[weakIdx]?.saves ?? 0,
  }

  // COLLAPSED CARD
  if (!showZoneMap) {
    return (
      <div
        onClick={() => setShowZoneMap(true)}
        className="
        w-[220px]
        bg-neutral-900
        border border-white/10
        rounded-2xl
        p-5
        cursor-pointer
        transition-all
        hover:-translate-y-1
        hover:bg-neutral-800
        hover:border-white/20
        hover:shadow-2xl
        shadow-lg
        relative
        overflow-hidden
        "
      >

        <div className="relative h-18 mb-4">

          {teamLogo && (
            <img
              src={teamLogo}
              className="absolute left-1/2 -translate-x-1/2 w-12 h-12 opacity-25"
            />
          )}

          {headshot && (
            <img
              src={headshot}
              className="
              absolute left-1/2 -translate-x-1/2
              w-[68px] h-[68px]
              rounded-full
              border border-white/10
              object-cover
              shadow-lg
              "
            />
          )}

        </div>

        <div className="text-center mb-4">
          <div className="text-sm font-semibold tracking-tight text-neutral-100">
            {firstName} {lastName}
          </div>
          <div className="text-[10px] text-gray-500 uppercase tracking-wider mt-1">
            Goaltender
          </div>
        </div>

        <div className="h-px bg-white/10 mb-3" />

        <div className="text-center mb-3">
          <div className={`text-3xl font-bold tracking-tight ${svColor(all.sv)}`}>
            {fmtSv(all.sv)}
          </div>
          <div className="text-[9px] text-gray-500 uppercase tracking-widest mt-1">
            Overall SV%
          </div>
        </div>

        <div className="flex justify-between mt-2">

          {[{ label: "Shots", value: all.shots },
            { label: "Saves", value: all.saves },
            { label: "Pct", value: `${all.pct}th` }
          ].map(stat => (

            <div key={stat.label} className="text-center">
              <div className="text-sm font-semibold text-gray-300">
                {stat.value}
              </div>
              <div className="text-[9px] uppercase text-gray-500 tracking-wider">
                {stat.label}
              </div>
            </div>

          ))}

        </div>

        <div className="text-center mt-4 text-[9px] uppercase tracking-wider text-gray-600">
          Tap for zone map →
        </div>

      </div>
    )
  }

  // MODAL VIEW
  return (
    <div
      onClick={() => setShowZoneMap(false)}
      className="
      fixed inset-0
      z-50
      flex items-center justify-center
      bg-black/70
      backdrop-blur-md
      "
    >

      <div
        onClick={(e) => e.stopPropagation()}
        className="
        bg-neutral-950
        border border-white/10
        rounded-2xl
        shadow-2xl
        flex flex-col
        w-[min(900px,96vw)]
        max-h-[92vh]
        overflow-hidden
        "
      >

        {/* HEADER */}

        <div className="
        flex justify-between items-center
        px-6 py-4
        border-b border-white/10
        ">

          <div className="flex items-center gap-3">

            {headshot &&
              <img
                src={headshot}
                className="w-10 h-10 rounded-full border border-white/10"
              />
            }

            <div>
              <div className="text-sm font-semibold text-neutral-100">
                {firstName} {lastName}
              </div>
              <div className="text-[10px] uppercase tracking-wider text-gray-500">
                Zone Save Map
              </div>
            </div>

          </div>

          <button
            onClick={() => setShowZoneMap(false)}
            className="
            w-7 h-7
            flex items-center justify-center
            rounded-full
            bg-white/10
            text-gray-400
            hover:bg-white/20
            "
          >
            ×
          </button>

        </div>


        {/* BODY */}

        <div className="flex flex-1 overflow-hidden">

          {/* STATS SIDEBAR */}

          <div className="
          w-[210px]
          border-r border-white/10
          p-4
          overflow-y-auto
          ">

            <SectionLabel>High Danger</SectionLabel>
            <StatRow label="SV%" value={fmtSv(high.sv)} accent={svColor(high.sv)} />
            <StatRow label="Shots" value={high.shots} />
            <StatRow label="Percentile" value={`${high.pct}th`} />

            <SectionLabel>Mid Range</SectionLabel>
            <StatRow label="SV%" value={fmtSv(mid.sv)} accent={svColor(mid.sv)} />
            <StatRow label="Shots" value={mid.shots} />
            <StatRow label="Percentile" value={`${mid.pct}th`} />

            <SectionLabel>Long Range</SectionLabel>
            <StatRow label="SV%" value={fmtSv(long.sv)} accent={svColor(long.sv)} />
            <StatRow label="Shots" value={long.shots} />
            <StatRow label="Percentile" value={`${long.pct}th`} />

            <SectionLabel>Weakest Zone</SectionLabel>

            <div className="
            bg-red-500/10
            border border-red-500/20
            rounded-lg
            p-3 mt-2
            ">

              <div className="text-sm font-semibold mb-2 text-neutral-100">
                {weak.name}
              </div>

              <StatRow label="SV%" value={fmtSv(weak.sv)} accent={svColor(weak.sv)} />
              <StatRow label="Saves" value={weak.saves} />
              <StatRow label="Percentile" value={`${weak.pct}th`} />

            </div>

          </div>

          {/* ZONE MAP */}

          <div className="flex-1 p-6 overflow-y-auto">

            <ZoneMapModel
              isOpen={showZoneMap}
              onClose={() => setShowZoneMap(false)}
              shotData={shotLocoDetails}
              goalieInfo={{ firstName, lastName, headshot, teamLogo }}
            />

          </div>

        </div>

      </div>

    </div>
  )
}

export default Goalie