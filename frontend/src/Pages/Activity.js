import React, { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { FiPlus, FiEdit2, FiTrash2, FiClock, FiX } from "react-icons/fi";
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
// falls back to today so we never round-trip junk into the backend.
const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/;
const sanitizeDateParam = (raw) =>
  raw && ISO_DATE_RE.test(raw) ? raw : todayIsoDate();

const blankForm = () => ({
  activityName: "",
  activityDescription: "",
  startTime: "",
  endTime: "",
});

function Activity({ setToastState }) {
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

  // Pick up date changes from outside the picker — e.g. browser back button.
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
    if (!form.activityName || !form.startTime || !form.endTime) {
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

  const formOpen = creating || editingId;

  return (
    <div className="ui-page ui-fade-in">
      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
        <div>
          <p className="ui-eyebrow">Tracker</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
            Activity tracker
          </h1>
        </div>
        <div className="mtm-flex mtm-flex-wrap mtm-items-center mtm-gap-3">
          <label className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-sm ui-muted">
            Date
            <input
              type="date"
              value={date}
              onChange={(e) => {
                const next = e.target.value;
                setDate(next);
                if (next) setSearchParams({ date: next }, { replace: true });
                else setSearchParams({}, { replace: true });
              }}
              className="ui-input mtm-w-auto mtm-py-1.5"
            />
          </label>
          <button
            className="ui-btn ui-btn-primary"
            onClick={() => {
              resetForm();
              setCreating(true);
            }}
          >
            <FiPlus size={18} /> New activity
          </button>
        </div>
      </div>

      {formOpen && (
        <form
          onSubmit={editingId ? handleUpdate : handleCreate}
          className="ui-card mtm-p-6 mtm-mb-8"
        >
          <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mb-5">
            <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-m-0">
              {editingId ? "Edit activity" : "New activity"}
            </h2>
            <button
              type="button"
              onClick={resetForm}
              aria-label="Close"
              className="mtm-text-muted hover:mtm-text-content"
            >
              <FiX size={18} />
            </button>
          </div>
          <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
            <div>
              <label className="ui-label" htmlFor="activityName">
                Name
              </label>
              <input
                id="activityName"
                type="text"
                value={form.activityName}
                onChange={(e) =>
                  setForm((s) => ({ ...s, activityName: e.target.value }))
                }
                list="mtm-activity-name-suggestions"
                autoComplete="off"
                className="ui-input"
                placeholder="e.g. Standup"
              />
              <datalist id="mtm-activity-name-suggestions">
                {nameSuggestions.map((name) => (
                  <option key={name} value={name} />
                ))}
              </datalist>
            </div>
            <div className="sm:mtm-col-span-2">
              <label className="ui-label" htmlFor="activityDescription">
                Description
              </label>
              <textarea
                id="activityDescription"
                rows={2}
                value={form.activityDescription}
                onChange={(e) =>
                  setForm((s) => ({
                    ...s,
                    activityDescription: e.target.value,
                  }))
                }
                className="ui-textarea"
                placeholder="Optional notes"
              />
            </div>
            <div>
              <label className="ui-label" htmlFor="startTime">
                Start time{editingId ? " (leave blank to keep)" : ""}
              </label>
              <input
                id="startTime"
                type="time"
                value={form.startTime}
                onChange={(e) =>
                  setForm((s) => ({ ...s, startTime: e.target.value }))
                }
                className="ui-input"
              />
            </div>
            <div>
              <label className="ui-label" htmlFor="endTime">
                End time{editingId ? " (leave blank to keep)" : ""}
              </label>
              <input
                id="endTime"
                type="time"
                value={form.endTime}
                onChange={(e) =>
                  setForm((s) => ({ ...s, endTime: e.target.value }))
                }
                className="ui-input"
              />
            </div>
          </div>
          <div className="mtm-mt-5 mtm-flex mtm-gap-2">
            <button type="submit" className="ui-btn ui-btn-primary">
              {editingId ? "Save changes" : "Create activity"}
            </button>
            <button type="button" className="ui-btn ui-btn-ghost" onClick={resetForm}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-mb-4">
        Activities for {date}
      </h2>

      {loading && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          Loading activities…
        </div>
      )}

      {!loading && activities.length === 0 && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          No activities recorded for this date.
        </div>
      )}

      <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
        {activities.map((activity) => (
          <li
            key={activity.id}
            className="ui-card-flat mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3"
          >
            <div className="mtm-flex mtm-items-start mtm-gap-3 mtm-min-w-0">
              <span className="ui-icon-tile mtm-shrink-0 mtm-mt-0.5">
                <FiClock size={17} />
              </span>
              <div className="mtm-min-w-0">
                <div className="mtm-text-content mtm-font-semibold">
                  {activity.activityName}
                </div>
                <div className="mtm-text-sm ui-muted">
                  {activity.activityStartTime} → {activity.activityEndTime} ·{" "}
                  <span className="mtm-text-primary mtm-font-medium">
                    {activity.activityTotalDuration}
                  </span>
                </div>
                {activity.activityDescription && (
                  <div className="mtm-text-sm ui-muted mtm-mt-1">
                    {activity.activityDescription}
                  </div>
                )}
              </div>
            </div>
            <div className="mtm-flex mtm-gap-2 mtm-shrink-0">
              <button
                className="ui-btn ui-btn-ghost ui-btn-sm"
                onClick={() => startEditing(activity)}
              >
                <FiEdit2 size={14} /> Edit
              </button>
              <button
                className="ui-btn ui-btn-danger ui-btn-sm"
                onClick={() => handleDelete(activity.id)}
              >
                <FiTrash2 size={14} /> Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Activity;
