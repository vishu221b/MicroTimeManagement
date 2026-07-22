import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { FiClock, FiLogIn } from "react-icons/fi";
import { isAuthenticated, loginUser } from "../service/ApiService";

function Login({ setToastState }) {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTarget =
    (location.state && location.state.from && location.state.from.pathname) ||
    "/dashboard";

  const [form, setForm] = useState({ username: "", password: "" });
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setToastState({ display: false });
    setSubmitting(true);

    await loginUser(
      { username: form.username, password: form.password },
      (response, errorResponse) => {
        setSubmitting(false);
        if (response && isAuthenticated()) {
          setToastState({
            display: true,
            variant: "success",
            messages: ["Logged in successfully."],
            includePrefix: true,
            includeSuffix: true,
            suffix: "Redirecting...",
          });
          navigate(redirectTarget, { replace: true });
          return;
        }
        if (errorResponse && errorResponse.error) {
          const finalErrors = [errorResponse.error.message];
          if (errorResponse.error.errors?.length > 0) {
            errorResponse.error.errors.forEach((err) => finalErrors.push(err));
          }
          setToastState({
            display: true,
            variant: "error",
            messages: finalErrors,
            includePrefix: true,
          });
        }
      }
    );
  };

  return (
    <div className="mtm-flex-1 mtm-flex mtm-items-center mtm-justify-center mtm-px-4 mtm-py-12">
      <div className="ui-card ui-fade-in mtm-w-full mtm-max-w-md mtm-p-8">
        <div className="mtm-flex mtm-flex-col mtm-items-center mtm-text-center mtm-mb-7">
          <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-12 mtm-w-12 mtm-rounded-2xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-text-white mtm-mb-4">
            <FiClock size={24} />
          </span>
          <h1 className="mtm-text-2xl mtm-font-display mtm-font-bold mtm-text-content mtm-m-0">
            Welcome back
          </h1>
          <p className="mtm-text-sm ui-muted mtm-mt-1 mtm-mb-0">
            Sign in to continue tracking your time.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mtm-flex mtm-flex-col mtm-gap-4">
          <div>
            <label className="ui-label" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              required
              className="ui-input"
              placeholder="your username"
              value={form.username}
              onChange={onChange}
            />
          </div>
          <div>
            <label className="ui-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              className="ui-input"
              placeholder="••••••••"
              value={form.password}
              onChange={onChange}
            />
          </div>

          <button type="submit" className="ui-btn ui-btn-primary mtm-mt-2" disabled={submitting}>
            {submitting ? <span className="ui-spinner" /> : <FiLogIn size={18} />}
            {submitting ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <p className="mtm-text-sm ui-muted mtm-text-center mtm-mt-6 mtm-mb-0">
          New here?{" "}
          <Link to="/register" className="ui-link">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
