import React, { createContext, useCallback, useContext, useRef, useState } from "react";
import { FiAlertTriangle } from "react-icons/fi";
import Modal from "./Modal";

const ConfirmContext = createContext(() => Promise.resolve(false));

export const useConfirm = () => useContext(ConfirmContext);

/**
 * Provides an async confirm() that renders a real modal instead of the native
 * window.confirm(). Usage: `if (await confirm({ title, message })) { ... }`.
 */
export function ConfirmProvider({ children }) {
  const [state, setState] = useState(null);
  const resolver = useRef(null);

  const confirm = useCallback(
    (opts = {}) => {
      setState({
        title: opts.title || "Are you sure?",
        message: opts.message || "",
        confirmLabel: opts.confirmLabel || "Confirm",
        cancelLabel: opts.cancelLabel || "Cancel",
        danger: opts.danger !== false,
      });
      return new Promise((resolve) => {
        resolver.current = resolve;
      });
    },
    []
  );

  const close = (result) => {
    setState(null);
    if (resolver.current) {
      resolver.current(result);
      resolver.current = null;
    }
  };

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      <Modal
        open={!!state}
        onClose={() => close(false)}
        title={state ? state.title : ""}
        icon={<FiAlertTriangle />}
        maxWidth="mtm-max-w-md"
        footer={
          <>
            <button className="ui-btn ui-btn-ghost" onClick={() => close(false)}>
              {state ? state.cancelLabel : "Cancel"}
            </button>
            <button
              className={`ui-btn ${state && state.danger ? "ui-btn-danger" : "ui-btn-primary"}`}
              onClick={() => close(true)}
            >
              {state ? state.confirmLabel : "Confirm"}
            </button>
          </>
        }
      >
        <p className="mtm-text-content mtm-font-medium mtm-m-0">{state ? state.message : ""}</p>
      </Modal>
    </ConfirmContext.Provider>
  );
}
