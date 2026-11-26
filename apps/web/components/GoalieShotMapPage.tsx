"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import type { LeaderboardRow } from "@/components/LeaderboardExplorer";

const season = "20262027";
type Shot = { gameId: number; eventId: number; xCoordinate: number; yCoordinate: number; outcome: "SAVE" | "GOAL" };
type ShotMap = { shots: Shot[] };
type Game = { gameId: number; gameDate: string; team: string; opponent: string; teamScore: number | null; opponentScore: number | null };
type GameResponse = { rows: Game[] };

const pct = (value: number) => value.toFixed(3).replace(/^0/, "");

function Rink({ shots }: { shots: Shot[] }) {
  const rinkScale = 690 / 85;
  const centreX = 380;
  const centreLineY = 20;
  const blueLineY = centreLineY + 25 * rinkScale;
  const faceoffY = centreLineY + 69 * rinkScale;
  const goalLineY = centreLineY + 89 * rinkScale;
  const faceoffX = 35 + 22 * rinkScale;
  const faceoffRadius = 15 * rinkScale;
  const creaseRadius = 6 * rinkScale;
  const endBoardY = centreLineY + 100 * rinkScale;
  const cornerRadius = 28 * rinkScale;
  const rinkOutline = `M35 ${centreLineY}H725V${endBoardY - cornerRadius}A${cornerRadius} ${cornerRadius} 0 0 1 ${725 - cornerRadius} ${endBoardY}H${35 + cornerRadius}A${cornerRadius} ${cornerRadius} 0 0 1 35 ${endBoardY - cornerRadius}Z`;
  const placeShot = (shot: Shot) => {
    // NHL coordinates alternate ends each period. Mirror the far end so every
    // event is plotted against one net, with centre ice at the top of the map.
    const rinkX = Math.max(0, Math.min(89, Math.abs(shot.xCoordinate)));
    const lateral = Math.max(-42.5, Math.min(42.5, shot.xCoordinate < 0 ? -shot.yCoordinate : shot.yCoordinate));
    return { x: centreX + lateral * rinkScale, y: centreLineY + rinkX * rinkScale };
  };
  const faceoffCircle = (x: number) => <g key={x} fill="none" stroke="#dc2626" strokeWidth="3"><circle cx={x} cy={faceoffY} r={faceoffRadius} /><circle cx={x} cy={faceoffY} r={rinkScale / 2} fill="#dc2626" /></g>;

  return <svg viewBox="0 0 760 870" role="img" aria-label="Goalie half-rink shot map" className="mx-auto block w-full max-w-[34rem]">
    <defs><clipPath id="half-rink"><path d={rinkOutline} /></clipPath></defs>
    <path d={rinkOutline} fill="#ffffff" stroke="#64748b" strokeWidth="5" />
    <g clipPath="url(#half-rink)">
      <rect x="37" y="22" width="686" height={endBoardY - 22} fill="#f8fafc" />
    </g>
    <line x1="35" y1={centreLineY} x2="725" y2={centreLineY} stroke="#dc2626" strokeWidth="5" />
    <line x1="35" y1={blueLineY} x2="725" y2={blueLineY} stroke="#2563eb" strokeWidth="5" />
    <line x1="35" y1={goalLineY} x2="725" y2={goalLineY} stroke="#dc2626" strokeWidth="4" />
    <circle cx={faceoffX} cy={blueLineY - 5 * rinkScale} r={rinkScale} fill="#dc2626" />
    <circle cx={760 - faceoffX} cy={blueLineY - 5 * rinkScale} r={rinkScale} fill="#dc2626" />
    {faceoffCircle(faceoffX)}{faceoffCircle(760 - faceoffX)}
    <path d={`M${centreX - creaseRadius} ${goalLineY}A${creaseRadius} ${creaseRadius} 0 0 1 ${centreX + creaseRadius} ${goalLineY}`} fill="#bfdbfe" fillOpacity="0.8" stroke="#2563eb" strokeWidth="3" />
    <path d={`M${centreX - 3 * rinkScale} ${goalLineY}v${4 * rinkScale}h${6 * rinkScale}v${-4 * rinkScale}`} fill="none" stroke="#dc2626" strokeWidth="4" />
    {shots.map((shot) => { const point = placeShot(shot); return <circle key={`${shot.gameId}-${shot.eventId}`} cx={point.x} cy={point.y} r="6" fill={shot.outcome === "GOAL" ? "#dc2626" : "#2563eb"} fillOpacity="0.76" stroke="#ffffff" strokeWidth="1.25" />; })}
  </svg>;
}

export default function GoalieShotMapPage({ goalieId, goalie }: { goalieId: number; goalie: LeaderboardRow | null }) {
  const [games, setGames] = useState<Game[]>([]);
  const [gameId, setGameId] = useState("");
  const [map, setMap] = useState<ShotMap | null>(null);
  const [mapSelection, setMapSelection] = useState<string | null>(null);
  const [mapError, setMapError] = useState(false);
  const base = process.env.NEXT_PUBLIC_API_URL;

  useEffect(() => { if (!base) return; fetch(`${base}/api/goalies/${goalieId}/games?season=${season}&page=0&size=100`).then((response) => response.ok ? response.json() : Promise.reject()).then((data: GameResponse) => setGames(data.rows ?? [])).catch(() => setGames([])); }, [base, goalieId]);
  useEffect(() => { if (!base) return; const controller = new AbortController(); const selectedGame = gameId ? `&gameId=${gameId}` : ""; fetch(`${base}/api/goalies/${goalieId}/shot-map?season=${season}${selectedGame}`, { signal: controller.signal }).then((response) => response.ok ? response.json() : Promise.reject()).then((data: ShotMap) => { setMap(data); setMapError(false); setMapSelection(gameId); }).catch((error) => { if (error.name !== "AbortError") { setMap(null); setMapError(true); setMapSelection(gameId); } }); return () => controller.abort(); }, [base, gameId, goalieId]);

  const totals = useMemo(() => { const shots = map?.shots ?? []; const goals = shots.filter((shot) => shot.outcome === "GOAL").length; return { shots: shots.length, goals, saves: shots.length - goals }; }, [map]);
  const selectedGame = games.find((game) => String(game.gameId) === gameId);
  const gameLabel = selectedGame ? `${selectedGame.team} vs ${selectedGame.opponent} · ${selectedGame.gameDate}` : "Season wide";

  return <main className="cv-shell py-6 sm:py-8">
    <Link href="/" className="inline-flex text-sm font-medium text-slate-600 hover:text-[#1d4ed8]">← Back to leaderboard</Link>
    <section className="mt-5 rounded-2xl bg-[#eff6ff] px-5 py-6 sm:px-8 sm:py-8">
      <p className="cv-kicker text-[#1d4ed8]">2026–27 REGULAR SEASON</p>
      <div className="mt-2 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between"><div><h1 className="text-3xl font-semibold tracking-[-0.05em] text-slate-950 sm:text-4xl">{goalie?.name ?? "Goalie shot map"}</h1><p className="mt-2 text-sm text-slate-600">{goalie ? `${goalie.teamAbbreviations.join(" · ")} · ${goalie.gamesPlayed} GP · ${goalie.wins}–${goalie.losses}–${goalie.overtimeLosses}` : "Season shot locations"}</p></div><p className="font-mono text-3xl font-semibold tracking-[-0.04em] text-[#1d4ed8]">{goalie ? pct(goalie.savePctg) : "—"}<span className="ml-2 font-sans text-sm font-medium text-slate-500">SV%</span></p></div>
      {goalie && <dl className="mt-6 grid grid-cols-3 gap-3 sm:max-w-lg"><div className="rounded-xl bg-white px-4 py-3"><dt className="cv-kicker">SAVES</dt><dd className="mt-1 text-lg font-semibold">{goalie.saves.toLocaleString()}</dd></div><div className="rounded-xl bg-white px-4 py-3"><dt className="cv-kicker">SHOTS AGAINST</dt><dd className="mt-1 text-lg font-semibold">{goalie.shotsAgainst.toLocaleString()}</dd></div><div className="rounded-xl bg-white px-4 py-3"><dt className="cv-kicker">GOALS AGAINST</dt><dd className="mt-1 text-lg font-semibold">{goalie.shotsAgainst - goalie.saves}</dd></div></dl>}
    </section>
    <section className="mt-5 rounded-2xl bg-white px-5 py-6 sm:mt-7 sm:px-8 sm:py-8">
      <div className="flex flex-col gap-4 border-b border-slate-200 pb-5 sm:flex-row sm:items-end sm:justify-between"><div><p className="cv-kicker">SHOT LOCATIONS</p><h2 className="mt-1 text-xl font-semibold tracking-[-0.04em] text-slate-950">{gameLabel}</h2></div><label className="text-sm font-medium text-slate-700">Game <select value={gameId} onChange={(event) => setGameId(event.target.value)} className="ml-2 max-w-[19rem] border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 outline-none focus:border-[#2563eb]"><option value="">Season wide</option>{games.map((game) => <option key={game.gameId} value={game.gameId}>{game.gameDate} · {game.team} vs {game.opponent}</option>)}</select></label></div>
      <div className="mt-6 overflow-x-auto border border-slate-200 bg-slate-50">{mapSelection !== gameId ? <div className="flex h-[24rem] items-center justify-center text-sm text-slate-500">Loading shot locations…</div> : mapError ? <div className="flex h-[24rem] items-center justify-center px-6 text-center text-sm text-slate-500">No shot map is available for this goalie in the selected game.</div> : <Rink shots={map?.shots ?? []} />}</div>
      <div className="mt-5 flex flex-wrap items-center gap-x-6 gap-y-3 text-sm"><span className="font-medium text-slate-700">{totals.shots.toLocaleString()} shots</span><span className="text-[#2563eb]"><span aria-hidden="true">●</span> {totals.saves.toLocaleString()} saves</span><span className="text-[#dc2626]"><span aria-hidden="true">●</span> {totals.goals.toLocaleString()} goals</span><span className="text-slate-500">Blue: save · Red: goal</span></div>
    </section>
  </main>;
}
