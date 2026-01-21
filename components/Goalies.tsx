import Goalie from "./Goalie";

interface GoalieType {
  player: {
    id: number;
    firstName: {
      default: string;
    };
    lastName: {
      default: string;
    };
    savePctg: number
    headshot: string;
  };
}
const Goalies = async () => {
  // http://127.0.0.1:8000/goalies/full
  const renderBackend = "https://goaliestatsfastapi.onrender.com/"
  const res = await fetch(`${renderBackend}/goalies/full`, {
    cache: "no-store",
  });

  console.log("STATUS:", res.status);
  console.log("OK:", res.ok);

  const text = await res.text();
  // console.log("RAW RESPONSE:", text);

  if (!res.ok) {
    throw new Error(`API failed with ${res.status}`);
  }

  const data: GoalieType[] = JSON.parse(text);
  console.log("PARSED:", data);

  return (
    <div className="flex flex-wrap gap-6 w-400">
      {data
  .sort((a: GoalieType, b: GoalieType) => {
    // Sort by save percentage in descending order
    const savePctA = a.player.savePctg ?? 0;
    const savePctB = b.player.savePctg ?? 0;
    return savePctB - savePctA;
  })
  .map((g: GoalieType) => (
    <Goalie key={g.player.id} data={g}></Goalie>
  ))
}
    </div>
  )


}
export default Goalies