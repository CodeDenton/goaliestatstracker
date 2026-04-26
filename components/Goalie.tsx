"use client"
import { useState } from "react"
import GoalieModal from "./GoalieModal"

const fmtSv = (v: number) =>
  `.${String(Math.round(v * 1000)).padStart(3, "0")}`

const svColor = (v: number) =>
  v >= 0.94 ? "#10b981"
    : v >= 0.91 ? "#34d399"
      : v >= 0.89 ? "#fbbf24"
        : v >= 0.86 ? "#f97316"
          : "#ef4444"

const gaaColor = (v: number) =>
  v <= 2.0 ? "#10b981"
  : v <= 2.5 ? "#34d399"
  : v <= 3.0 ? "#fbbf24"
  : v <= 3.5 ? "#f97316"
  : "#ef4444"

const Goalie = ({ data, rank, sortBy }: any) => {

  //View goalie JSON
  // console.log(data)

  const [showModal, setShowModal] = useState(false)

  const teamLogo = data?.player?.team?.teamLogo?.dark
  const firstName = data?.player?.firstName?.default
  const lastName = data?.player?.lastName?.default
  const headshot = data?.player?.headshot

  const all = data?.shotLocationSummary?.[0]
  const sv = all?.savePctg ?? 0
  const shots = (all?.goalsAgainst ?? 0) + (all?.saves ?? 0)
  const saves = all?.saves ?? 0
  const pct = all?.savePctgPercentile ?? 0

  const gaa = data?.player?.goalsAgainstAvg ?? 0


  return (
    <>
      <div
        onClick={() => setShowModal(true)}
        className="group relative bg-neutral-900 border border-white/8 rounded-2xl p-5 cursor-pointer transition-all duration-200 hover:-translate-y-1 hover:border-white/15 hover:shadow-[0_0_20px_rgba(234,179,8,0.3)]"
      >
        {/* Rank badge */}
        <div className="absolute top-3 left-3 text-[10px] font-bold text-neutral-600 tabular-nums">
          #{rank}
        </div>
        {/* Team logo watermark */}
        {teamLogo && (
          <img
            src={teamLogo}
            className="absolute right-4 top-4 w-10 h-10 opacity-10 group-hover:opacity-15 transition-opacity"
          />
        )}

        {/* Headshot */}
        <div className="flex justify-center mb-4">
          {headshot && (
            <img
              src={headshot}
              className="w-16 h-16 rounded-full border border-white/10 object-cover shadow-lg"
            />
          )}
        </div>

        {/* Name */}
        <div className="text-center mb-4">
          <div className="text-sm font-semibold text-neutral-100 tracking-tight">
            {firstName} {lastName}
          </div>
          <div className="text-[10px] text-neutral-500 uppercase tracking-widest mt-0.5">
            {data?.player?.team?.abbrev ?? "NHL"}
          </div>
        </div>

        {/* Divider */}
        <div className="h-px bg-white/6 mb-4" />

        {/* Save % / GAA */}

        <div className="text-center mb-4">
          <div
            className="text-3xl font-bold tracking-tight tabular-nums"
             style={{ color: sortBy === 'gaa' ? gaaColor(gaa) : svColor(sv) }}
          >
            {sortBy === 'gaa'
              ? (data?.player?.goalsAgainstAvg ?? 0).toFixed(2)
              : fmtSv(sv)}
          </div>
          <div className="text-[9px] text-neutral-500 uppercase tracking-widest mt-1">
            {sortBy === 'gaa' ? 'GAA' : 'Save %'}
          </div>
        </div>

        {/* Stats row */}
        <div className="grid grid-cols-3 gap-2 text-center">
          {[
            { label: "Shots", value: shots },
            { label: "Saves", value: saves },
            { label: "Pctile", value: `${Math.round(pct * 100)}th` },
          ].map(({ label, value }) => (
            <div key={label} className="bg-neutral-800/60 rounded-lg py-2">
              <div className="text-xs font-semibold text-neutral-200">{value}</div>
              <div className="text-[9px] text-neutral-500 uppercase tracking-wider mt-0.5">{label}</div>
            </div>
          ))}
        </div>

        {/* CTA */}
        <div className="mt-4 text-center text-[9px] text-neutral-600 uppercase tracking-wider group-hover:text-neutral-400 transition-colors">
          View zone map →
        </div>
      </div>

      {showModal && (
        <GoalieModal
          data={data}
          firstName={firstName}
          lastName={lastName}
          headshot={headshot}
          teamLogo={teamLogo}
          onClose={() => setShowModal(false)}
        />
      )}
    </>
  )
}

export default Goalie