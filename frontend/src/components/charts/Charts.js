import React from "react";

/**
 * Lightweight, dependency-free charts built on the design-system tokens.
 * Both are single-series (magnitude), so they use one hue (the primary) and
 * need no legend — the surrounding heading names the series. Bars are thin
 * with rounded ends anchored to the baseline; all text stays in ink tokens.
 */

const humanMinutes = (m) => {
  const mins = Math.max(0, Math.round(m || 0));
  const h = Math.floor(mins / 60);
  const r = mins % 60;
  if (h && r) return `${h}h ${r}m`;
  if (h) return `${h}h`;
  return `${r}m`;
};

// Vertical time-series bars (e.g. minutes per day).
export function VerticalBars({ data = [], labelKey = "date", valueKey = "totalMinutes", height = 160 }) {
  const max = Math.max(1, ...data.map((d) => d[valueKey] || 0));
  const n = data.length;
  // Show at most ~7 x labels to avoid crowding on a 30-day window.
  const labelEvery = Math.ceil(n / 7);

  return (
    <div>
      <div
        className="mtm-flex mtm-items-end mtm-gap-[3px] mtm-w-full"
        style={{ height }}
        role="img"
        aria-label="Time logged per day"
      >
        {data.map((d, i) => {
          const v = d[valueKey] || 0;
          const pct = (v / max) * 100;
          return (
            <div key={d[labelKey] || i} className="mtm-flex-1 mtm-flex mtm-items-end mtm-h-full mtm-group mtm-relative">
              <div
                title={`${d[labelKey]} · ${humanMinutes(v)}`}
                className="mtm-w-full mtm-rounded-t-md mtm-transition-all hover:mtm-opacity-80"
                style={{
                  height: `${v === 0 ? 0 : Math.max(pct, 3)}%`,
                  minHeight: v === 0 ? 0 : 3,
                  background: "linear-gradient(180deg, rgb(var(--mtm-primary-rgb)), rgb(var(--mtm-accent-rgb)))",
                }}
              />
            </div>
          );
        })}
      </div>
      <div className="mtm-flex mtm-justify-between mtm-mt-2">
        {data.map((d, i) => (
          <span key={`l-${i}`} className="mtm-flex-1 mtm-text-center mtm-text-[10px] ui-muted mtm-truncate">
            {i % labelEvery === 0 ? (d[labelKey] || "").slice(5) : ""}
          </span>
        ))}
      </div>
    </div>
  );
}

// Horizontal ranked bars (e.g. top activities by total time).
export function HorizontalBars({ data = [], labelKey = "activityName", valueKey = "totalMinutes", humanKey = "totalDurationHuman" }) {
  const max = Math.max(1, ...data.map((d) => d[valueKey] || 0));
  return (
    <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
      {data.map((d, i) => {
        const v = d[valueKey] || 0;
        const pct = (v / max) * 100;
        return (
          <li key={d[labelKey] || i}>
            <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mb-1">
              <span className="mtm-text-sm mtm-text-content mtm-font-medium mtm-truncate mtm-mr-2">{d[labelKey]}</span>
              <span className="mtm-text-xs ui-muted mtm-shrink-0">{d[humanKey] || humanMinutes(v)}</span>
            </div>
            <div className="mtm-h-2.5 mtm-w-full mtm-rounded-full mtm-bg-surface-2 mtm-overflow-hidden">
              <div
                title={`${d[labelKey]} · ${d[humanKey] || humanMinutes(v)}`}
                className="mtm-h-full mtm-rounded-full"
                style={{
                  width: `${Math.max(pct, 2)}%`,
                  background: "linear-gradient(90deg, rgb(var(--mtm-primary-rgb)), rgb(var(--mtm-accent-rgb)))",
                }}
              />
            </div>
          </li>
        );
      })}
    </ul>
  );
}
