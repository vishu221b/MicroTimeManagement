import React, { useCallback, useEffect, useState } from "react";
import Button from "react-bootstrap/Button";
import { useSearchParams } from "react-router-dom";
import {
  createActivity,
  deleteActivity,
  getActivitiesForDate,
  getActivityNames,
  updateActivity,
} from "../service/ApiService";

// Backend expects time strings shaped "HH:MM:M" where the final digit is
// 0 for AM and 1 for PM, with HH in the 1..12 range. The HTML <input type="time">
// gives us back "HH:MM" in 24-hour form, so we translate on the way out.
const toBackendTimeString = (hhmm24) => {
  if (!hhmm24) return "";
  const [hStr, mStr] = hhmm24.split(":");
  const hour24 = parseInt(hStr, 10);
  const minutes = parseInt(mStr, 10);
  const meridian = hour24 < 12 ? 0 : 1;
  let hour12 = hour24 % 12;
  if (hour12 === 0) hour12 = 12;
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`);
  return `${pad(hour12)}:${pad(minutes)}:${meridian}`;
};

const todayIsoDate = () => {
  const now = new Date();
  const pad = (n) => (n < 10 ? `0${n}` : `${n}`);
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
};

// Accept only yyyy-MM-dd strings on the way in from the URL — anything else
// (truncated value, copy-paste from another locale) falls back to today so we
// never round-trip junk into the backend.
const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/;
const sanitizeDateParam = (raw) =>
  raw && ISO_DATE_RE.test(raw) ? raw : todayIsoDate();

const blankForm = () => ({
  activityName: "",
  activityDescription: "",
  startTime: "",
  endTime: "",
});

function Activity({ toastState, setToastState }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const [date, setDate] = useState(() =>
    sanitizeDateParam(searchParams.get("date"))
  );
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState(blankForm());
  const [editingId, setEditingId] = useState(null);
  const [nameSuggestions, setNameSuggestions] = useState([]);

  const showSuccess = (message) =>
    setToastState({
      display: true,
      variant: "success",
      messages: [message],
      includePrefix: false,
      includeSuffix: false,
      suffix: "",
    });

  const showError = (messages) =>
    setToastState({
      display: true,
      variant: "error",
      messages: Array.isArray(messages) ? messages : [messages],
      includePrefix: true,
      includeSuffix: false,
      suffix: "",
    });

  const errorMessageFrom = (errorPayload) => {
    if (!errorPayload) return "Something went wrong.";
    if (errorPayload.error && errorPayload.error.message)
      return errorPayload.error.message;
    if (errorPayload.message) return errorPayload.message;
    return "Something went wrong.";
  };

  const loadActivities = useCallback(
    (forDate) => {
      setLoading(true);
      getActivitiesForDate(forDate, (data, err) => {
        setLoading(false);
        if (err) {
          // No record for the date is an expected "empty" state, not an error.
          const msg = errorMessageFrom(err);
          if (msg && msg.toLowerCase().includes("no record found")) {
            setActivities([]);
            return;
          }
          showError(msg);
          setActivities([]);
          return;
        }
        setActivities((data && data.activities) || []);
      });
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  useEffect(() => {
    loadActivities(date);
  }, [date, loadActivities]);

  const refreshNameSuggestions = useCallback(() => {
    getActivityNames((data, err) => {
      if (err) return; // Autocomplete is a nicety — failure should be silent.
      setNameSuggestions((data && data.names) || []);
    });
  }, []);

  useEffect(() => {
    refreshNameSuggestions();
  }, [refreshNameSuggestions]);

  // Pick up date changes that come from outside the date picker — e.g. the
  // browser back button after a click from the History page.
  useEffect(() => {
    const fromUrl = sanitizeDateParam(searchParams.get("date"));
    if (fromUrl !== date) {
      setDate(fromUrl);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const resetForm = () => {
    setForm(blankForm());
    setEditingId(null);
    setCreating(false);
  };

  const handleCreate = (e) => {
    e.preventDefault();
    if (
      !form.activityName ||
      !form.startTime ||
      !form.endTime
    ) {
      showError("Name, start time, and end time are required.");
      return;
    }
    const payload = {
      recordDate: date,
      activityName: form.activityName,
      activityDescription: form.activityDescription,
      activityStartHourMinutes: toBackendTimeString(form.startTime),
      activityEndHourMinutes: toBackendTimeString(form.endTime),
    };
    createActivity(payload, (data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Activity created.");
      resetForm();
      loadActivities(date);
      refreshNameSuggestions();
    });
  };

  const handleUpdate = (e) => {
    e.preventDefault();
    if (!editingId) return;
    const payload = {
      recordId: editingId,
      activityName: form.activityName || undefined,
      activityDescription: form.activityDescription || undefined,
      activityStartHourMinutes: form.startTime
        ? toBackendTimeString(form.startTime)
        : undefined,
      activityEndHourMinutes: form.endTime
        ? toBackendTimeString(form.endTime)
        : undefined,
    };
    updateActivity(date, payload, (data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Activity updated.");
      resetForm();
      loadActivities(date);
      refreshNameSuggestions();
    });
  };

  const handleDelete = (recordId) => {
    if (!window.confirm("Delete this activity?")) return;
    deleteActivity(date, recordId, (data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Activity deleted.");
      loadActivities(date);
    });
  };

  const startEditing = (activity) => {
    setEditingId(activity.id);
    setCreating(false);
    setForm({
      activityName: activity.activityName || "",
      activityDescription: activity.activityDescription || "",
      // Start blank so users only submit times when they want to retime.
      startTime: "",
      endTime: "",
    });
  };

  return (
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-4xl mtm-mx-auto">
        <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400 mtm-mb-8">
          Activity Tracker
        </h1>

        <div className="mtm-flex mtm-flex-col sm:mtm-flex-row mtm-items-start sm:mtm-items-center mtm-gap-4 mtm-mb-8">
          <label className="mtm-text-white/80 mtm-tracking-wide">
            Date:&nbsp;
            <input
              type="date"
              value={date}
              onChange={(e) => {
                const next = e.target.value;
                setDate(next);
                // Keep the URL in sync so the date is shareable/back-navigable.
                if (next) setSearchParams({ date: next }, { replace: true });
                else setSearchParams({}, { replace: true });
              }}
              className="mtm-bg-black mtm-text-white mtm-border mtm-border-sky-400 mtm-rounded mtm-px-3 mtm-py-1 mtm-ml-2"
            />
          </label>
          <Button
            variant="warning"
            onClick={() => {
              resetForm();
              setCreating(true);
            }}
          >
            + New Activity
          </Button>
        </div>

        {(creating || editingId) && (
          <form
            onSubmit={editingId ? handleUpdate : handleCreate}
            className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-6 mtm-mb-8"
          >
            <h2 className="mtm-text-lg mtm-text-yellow-300 mtm-mb-4">
              {editingId ? "Edit activity" : "New activity"}
            </h2>
            <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
              <label className="mtm-flex mtm-flex-col">
                <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                  Name
                </span>
                <input
                  type="text"
                  value={form.activityName}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, activityName: e.target.value }))
                  }
                  list="mtm-activity-name-suggestions"
                  autoComplete="off"
                  className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
                />
                <datalist id="mtm-activity-name-suggestions">
                  {nameSuggestions.map((name) => (
                    <option key={name} value={name} />
                  ))}
                </datalist>
              </label>
              <label className="mtm-flex mtm-flex-col sm:mtm-col-span-2">
                <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                  Description
                </span>
                <textarea
                  rows={2}
                  value={form.activityDescription}
                  onChange={(e) =>
                    setForm((s) => ({
                      ...s,
                      activityDescription: e.target.value,
                    }))
                  }
                  className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
                />
              </label>
              <label className="mtm-flex mtm-flex-col">
                <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                  Start time
                  {editingId ? " (leave blank to keep)" : ""}
                </span>
                <input
                  type="time"
                  value={form.startTime}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, startTime: e.target.value }))
                  }
                  className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
                />
              </label>
              <label className="mtm-flex mtm-flex-col">
                <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
                  End time
                  {editingId ? " (leave blank to keep)" : ""}
                </span>
                <input
                  type="time"
                  value={form.endTime}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, endTime: e.target.value }))
                  }
                  className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
                />
              </label>
            </div>
            <div className="mtm-mt-4 mtm-flex mtm-gap-2">
              <Button type="submit" variant="warning">
                {editingId ? "Save changes" : "Create activity"}
              </Button>
              <Button
                type="button"
                variant="outline-light"
                onClick={resetForm}
              >
                Cancel
              </Button>
            </div>
          </form>
        )}

        <h2 className="mtm-text-xl mtm-text-yellow-300 mtm-mb-4">
          Activities for {date}
        </h2>

        {loading && (
          <p className="mtm-text-white/60">Loading activities…</p>
        )}

        {!loading && activities.length === 0 && (
          <p className="mtm-text-white/60">
            No activities recorded for this date.
          </p>
        )}

        <ul className="mtm-space-y-3">
          {activities.map((activity) => (
            <li
              key={activity.id}
              className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-2"
            >
              <div>
                <div className="mtm-text-lg mtm-text-white">
                  {activity.activityName}
                </div>
                <div className="mtm-text-sm mtm-text-white/70">
                  {activity.activityStartTime} → {activity.activityEndTime}
                  &nbsp;·&nbsp;
                  {activity.activityTotalDuration}
                </div>
                {activity.activityDescription && (
                  <div className="mtm-text-sm mtm-text-white/60 mtm-mt-1">
                    {activity.activityDescription}
                  </div>
                )}
              </div>
              <div className="mtm-flex mtm-gap-2">
                <Button
                  size="sm"
                  variant="outline-warning"
                  onClick={() => startEditing(activity)}
                >
                  Edit
                </Button>
                <Button
                  size="sm"
                  variant="outline-danger"
                  onClick={() => handleDelete(activity.id)}
                >
                  Delete
                </Button>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default Activity;
