"use client"
import { useState } from "react"
import ZoneMapModel from "./ZoneMapModel"


interface GoalieModalProps {
  data: any
  firstName: string
  lastName: string
  headshot: string
  teamLogo: string
  onClose: () => void
}

const fmtSv = (v: number) => v.toFixed(3).substring(1)

const svColor = (v: number) =>
  v >= 0.94 ? "#10b981"
  : v >= 0.91 ? "#34d399"
  : v >= 0.89 ? "#fbbf24"
  : v >= 0.86 ? "#f97316"
  : "#ef4444"


const StatBlock = ({ label, value, color }: { label: string; value: string | number; color?: string }) => (
  <div className="flex justify-between items-center py-2.5 border-b border-neutral-800/60 last:border-0">
    <span className="text-xs text-neutral-500 tracking-wide">{label}</span>
    <span className="text-sm font-semibold" style={{ color: color ?? "#e5e5e5" }}>
      {value}
    </span>
  </div>
)

const SectionHeader = ({ children }: { children: string }) => (
  <div className="text-[10px] font-bold tracking-widest uppercase text-neutral-600 mt-5 mb-1.5">
    {children}
  </div>
)

const GoalieModal = ({ data, firstName, lastName, headshot, teamLogo, onClose }: GoalieModalProps) => {

  const [mapView, setMapView] = useState<'save' | 'shot'>('save')

  const build = (idx: number) => {
    const d = data?.shotLocationSummary?.[idx]
    const ga = d?.goalsAgainst ?? 0
    const sv = d?.saves ?? 0
    return {
      sv: d?.savePctg ?? 0,
      pct: d?.savePctgPercentile ?? 0,
      shots: ga + sv,
      saves: sv,
    }
  }

  const high = build(1)
  const mid  = build(3)
  const long = build(2)

  const shotLocationDetails = data?.shotLocationDetails ?? []

  let weakIdx = 0
  for (let i = 0; i < shotLocationDetails.length; i++) {
    if (
      shotLocationDetails[i].savePctg < shotLocationDetails[weakIdx].savePctg &&
      shotLocationDetails[i].saves > 5
    ) weakIdx = i
  }

  const weak = {
    name:  shotLocationDetails[weakIdx]?.area ?? "—",
    sv:    shotLocationDetails[weakIdx]?.savePctg ?? 0,
    pct:   shotLocationDetails[weakIdx]?.savePctgPercentile ?? 0,
    saves: shotLocationDetails[weakIdx]?.saves ?? 0,
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-neutral-950 border border-white/8 rounded-2xl shadow-2xl flex flex-col w-[min(920px,96vw)] max-h-[92vh] overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/8">
          <div className="flex items-center gap-3">
            {headshot && (
              <img src={headshot} className="w-9 h-9 rounded-full border border-white/10 object-cover" />
            )}
            <div>
              <div className="text-sm font-semibold text-neutral-100">
                {firstName} {lastName}
              </div>
              <div className="text-[10px] uppercase tracking-widest text-neutral-500 mt-0.5">
                Zone Save Map
              </div>
            </div>
            <button
            onClick={() => setMapView('save')}
            className={`px-4 py-2 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
              mapView === 'save'
                ? 'bg-neutral-100 text-neutral-900'
                : 'bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200'
            }`}
          >
            Save % Map
          </button>
          <button
            onClick={() => setMapView('shot')}
            className={`px-4 py-2 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
              mapView === 'shot'
                ? 'bg-neutral-100 text-neutral-900'
                : 'bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200'
            }`}
          >
            Total Shot Map
          </button>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 flex items-center justify-center rounded-full bg-neutral-800 text-neutral-400 hover:bg-neutral-700 hover:text-neutral-100 transition-colors text-sm"
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="flex flex-1 overflow-hidden">

          {/* Sidebar */}
          <div className="w-[200px] shrink-0 border-r border-white/8 px-4 py-4 overflow-y-auto bg-neutral-950">

            <SectionHeader>High Danger</SectionHeader>
            <StatBlock label="SV%" value={fmtSv(high.sv)} color={svColor(high.sv)} />
            <StatBlock label="Shots" value={high.shots} />
            <StatBlock label="Percentile" value={`${Math.round(high.pct * 100)}th`} />

            <SectionHeader>Mid Range</SectionHeader>
            <StatBlock label="SV%" value={fmtSv(mid.sv)} color={svColor(mid.sv)} />
            <StatBlock label="Shots" value={mid.shots} />
            <StatBlock label="Percentile" value={`${Math.round(mid.pct * 100)}th`} />

            <SectionHeader>Long Range</SectionHeader>
            <StatBlock label="SV%" value={fmtSv(long.sv)} color={svColor(long.sv)} />
            <StatBlock label="Shots" value={long.shots} />
            <StatBlock label="Percentile" value={`${Math.round(long.pct * 100)}th`} />

            <SectionHeader>Weakest Zone</SectionHeader>
            <div className="mt-1 rounded-xl bg-red-950/30 border border-red-500/15 p-3">
              <div className="text-sm font-semibold text-neutral-200 mb-2">{weak.name}</div>
              <StatBlock label="SV%" value={fmtSv(weak.sv)} color={svColor(weak.sv)} />
              <StatBlock label="Saves" value={weak.saves} />
              <StatBlock label="Percentile" value={`${Math.round(weak.pct * 100)}th`} />
            </div>

          </div>

          {/* Zone map */}
          <div className="flex-1 p-5 overflow-y-auto">
            <ZoneMapModel
              mode={mapView}
              isOpen={true}
              onClose={onClose}
              shotData={shotLocationDetails}
              goalieInfo={{ firstName, lastName, headshot, teamLogo }}
            />
          </div>

        </div>
      </div>
    </div>
  )
}

export default GoalieModal