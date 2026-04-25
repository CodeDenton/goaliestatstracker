const Navbar = () => {
  return (
    <nav className="sticky top-0 z-50 border-b border-white/10 bg-neutral-950/80 backdrop-blur">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <a href="#" className="text-xl font-semibold tracking-tight text-neutral-100 hover:text-white transition-colors">
              CreaseVision
            </a>
            <span className="font-serif italic text-base text-neutral-500 pt-1 font-normal tracking-wide">
              by denton
            </span>
          </div>
          <div className="flex items-center gap-8 text-sm text-neutral-400">
            <a href="/" className="hover:text-neutral-100 transition-colors">Goalies</a>
            <a href="/analyst" className="hover:text-neutral-100 transition-colors">AI Analyst</a>
            <a href="#" className="hover:text-neutral-100 transition-colors">About</a>
          </div>
        </div>
      </nav>
  );
};

export default Navbar