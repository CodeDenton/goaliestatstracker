import { NextRequest } from "next/server";
import Anthropic from "@anthropic-ai/sdk";
import { checkRateLimit } from "@vercel/firewall";

const ANALYST_RATE_LIMIT_ID = "analyst-chat";
const MAX_REQUEST_BYTES = 16 * 1024;
const MAX_QUESTION_LENGTH = 1_000;
const MAX_HISTORY_MESSAGES = 8;
const MAX_HISTORY_MESSAGE_LENGTH = 2_000;

type ConversationMessage = {
  role: "user" | "assistant";
  content: string;
};

type LocalizedName = { default: string };

type ShotLocationSummary = {
  locationCode: string;
  savePctg: number;
  savePctgPercentile: number;
  savePctgLeagueAvg: number;
  goalsAgainst: number;
  goalsAgainstLeagueAvg: number;
};

type ShotLocationDetail = {
  area: string;
  savePctg: number;
  savePctgPercentile: number;
  saves: number;
  savesPercentile: number;
};

type GoaliePayload = {
  player: {
    firstName: LocalizedName;
    lastName: LocalizedName;
    team: { commonName: LocalizedName };
    gamesPlayed: number;
    wins: number;
    losses: number;
    overtimeLosses: number;
    goalsAgainstAvg: number;
    savePctg: number;
  };
  shotLocationSummary?: ShotLocationSummary[];
  shotLocationDetails?: ShotLocationDetail[];
};

const client = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

export async function POST(req: NextRequest) {
  let rateLimitResult: Awaited<ReturnType<typeof checkRateLimit>>;

  try {
    rateLimitResult = await checkRateLimit(ANALYST_RATE_LIMIT_ID, { request: req });
  } catch (error) {
    // Fail closed: the expensive upstream request must not run if its protection is unavailable.
    console.error("Analyst rate-limit check failed", error);
    return new Response("The Analyst is temporarily unavailable. Please try again later.", { status: 503 });
  }

  if (rateLimitResult.error === "not-found") {
    console.error("Analyst rate-limit rule is not configured");
    return new Response("The Analyst is temporarily unavailable. Please try again later.", { status: 503 });
  }

  if (rateLimitResult.rateLimited) {
    return new Response("Too many requests. Please wait a minute and try again.", {
      status: 429,
      headers: { "Cache-Control": "no-store" },
    });
  }

  const contentLength = Number(req.headers.get("content-length") ?? 0);
  if (contentLength > MAX_REQUEST_BYTES) {
    return new Response("Request is too large.", { status: 413 });
  }

  try {
    const body: unknown = await req.json();
    if (!body || typeof body !== "object") {
      return new Response("Invalid request.", { status: 400 });
    }

    const { question, history } = body as { question?: unknown; history?: unknown };
    if (
      typeof question !== "string" ||
      question.trim().length === 0 ||
      question.length > MAX_QUESTION_LENGTH ||
      !Array.isArray(history) ||
      history.length > MAX_HISTORY_MESSAGES ||
      !history.every(
        (message): message is ConversationMessage =>
          Boolean(message) &&
          typeof message === "object" &&
          (message.role === "user" || message.role === "assistant") &&
          typeof message.content === "string" &&
          message.content.length <= MAX_HISTORY_MESSAGE_LENGTH,
      )
    ) {
      return new Response("Invalid request.", { status: 400 });
    }

    const goaliesRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/goalies`, {
      next: { revalidate: 604800 }, // once every 7 days (to save claude credits and reg season is over rn so no need)
    });
    if (!goaliesRes.ok) {
      return new Response("Goalie data is temporarily unavailable.", { status: 503 });
    }
    const goalies = (await goaliesRes.json()) as GoaliePayload[];

    const goalieContext = goalies.map((g) => ({
      name: `${g.player.firstName.default} ${g.player.lastName.default}`,
      team: g.player.team.commonName.default,
      gamesPlayed: g.player.gamesPlayed,
      wins: g.player.wins,
      losses: g.player.losses,
      overtimeLosses: g.player.overtimeLosses,
      goalsAgainstAvg: g.player.goalsAgainstAvg,
      savePctg: g.player.savePctg,
      shotLocationSummary: g.shotLocationSummary?.map((s) => ({
        locationCode: s.locationCode,
        savePctg: s.savePctg,
        savePctgPercentile: s.savePctgPercentile,
        savePctgLeagueAvg: s.savePctgLeagueAvg,
        goalsAgainst: s.goalsAgainst,
        goalsAgainstLeagueAvg: s.goalsAgainstLeagueAvg,
      })),
      shotLocationDetails: g.shotLocationDetails?.map((d) => ({
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
      ...history.map((msg) => ({
        role: msg.role,
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
        "Cache-Control": "no-store",
      },
    });
  } catch (error) {
    console.error("Analyst request failed", error);
    return new Response("Something went wrong", { status: 500 });
  }
}
