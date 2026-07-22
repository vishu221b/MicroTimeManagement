import React, { useEffect, useState } from "react";
import { FiCheckCircle, FiAlertCircle, FiX } from "react-icons/fi";

function Toast({
  variant = "success",
  includePrefix,
  includeSuffix,
  suffix,
  show,
  autoHide,
  autoHideDelayInMs = 5000,
  children,
}) {
  const [visible, setVisible] = useState(false);

  // slide in on mount
  useEffect(() => {
    const id = requestAnimationFrame(() => setVisible(Boolean(show)));
    return () => cancelAnimationFrame(id);
  }, [show]);

  useEffect(() => {
    if (!autoHide) return undefined;
    const id = setTimeout(() => setVisible(false), autoHideDelayInMs);
    return () => clearTimeout(id);
  }, [autoHide, autoHideDelayInMs]);

  const isSuccess = variant === "success";
  const prefix = isSuccess ? "Success: " : "Error: ";

  const accent = isSuccess
    ? "mtm-text-ok mtm-border-ok/40"
    : "mtm-text-danger mtm-border-danger/40";

  return (
    <div
      className={`ui-card mtm-pointer-events-auto mtm-mt-3 mtm-w-full mtm-border ${accent} mtm-transition-all mtm-duration-300 ${
        visible
          ? "mtm-opacity-100 mtm-translate-x-0"
          : "mtm-opacity-0 mtm-translate-x-6"
      }`}
    >
      <div className="mtm-flex mtm-items-start mtm-gap-3 mtm-p-3.5">
        <span className={`mtm-mt-0.5 ${isSuccess ? "mtm-text-ok" : "mtm-text-danger"}`}>
          {isSuccess ? <FiCheckCircle size={20} /> : <FiAlertCircle size={20} />}
        </span>
        <div className="mtm-flex-1 mtm-text-sm mtm-text-content mtm-break-words">
          {includePrefix && <span className="mtm-font-semibold">{prefix}</span>}
          {children}
          {includeSuffix && suffix ? (
            <>
              <br />
              <span className="mtm-text-muted">{suffix}</span>
            </>
          ) : null}
        </div>
        <button
          type="button"
          onClick={() => setVisible(false)}
          aria-label="Dismiss"
          className="mtm-text-muted hover:mtm-text-content mtm-transition-colors"
        >
          <FiX size={16} />
        </button>
      </div>
    </div>
  );
}

export default Toast;
