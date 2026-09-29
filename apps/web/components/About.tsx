const About = () => {
  return (
    <main className="min-h-screen bg-neutral-950 text-neutral-100 font-sans">
      <div className="max-w-3xl mx-auto px-6 py-20">

        {/* Header */}
        <div className="mb-20">
          <p className="text-[10px] uppercase tracking-[0.2em] text-neutral-600 mb-4">About</p>
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-neutral-100 leading-tight mb-6">
            CreaseVision
          </h1>
          <p className="text-lg text-neutral-400 leading-relaxed max-w-xl font-light">
            A goalie on a struggling team carries numbers that often lie. CreaseVision looks past general team stats and dives deep into the data that actually matters.
          </p>
        </div>

        <div className="h-px bg-white/6 mb-20" />

        {/* About */}
        <section className="mb-20">
          <p className="text-[10px] uppercase tracking-[0.2em] text-neutral-600 mb-8">The Platform</p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[
              {
                title: "Zone Save Maps",
                desc: "Interactive heat maps showing save percentage in all areas of the offensive zone",
              },
              {
                title: "Shot Location Analysis",
                desc: "View high-danger, mid-range, and long-range performance with percentile rankings against the league.",
              },
              {
                title: "AI Analyst",
                desc: "Ask questions about any goalie and get data driven answers powered by Claude AI.",
              },
              {
                title: "Live Season Data",
                desc: "Pulled from the NHL public API and refreshed regularly, covering every active NHL goalie this season.",
              },
            ].map(({ title, desc }) => (
              <div key={title} className="bg-neutral-900 border border-white/6 rounded-2xl p-6">
                <div className="text-sm font-semibold text-neutral-100 mb-2">{title}</div>
                <div className="text-sm text-neutral-500 leading-relaxed font-light">{desc}</div>
              </div>
            ))}
          </div>
        </section>

        <div className="h-px bg-white/6 mb-20" />

        {/* Tech stack */}
        <section className="mb-20">
          <p className="text-[10px] uppercase tracking-[0.2em] text-neutral-600 mb-8">Built With</p>
          <div className="flex flex-wrap gap-3">
            {[
              "Next.js", "TypeScript", "Tailwind CSS",
              "Spring Boot", "PostgreSQL", "Claude AI", "NHL Public API",
            ].map((tech) => (
              <span
                key={tech}
                className="px-4 py-2 bg-neutral-900 border border-white/6 rounded-xl text-xs font-medium text-neutral-400 tracking-wide"
              >
                {tech}
              </span>
            ))}
          </div>
        </section>

        <div className="h-px bg-white/6 mb-20" />

        {/* About me */}
        <section className="mb-20">
          <p className="text-[10px] uppercase tracking-[0.2em] text-neutral-600 mb-8">The Builder</p>
          <div className="flex flex-col gap-4 max-w-xl">
            <p className="text-sm text-neutral-400 leading-relaxed font-light">
              I'm Denton, a aspiring software developer with a background in computer science and statistics with a passion for hockey analytics. 
              As a goalie myself, CreaseVision started as a curiosity about what modern goalie evaluation was missing, 
              and turned into a full-stack project I'm genuinely proud of.
            </p>
            <p className="text-sm text-neutral-400 leading-relaxed font-light">
              If you have feedback, find a bug, or just want to talk hockey and tech, please reach out.
            </p>
            <div className="flex gap-4 mt-2">
              <a
                href="https://github.com/CodeDenton"
                target="_blank"
                rel="noopener noreferrer"
                className="text-xs text-neutral-500 hover:text-neutral-200 transition-colors tracking-wide uppercase"
              >
                GitHub →
              </a>
              <a
                href="mailto:dentonmanders@gmail.com"
                className="text-xs text-neutral-500 hover:text-neutral-200 transition-colors tracking-wide uppercase"
              >
                Email →
              </a>
            </div>
          </div>
        </section>

        {/* Footer note */}
        <div className="border-t border-white/6 pt-8">
          <p className="text-xs text-neutral-700">
            © 2026 CreaseVision · Data via NHL Public API · Not affiliated with the NHL
          </p>
        </div>

      </div>
    </main>
  )
}

export default About