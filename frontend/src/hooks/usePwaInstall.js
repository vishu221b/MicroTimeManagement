import { useEffect, useState } from "react";

/**
 * Captures the browser's `beforeinstallprompt` event so the app can offer a
 * custom "Install app" button. `canInstall` is only true on browsers that
 * support programmatic install (Chrome/Edge/Android) and when not already
 * installed. iOS Safari installs via Share → Add to Home Screen (no event).
 */
export default function usePwaInstall() {
  const [deferred, setDeferred] = useState(null);
  const [installed, setInstalled] = useState(false);

  useEffect(() => {
    const onPrompt = (e) => {
      e.preventDefault();
      setDeferred(e);
    };
    const onInstalled = () => {
      setInstalled(true);
      setDeferred(null);
    };
    window.addEventListener("beforeinstallprompt", onPrompt);
    window.addEventListener("appinstalled", onInstalled);
    if (window.matchMedia && window.matchMedia("(display-mode: standalone)").matches) {
      setInstalled(true);
    }
    return () => {
      window.removeEventListener("beforeinstallprompt", onPrompt);
      window.removeEventListener("appinstalled", onInstalled);
    };
  }, []);

  const promptInstall = async () => {
    if (!deferred) return;
    deferred.prompt();
    try {
      await deferred.userChoice;
    } catch (e) {
      /* user dismissed — non-fatal */
    }
    setDeferred(null);
  };

  return { canInstall: !!deferred && !installed, installed, promptInstall };
}
