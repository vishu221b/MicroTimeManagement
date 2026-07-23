import React, { useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { FiX } from "react-icons/fi";

/**
 * A comic-styled, animated modal. Closes on backdrop click + Escape.
 * `maxWidth` is a full (prefixed) Tailwind class, e.g. "mtm-max-w-2xl".
 */
function Modal({ open, onClose, title, icon, children, footer, maxWidth = "mtm-max-w-2xl" }) {
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open && (
        <div className="mtm-fixed mtm-inset-0 mtm-z-[80] mtm-flex mtm-items-center mtm-justify-center mtm-p-4">
          <motion.div
            className="mtm-absolute mtm-inset-0 mtm-bg-ink/60 mtm-backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div
            role="dialog"
            aria-modal="true"
            className={`ui-card mtm-relative mtm-w-full ${maxWidth} mtm-max-h-[88vh] mtm-flex mtm-flex-col mtm-p-0`}
            initial={{ opacity: 0, scale: 0.94, y: 18 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.96, y: 10 }}
            transition={{ type: "spring", stiffness: 320, damping: 26 }}
          >
            <div className="mtm-flex mtm-items-center mtm-justify-between mtm-gap-3 mtm-px-6 mtm-py-4 mtm-border-b-[3px] mtm-border-ink">
              <h2 className="mtm-font-comic mtm-text-2xl mtm-text-content mtm-m-0 mtm-flex mtm-items-center mtm-gap-2">
                {icon && <span className="mtm-text-accent">{icon}</span>}
                {title}
              </h2>
              <button onClick={onClose} aria-label="Close" className="ui-btn ui-btn-ghost ui-btn-sm">
                <FiX size={16} />
              </button>
            </div>
            <div className="mtm-overflow-auto mtm-px-6 mtm-py-5 mtm-flex-1">{children}</div>
            {footer && (
              <div className="mtm-px-6 mtm-py-4 mtm-border-t-[3px] mtm-border-ink mtm-flex mtm-justify-end mtm-gap-2">
                {footer}
              </div>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}

export default Modal;
