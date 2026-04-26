import GoalieGrid from "@/components/GoalieGrid"

const Home = async () => {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/goalies`, {
    next: { revalidate: 604800 }, // once every 7 days
  })

  if (!res.ok) throw new Error(`API failed with ${res.status}`)

  const data = await res.json()

  // const totalGoalies = data.length
  // const avgSavePctg = data.reduce((sum: number, g: any) => sum + (g.player.savePctg ?? 0), 0) / totalGoalies
  

  return (
    <main className="min-h-screen bg-neutral-950 text-neutral-100 font-sans">

      {/* Hero */}
      <header className="max-w-7xl mx-auto px-6 pt-10 pb-6">
        <h1 className="text-3xl md:text-4xl font-bold tracking-tight text-neutral-100">
          NHL Goalie Analytics
        </h1>
        <p className="text-sm text-neutral-500 mt-1.5 max-w-xl">
          Zone save maps, shot heatmaps, and performance metrics for every goalie this season.
        </p>

     
    </header>

    <GoalieGrid goalies={data} />

      {/* Footer */}
      <footer className="border-t border-white/6 py-5">
        <div className="max-w-7xl mx-auto px-6 text-center text-xs text-neutral-600">
          © 2026 CreaseVision · Data via NHL Public API
        </div>
      </footer>
    </main>
  )
}

export default Home