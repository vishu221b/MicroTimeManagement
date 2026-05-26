import { useEffect, useState } from "react";
import { isAuthenticated, subscribeAuth } from "../service/AuthStorage";

export default function useAuth() {
  const [authed, setAuthed] = useState(isAuthenticated());

  useEffect(() => {
    const update = () => setAuthed(isAuthenticated());
    const unsubscribe = subscribeAuth(update);
    update();
    return unsubscribe;
  }, []);

  return { isAuthenticated: authed };
}
