import React, { useCallback, useEffect, useMemo, useState } from "react";
import { FiClock, FiCheckSquare, FiBell } from "react-icons/fi";
import { listTasks, listReminders, getActivityStats } from "../service/ApiService";

const DAY = 86400000;

const errorMessageFrom = (e) =>
  (e && e.error && e.error.message) || (e && e.message) || "Something went wrong.";

const RANGES = [
  { key: 21, label: "3 weeks" },
  { key: 45, label: "45 days" },
  { key: 90, label: "3 months" },
];

const isoDate = (ms) => new Date(ms).toISOString().slice(0, 10);
const midnight = (ms) => {
  const d = new Date(ms);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
};
// Accepts epoch ms (number), ISO string, or yyyy-MM-dd — returns ms or null.
const toMs = (v) => {
  if (v == null) return null;
  if (typeof v === "number") return v;
  const s = String(v);
  const parsed = Date.parse(s.length === 10 ? `${s}T00:00:00` : s);
  return Number.isNaN(parsed) ? null : parsed;
};

const fmtDay = (ms) =>
  new Date(ms).toLocaleDateString(undefined, { month: "short", day: "numeric" });

function Gantt({ setToastState }) {
  const [span, setSpan] = useState(45);
  const [tasks, setTasks] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [daily, setDaily] = useState([]);
  const [loading, setLoading] = useState(false);
  const [tip, setTip] = useState(null); // { x, y, title, sub }

  const showError = (m) =>
    setToastState({ display: true, variant: "error", messages: [m], includePrefix: true });

  // Window: bias a quarter into the past so recent work + upcoming due dates both show.
  const { startMs, endMs } = useMemo(() => {
    const today = midnight(Date.now());
    const start = today - Math.floor(span * 0.25) * DAY;
    return { startMs: start, endMs: start + span * DAY };
  }, [span]);

  const totalMs = endMs - startMs;
  const pct = useCallback((ms) => ((ms - startMs) / totalMs) * 100, [startMs, totalMs]);

  const load = useCallback(() => {
    setLoading(true);
    let pending = 3;
    const done = () => {
      pending -= 1;
      if (pending === 0) setLoading(false);
    };
    listTasks({}, (data, err) => {
      if (err) showError(errorMessageFrom(err));
      else setTasks(Array.isArray(data) ? data : []);
      done();
    });
    listReminders((data, err) => {
      if (err) showError(errorMessageFrom(err));
      else setReminders(Array.isArray(data) ? data : []);
      done();
    });
    getActivityStats({ from: isoDate(startMs), to: isoDate(endMs) }, (data, err) => {
      if (!err && data) setDaily(Array.isArray(data.dailyBreakdown) ? data.dailyBreakdown : []);
      done();
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startMs, endMs]);

  useEffect(() => {
    load();
  }, [load]);

  // ---- derive rows ----
  const taskBars = useMemo(() => {
    return tasks
      .filter((t) => t.dueDate)
      .map((t) => {
        const due = toMs(t.dueDate);
        if (due == null) return null;
        const end = due + DAY; // inclusive of the due day
        const created = toMs(t.createdAt);
        const start = created != null ? Math.min(created, due) : due - DAY;
        return { ...t, _start: start, _end: end };
      })
      .filter((t) => t && t._end > startMs && t._start < endMs)
      .sort((a, b) => a._start - b._start);
  }, [tasks, startMs, endMs]);

  const reminderPoints = useMemo(() => {
    return reminders
      .map((r) => ({ ...r, _at: toMs(r.remindAt) }))
      .filter((r) => r._at != null && r._at >= startMs && r._at <= endMs)
      .sort((a, b) => a._at - b._at);
  }, [reminders, startMs, endMs]);

  const maxMinutes = useMemo(
    () => daily.reduce((m, d) => Math.max(m, d.totalMinutes || 0), 0),
    [daily]
  );

  // Weekly axis ticks.
  const ticks = useMemo(() => {
    const out = [];
    // Start from the first midnight >= startMs, step 7 days.
    let t = midnight(startMs);
    while (t <= endMs) {
      out.push(t);
      t += 7 * DAY;
    }
    return out;
  }, [startMs, endMs]);

  const todayMs = midnight(Date.now());
  const todayInWindow = todayMs >= startMs && todayMs <= endMs;

  const onEnter = (e, title, sub) =>
    setTip({ x: e.clientX, y: e.clientY, title, sub });
  const onMove = (e) => setTip((t) => (t ? { ...t, x: e.clientX, y: e.clientY } : t));
  const onLeave = () => setTip(null);

  const empty = !loading && taskBars.length === 0 && reminderPoints.length === 0 && maxMinutes === 0;

  return (
    <div className="ui-page ui-fade-in" data-palette="#7c3aed,#0d9488">
      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-6">
        <div>
          <p className="ui-eyebrow">Timeline</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
            Gantt
          </h1>
          <p className="ui-muted mtm-mt-1 mtm-mb-0">
            Tasks as duration bars, reminders as milestones, and your daily activity load — all on one timeline.
          </p>
        </div>
        <div className="mtm-flex mtm-gap-2">
          {RANGES.map((r) => (
            <button
              key={r.key}
              onClick={() => setSpan(r.key)}
              className={`ui-btn ui-btn-sm ${span === r.key ? "ui-btn-primary" : "ui-btn-ghost"}`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {/* Legend — identity is never color-alone (icon + label). */}
      <div className="mtm-flex mtm-flex-wrap mtm-gap-4 mtm-mb-5 mtm-text-sm">
        <span className="mtm-inline-flex mtm-items-center mtm-gap-2 mtm-text-muted">
          <span className="mtm-inline-block mtm-h-3 mtm-w-6 mtm-rounded" style={{ backgroundColor: "rgb(var(--mtm-gantt-task-rgb))" }} />
          <FiCheckSquare size={13} /> Task
        </span>
        <span className="mtm-inline-flex mtm-items-center mtm-gap-2 mtm-text-muted">
          <span className="mtm-inline-block mtm-h-3 mtm-w-3 mtm-rotate-45" style={{ backgroundColor: "rgb(var(--mtm-gantt-reminder-rgb))" }} />
          <FiBell size={13} /> Reminder
        </span>
        <span className="mtm-inline-flex mtm-items-center mtm-gap-2 mtm-text-muted">
          <span className="mtm-inline-block mtm-h-3 mtm-w-6 mtm-rounded" style={{ backgroundColor: "rgb(var(--mtm-gantt-task-rgb) / 0.35)" }} />
          <FiClock size={13} /> Activity load
        </span>
      </div>

      {loading && <div className="ui-card mtm-p-10 mtm-text-center ui-muted">Loading timeline…</div>}

      {empty && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          Nothing scheduled in this window. Give tasks a due date, set reminders, or log activities to see them here.
        </div>
      )}

      {!loading && !empty && (
        <div className="ui-card mtm-p-0 mtm-overflow-x-auto">
          <div className="mtm-min-w-[720px]">
            {/* Axis */}
            <div className="mtm-flex mtm-border-b-2 mtm-border-line">
              <div className="mtm-w-44 mtm-shrink-0 mtm-px-4 mtm-py-2 mtm-text-xs ui-muted mtm-font-semibold">
                {fmtDay(startMs)} – {fmtDay(endMs)}
              </div>
              <div className="mtm-relative mtm-flex-1 mtm-h-8">
                {ticks.map((t) => (
                  <div
                    key={t}
                    className="mtm-absolute mtm-top-0 mtm-bottom-0 mtm-flex mtm-items-center mtm-text-[11px] ui-muted mtm-border-l mtm-border-line mtm-pl-1"
                    style={{ left: `${pct(t)}%` }}
                  >
                    {fmtDay(t)}
                  </div>
                ))}
              </div>
            </div>

            {/* Body with a shared today-line overlay */}
            <div className="mtm-relative">
              {todayInWindow && (
                <div
                  className="mtm-absolute mtm-top-0 mtm-bottom-0 mtm-z-10 mtm-pointer-events-none"
                  style={{
                    left: `calc(11rem + (100% - 11rem) * ${pct(todayMs) / 100})`,
                    width: 2,
                    backgroundColor: "rgb(var(--mtm-accent-rgb))",
                  }}
                  title="Today"
                />
              )}

              {/* Activity load strip */}
              <Row label={<span className="mtm-inline-flex mtm-items-center mtm-gap-1.5"><FiClock size={13} /> Activity</span>}>
                {daily.map((d) => {
                  const ms = toMs(d.date);
                  if (ms == null || ms < startMs || ms > endMs) return null;
                  const mins = d.totalMinutes || 0;
                  if (mins === 0) return null;
                  const alpha = 0.15 + 0.85 * (maxMinutes ? mins / maxMinutes : 0);
                  return (
                    <div
                      key={d.date}
                      className="mtm-absolute mtm-top-1.5 mtm-bottom-1.5 mtm-rounded"
                      style={{
                        left: `${pct(ms)}%`,
                        width: `${100 / (totalMs / DAY)}%`,
                        backgroundColor: `rgb(var(--mtm-gantt-task-rgb) / ${alpha})`,
                      }}
                      onMouseEnter={(e) => onEnter(e, fmtDay(ms), `${mins} min logged`)}
                      onMouseMove={onMove}
                      onMouseLeave={onLeave}
                    />
                  );
                })}
              </Row>

              {/* Task bars */}
              {taskBars.map((t) => {
                const left = Math.max(pct(t._start), 0);
                const right = Math.min(pct(t._end), 100);
                const width = Math.max(right - left, 1.2);
                const done = t.status === "DONE";
                return (
                  <Row key={t.id} label={<span className="mtm-truncate mtm-inline-flex mtm-items-center mtm-gap-1.5"><FiCheckSquare size={13} /> {t.name}</span>}>
                    <div
                      className="mtm-absolute mtm-top-1.5 mtm-bottom-1.5 mtm-rounded-md mtm-flex mtm-items-center mtm-px-2 mtm-overflow-hidden mtm-border-2 mtm-border-ink/20"
                      style={{
                        left: `${left}%`,
                        width: `${width}%`,
                        backgroundColor: `rgb(var(--mtm-gantt-task-rgb) / ${done ? 0.4 : 1})`,
                      }}
                      onMouseEnter={(e) =>
                        onEnter(e, t.name, `${fmtDay(t._start)} → ${fmtDay(t._end - DAY)} · ${(t.status || "TODO").replace("_", " ")}`)
                      }
                      onMouseMove={onMove}
                      onMouseLeave={onLeave}
                    >
                      <span className="mtm-text-[11px] mtm-font-semibold mtm-text-white mtm-truncate">
                        {done ? "✓ " : ""}{t.name}
                      </span>
                    </div>
                  </Row>
                );
              })}

              {/* Reminder milestones */}
              {reminderPoints.map((r) => {
                const left = pct(r._at);
                const handled = r.status === "DONE" || r.status === "DISMISSED";
                return (
                  <Row key={r.id} label={<span className="mtm-truncate mtm-inline-flex mtm-items-center mtm-gap-1.5"><FiBell size={13} /> {r.title}</span>}>
                    <div
                      className="mtm-absolute mtm-top-1/2 mtm--translate-y-1/2 mtm-h-3.5 mtm-w-3.5 mtm-rotate-45 mtm-rounded-sm mtm-border-2 mtm-border-ink/30"
                      style={{
                        left: `calc(${left}% - 7px)`,
                        backgroundColor: handled
                          ? "rgb(var(--mtm-gantt-reminder-rgb) / 0.4)"
                          : "rgb(var(--mtm-gantt-reminder-rgb))",
                      }}
                      onMouseEnter={(e) =>
                        onEnter(e, r.title, `${new Date(r._at).toLocaleString()} · ${r.status}`)
                      }
                      onMouseMove={onMove}
                      onMouseLeave={onLeave}
                    />
                  </Row>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {tip && (
        <div
          className="mtm-fixed mtm-z-[90] mtm-pointer-events-none ui-card mtm-px-3 mtm-py-2 mtm-shadow-comic-sm"
          style={{ left: Math.min(tip.x + 14, window.innerWidth - 220), top: tip.y + 14 }}
        >
          <div className="mtm-font-semibold mtm-text-content mtm-text-sm mtm-truncate mtm-max-w-[200px]">{tip.title}</div>
          {tip.sub && <div className="ui-muted mtm-text-xs mtm-mt-0.5">{tip.sub}</div>}
        </div>
      )}
    </div>
  );
}

function Row({ label, children }) {
  return (
    <div className="mtm-flex mtm-items-stretch mtm-border-b mtm-border-line/60 last:mtm-border-b-0 hover:mtm-bg-surface-2/40">
      <div className="mtm-w-44 mtm-shrink-0 mtm-px-4 mtm-py-2 mtm-text-sm mtm-text-content mtm-font-medium mtm-truncate">
        {label}
      </div>
      <div className="mtm-relative mtm-flex-1 mtm-h-9">{children}</div>
    </div>
  );
}

export default Gantt;
