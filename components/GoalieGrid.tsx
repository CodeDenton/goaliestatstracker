"use client"
import { useState } from "react"
import Goalie from "./Goalie"

const GoalieGrid = ({ goalies, avgSavePctg, totalGoalies }: any) => {
  const [selectedTeam, setSelectedTeam] = useState("ALL")

  const teams = ["ALL", ...Array.from(
    new Set(goalies.map((g: any) => g.player?.team?.abbrev).filter(Boolean))
  ).sort()] as string[]

  const filtered = selectedTeam === "ALL"
    ? goalies
    : goalies.filter((g: any) => g.player?.team?.abbrev === selectedTeam)

    const filteredAvg = filtered.reduce((sum: number, g: any) => sum + (g.player?.savePctg ?? 0), 0) / filtered.length
    const filteredTotal = filtered.length

  return (
    <>
      {/* Stats strip */}
      <div className="flex items-center gap-6 px-6 py-3 border-b border-white/6 max-w-7xl mx-auto w-full">
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-neutral-500">Avg SV%</span>
          <span className="text-xs font-semibold text-green-400">{filteredAvg.toFixed(3)}</span>
        </div>
        <div className="w-px h-3 bg-white/10" />
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-neutral-500">Goalies</span>
          <span className="text-xs font-semibold text-neutral-200">{filteredTotal}</span>
        </div>
        <div className="w-px h-3 bg-white/10" />
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-neutral-500">Season</span>
          <span className="text-xs font-semibold text-neutral-200">25–26</span>
        </div>
      </div>

      {/* Team filter */}
      <div className="max-w-7xl mx-auto px-6 pt-6 pb-4">
        <div className="flex flex-wrap gap-2">
          {teams.map((team) => (
            <button
              key={team}
              onClick={() => setSelectedTeam(team)}
              className={`px-3 py-1 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
                selectedTeam === team
                  ? "bg-neutral-100 text-neutral-900"
                  : "bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200"
              }`}
            >
              {team}
            </button>
          ))}
        </div>
      </div>

      {/* Goalie grid */}
      <section className="max-w-7xl mx-auto px-6 pb-16">
        <div className="flex items-center justify-between mb-5">
          <p className="text-xs text-neutral-600">
            {filtered.length} goalie{filtered.length !== 1 ? "s" : ""}
            {selectedTeam !== "ALL" ? ` · ${selectedTeam}` : ""}
          </p>
        </div>
        <div className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-5">
          {filtered.map((g: any, index: number) => (
            <Goalie key={g.player.id} data={g} rank={index + 1} />
          ))}
        </div>
      </section>
    </>
  )
}

export default GoalieGrid