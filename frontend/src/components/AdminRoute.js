import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import useAuth from "../hooks/useAuth";

export default function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin, profileLoaded } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  // Wait for the profile fetch to settle so we don't bounce admins on first paint.
  if (!profileLoaded) {
    return (
      <div className="mtm-min-h-[60vh] mtm-bg-black mtm-text-white mtm-py-20 mtm-text-center">
        Loading…
      </div>
    );
  }
  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}
