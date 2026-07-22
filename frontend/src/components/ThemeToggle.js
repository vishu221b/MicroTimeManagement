import React from "react";
import { FiMoon, FiSun } from "react-icons/fi";
import useTheme from "../hooks/useTheme";

function ThemeToggle({ className = "" }) {
  const { theme, toggle } = useTheme();
  const isDark = theme === "dark";
  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
      title={isDark ? "Light mode" : "Dark mode"}
      className={`mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-9 mtm-w-9 mtm-rounded-lg mtm-border mtm-border-line mtm-bg-surface-2 mtm-text-content hover:mtm-text-primary mtm-transition-colors ${className}`}
    >
      {isDark ? <FiSun size={18} /> : <FiMoon size={18} />}
    </button>
  );
}

export default ThemeToggle;
