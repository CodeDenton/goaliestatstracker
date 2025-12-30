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
    headshot: string;
  };
}
const Goalies = async () => {
    const res = await fetch(`http://localhost:8000/goalies/full`, {
  cache: "no-store",
});

console.log("STATUS:", res.status);
console.log("OK:", res.ok);

const text = await res.text();
// console.log("RAW RESPONSE:", text);

if (!res.ok) {
  throw new Error(`API failed with ${res.status}`);
}

const data : GoalieType[] = JSON.parse(text);
console.log("PARSED:", data);

    return(
        <>
        {data.map((g : GoalieType) => (
            <Goalie key={g.player.id} data={g}></Goalie>
        ))}
        </>
    )

    
}
export default Goalies