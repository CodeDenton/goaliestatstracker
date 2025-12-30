import Goalie from "./Goalie"
import Goalies from "./Goalies"

const Home = () => {
    
  return (
   <>
   <main className="min-h-screen bg-slate-900 text-white">
    <header className="p-6 border-b border-slate-700">
        <h1 className="text-3xl font-bold">Goalie Stats Dashboard</h1>
        <p className="text-slate-400 mt-1">NHL goalie performance and analytics</p>
    </header>

     <section className="p-6">
        <h2 className="text-xl font-semibold mb-4">
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