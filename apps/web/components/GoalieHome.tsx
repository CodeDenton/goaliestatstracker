import LeaderboardExplorer, { type LeaderboardRow } from "@/components/LeaderboardExplorer";

type Response = { total: number; rows: LeaderboardRow[] };
const season = "20262027";

async function getLeaderboard(): Promise<Response | null> {
  const base = process.env.NEXT_PUBLIC_API_URL;
  if (!base) return null;
  try {
    const response = await fetch(`${base}/api/leaderboard?season=${season}&minGames=0&size=100&sort=savePctg&direction=desc`, {
      cache: "force-cache",
      next: { revalidate: 3600 },
    });
    return response.ok ? response.json() : null;
  } catch { return null; }
}

export default async function GoalieHome() {
  const data = await getLeaderboard();
  const rows = data?.rows ?? [];
  const totalSaves = rows.reduce((sum, goalie) => sum + goalie.saves, 0);
  const totalShots = rows.reduce((sum, goalie) => sum + goalie.shotsAgainst, 0);
  const leagueSavePct = totalShots ? totalSaves / totalShots : 0;

  return <main className="cv-shell py-6 sm:py-8">
    <section className="rounded-2xl bg-[#eff6ff] px-5 py-6 sm:px-8 sm:py-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="cv-kicker text-[#1d4ed8]">NHL · 2026–27 REGULAR SEASON</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.045em] text-slate-950 sm:text-3xl">Goalie leaderboard</h1>
        </div>
        <p className="text-sm text-slate-500">Updated from NHL game data</p>
      </div>

      <dl className="mt-7 grid gap-3 sm:grid-cols-3">
        <div className="rounded-xl bg-white px-4 py-4"><dt className="cv-kicker">GOALIES</dt><dd className="mt-1 text-2xl font-semibold tracking-[-0.04em] text-slate-950">{data?.total ?? "—"}</dd></div>
        <div className="rounded-xl bg-white px-4 py-4"><dt className="cv-kicker">LEAGUE SV%</dt><dd className="mt-1 font-mono text-2xl font-semibold tracking-[-0.04em] text-[#1d4ed8]">{leagueSavePct ? leagueSavePct.toFixed(3).replace(/^0/, "") : "—"}</dd></div>
        <div className="rounded-xl bg-white px-4 py-4"><dt className="cv-kicker">SHOTS TRACKED</dt><dd className="mt-1 font-mono text-2xl font-semibold tracking-[-0.04em] text-[#b91c1c]">{totalShots ? totalShots.toLocaleString() : "—"}</dd></div>
      </dl>
    </section>

    <section className="mt-5 rounded-2xl bg-white px-5 py-6 sm:mt-7 sm:px-8 sm:py-8">
      <LeaderboardExplorer goalies={rows} unavailable={!data} />
    </section>
  </main>;
}
