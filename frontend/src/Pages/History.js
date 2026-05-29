import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "react-bootstrap/Button";
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
      className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3 hover:mtm-bg-white/10 mtm-cursor-pointer mtm-transition"
      onClick={() => onOpen(item.recordDate)}
    >
      <div>
        <div className="mtm-text-lg mtm-text-white mtm-tracking-wider">
          {item.recordDate}
        </div>
        <div className="mtm-text-sm mtm-text-white/70">
          {item.activityCount} {item.activityCount === 1 ? "activity" : "activities"}
          &nbsp;·&nbsp;
          {item.totalDurationHuman || "0m"}
        </div>
      </div>
      <Button
        size="sm"
        variant="outline-warning"
        onClick={(e) => {
          e.stopPropagation();
          onOpen(item.recordDate);
        }}
      >
        Open day
      </Button>
    </li>
  );
}

function History({ toastState, setToastState }) {
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
      // Backend returns PaginationResultResponseDTO: { results, page, size, totalPages }.
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

  // totalPages is 0 when there are no records — clamp display to "1 / 1" so we
  // don't render a confusing "Page 1 of 0".
  const displayTotal = totalPages > 0 ? totalPages : 1;
  const canPrev = page > 0;
  const canNext = page + 1 < totalPages;

  return (
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-4xl mtm-mx-auto">
        <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400 mtm-mb-2">
          Activity History
        </h1>
        <p className="mtm-text-white/60 mtm-mb-8">
          Every day you've tracked, newest first. Click a row to open that day's activities.
        </p>

        <div className="mtm-flex mtm-flex-col sm:mtm-flex-row mtm-justify-between mtm-items-start sm:mtm-items-center mtm-mb-6 mtm-gap-3">
          <label className="mtm-text-white/80 mtm-text-sm">
            Page size:&nbsp;
            <select
              value={size}
              onChange={(e) => {
                setPage(0);
                setSize(parseInt(e.target.value, 10));
              }}
              className="mtm-bg-black mtm-text-white mtm-border mtm-border-sky-400 mtm-rounded mtm-px-2 mtm-py-1 mtm-ml-1"
            >
              {PAGE_SIZE_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </label>
          <div className="mtm-flex mtm-items-center mtm-gap-3">
            <Button
              size="sm"
              variant="outline-light"
              disabled={!canPrev || loading}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              ← Prev
            </Button>
            <span className="mtm-text-white/70 mtm-text-sm">
              Page {page + 1} of {displayTotal}
            </span>
            <Button
              size="sm"
              variant="outline-light"
              disabled={!canNext || loading}
              onClick={() => setPage((p) => p + 1)}
            >
              Next →
            </Button>
          </div>
        </div>

        {loading && <p className="mtm-text-white/60">Loading history…</p>}

        {!loading && items.length === 0 && (
          <div className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-6 mtm-text-white/70">
            No tracked days yet. Head to the{" "}
            <span
              className="mtm-text-yellow-300 mtm-cursor-pointer mtm-underline"
              onClick={() => navigate("/activity")}
            >
              Activity tracker
            </span>{" "}
            to log your first one.
          </div>
        )}

        <ul className="mtm-space-y-3">
          {items.map((item) => (
            <HistoryRow key={item.recordDate} item={item} onOpen={openDay} />
          ))}
        </ul>
      </div>
    </div>
  );
}

export default History;
