import GoalieHome from "@/components/GoalieHome"

// Cache the rendered leaderboard at Vercel. The API data is refreshed by the
// nightly import, so rendering this route for every visitor only adds API wake
// time without making the data meaningfully fresher.
export const revalidate = 3600

const page = () => {
  return (
    <GoalieHome/>
  )
}

export default page
