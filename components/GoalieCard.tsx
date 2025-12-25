
const GoalieCard = async () => {
    const res = await fetch("https://api-web.nhle.com/v1/player/8478048/landing");
    const data = await res.json();

// Navigate the nested object
    const savePcg = data.featuredStats?.regularSeason?.subSeason?.savePctg;
    console.log(savePcg);
    console.log("Helloooo")

    
  return (
    <>
    <div className="bg-slate-800 rounded-xl p-4 hover:bg-slate-700 transition">
  <h3 className="text-lg font-semibold">
    Stuart Skinner
  </h3>
  <p className="text-slate-400">
    Pittsburgh Penguins
  </p>
  <p>{Number(savePcg.toFixed(3))}</p>
</div>
    </>
  )
}

export default GoalieCard