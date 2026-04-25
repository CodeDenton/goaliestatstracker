"use client";

import { useState } from "react";

interface Message {
  role: "user" | "assistant";
  content: string;
}

const AnalystPage = () => {

  const suggestedQuestions = [
  "Who has the best save percentage this season?",
  "Which goalie is weakest around the crease?",
  "Who has the most wins this season?",
  "Which goalie is above league average in the high slot?",
];

  const [messages, setMessages] = useState<Message[]>([
    {
      role: "assistant",
      content: "Hey! I'm the CreaseVision AI Analyst. Ask me anything about NHL goalie performance this season.",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  

  const sendMessage = async () => {
  if (!input.trim() || loading) return;

  const userMessage: Message = { role: "user", content: input };
  const updatedMessages = [...messages, userMessage];
  setMessages(updatedMessages);
  setInput("");
  setLoading(true);

  // add empty assistant message to fill in as stream comes in
  setMessages((prev) => [...prev, { role: "assistant", content: "" }]);

  try {
    const res = await fetch("/api/analyst", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        question: input,
        history: updatedMessages.slice(1, -1),
      }),
    });

    const reader = res.body?.getReader();
    const decoder = new TextDecoder();

    if (!reader) return;

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const chunk = decoder.decode(value);
      // append each chunk to the last message
      setMessages((prev) => {
        const updated = [...prev];
        updated[updated.length - 1] = {
          role: "assistant",
          content: updated[updated.length - 1].content + chunk,
        };
        return updated;
      });
    }
  } catch {
    setMessages((prev) => [
      ...prev,
      { role: "assistant", content: "Something went wrong, please try again." },
    ]);
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="h-[calc(100vh-64px)] bg-neutral-950 text-neutral-100 flex flex-col">
      {/* Header */}
      <div className="border-b border-white/10 px-6 py-6 max-w-4xl mx-auto w-full">
        <h1 className="text-2xl font-semibold tracking-tight">AI Analyst</h1>
        <p className="text-sm text-neutral-500 mt-1">
          Ask anything about NHL goalie performance this season
        </p>
      </div>

      {/* Messages */}
     <div className="flex-1 overflow-y-auto px-6 py-6 max-w-4xl mx-auto w-full space-y-4 min-h-0">
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div
              className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                msg.role === "user"
                  ? "bg-blue-600 text-white"
                  : "bg-neutral-800 text-neutral-100"
              }`}
            >
              {msg.content}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-neutral-800 rounded-2xl px-4 py-3 text-sm text-neutral-400">
              Analyzing...
            </div>
          </div>
        )}
      </div>

      {/* Input */}
      <div className="border-t border-white/10 px-6 py-4 max-w-4xl mx-auto w-full">
       {/* Suggested Questions */}
        {messages.length === 1 && (
          <div className="mb-3">
            <p className="text-xs text-neutral-500 mb-2">Try asking...</p>
            <div className="flex flex-wrap gap-2">
              {suggestedQuestions.map((q, i) => (
                <button
                  key={i}
                  onClick={() => {
                    setInput(q);
                    sendMessage();
                  }}
                  className="text-sm bg-neutral-800 hover:bg-neutral-700 border border-white/10 text-neutral-300 px-4 py-2 rounded-xl transition-colors"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="flex gap-3">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            placeholder="Who has the best save percentage this season?"
            className="flex-1 bg-neutral-800 border border-white/10 rounded-xl px-4 py-3 text-sm text-neutral-100 placeholder-neutral-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
          <button
            onClick={sendMessage}
            disabled={loading || !input.trim()}
            className="bg-blue-600 hover:bg-blue-500 disabled:opacity-40 disabled:cursor-not-allowed text-white text-sm font-medium px-5 py-3 rounded-xl transition-colors"
          >
            Send
          </button>
        </div>
      </div>
        
      </div>
  );
};
export default AnalystPage;