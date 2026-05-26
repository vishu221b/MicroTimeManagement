import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Button from "react-bootstrap/Button";
import { getActivityStats } from "../service/ApiService";

// Three quick presets the dashboard offers out of the box. Custom ranges can
// be added later — keeping this small for now so the UI stays calm.
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

function StatCard({ label, value, hint }) {
  return (
    <div className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-5">
      <div className="mtm-text-xs mtm-uppercase mtm-tracking-widest mtm-text-white/50">
        {label}
      </div>
      <div className="mtm-text-3xl mtm-text-yellow-300 mtm-mt-1 mtm-tracking-wider">
        {value}
      </div>
      {hint && (
        <div className="mtm-text-xs mtm-text-white/40 mtm-mt-1">{hint}</div>
      )}
    </div>
  );
}

function Dashboard({ toastState, setToastState }) {
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
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-5xl mtm-mx-auto">
        <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
          <div>
            <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400">
              Welcome back
            </h1>
            <p className="mtm-text-white/60 mtm-mt-2">
              Here's how your time has been spent.
            </p>
          </div>
          <Link to="/activity">
            <Button variant="warning">
              <span className="mtm-tracking-widest">Track Activities</span>
            </Button>
          </Link>
        </div>

        <div className="mtm-flex mtm-gap-2 mtm-mb-6">
          {RANGES.map((r) => (
            <Button
              key={r.id}
              size="sm"
              variant={range === r.id ? "warning" : "outline-light"}
              onClick={() => setRange(r.id)}
            >
              {r.label}
            </Button>
          ))}
        </div>

        {loading && (
          <p className="mtm-text-white/60">Loading your activity summary…</p>
        )}

        {!loading && stats && (
          <>
            <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2 lg:mtm-grid-cols-4 mtm-mb-8">
              <StatCard
                label="Total time"
                value={stats.totalDurationHuman || "0m"}
                hint={`${safeNumber(stats.totalMinutes)} min total`}
              />
              <StatCard
                label="Activities"
                value={safeNumber(stats.totalActivities)}
                hint="Entries logged"
              />
              <StatCard
                label="Days tracked"
                value={safeNumber(stats.daysWithActivity)}
                hint={`${params.from} → ${params.to}`}
              />
              <StatCard
                label="Avg / active day"
                value={`${safeNumber(stats.averageMinutesPerActiveDay)}m`}
                hint="Minutes per day with activity"
              />
            </div>

            <div className="mtm-grid mtm-gap-6 lg:mtm-grid-cols-2">
              <section className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-5">
                <h2 className="mtm-text-lg mtm-text-yellow-300 mtm-mb-4">
                  Top activities
                </h2>
                {(stats.topActivitiesByDuration || []).length === 0 ? (
                  <p className="mtm-text-white/50">
                    Nothing logged in this window yet.
                  </p>
                ) : (
                  <ul className="mtm-space-y-3">
                    {stats.topActivitiesByDuration.map((item) => (
                      <li
                        key={item.activityName}
                        className="mtm-flex mtm-justify-between mtm-items-center"
                      >
                        <div>
                          <div className="mtm-text-white">
                            {item.activityName}
                          </div>
                          <div className="mtm-text-xs mtm-text-white/50">
                            {item.occurrenceCount}{" "}
                            {item.occurrenceCount === 1 ? "entry" : "entries"}
                          </div>
                        </div>
                        <div className="mtm-text-yellow-300 mtm-tracking-wider">
                          {item.totalDurationHuman}
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </section>

              <section className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-5">
                <h2 className="mtm-text-lg mtm-text-yellow-300 mtm-mb-4">
                  Recent activities
                </h2>
                {(stats.recentActivities || []).length === 0 ? (
                  <p className="mtm-text-white/50">
                    No recent activities to show.
                  </p>
                ) : (
                  <ul className="mtm-space-y-3">
                    {stats.recentActivities.map((a) => (
                      <li key={a.id} className="mtm-text-sm">
                        <div className="mtm-text-white">{a.activityName}</div>
                        <div className="mtm-text-white/50 mtm-text-xs">
                          {a.activityDate} · {a.activityStartTime} →{" "}
                          {a.activityEndTime} · {a.activityTotalDuration}
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
          <p className="mtm-text-white/60">
            We couldn't load your activity summary. Try again in a moment.
          </p>
        )}
      </div>
    </div>
  );
}

export default Dashboard;
