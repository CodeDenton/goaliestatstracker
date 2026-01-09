import Goalie from "./Goalie"
import Goalies from "./Goalies"

const Home = () => {
    
  return (
   <>
   <main className="min-h-screen bg-[#495057] text-white">
    <nav className="bg-[#212529]">
  <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
    <div className="text-[#F8F9FA] text-xl font-bold">
      CreaseVision
    </div>
    
    <div className="flex gap-6">
      <a href="#" className="hover:text-gray-300">Home</a>
      <a href="#" className="hover:text-gray-300">Goalies</a>
      <a href="#" className="hover:text-gray-300">Contact</a>
    </div>
  </div>
</nav>
    {/* <header className="p-6 border-b border-slate-700">
        <h1 className="text-[#F8F9FA] text-3xl font-bold">CreaseVision</h1>
        <p className="text-[#F8F9FA] mt-1">Visualizing NHL goalie performance through shot maps, danger zones, and advanced metrics.</p>
    </header> */}

     <section className="p-6">
        <h2 className="text-[#F8F9FA] text-xl font-semibold mb-4">
          Goalies
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {/* Goalie cards will go here */}
            {/* <Goalie playerId="8476945"></Goalie>
            <Goalie playerId="8477992"></Goalie>
            <Goalie playerId="8476412"></Goalie> */}
            <Goalies></Goalies>


        </div>
      </section>

   </main>
   </>
  )
}

export default Home