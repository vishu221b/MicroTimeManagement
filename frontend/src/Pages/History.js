import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FiChevronLeft,
  FiChevronRight,
  FiCalendar,
  FiArrowRight,
} from "react-icons/fi";
import { getActivityHistory } from "../service/ApiService";

const PAGE_SIZE_OPTIONS = [10, 20, 50];

const errorMessageFrom = (errorPayload) => {
  if (!errorPayload) return "Something went wrong.";
  if (errorPayload.error && errorPayload.error.message)
    return errorPayload.error.message;
  if (errorPayload.message) return errorPayload.message;
  return "Something went wrong.";
};

function HistoryRow({ item, onOpen }) {
  return (
    <li
      className="ui-card-flat mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3 hover:mtm-border-primary/50 mtm-cursor-pointer mtm-transition-colors"
      onClick={() => onOpen(item.recordDate)}
    >
      <div className="mtm-flex mtm-items-center mtm-gap-3">
        <span className="ui-icon-tile mtm-shrink-0">
          <FiCalendar size={17} />
        </span>
        <div>
          <div className="mtm-text-content mtm-font-semibold">
            {item.recordDate}
          </div>
          <div className="mtm-text-sm ui-muted">
            {item.activityCount}{" "}
            {item.activityCount === 1 ? "activity" : "activities"} ·{" "}
            {item.totalDurationHuman || "0m"}
          </div>
        </div>
      </div>
      <button
        className="ui-btn ui-btn-soft ui-btn-sm"
        onClick={(e) => {
          e.stopPropagation();
          onOpen(item.recordDate);
        }}
      >
        Open day <FiArrowRight size={15} />
      </button>
    </li>
  );
}

function History({ setToastState }) {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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
    getActivityHistory({ page, size }, (data, err) => {
      setLoading(false);
      if (err) {
        showError(errorMessageFrom(err));
        setItems([]);
        setTotalPages(0);
        return;
      }
      setItems((data && data.results) || []);
      setTotalPages((data && data.totalPages) || 0);
    });
  }, [page, size, showError]);

  useEffect(() => {
    load();
  }, [load]);

  const openDay = (recordDate) => {
    navigate(`/activity?date=${encodeURIComponent(recordDate)}`);
  };

  const displayTotal = totalPages > 0 ? totalPages : 1;
  const canPrev = page > 0;
  const canNext = page + 1 < totalPages;

  return (
    <div className="ui-page ui-fade-in">
      <p className="ui-eyebrow">History</p>
      <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-2">
        Activity history
      </h1>
      <p className="ui-muted mtm-mb-8">
        Every day you've tracked, newest first. Click a row to open that day's
        activities.
      </p>

      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row mtm-justify-between mtm-items-start sm:mtm-items-center mtm-mb-6 mtm-gap-3">
        <label className="mtm-text-sm ui-muted mtm-flex mtm-items-center mtm-gap-2">
          Page size
          <select
            value={size}
            onChange={(e) => {
              setPage(0);
              setSize(parseInt(e.target.value, 10));
            }}
            className="ui-select mtm-w-auto mtm-py-1.5"
          >
            {PAGE_SIZE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>
        <div className="mtm-flex mtm-items-center mtm-gap-2">
          <button
            className="ui-btn ui-btn-ghost ui-btn-sm"
            disabled={!canPrev || loading}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            <FiChevronLeft size={16} /> Prev
          </button>
          <span className="ui-muted mtm-text-sm mtm-min-w-[92px] mtm-text-center">
            Page {page + 1} of {displayTotal}
          </span>
          <button
            className="ui-btn ui-btn-ghost ui-btn-sm"
            disabled={!canNext || loading}
            onClick={() => setPage((p) => p + 1)}
          >
            Next <FiChevronRight size={16} />
          </button>
        </div>
      </div>

      {loading && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          Loading history…
        </div>
      )}

      {!loading && items.length === 0 && (
        <div className="ui-card mtm-p-10 mtm-text-center">
          <p className="ui-muted mtm-mb-4">No tracked days yet.</p>
          <button
            className="ui-btn ui-btn-primary"
            onClick={() => navigate("/activity")}
          >
            Log your first activity <FiArrowRight size={16} />
          </button>
        </div>
      )}

      <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
        {items.map((item) => (
          <HistoryRow key={item.recordDate} item={item} onOpen={openDay} />
        ))}
      </ul>
    </div>
  );
}

export default History;
