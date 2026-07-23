import React, { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import {
  FiGrid,
  FiClock,
  FiFolder,
  FiBell,
  FiCalendar,
  FiUser,
  FiShield,
  FiLogOut,
  FiMenu,
  FiX,
  FiZap,
  FiTrash2,
} from "react-icons/fi";
import useAuth from "../hooks/useAuth";
import { logoutUser } from "../service/ApiService";
import ThemeToggle from "./ThemeToggle";
import InstallButton from "./InstallButton";

const NAV = [
  { to: "/dashboard", label: "Dashboard", icon: <FiGrid /> },
  { to: "/activity", label: "Activity", icon: <FiClock /> },
  { to: "/projects", label: "Projects", icon: <FiFolder /> },
  { to: "/reminders", label: "Reminders", icon: <FiBell /> },
  { to: "/history", label: "History", icon: <FiCalendar /> },
  { to: "/trash", label: "Trash", icon: <FiTrash2 /> },
  { to: "/profile", label: "Profile", icon: <FiUser /> },
];

function NavItems({ isAdmin, onNavigate }) {
  const items = isAdmin
    ? [...NAV, { to: "/admin", label: "Admin", icon: <FiShield /> }]
    : NAV;
  const cls = ({ isActive }) =>
    `mtm-flex mtm-items-center mtm-gap-3 mtm-px-3.5 mtm-py-2.5 mtm-rounded-xl mtm-font-bold mtm-border-2 mtm-transition-all ${
      isActive
        ? "mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-border-ink mtm-shadow-comic-sm"
        : "mtm-text-content mtm-border-transparent hover:mtm-border-ink hover:mtm-bg-surface-2 hover:-mtm-translate-y-0.5"
    }`;
  return (
    <nav className="mtm-flex mtm-flex-col mtm-gap-1.5">
      {items.map((it) => (
        <NavLink key={it.to} to={it.to} className={cls} onClick={onNavigate}>
          <span className="mtm-text-lg">{it.icon}</span>
          <span>{it.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

function Brand() {
  return (
    <Link to="/dashboard" className="mtm-flex mtm-items-center mtm-gap-2 mtm-no-underline">
      <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-10 mtm-w-10 mtm-rounded-xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-border-2 mtm-border-ink mtm-shadow-comic-sm">
        <FiZap size={20} />
      </span>
      <span className="mtm-font-comic mtm-text-2xl mtm-text-content mtm-tracking-wide">MTM</span>
    </Link>
  );
}

function Sidebar() {
  const [open, setOpen] = useState(false);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    setOpen(false);
    await logoutUser();
    navigate("/login", { replace: true });
  };

  const Footer = (
    <div className="mtm-pt-3 mtm-mt-3 mtm-border-t-2 mtm-border-ink/15">
      <InstallButton className="ui-btn ui-btn-soft ui-btn-sm mtm-w-full mtm-mb-2" />
      <div className="mtm-flex mtm-items-center mtm-justify-between mtm-gap-2">
        <ThemeToggle />
        <button onClick={handleLogout} className="ui-btn ui-btn-danger ui-btn-sm">
          <FiLogOut size={14} /> Logout
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <aside className="mtm-hidden lg:mtm-flex mtm-flex-col mtm-fixed mtm-inset-y-0 mtm-left-0 mtm-w-64 mtm-p-4 mtm-bg-surface mtm-border-r-[3px] mtm-border-ink mtm-z-40">
        <div className="mtm-mb-6">
          <Brand />
        </div>
        <NavItems isAdmin={isAdmin} />
        <div className="mtm-mt-auto">{Footer}</div>
      </aside>

      {/* Mobile top bar */}
      <div className="lg:mtm-hidden mtm-sticky mtm-top-0 mtm-z-40 mtm-flex mtm-items-center mtm-justify-between mtm-px-4 mtm-h-16 mtm-bg-surface mtm-border-b-[3px] mtm-border-ink">
        <Brand />
        <div className="mtm-flex mtm-items-center mtm-gap-2">
          <ThemeToggle />
          <button
            onClick={() => setOpen(true)}
            aria-label="Open menu"
            className="mtm-inline-flex mtm-h-10 mtm-w-10 mtm-items-center mtm-justify-center mtm-rounded-xl mtm-border-2 mtm-border-ink mtm-bg-surface-2 mtm-shadow-comic-sm"
          >
            <FiMenu size={20} />
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      <AnimatePresence>
        {open && (
          <>
            <motion.div
              className="mtm-fixed mtm-inset-0 mtm-bg-ink/50 mtm-z-50 lg:mtm-hidden"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setOpen(false)}
            />
            <motion.aside
              className="mtm-fixed mtm-inset-y-0 mtm-left-0 mtm-w-72 mtm-p-4 mtm-bg-surface mtm-border-r-[3px] mtm-border-ink mtm-z-50 mtm-flex mtm-flex-col lg:mtm-hidden"
              initial={{ x: "-100%" }}
              animate={{ x: 0 }}
              exit={{ x: "-100%" }}
              transition={{ type: "spring", stiffness: 320, damping: 32 }}
            >
              <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mb-6">
                <Brand />
                <button
                  onClick={() => setOpen(false)}
                  aria-label="Close menu"
                  className="mtm-inline-flex mtm-h-9 mtm-w-9 mtm-items-center mtm-justify-center mtm-rounded-lg mtm-border-2 mtm-border-ink mtm-bg-surface-2"
                >
                  <FiX size={18} />
                </button>
              </div>
              <NavItems isAdmin={isAdmin} onNavigate={() => setOpen(false)} />
              <div className="mtm-mt-auto">{Footer}</div>
            </motion.aside>
          </>
        )}
      </AnimatePresence>
    </>
  );
}

export default Sidebar;
