import React, { useCallback, useEffect, useState } from "react";
import Button from "react-bootstrap/Button";
import {
  createRole,
  deleteRole,
  listRoles,
  updateRole,
} from "../service/ApiService";

const errorMessageFrom = (errorPayload) => {
  if (!errorPayload) return "Something went wrong.";
  if (errorPayload.error && errorPayload.error.message)
    return errorPayload.error.message;
  if (errorPayload.message) return errorPayload.message;
  return "Something went wrong.";
};

const extractRoles = (data) => {
  // GenericMessageResponseDTO wraps the payload under `data`.
  const payload = (data && data.data) || data || [];
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload.content)) return payload.content;
  return [];
};

function Admin({ toastState, setToastState }) {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newRoleName, setNewRoleName] = useState("");
  const [editing, setEditing] = useState({ id: null, name: "" });

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

  const load = useCallback(() => {
    setLoading(true);
    listRoles((data, err) => {
      setLoading(false);
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      setRoles(extractRoles(data));
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleCreate = (e) => {
    e.preventDefault();
    if (!newRoleName.trim()) {
      showError("Role name is required.");
      return;
    }
    createRole({ roleName: newRoleName.trim() }, (data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Role created.");
      setNewRoleName("");
      load();
    });
  };

  const handleSaveRename = (e) => {
    e.preventDefault();
    if (!editing.id) return;
    if (!editing.name.trim()) {
      showError("Role name is required.");
      return;
    }
    updateRole(
      { roleId: editing.id, roleName: editing.name.trim() },
      (data, err) => {
        if (err) {
          showError(errorMessageFrom(err));
          return;
        }
        showSuccess("Role updated.");
        setEditing({ id: null, name: "" });
        load();
      }
    );
  };

  const handleDelete = (role) => {
    if (
      !window.confirm(`Soft-delete role "${role.name}"? Users with this role will lose it.`)
    )
      return;
    deleteRole(role.id, (data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      showSuccess("Role removed.");
      load();
    });
  };

  return (
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-3xl mtm-mx-auto">
        <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400 mtm-mb-8">
          Admin · Roles
        </h1>

        <form
          onSubmit={handleCreate}
          className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-6 mtm-mb-8 mtm-flex mtm-flex-col sm:mtm-flex-row mtm-gap-3 sm:mtm-items-end"
        >
          <label className="mtm-flex mtm-flex-col mtm-flex-1">
            <span className="mtm-text-sm mtm-text-white/70 mtm-mb-1">
              New role name
            </span>
            <input
              type="text"
              value={newRoleName}
              placeholder="MTM_NEW_ROLE"
              onChange={(e) => setNewRoleName(e.target.value)}
              className="mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
            />
          </label>
          <Button type="submit" variant="warning">
            Create role
          </Button>
        </form>

        {loading && <p className="mtm-text-white/60">Loading roles…</p>}
        {!loading && roles.length === 0 && (
          <p className="mtm-text-white/60">No roles defined.</p>
        )}

        <ul className="mtm-space-y-3">
          {roles.map((role) => (
            <li
              key={role.id}
              className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3"
            >
              {editing.id === role.id ? (
                <form
                  onSubmit={handleSaveRename}
                  className="mtm-flex mtm-flex-1 mtm-gap-2"
                >
                  <input
                    type="text"
                    value={editing.name}
                    onChange={(e) =>
                      setEditing((s) => ({ ...s, name: e.target.value }))
                    }
                    className="mtm-flex-1 mtm-bg-black mtm-border mtm-border-white/30 mtm-rounded mtm-px-3 mtm-py-2"
                  />
                  <Button type="submit" size="sm" variant="warning">
                    Save
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="outline-light"
                    onClick={() => setEditing({ id: null, name: "" })}
                  >
                    Cancel
                  </Button>
                </form>
              ) : (
                <>
                  <div>
                    <div className="mtm-text-lg mtm-text-white">{role.name}</div>
                    <div className="mtm-text-xs mtm-text-white/50">{role.id}</div>
                  </div>
                  <div className="mtm-flex mtm-gap-2">
                    <Button
                      size="sm"
                      variant="outline-warning"
                      onClick={() =>
                        setEditing({ id: role.id, name: role.name })
                      }
                    >
                      Rename
                    </Button>
                    <Button
                      size="sm"
                      variant="outline-danger"
                      onClick={() => handleDelete(role)}
                    >
                      Delete
                    </Button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default Admin;
