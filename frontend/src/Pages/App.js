import "../style/tailwind.css";
import "../style/App.css";
import Home from "./Home";
import Footer from "../components/Footer";
import Sidebar from "../components/Sidebar";
import PublicNav from "../components/PublicNav";
import { Route, Routes, useLocation } from "react-router-dom";
import { motion } from "framer-motion";
import Login from "./Login";
import Registration from "./Registration";
import Dashboard from "./Dashboard";
import Activity from "./Activity";
import History from "./History";
import Projects from "./Projects";
import ProjectDetail from "./ProjectDetail";
import Reminders from "./Reminders";
import Trash from "./Trash";
import Profile from "./Profile";
import Admin from "./Admin";
import ProtectedRoute from "../components/ProtectedRoute";
import AdminRoute from "../components/AdminRoute";
import { useCallback, useState } from "react";
import Toast from "../components/Toast";
import useReminderNotifications from "../hooks/useReminderNotifications";

const PUBLIC_PATHS = ["/", "/login", "/register"];

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
  const location = useLocation();
  const isPublic = PUBLIC_PATHS.includes(location.pathname);

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
      {isPublic ? <PublicNav /> : <Sidebar />}

      {/* Toast overlay */}
      <div className="mtm-fixed mtm-top-20 mtm-right-3 sm:mtm-right-5 mtm-z-[70] mtm-w-[88%] sm:mtm-w-[360px] mtm-flex mtm-flex-col mtm-pointer-events-none">
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

      <motion.main
        key={location.pathname}
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: "easeOut" }}
        className={`mtm-flex-1 mtm-flex mtm-flex-col ${isPublic ? "" : "lg:mtm-pl-64"}`}
      >
        <Routes location={location}>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login {...toastProps} />} />
          <Route path="/register" element={<Registration {...toastProps} />} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard {...toastProps} /></ProtectedRoute>} />
          <Route path="/activity" element={<ProtectedRoute><Activity {...toastProps} /></ProtectedRoute>} />
          <Route path="/history" element={<ProtectedRoute><History {...toastProps} /></ProtectedRoute>} />
          <Route path="/projects" element={<ProtectedRoute><Projects {...toastProps} /></ProtectedRoute>} />
          <Route path="/projects/:id" element={<ProtectedRoute><ProjectDetail {...toastProps} /></ProtectedRoute>} />
          <Route path="/reminders" element={<ProtectedRoute><Reminders {...toastProps} /></ProtectedRoute>} />
          <Route path="/trash" element={<ProtectedRoute><Trash {...toastProps} /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile {...toastProps} /></ProtectedRoute>} />
          <Route path="/admin" element={<AdminRoute><Admin {...toastProps} /></AdminRoute>} />
        </Routes>
      </motion.main>

      {isPublic && <Footer />}
    </div>
  );
}

export default App;
