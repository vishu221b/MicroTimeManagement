import React, { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiPlus, FiFolder, FiTrash2, FiArrowRight, FiX } from "react-icons/fi";
import {
  createProject,
  deleteProject,
  listProjects,
} from "../service/ApiService";

const SWATCHES = ["#10b981", "#14b8a6", "#6366f1", "#f59e0b", "#ef4444", "#ec4899"];

const errorMessageFrom = (e) =>
  (e && e.error && e.error.message) || (e && e.message) || "Something went wrong.";

function Projects({ setToastState }) {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ name: "", description: "", color: SWATCHES[0] });
  const navigate = useNavigate();

  const showError = (m) =>
    setToastState({ display: true, variant: "error", messages: [m], includePrefix: true });
  const showSuccess = (m) =>
    setToastState({ display: true, variant: "success", messages: [m] });

  const load = useCallback(() => {
    setLoading(true);
    listProjects((data, err) => {
      setLoading(false);
      if (err) return showError(errorMessageFrom(err));
      setProjects(Array.isArray(data) ? data : []);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleCreate = (e) => {
    e.preventDefault();
    if (!form.name.trim()) return showError("Project name is required.");
    createProject(form, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Project created.");
      setForm({ name: "", description: "", color: SWATCHES[0] });
      setCreating(false);
      load();
    });
  };

  const handleDelete = (project) => {
    if (!window.confirm(`Delete project "${project.name}"?`)) return;
    deleteProject(project.id, (data, err) => {
      if (err) return showError(errorMessageFrom(err));
      showSuccess("Project deleted.");
      load();
    });
  };

  return (
    <div className="ui-page ui-fade-in">
      <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-items-end sm:mtm-justify-between mtm-gap-4 mtm-mb-8">
        <div>
          <p className="ui-eyebrow">Workspace</p>
          <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-0">
            Projects
          </h1>
          <p className="ui-muted mtm-mt-1 mtm-mb-0">
            Group your tasks and activities into projects.
          </p>
        </div>
        <button className="ui-btn ui-btn-primary" onClick={() => setCreating((c) => !c)}>
          <FiPlus size={18} /> New project
        </button>
      </div>

      {creating && (
        <form onSubmit={handleCreate} className="ui-card mtm-p-6 mtm-mb-8">
          <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mb-5">
            <h2 className="mtm-text-lg mtm-font-semibold mtm-text-content mtm-m-0">New project</h2>
            <button type="button" onClick={() => setCreating(false)} className="mtm-text-muted hover:mtm-text-content">
              <FiX size={18} />
            </button>
          </div>
          <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2">
            <div>
              <label className="ui-label" htmlFor="pname">Name</label>
              <input id="pname" className="ui-input" value={form.name}
                onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))}
                placeholder="e.g. Website redesign" />
            </div>
            <div>
              <label className="ui-label">Accent color</label>
              <div className="mtm-flex mtm-gap-2 mtm-mt-1">
                {SWATCHES.map((c) => (
                  <button key={c} type="button" onClick={() => setForm((s) => ({ ...s, color: c }))}
                    className={`mtm-h-8 mtm-w-8 mtm-rounded-full mtm-transition-transform ${form.color === c ? "mtm-ring-2 mtm-ring-offset-2 mtm-ring-offset-surface mtm-scale-110" : ""}`}
                    style={{ backgroundColor: c }} aria-label={c} />
                ))}
              </div>
            </div>
            <div className="sm:mtm-col-span-2">
              <label className="ui-label" htmlFor="pdesc">Description</label>
              <textarea id="pdesc" className="ui-textarea" rows={2} value={form.description}
                onChange={(e) => setForm((s) => ({ ...s, description: e.target.value }))}
                placeholder="Optional" />
            </div>
          </div>
          <div className="mtm-mt-5 mtm-flex mtm-gap-2">
            <button type="submit" className="ui-btn ui-btn-primary">Create project</button>
            <button type="button" className="ui-btn ui-btn-ghost" onClick={() => setCreating(false)}>Cancel</button>
          </div>
        </form>
      )}

      {loading && <div className="ui-card mtm-p-10 mtm-text-center ui-muted">Loading projects…</div>}

      {!loading && projects.length === 0 && (
        <div className="ui-card mtm-p-10 mtm-text-center ui-muted">
          No projects yet. Create one to start organizing your work.
        </div>
      )}

      <div className="mtm-grid mtm-gap-4 sm:mtm-grid-cols-2 lg:mtm-grid-cols-3">
        {projects.map((p) => (
          <div key={p.id} className="ui-card mtm-p-5 mtm-flex mtm-flex-col mtm-cursor-pointer hover:mtm-border-primary/50 mtm-transition-colors"
            onClick={() => navigate(`/projects/${p.id}`)}>
            <div className="mtm-flex mtm-items-start mtm-justify-between mtm-mb-3">
              <span className="mtm-inline-flex mtm-items-center mtm-justify-center mtm-h-10 mtm-w-10 mtm-rounded-xl mtm-text-white"
                style={{ backgroundColor: p.color || "#10b981" }}>
                <FiFolder size={18} />
              </span>
              <span className={`ui-chip mtm-text-xs ${p.status === "ARCHIVED" ? "" : "mtm-text-primary"}`}>
                {p.status}
              </span>
            </div>
            <div className="mtm-font-semibold mtm-text-content mtm-text-lg">{p.name}</div>
            {p.description && <div className="ui-muted mtm-text-sm mtm-mt-1 mtm-line-clamp-2">{p.description}</div>}
            <div className="mtm-flex mtm-items-center mtm-justify-between mtm-mt-4">
              <span className="ui-link mtm-text-sm mtm-inline-flex mtm-items-center mtm-gap-1">
                Open <FiArrowRight size={14} />
              </span>
              <button className="mtm-text-muted hover:mtm-text-danger mtm-transition-colors"
                onClick={(e) => { e.stopPropagation(); handleDelete(p); }} aria-label="Delete">
                <FiTrash2 size={16} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Projects;
