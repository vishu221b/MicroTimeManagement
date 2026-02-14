# MICRO TIME MANAGEMENT (Work In Progress)

### ABOUT

I’m finally finishing Micro Time Management!
I started this app to survive my Master’s degree, and while life got in the way for a bit, it’s time to get the final features across the finish line. 

Stay tuned for updates!

### What It Does
It's a **time/activity tracking app** — users register, log in, and record daily "activities" (time blocks with start/end hours, descriptions, and meridian indicators). An admin role manages user roles. Sessions are stored in MongoDB and validated via a custom JWT filter.

### Backend Tech Stack

| Component | Current Implementation                                 |
|-----------|--------------------------------------------------------|
| Framework | Spring Boot 3.0.0, Java 17                             |
| Database | MongoDB (Spring Data MongoDB)                          |
| Auth | Custom JWT with `jjwt` + session-in-DB validation      |
| API Docs | springdoc-openapi             |
| Security | Spring Security 6 with custom `MtmSessionFilter`       |
| Containerization | Docker + Docker Compose (MongoDB + Mongo Express + app) |
| Build | Maven                                                  |

### Frontend Tech Stack

| Component | Current Implementation |
|-----------|----------------------|
| Framework | React 18 with React Router v6 |
| Styling | Tailwind CSS 3.2 + Bootstrap 5 + Ant Design 5 |
| HTTP Client | Axios |
| State Management | React `useState` (local) |