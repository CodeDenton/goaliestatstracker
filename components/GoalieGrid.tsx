"use client"
import { useState } from "react"
import Goalie from "./Goalie"

const GoalieGrid = ({ goalies, avgSavePctg, totalGoalies }: any) => {
  const [selectedTeam, setSelectedTeam] = useState("ALL")
  const [search, setSearch] = useState("")
  type SortOption = 'savePctg' | 'gaa' | 'gamesPlayed'

  const [sortBy, setSortBy] = useState<SortOption>('savePctg')

  const sortedSavePctg = [...(goalies ?? [])].sort((a: any, b: any) => (b.player.savePctg ?? 0) - (a.player.savePctg ?? 0))
  const sortedGAA = [...(goalies ?? [])].sort((a: any, b: any) => (a.player.goalsAgainstAvg ?? 0) - (b.player.goalsAgainstAvg ?? 0))
  const sortedGP = [...(goalies ?? [])].sort((a: any, b: any) => (b.player.gamesPlayed ?? 0) - (a.player.gamesPlayed ?? 0))
  const sortedGoalies = sortBy === 'savePctg' ? sortedSavePctg : sortBy === 'gaa' ? sortedGAA : sortedGP;

  const teams = ["ALL", ...Array.from(
    new Set((goalies ?? []).map((g: any) => g.player?.team?.abbrev).filter(Boolean))
  ).sort()] as string[]

  const filtered = sortedGoalies
    .filter((g: any) => selectedTeam === "ALL" || g.player?.team?.abbrev === selectedTeam)
    .filter((g: any) => {
      const name = `${g.player?.firstName?.default} ${g.player?.lastName?.default}`.toLowerCase()
      return name.includes(search.toLowerCase())
    })

  const filteredAvg = filtered.reduce((sum: number, g: any) => sum + (g.player?.savePctg ?? 0), 0) / (filtered.length || 1)
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

      {/* Search and Sort */}
      <div className="max-w-7xl mx-auto px-6 pt-6 pb-2 flex items-center gap-3">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search goalie..."
          className="w-64 bg-neutral-800/60 border border-white/8 rounded-lg px-4 py-2 text-sm text-neutral-200 placeholder-neutral-600 focus:outline-none focus:border-white/20 transition-colors"
        />
        <div className="flex gap-2">
          <button
            onClick={() => setSortBy('savePctg')}
            className={`px-4 py-2 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
              sortBy === 'savePctg'
                ? 'bg-neutral-100 text-neutral-900'
                : 'bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200'
            }`}
          >
            SV%
          </button>
          <button
            onClick={() => setSortBy('gaa')}
            className={`px-4 py-2 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
              sortBy === 'gaa'
                ? 'bg-neutral-100 text-neutral-900'
                : 'bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200'
            }`}
          >
            GAA
          </button>
           <button
            onClick={() => setSortBy('gamesPlayed')}
            className={`px-4 py-2 rounded-lg text-[11px] font-semibold tracking-wide transition-all ${
              sortBy === 'gamesPlayed'
                ? 'bg-neutral-100 text-neutral-900'
                : 'bg-neutral-800/60 text-neutral-400 hover:bg-neutral-700/60 hover:text-neutral-200'
            }`}
          >
            GP
          </button>
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
            <Goalie key={g.player.id} data={g} rank={index + 1} sortBy={sortBy} />
          ))}
        </div>
      </section>
    </>
  )
}

export default GoalieGrid