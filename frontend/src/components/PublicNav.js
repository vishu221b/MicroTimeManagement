import React, { useState } from "react";
import { Link } from "react-router-dom";
import { FiZap, FiMenu, FiX } from "react-icons/fi";
import useAuth from "../hooks/useAuth";
import ThemeToggle from "./ThemeToggle";
import InstallButton from "./InstallButton";

function PublicNav() {
  const { isAuthenticated } = useAuth();
  const [open, setOpen] = useState(false);

  const anchors = [
    { href: "/#features", label: "Features" },
    { href: "/#pricing", label: "Pricing" },
    { href: "/#how", label: "How it works" },
  ];

  return (
    <header className="mtm-sticky mtm-top-0 mtm-z-50 mtm-bg-surface/90 mtm-backdrop-blur mtm-border-b-[3px] mtm-border-ink">
      <nav className="mtm-max-w-6xl mtm-mx-auto mtm-px-4 sm:mtm-px-6 mtm-h-16 mtm-flex mtm-items-center mtm-justify-between">
        <Link to="/" className="mtm-flex mtm-items-center mtm-gap-2 mtm-no-underline">
          <span className="mtm-inline-flex mtm-h-9 mtm-w-9 mtm-items-center mtm-justify-center mtm-rounded-xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-border-2 mtm-border-ink mtm-shadow-comic-sm">
            <FiZap size={18} />
          </span>
          <span className="mtm-font-comic mtm-text-2xl mtm-text-content mtm-tracking-wide">MTM</span>
        </Link>

        <div className="mtm-hidden md:mtm-flex mtm-items-center mtm-gap-6">
          {anchors.map((a) => (
            <a key={a.href} href={a.href} className="mtm-font-bold mtm-text-content hover:mtm-text-primary mtm-transition-colors">
              {a.label}
            </a>
          ))}
        </div>

        <div className="mtm-hidden md:mtm-flex mtm-items-center mtm-gap-2">
          <InstallButton className="ui-btn ui-btn-ghost ui-btn-sm" label="Install" />
          <ThemeToggle />
          {isAuthenticated ? (
            <Link to="/dashboard" className="ui-btn ui-btn-primary ui-btn-sm">Open app</Link>
          ) : (
            <>
              <Link to="/login" className="ui-btn ui-btn-ghost ui-btn-sm">Sign in</Link>
              <Link to="/register" className="ui-btn ui-btn-primary ui-btn-sm">Try free</Link>
            </>
          )}
        </div>

        <div className="mtm-flex md:mtm-hidden mtm-items-center mtm-gap-2">
          <ThemeToggle />
          <button onClick={() => setOpen((o) => !o)} aria-label="Menu"
            className="mtm-inline-flex mtm-h-10 mtm-w-10 mtm-items-center mtm-justify-center mtm-rounded-xl mtm-border-2 mtm-border-ink mtm-bg-surface-2 mtm-shadow-comic-sm">
            {open ? <FiX size={20} /> : <FiMenu size={20} />}
          </button>
        </div>
      </nav>

      {open && (
        <div className="md:mtm-hidden mtm-border-t-2 mtm-border-ink mtm-bg-surface mtm-px-4 mtm-py-3 mtm-flex mtm-flex-col mtm-gap-2">
          {anchors.map((a) => (
            <a key={a.href} href={a.href} onClick={() => setOpen(false)}
              className="mtm-font-bold mtm-text-content mtm-py-1">{a.label}</a>
          ))}
          {isAuthenticated ? (
            <Link to="/dashboard" className="ui-btn ui-btn-primary" onClick={() => setOpen(false)}>Open app</Link>
          ) : (
            <>
              <Link to="/login" className="ui-btn ui-btn-ghost" onClick={() => setOpen(false)}>Sign in</Link>
              <Link to="/register" className="ui-btn ui-btn-primary" onClick={() => setOpen(false)}>Try free</Link>
            </>
          )}
        </div>
      )}
    </header>
  );
}

export default PublicNav;
