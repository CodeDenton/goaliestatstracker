import GoalieHome from "@/components/GoalieHome"

// The homepage reads live data from the separately deployed API. Render it on
// request so a Vercel build does not depend on that service being awake.
export const dynamic = "force-dynamic"

const page = () => {
  return (
    <GoalieHome/>
  )
}

export default page
