import React from "react";
import { Link } from "react-router-dom";
import {
  FiArrowRight,
  FiClock,
  FiBarChart2,
  FiCalendar,
  FiZap,
  FiShield,
  FiLayers,
} from "react-icons/fi";
import useAuth from "../hooks/useAuth";

const FEATURES = [
  {
    icon: <FiClock size={20} />,
    title: "Log activities fast",
    text: "Capture time blocks with a name, note, and start/end — overlap-checked and kept in order automatically.",
  },
  {
    icon: <FiBarChart2 size={20} />,
    title: "See where time goes",
    text: "A live dashboard breaks down totals, top activities, and averages across today, 7 days, and 30 days.",
  },
  {
    icon: <FiCalendar size={20} />,
    title: "Full history",
    text: "Every tracked day, newest first, one click from the detailed view. Nothing gets lost.",
  },
  {
    icon: <FiZap size={20} />,
    title: "Smart autocomplete",
    text: "Reuse the names you already track. Recent activities surface as you type.",
  },
  {
    icon: <FiShield size={20} />,
    title: "Secure by default",
    text: "JWT sessions with single-session enforcement and fine-grained role-based access control.",
  },
  {
    icon: <FiLayers size={20} />,
    title: "Built for teams",
    text: "Admins manage roles and user membership from a dedicated, gated panel.",
  },
];

const STEPS = [
  { n: "01", title: "Create an account", text: "Register in seconds — no setup, no credit card." },
  { n: "02", title: "Log your day", text: "Add activities as you go with start and end times." },
  { n: "03", title: "Review & improve", text: "Watch the dashboard reveal your real patterns." },
];

function Home() {
  const { isAuthenticated } = useAuth();
  const primaryTo = isAuthenticated ? "/dashboard" : "/register";
  const primaryLabel = isAuthenticated ? "Go to dashboard" : "Get started free";

  return (
    <div className="ui-fade-in">
      {/* Hero */}
      <section className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-pt-16 mtm-pb-12 sm:mtm-pt-24 sm:mtm-pb-16 mtm-text-center">
        <span className="ui-badge mtm-mb-5">
          <FiZap size={13} /> Time tracking, minus the friction
        </span>
        <h1 className="mtm-font-display mtm-font-extrabold mtm-text-content mtm-text-4xl sm:mtm-text-6xl mtm-leading-tight mtm-max-w-3xl mtm-mx-auto">
          Track your time,{" "}
          <span className="mtm-bg-gradient-to-r mtm-from-primary mtm-to-accent mtm-bg-clip-text mtm-text-transparent">
            one activity at a time
          </span>
        </h1>
        <p className="ui-muted mtm-text-lg mtm-mt-6 mtm-max-w-2xl mtm-mx-auto">
          Micro Time Management turns your day into clear, actionable insight. Log
          what you do, and let the dashboard show you where your hours actually go.
        </p>
        <div className="mtm-flex mtm-flex-wrap mtm-items-center mtm-justify-center mtm-gap-3 mtm-mt-9">
          <Link to={primaryTo} className="ui-btn ui-btn-primary">
            {primaryLabel} <FiArrowRight size={18} />
          </Link>
          {!isAuthenticated && (
            <Link to="/login" className="ui-btn ui-btn-ghost">
              Sign in
            </Link>
          )}
        </div>
      </section>

      {/* Preview stat strip */}
      <section className="mtm-max-w-5xl mtm-mx-auto mtm-px-6 mtm-mb-6">
        <div className="ui-card mtm-p-6 mtm-grid mtm-grid-cols-2 md:mtm-grid-cols-4 mtm-gap-6">
          {[
            { v: "4h 20m", l: "Tracked today" },
            { v: "12", l: "Activities" },
            { v: "5", l: "Days this week" },
            { v: "52m", l: "Avg / active day" },
          ].map((s) => (
            <div key={s.l} className="mtm-text-center">
              <div className="ui-stat-value mtm-text-content">{s.v}</div>
              <div className="ui-stat-label mtm-mt-1">{s.l}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features */}
      <section className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-py-16">
        <div className="mtm-text-center mtm-mb-12">
          <p className="ui-eyebrow">Everything you need</p>
          <h2 className="mtm-font-display mtm-font-bold mtm-text-content mtm-text-3xl mtm-mt-2">
            Simple to log. Powerful to review.
          </h2>
        </div>
        <div className="mtm-grid mtm-grid-cols-1 sm:mtm-grid-cols-2 lg:mtm-grid-cols-3 mtm-gap-5">
          {FEATURES.map((f) => (
            <div key={f.title} className="ui-card mtm-p-6">
              <span className="ui-icon-tile mtm-mb-4">{f.icon}</span>
              <h3 className="mtm-font-semibold mtm-text-content mtm-text-lg mtm-mb-1.5">
                {f.title}
              </h3>
              <p className="ui-muted mtm-text-sm mtm-m-0">{f.text}</p>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="mtm-max-w-6xl mtm-mx-auto mtm-px-6 mtm-pb-16">
        <div className="mtm-text-center mtm-mb-12">
          <p className="ui-eyebrow">How it works</p>
          <h2 className="mtm-font-display mtm-font-bold mtm-text-content mtm-text-3xl mtm-mt-2">
            Three steps to clarity
          </h2>
        </div>
        <div className="mtm-grid mtm-grid-cols-1 md:mtm-grid-cols-3 mtm-gap-5">
          {STEPS.map((s) => (
            <div key={s.n} className="ui-card mtm-p-6">
              <div className="mtm-font-display mtm-font-extrabold mtm-text-4xl mtm-bg-gradient-to-br mtm-from-primary mtm-to-accent mtm-bg-clip-text mtm-text-transparent">
                {s.n}
              </div>
              <h3 className="mtm-font-semibold mtm-text-content mtm-text-lg mtm-mt-3 mtm-mb-1.5">
                {s.title}
              </h3>
              <p className="ui-muted mtm-text-sm mtm-m-0">{s.text}</p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="mtm-max-w-5xl mtm-mx-auto mtm-px-6 mtm-pb-20">
        <div className="ui-card mtm-p-10 mtm-text-center mtm-relative mtm-overflow-hidden">
          <div className="mtm-relative">
            <h2 className="mtm-font-display mtm-font-bold mtm-text-content mtm-text-3xl">
              Ready to reclaim your hours?
            </h2>
            <p className="ui-muted mtm-mt-3 mtm-mb-7">
              Join now and see your first insights today.
            </p>
            <Link to={primaryTo} className="ui-btn ui-btn-primary">
              {primaryLabel} <FiArrowRight size={18} />
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;
