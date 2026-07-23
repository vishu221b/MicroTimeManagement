import { useEffect, useState } from "react";
import { isAuthenticated, subscribeAuth } from "../service/AuthStorage";
import { getUserProfile } from "../service/ApiService";

const ADMIN_ROLE = "MTM_ADMIN_OPS";

export default function useAuth() {
  const [authed, setAuthed] = useState(isAuthenticated());
  const [roles, setRoles] = useState([]);
  const [profileLoaded, setProfileLoaded] = useState(false);

  useEffect(() => {
    const update = () => setAuthed(isAuthenticated());
    const unsubscribe = subscribeAuth(update);
    update();
    return unsubscribe;
  }, []);

  useEffect(() => {
    let cancelled = false;
    if (!authed) {
      setRoles([]);
      setProfileLoaded(false);
      return;
    }
    setProfileLoaded(false);
    getUserProfile((data, err) => {
      if (cancelled) return;
      setProfileLoaded(true);
      if (err) {
        setRoles([]);
        return;
      }
      // /user/profile returns GenericMessageResponseDTO → { payload, message }.
      const payload = (data && data.payload) || data || {};
      const incomingRoles = Array.isArray(payload.roles)
        ? payload.roles
        : payload.roles
        ? Array.from(payload.roles)
        : [];
      setRoles(incomingRoles);
    });
    return () => {
      cancelled = true;
    };
  }, [authed]);

  const isAdmin = roles.some(
    (r) => r === ADMIN_ROLE || r === `ROLE_${ADMIN_ROLE}`
  );

  return { isAuthenticated: authed, roles, isAdmin, profileLoaded };
}
