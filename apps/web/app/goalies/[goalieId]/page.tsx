import GoalieShotMapPage from "@/components/GoalieShotMapPage";
import type { LeaderboardRow } from "@/components/LeaderboardExplorer";

export const dynamic = "force-dynamic";
const season = "20262027";

async function getGoalie(goalieId: number): Promise<LeaderboardRow | null> {
  const base = process.env.NEXT_PUBLIC_API_URL;
  if (!base) return null;
  try {
    const response = await fetch(`${base}/api/leaderboard?season=${season}&minGames=0&size=100&sort=savePctg&direction=desc`, { cache: "no-store" });
    if (!response.ok) return null;
    const data: { rows: LeaderboardRow[] } = await response.json();
    return data.rows.find((goalie) => goalie.goalieId === goalieId) ?? null;
  } catch { return null; }
}

export default async function GoaliePage({ params }: { params: Promise<{ goalieId: string }> }) {
  const { goalieId: rawGoalieId } = await params;
  const goalieId = Number(rawGoalieId);
  return <GoalieShotMapPage goalieId={goalieId} goalie={Number.isFinite(goalieId) ? await getGoalie(goalieId) : null} />;
}
