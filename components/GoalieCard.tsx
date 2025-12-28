type GoalieCardProps = {
  playerId: number;
};

const GoalieCard = async () => {
    const res = await fetch("https://api-web.nhle.com/v1/player/8479973/landing");
    const data = await res.json();

// Navigate the nested object
    const firstName = data["firstName"]["default"];
    const lastName = data["lastName"]["default"]
    const teamName = data["fullTeamName"]["default"]
    const savePcg = data.featuredStats?.regularSeason?.subSeason?.savePctg;
    const careerSavePcg = data.featuredStats?.regularSeason?.career?.savePctg
    const headshot = data.headshot

    console.log(data);

    
  return (
    <>
    <div className="bg-slate-800 rounded-xl p-4 hover:bg-slate-700 transition">
  <h3 className="text-lg font-semibold">
    {firstName + " " + lastName}
  </h3>
  <p className="text-slate-400">
    {teamName}
  </p>
  <p>Season Save Percentage: <strong>{Number(savePcg.toFixed(3))}</strong></p>
  <p>Career Save Percentage: <strong>{Number(careerSavePcg.toFixed(3))}</strong></p>
  <p><img src={headshot}></img></p>
</div>
    </>
  )
}

export default GoalieCard