// Home.tsx
import Goalie from "./Goalie";

interface GoalieType {
  player: {
    id: number;
    firstName: { default: string };
    lastName: { default: string };
    savePctg: number;
    headshot: string;
  };
}

const Home = async () => {
  // const res = await fetch("http://localhost:8080/api/goalies", { cache: "no-store" });
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/goalies`, { cache: "no-store" });

  if (!res.ok) {
    throw new Error(`API failed with ${res.status}`);
  }

  const data: GoalieType[] = await res.json();

  const totalGoalies = data.length;
  const avgSavePctg = data.reduce((sum, g) => sum + (g.player.savePctg ?? 0), 0) / totalGoalies;

  const sorted = data.sort((a, b) => (b.player.savePctg ?? 0) - (a.player.savePctg ?? 0));

  return (
    <main className="min-h-screen bg-neutral-950 text-neutral-100 font-sans">

      {/* NAVBAR */}
      <nav className="sticky top-0 z-50 border-b border-white/10 bg-neutral-950/80 backdrop-blur">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <a href="#" className="text-xl font-semibold tracking-tight text-neutral-100 hover:text-white transition-colors">
              CreaseVision
            </a>
            <span className="font-serif italic text-base text-neutral-500 pt-1 font-normal tracking-wide">
              by denton
            </span>
          </div>
          <div className="flex items-center gap-8 text-sm text-neutral-400">
            <a href="#" className="hover:text-neutral-100 transition-colors">Home</a>
            <a href="#" className="hover:text-neutral-100 transition-colors">Goalies</a>
            <a href="#" className="hover:text-neutral-100 transition-colors">About</a>
          </div>
        </div>
      </nav>

      {/* HERO / DASHBOARD OVERVIEW */}
      <header className="max-w-7xl mx-auto px-6 pt-10 pb-8">
        <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-neutral-100">
          Advanced NHL goalie analytics
        </h1>
        <p className="text-sm md:text-base text-neutral-400 mt-2 max-w-2xl">
          Track shot maps, danger zones, save metrics, and zone performance all in one place.
        </p>

        {/* Dashboard Stats Row */}
        <div className="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-6">
          <div className="bg-neutral-900/70 border border-white/10 rounded-xl p-6 text-center hover:shadow-lg transition-shadow">
            <div className="text-2xl font-bold text-green-500">
              {(avgSavePctg).toFixed(3)}%
            </div>
            <div className="mt-1 text-xs text-neutral-400 uppercase tracking-wide">Average SV%</div>
          </div>
          <div className="bg-neutral-900/70 border border-white/10 rounded-xl p-6 text-center hover:shadow-lg transition-shadow">
            <div className="text-2xl font-bold text-blue-400">{totalGoalies}</div>
            <div className="mt-1 text-xs text-neutral-400 uppercase tracking-wide">Goalies</div>
          </div>
          <div className="bg-neutral-900/70 border border-white/10 rounded-xl p-6 text-center hover:shadow-lg transition-shadow">
            <div className="text-2xl font-bold text-yellow-400">25-26</div>
            <div className="mt-1 text-xs text-neutral-400 uppercase tracking-wide">Season</div>
          </div>
        </div>
      </header>

      {/* GOALIE GRID */}
      <section className="max-w-7xl mx-auto px-6 pb-16">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-sm font-semibold tracking-wider uppercase text-neutral-400">
            Goalie Analytics
          </h2>
        </div>

        <div className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-6">
          {sorted.map((g) => (
            <Goalie key={g.player.id} data={g} />
          ))}
        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t border-white/10 bg-neutral-950/80 backdrop-blur py-6">
        <div className="max-w-7xl mx-auto px-6 text-center text-xs text-neutral-400">
          © 2026 CreaseVision · Data via NHL API
        </div>
      </footer>
    </main>
  );
};

export default Home;