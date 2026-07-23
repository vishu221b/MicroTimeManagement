import React, { useCallback, useEffect, useState } from "react";
import {
  FiTrash2,
  FiFolder,
  FiCheckSquare,
  FiBell,
  FiRotateCcw,
  FiArchive,
} from "react-icons/fi";
import { listTrash, restoreItem, purgeItem } from "../service/ApiService";
import { useConfirm } from "../components/ConfirmProvider";

const errorMessageFrom = (e) =>
  (e && e.error && e.error.message) || (e && e.message) || "Something went wrong.";

const TYPE_META = {
  PROJECT: { icon: <FiFolder />, label: "Project" },
  TASK: { icon: <FiCheckSquare />, label: "Task" },
  REMINDER: { icon: <FiBell />, label: "Reminder" },
};

const TABS = [
  { key: "DELETED", label: "Trash", icon: <FiTrash2 /> },
  { key: "ARCHIVED", label: "Archive", icon: <FiArchive /> },
];

function Trash({ setToastState }) {
  const [tab, setTab] = useState("DELETED");
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const confirm = useConfirm();

  const showError = (m) =>
    setToastState({ display: true, variant: "error", messages: [m], includePrefix: true });
  const showSuccess = (m) => setToastState({ display: true, variant: "success", messages: [m] });

  const load = useCallback(() => {
    setLoading(true);
    listTrash(tab, (data, err) => {
      setLoading(false);
      if (err) return showError(errorMessageFrom(err));
      setItems(Array.isArray(data) ? data : []);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab]);

  useEffect(() => {
    load();
  }, [load]);

  const restore = (item) => {
    restoreItem(item.type, item.id, (d, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess(`${TYPE_META[item.type].label} restored.`);
      load();
    });
  };

  const purge = async (item) => {
    const ok = await confirm({
      title: "Delete permanently?",
      message: `"${item.title}" will be gone for good — this can't be undone.`,
      confirmLabel: "Delete forever",
    });
    if (!ok) return;
    purgeItem(item.type, item.id, (d, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Deleted permanently.");
      load();
    });
  };

  const emptyCopy =
    tab === "DELETED"
      ? "Nothing in the trash. Deleted projects, tasks, and reminders show up here."
      : "Nothing archived. Archive items from their pages to tuck them away without deleting.";

  return (
    <div className="ui-page ui-fade-in">
      <div className="mtm-mb-8">
        <p className="ui-eyebrow">Cleanup</p>
        <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
          Trash &amp; Archive
        </h1>
        <p className="ui-muted mtm-mt-1 mtm-mb-0">
          Restore anything you deleted or archived — or clear it out for good.
        </p>
      </div>

      <div className="mtm-flex mtm-gap-2 mtm-mb-6">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`ui-btn ui-btn-sm ${tab === t.key ? "ui-btn-primary" : "ui-btn-ghost"}`}
          >
            {t.icon} {t.label}
          </button>
        ))}
      </div>

      {loading && <div className="ui-card mtm-p-10 mtm-text-center ui-muted">Loading…</div>}

      {!loading && items.length === 0 && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">{emptyCopy}</div>
      )}

      {!loading && items.length > 0 && (
        <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
          {items.map((item) => {
            const meta = TYPE_META[item.type] || {};
            return (
              <li
                key={`${item.type}-${item.id}`}
                className="ui-card-flat mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-center sm:mtm-justify-between mtm-gap-3"
              >
                <div className="mtm-flex mtm-items-start mtm-gap-3 mtm-min-w-0">
                  <span className="ui-icon-tile mtm-shrink-0">{meta.icon}</span>
                  <div className="mtm-min-w-0">
                    <div className="mtm-flex mtm-items-center mtm-gap-2">
                      <span className="mtm-font-semibold mtm-text-content mtm-truncate">{item.title}</span>
                      <span className="ui-chip mtm-text-xs mtm-shrink-0">{meta.label}</span>
                    </div>
                    {item.subtitle && (
                      <div className="mtm-text-sm ui-muted mtm-truncate mtm-mt-0.5">{item.subtitle}</div>
                    )}
                  </div>
                </div>
                <div className="mtm-flex mtm-gap-2 mtm-shrink-0">
                  <button className="ui-btn ui-btn-soft ui-btn-sm" onClick={() => restore(item)}>
                    <FiRotateCcw size={14} /> Restore
                  </button>
                  {tab === "DELETED" && (
                    <button className="ui-btn ui-btn-danger ui-btn-sm" onClick={() => purge(item)}>
                      <FiTrash2 size={14} /> Delete forever
                    </button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

export default Trash;
