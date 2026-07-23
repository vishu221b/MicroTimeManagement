import React from "react";
import { FiDownload } from "react-icons/fi";
import usePwaInstall from "../hooks/usePwaInstall";

/**
 * "Install app" button — self-hides on browsers where the app is already
 * installed or programmatic install isn't available.
 */
function InstallButton({ className = "ui-btn ui-btn-soft ui-btn-sm", label = "Install app" }) {
  const { canInstall, promptInstall } = usePwaInstall();
  if (!canInstall) return null;
  return (
    <button className={className} onClick={promptInstall} title="Install MTM as an app">
      <FiDownload size={14} /> {label}
    </button>
  );
}

export default InstallButton;
