import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  FiArrowRight,
  FiClock,
  FiList,
  FiCalendar,
  FiTrendingUp,
} from "react-icons/fi";
import { getActivityStats } from "../service/ApiService";
import { VerticalBars, HorizontalBars } from "../components/charts/Charts";

// Three quick presets the dashboard offers out of the box.
const RANGES = [
  { id: "today", label: "Today" },
  { id: "7d", label: "Last 7 days" },
  { id: "30d", label: "Last 30 days" },
];

const isoDate = (date) => {
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`);
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}`;
};

const rangeToParams = (rangeId) => {
  const today = new Date();
  if (rangeId === "today") {
    const d = isoDate(today);
    return { from: d, to: d };
  }
  const days = rangeId === "30d" ? 30 : 7;
  const start = new Date(today);
  start.setDate(today.getDate() - (days - 1));
  return { from: isoDate(start), to: isoDate(today) };
};

const errorMessageFrom = (errorPayload) => {
  if (!errorPayload) return "Something went wrong.";
  if (errorPayload.error && errorPayload.error.message)
    return errorPayload.error.message;
  if (errorPayload.message) return errorPayload.message;
  return "Something went wrong.";
};

function StatCard({ icon, label, value, hint }) {
  return (
    <div className="ui-stat">
      <div className="mtm-flex mtm-items-center mtm-justify-between">
        <span className="ui-stat-label">{label}</span>
        <span className="ui-icon-tile mtm-h-9 mtm-w-9">{icon}</span>
      </div>
      <div className="ui-stat-value mtm-text-content mtm-mt-3">{value}</div>
      {hint && <div className="mtm-text-xs ui-muted mtm-mt-1">{hint}</div>}
    </div>
  );
}

function Dashboard({ setToastState }) {
  const [range, setRange] = useState("7d");
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);

  const params = useMemo(() => rangeToParams(range), [range]);

  const showError = useCallback(
    (msg) =>
      setToastState({
        display: true,
        variant: "error",
        messages: [msg],
        includePrefix: true,
        includeSuffix: false,
        suffix: "",
      }),
    [setToastState]
  );

  const load = useCallback(() => {
    setLoading(true);
    getActivityStats(params, (data, err) => {
      setLoading(false);
      if (err) {
        showError(errorMessageFrom(err));
        setStats(null);
        return;
      }
      setStats(data || null);
    });
  }, [params, showError]);

  useEffect(() => {
    load();
  }, [load]);

  const safeNumber = (n) => (typeof n === "number" ? n : 0);

  return (
    <div className="ui-page ui-fade-in">
      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
        <div>
          <p className="ui-eyebrow">Dashboard</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1">
            Welcome back
          </h1>
          <p className="ui-muted mtm-mt-1 mtm-mb-0">
            Here's how your time has been spent.
          </p>
        </div>
        <Link to="/activity" className="ui-btn ui-btn-primary">
          Track activities <FiArrowRight size={18} />
        </Link>
      </div>

      <div className="mtm-inline-flex mtm-p-1 mtm-rounded-xl mtm-bg-surface-2 mtm-border mtm-border-line mtm-mb-7">
        {RANGES.map((r) => (
          <button
            key={r.id}
            onClick={() => setRange(r.id)}
            className={`mtm-px-4 mtm-py-1.5 mtm-rounded-lg mtm-text-sm mtm-font-semibold mtm-transition-colors ${
              range === r.id
                ? "mtm-bg-surface mtm-text-primary mtm-shadow-sm"
                : "mtm-text-muted hover:mtm-text-content"
            }`}
          >
            {r.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          Loading your activity summary…
        </div>
      )}

      {!loading && stats && (
        <>
          <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2 lg:mtm-grid-cols-4 mtm-mb-6">
            <StatCard
              icon={<FiClock size={16} />}
              label="Total time"
              value={stats.totalDurationHuman || "0m"}
              hint={`${safeNumber(stats.totalMinutes)} min total`}
            />
            <StatCard
              icon={<FiList size={16} />}
              label="Activities"
              value={safeNumber(stats.totalActivities)}
              hint="Entries logged"
            />
            <StatCard
              icon={<FiCalendar size={16} />}
              label="Days tracked"
              value={safeNumber(stats.daysWithActivity)}
              hint={`${params.from} → ${params.to}`}
            />
            <StatCard
              icon={<FiTrendingUp size={16} />}
              label="Avg / active day"
              value={`${safeNumber(stats.averageMinutesPerActiveDay)}m`}
              hint="Minutes per active day"
            />
          </div>

          {/* Time-per-day chart (full width) */}
          <section className="ui-card mtm-p-6 mtm-mb-5">
            <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-mb-1">
              Time per day
            </h2>
            <p className="ui-muted mtm-text-sm mtm-mb-5">
              {params.from} → {params.to}
            </p>
            {(stats.dailyBreakdown || []).length === 0 ? (
              <p className="ui-muted mtm-m-0">No data for this window yet.</p>
            ) : (
              <VerticalBars data={stats.dailyBreakdown} />
            )}
          </section>

          <div className="mtm-grid mtm-gap-5 lg:mtm-grid-cols-2">
            <section className="ui-card mtm-p-6">
              <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-mb-4">
                Top activities
              </h2>
              {(stats.topActivitiesByDuration || []).length === 0 ? (
                <p className="ui-muted mtm-m-0">
                  Nothing logged in this window yet.
                </p>
              ) : (
                <HorizontalBars data={stats.topActivitiesByDuration} />
              )}
            </section>

            <section className="ui-card mtm-p-6">
              <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-mb-4">
                Recent activities
              </h2>
              {(stats.recentActivities || []).length === 0 ? (
                <p className="ui-muted mtm-m-0">No recent activities to show.</p>
              ) : (
                <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
                  {stats.recentActivities.map((a) => (
                    <li
                      key={a.id}
                      className="mtm-flex mtm-items-start mtm-gap-3 mtm-border-b mtm-border-line last:mtm-border-0 mtm-pb-3 last:mtm-pb-0"
                    >
                      <span className="ui-icon-tile mtm-h-9 mtm-w-9 mtm-mt-0.5 mtm-shrink-0">
                        <FiClock size={15} />
                      </span>
                      <div className="mtm-min-w-0">
                        <div className="mtm-text-content mtm-font-medium mtm-truncate">
                          {a.activityName}
                        </div>
                        <div className="ui-muted mtm-text-xs">
                          {a.activityDate} · {a.activityStartTime} →{" "}
                          {a.activityEndTime} · {a.activityTotalDuration}
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        </>
      )}

      {!loading && !stats && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          We couldn't load your activity summary. Try again in a moment.
        </div>
      )}
    </div>
  );
}

export default Dashboard;
