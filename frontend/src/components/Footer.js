import React from "react";
import { FiClock } from "react-icons/fi";

function Footer() {
  return (
    <footer className="mtm-mt-auto mtm-border-t mtm-border-line mtm-bg-surface/60">
      <div className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-py-8 mtm-flex mtm-flex-col sm:mtm-flex-row mtm-items-center mtm-justify-between mtm-gap-3">
        <div className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-content">
          <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-7 mtm-w-7 mtm-rounded-lg mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white">
            <FiClock size={14} />
          </span>
          <span className="mtm-font-display mtm-font-bold">Micro Time Management</span>
        </div>
        <p className="mtm-text-sm mtm-text-muted mtm-m-0">
          &copy; {new Date().getFullYear()} MTM · Track your time, one activity at a time.
        </p>
      </div>
    </footer>
  );
}

export default Footer;
