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

const Goalies = async () => {
  const renderBackend = "https://goaliestatsfastapi.onrender.com";
  const res = await fetch(`${renderBackend}/goalies/full`, { cache: "no-store" });

  if (!res.ok) {
    throw new Error(`API failed with ${res.status}`);
  }

  const data: GoalieType[] = await res.json();

  const totalGoalies = data.length;

  const avgSavePctg = data.reduce((sum, g) => sum + (g.player.savePctg ?? 0), 0) / totalGoalies;

  return (
    <>
      {data
        .sort((a: GoalieType, b: GoalieType) => (b.player.savePctg ?? 0) - (a.player.savePctg ?? 0))
        .map((g: GoalieType) => (
          <Goalie key={g.player.id} data={g} />
        ))}
    </>
  );
};

export default Goalies;