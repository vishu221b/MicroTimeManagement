import React, { useCallback, useEffect, useState } from "react";
import {
  FiPlus,
  FiBell,
  FiTrash2,
  FiCheck,
  FiX,
  FiMail,
  FiClock,
} from "react-icons/fi";
import {
  createReminder,
  deleteReminder,
  listReminders,
  updateReminder,
} from "../service/ApiService";
import { useConfirm } from "../components/ConfirmProvider";

const errorMessageFrom = (e) =>
  (e && e.error && e.error.message) || (e && e.message) || "Something went wrong.";

const fmt = (ms) => (ms ? new Date(ms).toLocaleString() : "");

const relative = (ms) => {
  if (!ms) return "";
  const diff = ms - Date.now();
  const abs = Math.abs(diff);
  const mins = Math.round(abs / 60000);
  const hrs = Math.round(abs / 3600000);
  const days = Math.round(abs / 86400000);
  const unit = mins < 60 ? `${mins}m` : hrs < 48 ? `${hrs}h` : `${days}d`;
  return diff >= 0 ? `in ${unit}` : `${unit} ago`;
};

const STATUS_CHIP = {
  PENDING: "mtm-text-primary",
  DONE: "mtm-text-ok",
  DISMISSED: "ui-muted",
};

function Reminders({ setToastState }) {
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ title: "", remindAtLocal: "", notes: "", emailReminder: false });
  const confirm = useConfirm();

  const showError = (m) =>
    setToastState({ display: true, variant: "error", messages: [m], includePrefix: true });
  const showSuccess = (m) => setToastState({ display: true, variant: "success", messages: [m] });

  const load = useCallback(() => {
    setLoading(true);
    listReminders((data, err) => {
      setLoading(false);
      if (err) return showError(errorMessageFrom(err));
      setReminders(Array.isArray(data) ? data : []);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleCreate = (e) => {
    e.preventDefault();
    if (!form.title.trim()) return showError("Title is required.");
    if (!form.remindAtLocal) return showError("Pick a date and time.");
    const remindAt = new Date(form.remindAtLocal).getTime();
    createReminder(
      { title: form.title, notes: form.notes, remindAt, emailReminder: form.emailReminder },
      (data, err) => {
        if (err) return showError(errorMessageFrom(err));
        showSuccess("Reminder set.");
        setForm({ title: "", remindAtLocal: "", notes: "", emailReminder: false });
        setCreating(false);
        load();
      }
    );
  };

  const setStatus = (r, status) => {
    updateReminder(r.id, { status }, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      load();
    });
  };

  const remove = async (r) => {
    const ok = await confirm({ title: "Delete reminder?", message: `"${r.title}" will be removed.` });
    if (!ok) return;
    deleteReminder(r.id, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Reminder deleted.");
      load();
    });
  };

  return (
    <div className="ui-page ui-fade-in mtm-max-w-3xl">
      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
        <div>
          <p className="ui-eyebrow">Reminders</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
            Reminders
          </h1>
          <p className="ui-muted mtm-mt-1 mtm-mb-0">
            Schedule nudges. You'll get an in-app + browser notification while MTM is open; email is optional.
          </p>
        </div>
        <button className="ui-btn ui-btn-primary" onClick={() => setCreating((c) => !c)}>
          <FiPlus size={18} /> New reminder
        </button>
      </div>

      {creating && (
        <form onSubmit={handleCreate} className="ui-card mtm-p-6 mtm-mb-8 mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
          <div className="sm:mtm-col-span-2">
            <label className="ui-label">Title</label>
            <input className="ui-input" value={form.title}
              onChange={(e) => setForm((s) => ({ ...s, title: e.target.value }))}
              placeholder="e.g. Submit timesheet" />
          </div>
          <div>
            <label className="ui-label">Remind me at</label>
            <input type="datetime-local" className="ui-input" value={form.remindAtLocal}
              onChange={(e) => setForm((s) => ({ ...s, remindAtLocal: e.target.value }))} />
          </div>
          <div className="mtm-flex mtm-items-end">
            <label className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-sm mtm-text-content mtm-pb-2">
              <input type="checkbox" className="mtm-h-4 mtm-w-4 mtm-accent-primary"
                checked={form.emailReminder}
                onChange={(e) => setForm((s) => ({ ...s, emailReminder: e.target.checked }))} />
              Also email me
            </label>
          </div>
          <div className="sm:mtm-col-span-2">
            <label className="ui-label">Notes</label>
            <textarea className="ui-textarea" rows={2} value={form.notes}
              onChange={(e) => setForm((s) => ({ ...s, notes: e.target.value }))} placeholder="Optional" />
          </div>
          <div className="sm:mtm-col-span-2 mtm-flex mtm-gap-2">
            <button type="submit" className="ui-btn ui-btn-primary">Set reminder</button>
            <button type="button" className="ui-btn ui-btn-ghost" onClick={() => setCreating(false)}>Cancel</button>
          </div>
        </form>
      )}

      {loading && <div className="ui-card mtm-p-10 mtm-text-center ui-muted">Loading reminders…</div>}

      {!loading && reminders.length === 0 && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">No reminders yet.</div>
      )}

      <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
        {reminders.map((r) => {
          const overdue = r.status === "PENDING" && r.remindAt && r.remindAt <= Date.now();
          return (
            <li key={r.id} className="ui-card-flat mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3">
              <div className="mtm-flex mtm-items-start mtm-gap-3 mtm-min-w-0">
                <span className={`ui-icon-tile mtm-shrink-0 mtm-mt-0.5 ${overdue ? "mtm-text-danger" : ""}`}>
                  <FiBell size={17} />
                </span>
                <div className="mtm-min-w-0">
                  <div className={`mtm-font-semibold ${r.status !== "PENDING" ? "ui-muted mtm-line-through" : "mtm-text-content"}`}>
                    {r.title}
                  </div>
                  <div className="mtm-text-sm ui-muted mtm-flex mtm-flex-wrap mtm-items-center mtm-gap-x-2">
                    <span className="mtm-inline-flex mtm-items-center mtm-gap-1">
                      <FiClock size={12} /> {fmt(r.remindAt)}
                    </span>
                    <span className={STATUS_CHIP[r.status]}>· {relative(r.remindAt)}</span>
                    {r.emailReminder && (
                      <span className="mtm-inline-flex mtm-items-center mtm-gap-1"><FiMail size={12} /> email</span>
                    )}
                  </div>
                  {r.notes && <div className="mtm-text-sm ui-muted mtm-mt-1">{r.notes}</div>}
                </div>
              </div>
              <div className="mtm-flex mtm-gap-2 mtm-shrink-0">
                {r.status === "PENDING" && (
                  <>
                    <button className="ui-btn ui-btn-soft ui-btn-sm" onClick={() => setStatus(r, "DONE")} title="Mark done">
                      <FiCheck size={14} /> Done
                    </button>
                    <button className="ui-btn ui-btn-ghost ui-btn-sm" onClick={() => setStatus(r, "DISMISSED")} title="Dismiss">
                      <FiX size={14} />
                    </button>
                  </>
                )}
                <button className="ui-btn ui-btn-danger ui-btn-sm" onClick={() => remove(r)} aria-label="Delete">
                  <FiTrash2 size={14} />
                </button>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export default Reminders;
