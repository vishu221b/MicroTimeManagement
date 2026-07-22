import { useEffect, useRef } from "react";
import { isAuthenticated } from "../service/AuthStorage";
import { listReminders } from "../service/ApiService";

const FIRED_KEY = "mtm-fired-reminders";

const loadFired = () => {
  try {
    return new Set(JSON.parse(localStorage.getItem(FIRED_KEY) || "[]"));
  } catch (e) {
    return new Set();
  }
};

const saveFired = (set) => {
  try {
    localStorage.setItem(FIRED_KEY, JSON.stringify([...set]));
  } catch (e) {
    /* storage unavailable — non-fatal */
  }
};

/**
 * While the app is open and the user is authenticated, polls their reminders
 * and fires a Web Notification (and an optional in-app callback) for any
 * PENDING reminder whose time has passed. Fired ids are remembered in
 * localStorage so a reminder never double-notifies across polls or reloads.
 */
export default function useReminderNotifications({ onFire } = {}) {
  const firedRef = useRef(loadFired());

  useEffect(() => {
    if (!isAuthenticated()) return undefined;

    if ("Notification" in window && Notification.permission === "default") {
      Notification.requestPermission().catch(() => {});
    }

    let cancelled = false;
    const poll = () => {
      if (!isAuthenticated()) return;
      listReminders((data, err) => {
        if (cancelled || err || !Array.isArray(data)) return;
        const now = Date.now();
        data.forEach((r) => {
          const due = r.status === "PENDING" && r.remindAt && r.remindAt <= now;
          if (due && !firedRef.current.has(r.id)) {
            firedRef.current.add(r.id);
            saveFired(firedRef.current);
            if ("Notification" in window && Notification.permission === "granted") {
              // eslint-disable-next-line no-new
              new Notification(`Reminder: ${r.title}`, { body: r.notes || "" });
            }
            if (onFire) onFire(r);
          }
        });
      });
    };

    poll();
    const id = setInterval(poll, 30000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [onFire]);
}
