import { NextRequest } from "next/server";
import Anthropic from "@anthropic-ai/sdk";

const client = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

export async function POST(req: NextRequest) {
  try {
    const { question, history } = await req.json();

    const goaliesRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/goalies`, {
      next: { revalidate: 604800 }, // once every 7 days (to save claude credits and reg season is over rn so no need)
    });
    const goalies = await goaliesRes.json();

    const goalieContext = goalies.map((g: any) => ({
      name: `${g.player.firstName.default} ${g.player.lastName.default}`,
      team: g.player.team.commonName.default,
      gamesPlayed: g.player.gamesPlayed,
      wins: g.player.wins,
      losses: g.player.losses,
      overtimeLosses: g.player.overtimeLosses,
      goalsAgainstAvg: g.player.goalsAgainstAvg,
      savePctg: g.player.savePctg,
      shotLocationSummary: g.shotLocationSummary?.map((s: any) => ({
        locationCode: s.locationCode,
        savePctg: s.savePctg,
        savePctgPercentile: s.savePctgPercentile,
        savePctgLeagueAvg: s.savePctgLeagueAvg,
        goalsAgainst: s.goalsAgainst,
        goalsAgainstLeagueAvg: s.goalsAgainstLeagueAvg,
      })),
      shotLocationDetails: g.shotLocationDetails?.map((d: any) => ({
        area: d.area,
        savePctg: d.savePctg,
        savePctgPercentile: d.savePctgPercentile,
        saves: d.saves,
        savesPercentile: d.savesPercentile,
      })),
    }));

    const conversationHistory = [
      {
        role: "user" as const,
        content: `Here is the current NHL goalie data:\n\n${JSON.stringify(goalieContext, null, 2)}`,
      },
      {
        role: "assistant" as const,
        content: "Got it! I have the full goalie dataset loaded. What would you like to know?",
      },
      ...history.map((msg: any) => ({
        role: msg.role as "user" | "assistant",
        content: msg.content,
      })),
      {
        role: "user" as const,
        content: question,
      },
    ];

    const stream = await client.messages.stream({
      model: "claude-haiku-4-5",
      max_tokens: 1024,
      system: `You are a passionate and conversational NHL goalie expert named Vision. 
  You love talking hockey and make stats feel exciting and accessible. 
  You speak directly and clearly showcase stats. 
  Use the goalie data provided to back up your points but keep it simple and clear. 
  Keep responses concise and short. Do not use any markdown formatting, bullet points, or asterisks in your responses. Plain text only. Round save percentage's to decimal with 3 decimals, 86.49% becomes 0.865`,
      messages: conversationHistory,
    });

    const encoder = new TextEncoder();
    const readable = new ReadableStream({
      async start(controller) {
        for await (const chunk of stream) {
          if (chunk.type === "content_block_delta" && chunk.delta.type === "text_delta") {
            controller.enqueue(encoder.encode(chunk.delta.text));
          }
        }
        controller.close();
      },
    });

    return new Response(readable, {
      headers: {
        "Content-Type": "text/plain; charset=utf-8",
        "Transfer-Encoding": "chunked",
      },
    });
  } catch (error) {
    console.error(error);
    return new Response("Something went wrong", { status: 500 });
  }
}