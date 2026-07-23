import React, { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  FiArrowLeft,
  FiPlus,
  FiTrash2,
  FiChevronLeft,
  FiChevronRight,
  FiChevronDown,
  FiChevronUp,
  FiCalendar,
  FiArchive,
} from "react-icons/fi";
import {
  createTask,
  deleteTask,
  getProject,
  listTasks,
  updateTask,
  archiveItem,
} from "../service/ApiService";
import { useConfirm } from "../components/ConfirmProvider";
import Modal from "../components/Modal";
import AttachmentPanel from "../components/AttachmentPanel";
import { FiPaperclip } from "react-icons/fi";

const COLUMNS = [
  { key: "TODO", label: "To do" },
  { key: "IN_PROGRESS", label: "In progress" },
  { key: "DONE", label: "Done" },
];
const NEXT = { TODO: "IN_PROGRESS", IN_PROGRESS: "DONE", DONE: null };
const PREV = { TODO: null, IN_PROGRESS: "TODO", DONE: "IN_PROGRESS" };
const PRIORITY_CLASS = {
  HIGH: "mtm-bg-danger/15 mtm-text-danger mtm-border-danger/30",
  MEDIUM: "mtm-bg-warn/15 mtm-text-warn mtm-border-warn/30",
  LOW: "mtm-bg-surface-2 mtm-text-muted mtm-border-line",
};

const errorMessageFrom = (e) =>
  (e && e.error && e.error.message) || (e && e.message) || "Something went wrong.";

function SubTasks({ parentId, showError }) {
  const [subs, setSubs] = useState([]);
  const [name, setName] = useState("");

  const load = useCallback(() => {
    listTasks({ parentTaskId: parentId }, (data, err) => {
      if (err) return;
      setSubs(Array.isArray(data) ? data : []);
    });
  }, [parentId]);

  useEffect(() => {
    load();
  }, [load]);

  const add = (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    createTask({ name, parentTaskId: parentId }, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      setName("");
      load();
    });
  };

  const toggle = (sub) => {
    updateTask(sub.id, { status: sub.status === "DONE" ? "TODO" : "DONE" }, (d, err) => {
      if (err) return showError(errorMessageFrom(err));
      load();
    });
  };

  return (
    <div className="mtm-mt-3 mtm-pt-3 mtm-border-t mtm-border-line">
      <ul className="mtm-flex mtm-flex-col mtm-gap-1.5 mtm-list-none mtm-p-0 mtm-m-0 mtm-mb-2">
        {subs.map((s) => (
          <li key={s.id} className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-sm">
            <input type="checkbox" className="mtm-h-4 mtm-w-4 mtm-accent-primary"
              checked={s.status === "DONE"} onChange={() => toggle(s)} />
            <span className={s.status === "DONE" ? "mtm-line-through ui-muted" : "mtm-text-content"}>
              {s.name}
            </span>
          </li>
        ))}
        {subs.length === 0 && <li className="ui-muted mtm-text-xs">No sub-tasks yet.</li>}
      </ul>
      <form onSubmit={add} className="mtm-flex mtm-gap-2">
        <input className="ui-input mtm-py-1.5 mtm-text-sm" placeholder="Add a sub-task"
          value={name} onChange={(e) => setName(e.target.value)} />
        <button type="submit" className="ui-btn ui-btn-soft ui-btn-sm"><FiPlus size={14} /></button>
      </form>
    </div>
  );
}

function ProjectDetail({ setToastState }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const confirm = useConfirm();
  const [viewingTask, setViewingTask] = useState(null);
  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [expanded, setExpanded] = useState(null);
  const [form, setForm] = useState({ name: "", description: "", dueDate: "", priority: "MEDIUM" });
  const [draggingId, setDraggingId] = useState(null);
  const [dragOverCol, setDragOverCol] = useState(null);

  const showError = (m) =>
    setToastState({ display: true, variant: "error", messages: [m], includePrefix: true });
  const showSuccess = (m) => setToastState({ display: true, variant: "success", messages: [m] });

  const loadTasks = useCallback(() => {
    setLoading(true);
    listTasks({ projectId: id }, (data, err) => {
      setLoading(false);
      if (err) return showError(errorMessageFrom(err));
      setTasks(Array.isArray(data) ? data : []);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    getProject(id, (data, err) => {
      if (!err) setProject(data);
    });
    loadTasks();
  }, [id, loadTasks]);

  const handleCreate = (e) => {
    e.preventDefault();
    if (!form.name.trim()) return showError("Task name is required.");
    createTask({ ...form, projectId: id }, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Task created.");
      setForm({ name: "", description: "", dueDate: "", priority: "MEDIUM" });
      setCreating(false);
      loadTasks();
    });
  };

  const move = (task, status) => {
    if (!status || (task.status || "TODO") === status) return;
    // Optimistic: reflect the move immediately, reconcile with the server after.
    setTasks((prev) => prev.map((t) => (t.id === task.id ? { ...t, status } : t)));
    updateTask(task.id, { status }, (d, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        loadTasks();
        return;
      }
      loadTasks();
    });
  };

  const onDragStart = (e, task) => {
    setDraggingId(task.id);
    e.dataTransfer.effectAllowed = "move";
    // Some browsers require data to be set for a drag to start.
    try {
      e.dataTransfer.setData("text/plain", task.id);
    } catch (_) {
      /* ignore */
    }
  };

  const onColumnDrop = (e, colKey) => {
    e.preventDefault();
    setDragOverCol(null);
    setDraggingId(null);
    const task = tasks.find((t) => t.id === draggingId);
    if (task) move(task, colKey);
  };

  const archive = (task) => {
    archiveItem("TASK", task.id, (d, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Task archived. Find it under Trash → Archive.");
      loadTasks();
    });
  };

  const remove = async (task) => {
    const ok = await confirm({ title: "Delete task?", message: `"${task.name}" will be removed.` });
    if (!ok) return;
    deleteTask(task.id, (d, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Task deleted.");
      loadTasks();
    });
  };

  return (
    <div className="ui-page ui-fade-in">
      <button className="ui-btn ui-btn-ghost ui-btn-sm mtm-mb-5" onClick={() => navigate("/projects")}>
        <FiArrowLeft size={16} /> Projects
      </button>

      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
        <div className="mtm-flex mtm-items-center mtm-gap-3">
          <span className="mtm-inline-flex mtm-h-11 mtm-w-11 mtm-rounded-xl mtm-items-center mtm-justify-center mtm-text-white mtm-font-display mtm-font-bold"
            style={{ backgroundColor: (project && project.color) || "#10b981" }}>
            {(project && project.name ? project.name[0] : "P").toUpperCase()}
          </span>
          <div>
            <h1 className="mtm-text-2xl mtm-font-display mtm-font-bold mtm-text-content mtm-m-0">
              {project ? project.name : "Project"}
            </h1>
            {project && project.description && (
              <p className="ui-muted mtm-text-sm mtm-mt-0.5 mtm-mb-0">{project.description}</p>
            )}
          </div>
        </div>
        <button className="ui-btn ui-btn-primary" onClick={() => setCreating((c) => !c)}>
          <FiPlus size={18} /> New task
        </button>
      </div>

      {creating && (
        <form onSubmit={handleCreate} className="ui-card mtm-p-6 mtm-mb-8 mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
          <div className="sm:mtm-col-span-2">
            <label className="ui-label">Task name</label>
            <input className="ui-input" value={form.name}
              onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="e.g. Design the hero" />
          </div>
          <div>
            <label className="ui-label">Due date</label>
            <input type="date" className="ui-input" value={form.dueDate}
              onChange={(e) => setForm((s) => ({ ...s, dueDate: e.target.value }))} />
          </div>
          <div>
            <label className="ui-label">Priority</label>
            <select className="ui-select" value={form.priority}
              onChange={(e) => setForm((s) => ({ ...s, priority: e.target.value }))}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>
          <div className="sm:mtm-col-span-2 mtm-flex mtm-gap-2">
            <button type="submit" className="ui-btn ui-btn-primary">Create task</button>
            <button type="button" className="ui-btn ui-btn-ghost" onClick={() => setCreating(false)}>Cancel</button>
          </div>
        </form>
      )}

      {loading && <div className="ui-card mtm-p-10 mtm-text-center ui-muted">Loading tasks…</div>}

      {!loading && (
        <div className="mtm-grid mtm-gap-4 md:mtm-grid-cols-3">
          {COLUMNS.map((col) => {
            const colTasks = tasks.filter((t) => (t.status || "TODO") === col.key);
            const isOver = dragOverCol === col.key;
            return (
              <div
                key={col.key}
                onDragOver={(e) => {
                  e.preventDefault();
                  e.dataTransfer.dropEffect = "move";
                  if (dragOverCol !== col.key) setDragOverCol(col.key);
                }}
                onDragLeave={(e) => {
                  // Only clear when leaving the column, not when moving over a child.
                  if (!e.currentTarget.contains(e.relatedTarget)) setDragOverCol(null);
                }}
                onDrop={(e) => onColumnDrop(e, col.key)}
                className={`ui-card-flat mtm-p-4 mtm-transition-all ${
                  isOver ? "mtm-ring-2 mtm-ring-primary mtm-bg-primary/5" : ""
                }`}
              >
                <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mb-3">
                  <h3 className="mtm-font-semibold mtm-text-content mtm-m-0">{col.label}</h3>
                  <span className="ui-chip mtm-text-xs">{colTasks.length}</span>
                </div>
                <div className="mtm-flex mtm-flex-col mtm-gap-2.5 mtm-min-h-[3rem]">
                  {colTasks.map((t) => (
                    <div
                      key={t.id}
                      draggable={expanded !== t.id}
                      onDragStart={(e) => onDragStart(e, t)}
                      onDragEnd={() => {
                        setDraggingId(null);
                        setDragOverCol(null);
                      }}
                      className={`ui-card mtm-p-3.5 mtm-cursor-grab active:mtm-cursor-grabbing mtm-transition-opacity ${
                        draggingId === t.id ? "mtm-opacity-40" : ""
                      }`}
                    >
                      <div className="mtm-flex mtm-items-start mtm-justify-between mtm-gap-2">
                        <button
                          type="button"
                          onClick={() => setViewingTask(t)}
                          title="View details & files"
                          className={`mtm-font-medium mtm-text-left hover:mtm-underline ${t.status === "DONE" ? "mtm-line-through ui-muted" : "mtm-text-content"}`}
                        >
                          {t.name}
                        </button>
                        <div className="mtm-flex mtm-items-center mtm-gap-2 mtm-shrink-0">
                          <button className="mtm-text-muted hover:mtm-text-primary" onClick={() => archive(t)} aria-label="Archive" title="Archive">
                            <FiArchive size={14} />
                          </button>
                          <button className="mtm-text-muted hover:mtm-text-danger" onClick={() => remove(t)} aria-label="Delete" title="Delete">
                            <FiTrash2 size={14} />
                          </button>
                        </div>
                      </div>
                      <div className="mtm-flex mtm-flex-wrap mtm-items-center mtm-gap-2 mtm-mt-2">
                        <span className={`mtm-text-xs mtm-px-2 mtm-py-0.5 mtm-rounded-full mtm-border ${PRIORITY_CLASS[t.priority] || PRIORITY_CLASS.LOW}`}>
                          {t.priority}
                        </span>
                        {t.dueDate && (
                          <span className="mtm-text-xs ui-muted mtm-inline-flex mtm-items-center mtm-gap-1">
                            <FiCalendar size={12} /> {t.dueDate}
                          </span>
                        )}
                      </div>
                      <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mt-3">
                        <div className="mtm-flex mtm-gap-1">
                          <button className="ui-btn ui-btn-ghost ui-btn-sm mtm-px-2" disabled={!PREV[t.status || "TODO"]}
                            onClick={() => move(t, PREV[t.status || "TODO"])} aria-label="Move back">
                            <FiChevronLeft size={14} />
                          </button>
                          <button className="ui-btn ui-btn-ghost ui-btn-sm mtm-px-2" disabled={!NEXT[t.status || "TODO"]}
                            onClick={() => move(t, NEXT[t.status || "TODO"])} aria-label="Move forward">
                            <FiChevronRight size={14} />
                          </button>
                        </div>
                        <button className="ui-link mtm-text-xs mtm-inline-flex mtm-items-center mtm-gap-1"
                          onClick={() => setExpanded(expanded === t.id ? null : t.id)}>
                          Sub-tasks {expanded === t.id ? <FiChevronUp size={12} /> : <FiChevronDown size={12} />}
                        </button>
                      </div>
                      {expanded === t.id && <SubTasks parentId={t.id} showError={showError} />}
                    </div>
                  ))}
                  {colTasks.length === 0 && (
                    <div className="ui-muted mtm-text-xs mtm-text-center mtm-py-6">Nothing here.</div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Modal
        open={!!viewingTask}
        onClose={() => setViewingTask(null)}
        title={viewingTask ? viewingTask.name : ""}
        icon={<FiPaperclip />}
        footer={<button className="ui-btn ui-btn-primary" onClick={() => setViewingTask(null)}>Done</button>}
      >
        {viewingTask && (
          <div className="mtm-flex mtm-flex-col mtm-gap-4">
            <div className="mtm-flex mtm-flex-wrap mtm-gap-2">
              <span className="ui-chip">{(viewingTask.status || "TODO").replace("_", " ")}</span>
              <span className={`mtm-text-xs mtm-px-2 mtm-py-0.5 mtm-rounded-full mtm-border ${PRIORITY_CLASS[viewingTask.priority] || PRIORITY_CLASS.LOW}`}>
                {viewingTask.priority}
              </span>
              {viewingTask.dueDate && (
                <span className="ui-chip"><FiCalendar size={12} /> {viewingTask.dueDate}</span>
              )}
            </div>
            {viewingTask.description && (
              <p className="mtm-text-content mtm-font-medium mtm-m-0">{viewingTask.description}</p>
            )}
            <div>
              <h3 className="mtm-font-comic mtm-text-lg mtm-text-content mtm-mb-2 mtm-flex mtm-items-center mtm-gap-2">
                <FiChevronDown /> Sub-tasks
              </h3>
              <SubTasks parentId={viewingTask.id} showError={showError} />
            </div>
            <div>
              <h3 className="mtm-font-comic mtm-text-lg mtm-text-content mtm-mb-2 mtm-flex mtm-items-center mtm-gap-2">
                <FiPaperclip /> Files &amp; images
              </h3>
              <AttachmentPanel parentType="TASK" parentId={viewingTask.id} onError={showError} />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default ProjectDetail;
