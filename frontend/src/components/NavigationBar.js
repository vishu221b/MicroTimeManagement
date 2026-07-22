import React, { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { FiMenu, FiX, FiClock } from "react-icons/fi";
import useAuth from "../hooks/useAuth";
import { logoutUser } from "../service/ApiService";
import ThemeToggle from "./ThemeToggle";

const authedLinks = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/activity", label: "Activity" },
  { to: "/history", label: "History" },
  { to: "/profile", label: "Profile" },
];

function NavigationBar() {
  const [open, setOpen] = useState(false);
  const { isAuthenticated, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    setOpen(false);
    await logoutUser();
    navigate("/login", { replace: true });
  };

  const links = isAdmin
    ? [...authedLinks, { to: "/admin", label: "Admin" }]
    : authedLinks;

  const linkClass = ({ isActive }) =>
    `mtm-px-3 mtm-py-2 mtm-rounded-lg mtm-text-sm mtm-font-semibold mtm-transition-colors ${
      isActive
        ? "mtm-bg-primary-soft mtm-text-primary-hover"
        : "mtm-text-muted hover:mtm-text-content hover:mtm-bg-surface-2"
    }`;

  return (
    <header className="mtm-sticky mtm-top-0 mtm-z-50 mtm-border-b mtm-border-line mtm-bg-surface/85 mtm-backdrop-blur">
      <nav className="mtm-max-w-6xl mtm-mx-auto mtm-px-4 sm:mtm-px-6 mtm-flex mtm-items-center mtm-justify-between mtm-h-16">
        <Link
          to="/"
          className="mtm-flex mtm-items-center mtm-gap-2 mtm-no-underline"
          onClick={() => setOpen(false)}
        >
          <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-9 mtm-w-9 mtm-rounded-xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white">
            <FiClock size={18} />
          </span>
          <span className="mtm-font-display mtm-font-extrabold mtm-text-lg mtm-text-content mtm-tracking-tight">
            MTM
          </span>
        </Link>

        {/* Desktop */}
        <div className="mtm-hidden md:mtm-flex mtm-items-center mtm-gap-1">
          {isAuthenticated &&
            links.map((l) => (
              <NavLink key={l.to} to={l.to} className={linkClass}>
                {l.label}
              </NavLink>
            ))}
        </div>

        <div className="mtm-hidden md:mtm-flex mtm-items-center mtm-gap-2">
          <ThemeToggle />
          {isAuthenticated ? (
            <button onClick={handleLogout} className="ui-btn ui-btn-ghost ui-btn-sm">
              Logout
            </button>
          ) : (
            <>
              <Link to="/login" className="ui-btn ui-btn-ghost ui-btn-sm">
                Sign in
              </Link>
              <Link to="/register" className="ui-btn ui-btn-primary ui-btn-sm">
                Try now
              </Link>
            </>
          )}
        </div>

        {/* Mobile toggle */}
        <div className="mtm-flex md:mtm-hidden mtm-items-center mtm-gap-2">
          <ThemeToggle />
          <button
            type="button"
            aria-label="Toggle menu"
            onClick={() => setOpen((o) => !o)}
            className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-9 mtm-w-9 mtm-rounded-lg mtm-border mtm-border-line mtm-bg-surface-2 mtm-text-content"
          >
            {open ? <FiX size={20} /> : <FiMenu size={20} />}
          </button>
        </div>
      </nav>

      {/* Mobile menu */}
      {open && (
        <div className="md:mtm-hidden mtm-border-t mtm-border-line mtm-bg-surface mtm-px-4 mtm-py-3 mtm-flex mtm-flex-col mtm-gap-1">
          {isAuthenticated ? (
            <>
              {links.map((l) => (
                <NavLink
                  key={l.to}
                  to={l.to}
                  className={linkClass}
                  onClick={() => setOpen(false)}
                >
                  {l.label}
                </NavLink>
              ))}
              <button
                onClick={handleLogout}
                className="ui-btn ui-btn-ghost mtm-mt-2"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="ui-btn ui-btn-ghost"
                onClick={() => setOpen(false)}
              >
                Sign in
              </Link>
              <Link
                to="/register"
                className="ui-btn ui-btn-primary mtm-mt-1"
                onClick={() => setOpen(false)}
              >
                Try now
              </Link>
            </>
          )}
        </div>
      )}
    </header>
  );
}

export default NavigationBar;
