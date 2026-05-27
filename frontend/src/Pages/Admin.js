import React, { useCallback, useEffect, useMemo, useState } from "react";
import Button from "react-bootstrap/Button";
import {
  addRolesToUsers,
  createRole,
  deleteRole,
  listRoles,
  listUsers,
  removeRolesFromUsers,
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

const extractUsersPage = (data) => {
  // PaginationResultResponseDTO is wrapped in GenericMessageResponseDTO.data.
  const payload = (data && data.data) || {};
  return {
    users: Array.isArray(payload.payload) ? payload.payload : [],
    pageNumber: payload.pageNumber ?? 0,
    totalPages: payload.totalPages ?? 1,
  };
};

// Cosmetic only — strip the Spring "ROLE_" prefix for display. The raw name
// is what the backend stores, and what /user/addRole + /user/removeRole match
// against, so we always send the raw name on the wire even though we render
// the stripped form.
const displayRoleName = (name = "") =>
  name.startsWith("ROLE_") ? name.slice("ROLE_".length) : name;

function RolesTab({ showSuccess, showError }) {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newRoleName, setNewRoleName] = useState("");
  const [editing, setEditing] = useState({ id: null, name: "" });

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
  }, [showError]);

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
      !window.confirm(
        `Soft-delete role "${role.name}"? Users with this role will lose it.`
      )
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
    <>
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
                    onClick={() => setEditing({ id: role.id, name: role.name })}
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
    </>
  );
}

function UsersTab({ showSuccess, showError }) {
  const [users, setUsers] = useState([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [allRoles, setAllRoles] = useState([]);
  const [editingUid, setEditingUid] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState(new Set());
  const [originalRoles, setOriginalRoles] = useState(new Set());
  const [saving, setSaving] = useState(false);

  // Load the full role catalogue once so the editor can offer every option,
  // not just whatever the user already has.
  useEffect(() => {
    listRoles((data, err) => {
      if (err) {
        showError(errorMessageFrom(err));
        return;
      }
      setAllRoles(extractRoles(data));
    });
  }, [showError]);

  const load = useCallback(
    (nextPage = 0) => {
      setLoading(true);
      listUsers({ page: nextPage, size: 20 }, (data, err) => {
        setLoading(false);
        if (err) {
          showError(errorMessageFrom(err));
          return;
        }
        const page = extractUsersPage(data);
        setUsers(page.users);
        setPageNumber(page.pageNumber);
        setTotalPages(page.totalPages || 1);
      });
    },
    [showError]
  );

  useEffect(() => {
    load(0);
  }, [load]);

  // Use the raw role name as the canonical key. displayRoleName() is applied
  // only at render time — see chips + checkbox labels below.
  const allRoleNames = useMemo(
    () =>
      allRoles
        .map((r) => r.name)
        .filter(Boolean)
        .sort(),
    [allRoles]
  );

  const startEditing = (user) => {
    setEditingUid(user.uid);
    const current = new Set(user.roles || []);
    setSelectedRoles(current);
    setOriginalRoles(new Set(current));
  };

  const toggleRole = (roleName) => {
    setSelectedRoles((prev) => {
      const next = new Set(prev);
      if (next.has(roleName)) next.delete(roleName);
      else next.add(roleName);
      return next;
    });
  };

  const cancelEdit = () => {
    setEditingUid(null);
    setSelectedRoles(new Set());
    setOriginalRoles(new Set());
  };

  const handleSaveRoles = async (user) => {
    const added = [...selectedRoles].filter((r) => !originalRoles.has(r));
    const removed = [...originalRoles].filter((r) => !selectedRoles.has(r));
    if (added.length === 0 && removed.length === 0) {
      showError("No role changes to save.");
      return;
    }
    setSaving(true);
    const runAdd = () =>
      new Promise((resolve, reject) => {
        if (added.length === 0) {
          resolve();
          return;
        }
        addRolesToUsers(
          { roleNames: added, usernames: [user.username] },
          (data, err) => (err ? reject(err) : resolve(data))
        );
      });
    const runRemove = () =>
      new Promise((resolve, reject) => {
        if (removed.length === 0) {
          resolve();
          return;
        }
        removeRolesFromUsers(
          { roleNames: removed, usernames: [user.username] },
          (data, err) => (err ? reject(err) : resolve(data))
        );
      });
    try {
      // Sequential — keeps error messages clearly tied to the failing op.
      await runAdd();
      await runRemove();
      showSuccess("Roles updated.");
      cancelEdit();
      load(pageNumber);
    } catch (err) {
      showError(errorMessageFrom(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      {loading && <p className="mtm-text-white/60">Loading users…</p>}
      {!loading && users.length === 0 && (
        <p className="mtm-text-white/60">No users found.</p>
      )}

      <ul className="mtm-space-y-3">
        {users.map((user) => {
          const userRoles = user.roles || [];
          const isEditing = editingUid === user.uid;
          return (
            <li
              key={user.uid || user.id}
              className="mtm-bg-white/5 mtm-border mtm-border-white/20 mtm-rounded mtm-p-4"
            >
              <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-start mtm-gap-3">
                <div>
                  <div className="mtm-text-lg mtm-text-white">
                    {user.firstName} {user.lastName}{" "}
                    <span className="mtm-text-white/40 mtm-text-sm">
                      @{user.username}
                    </span>
                  </div>
                  <div className="mtm-text-sm mtm-text-white/60">
                    {user.email}
                  </div>
                  <div className="mtm-text-xs mtm-text-white/40 mtm-mt-1">
                    uid: {user.uid}
                  </div>
                  <div className="mtm-flex mtm-flex-wrap mtm-gap-1 mtm-mt-2">
                    {userRoles.length === 0 ? (
                      <span className="mtm-text-xs mtm-text-white/40">
                        no roles
                      </span>
                    ) : (
                      userRoles.map((r) => (
                        <span
                          key={r}
                          className="mtm-text-xs mtm-px-2 mtm-py-0.5 mtm-rounded mtm-bg-sky-400/20 mtm-text-sky-200 mtm-border mtm-border-sky-400/40"
                        >
                          {displayRoleName(r)}
                        </span>
                      ))
                    )}
                  </div>
                </div>
                <div className="mtm-flex mtm-gap-2">
                  {!isEditing ? (
                    <Button
                      size="sm"
                      variant="outline-warning"
                      onClick={() => startEditing(user)}
                    >
                      Edit roles
                    </Button>
                  ) : (
                    <>
                      <Button
                        size="sm"
                        variant="warning"
                        disabled={saving}
                        onClick={() => handleSaveRoles(user)}
                      >
                        {saving ? "Saving…" : "Save"}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline-light"
                        disabled={saving}
                        onClick={cancelEdit}
                      >
                        Cancel
                      </Button>
                    </>
                  )}
                </div>
              </div>

              {isEditing && (
                <div className="mtm-mt-4 mtm-grid sm:mtm-grid-cols-2 mtm-gap-2">
                  {allRoleNames.length === 0 ? (
                    <p className="mtm-text-white/40">
                      No roles defined yet. Create one in the Roles tab first.
                    </p>
                  ) : (
                    allRoleNames.map((roleName) => (
                      <label
                        key={roleName}
                        className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-sm mtm-text-white/80"
                      >
                        <input
                          type="checkbox"
                          checked={selectedRoles.has(roleName)}
                          onChange={() => toggleRole(roleName)}
                        />
                        <span>{displayRoleName(roleName)}</span>
                      </label>
                    ))
                  )}
                </div>
              )}
            </li>
          );
        })}
      </ul>

      {totalPages > 1 && (
        <div className="mtm-flex mtm-gap-2 mtm-justify-center mtm-items-center mtm-mt-6">
          <Button
            size="sm"
            variant="outline-light"
            disabled={pageNumber === 0 || loading}
            onClick={() => load(pageNumber - 1)}
          >
            Prev
          </Button>
          <span className="mtm-text-white/60 mtm-text-sm">
            Page {pageNumber + 1} of {totalPages}
          </span>
          <Button
            size="sm"
            variant="outline-light"
            disabled={pageNumber >= totalPages - 1 || loading}
            onClick={() => load(pageNumber + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </>
  );
}

function Admin({ toastState, setToastState }) {
  const [tab, setTab] = useState("roles");

  const showSuccess = useCallback(
    (message) =>
      setToastState({
        display: true,
        variant: "success",
        messages: [message],
        includePrefix: false,
        includeSuffix: false,
        suffix: "",
      }),
    [setToastState]
  );

  const showError = useCallback(
    (messages) =>
      setToastState({
        display: true,
        variant: "error",
        messages: Array.isArray(messages) ? messages : [messages],
        includePrefix: true,
        includeSuffix: false,
        suffix: "",
      }),
    [setToastState]
  );

  return (
    <div className="mtm-min-h-[80vh] mtm-bg-black mtm-text-white mtm-py-12 mtm-px-4 sm:mtm-px-12">
      <div className="mtm-max-w-3xl mtm-mx-auto">
        <h1 className="mtm-text-3xl mtm-tracking-wider mtm-text-sky-400 mtm-mb-6">
          Admin
        </h1>
        <div className="mtm-flex mtm-gap-2 mtm-mb-6">
          <Button
            size="sm"
            variant={tab === "roles" ? "warning" : "outline-light"}
            onClick={() => setTab("roles")}
          >
            Roles
          </Button>
          <Button
            size="sm"
            variant={tab === "users" ? "warning" : "outline-light"}
            onClick={() => setTab("users")}
          >
            Users
          </Button>
        </div>

        {tab === "roles" ? (
          <RolesTab showSuccess={showSuccess} showError={showError} />
        ) : (
          <UsersTab showSuccess={showSuccess} showError={showError} />
        )}
      </div>
    </div>
  );
}

export default Admin;
