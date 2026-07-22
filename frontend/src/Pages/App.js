import NavigationBar from "../components/NavigationBar";
import "../style/tailwind.css";
import "../style/App.css";
import Home from "./Home";
import Footer from "../components/Footer";
import { Route, Routes } from "react-router-dom";
import Login from "./Login";
import Registration from "./Registration";
import Dashboard from "./Dashboard";
import Activity from "./Activity";
import History from "./History";
import Projects from "./Projects";
import ProjectDetail from "./ProjectDetail";
import Reminders from "./Reminders";
import Profile from "./Profile";
import Admin from "./Admin";
import ProtectedRoute from "../components/ProtectedRoute";
import AdminRoute from "../components/AdminRoute";
import { useCallback, useState } from "react";
import Toast from "../components/Toast";
import useReminderNotifications from "../hooks/useReminderNotifications";

function App() {
  const defaultToastState = {
    display: false,
    variant: "",
    messages: [],
    includePrefix: false,
    includeSuffix: false,
    suffix: "",
  };
  const [toastState, setToastState] = useState(defaultToastState);

  const toastProps = { toastState, setToastState };

  // App-wide: fire in-app + browser notifications for due reminders.
  const onReminderFire = useCallback(
    (r) =>
      setToastState({
        display: true,
        variant: "success",
        messages: [`Reminder: ${r.title}`],
        includePrefix: false,
      }),
    []
  );
  useReminderNotifications({ onFire: onReminderFire });

  return (
    <div className="mtm-app-shell mtm-flex mtm-flex-col mtm-min-h-screen">
      <NavigationBar />

      {/* Toast overlay */}
      <div className="mtm-fixed mtm-top-20 mtm-right-3 sm:mtm-right-5 mtm-z-[60] mtm-w-[88%] sm:mtm-w-[360px] mtm-flex mtm-flex-col mtm-pointer-events-none">
        {toastState.display &&
          toastState.messages.map((message, index) => (
            <Toast
              variant={toastState.variant}
              key={`${index}-${message}`}
              show={true}
              autoHide
              autoHideDelayInMs={5000}
              includePrefix={toastState.variant === "success"}
              includeSuffix={toastState.includeSuffix}
              suffix={toastState.suffix}
            >
              {message}
            </Toast>
          ))}
      </div>

      <main className="mtm-flex-1 mtm-flex mtm-flex-col">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login {...toastProps} />} />
          <Route path="/register" element={<Registration {...toastProps} />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activity"
            element={
              <ProtectedRoute>
                <Activity {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/history"
            element={
              <ProtectedRoute>
                <History {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/projects"
            element={
              <ProtectedRoute>
                <Projects {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/projects/:id"
            element={
              <ProtectedRoute>
                <ProjectDetail {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reminders"
            element={
              <ProtectedRoute>
                <Reminders {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Profile {...toastProps} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <Admin {...toastProps} />
              </AdminRoute>
            }
          />
        </Routes>
      </main>

      <Footer />
    </div>
  );
}

export default App;
