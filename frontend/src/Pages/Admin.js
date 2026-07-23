import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  FiPlus,
  FiEdit2,
  FiTrash2,
  FiChevronLeft,
  FiChevronRight,
  FiShield,
  FiUsers,
} from "react-icons/fi";
import {
  addRolesToUsers,
  createRole,
  deleteRole,
  listRoles,
  listUsers,
  removeRolesFromUsers,
  updateRole,
} from "../service/ApiService";
import { useConfirm } from "../components/ConfirmProvider";

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
// is what the backend stores and matches against, so we always send the raw
// name on the wire even though we render the stripped form.
const displayRoleName = (name = "") =>
  name.startsWith("ROLE_") ? name.slice("ROLE_".length) : name;

function RolesTab({ showSuccess, showError }) {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newRoleName, setNewRoleName] = useState("");
  const [editing, setEditing] = useState({ id: null, name: "" });
  const confirm = useConfirm();

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

  const handleDelete = async (role) => {
    const ok = await confirm({
      title: "Delete role?",
      message: `Soft-delete "${role.name}"? Users with this role will lose it.`,
    });
    if (!ok) return;
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
        className="ui-card mtm-p-5 mtm-mb-6 mtm-flex mtm-flex-col sm:mtm-flex-row mtm-gap-3 sm:mtm-items-end"
      >
        <div className="mtm-flex-1">
          <label className="ui-label" htmlFor="newRoleName">
            New role name
          </label>
          <input
            id="newRoleName"
            type="text"
            value={newRoleName}
            placeholder="MTM_NEW_ROLE"
            onChange={(e) => setNewRoleName(e.target.value)}
            className="ui-input"
          />
        </div>
        <button type="submit" className="ui-btn ui-btn-primary">
          <FiPlus size={16} /> Create role
        </button>
      </form>

      {loading && (
        <div className="ui-card mtm-p-8 mtm-text-center ui-muted">Loading roles…</div>
      )}
      {!loading && roles.length === 0 && (
        <div className="ui-card mtm-p-8 mtm-text-center ui-muted">No roles defined.</div>
      )}

      <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
        {roles.map((role) => (
          <li
            key={role.id}
            className="ui-card-flat mtm-p-4 mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-center mtm-gap-3"
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
                  className="ui-input mtm-flex-1"
                />
                <button type="submit" className="ui-btn ui-btn-primary ui-btn-sm">
                  Save
                </button>
                <button
                  type="button"
                  className="ui-btn ui-btn-ghost ui-btn-sm"
                  onClick={() => setEditing({ id: null, name: "" })}
                >
                  Cancel
                </button>
              </form>
            ) : (
              <>
                <div className="mtm-flex mtm-items-center mtm-gap-3">
                  <span className="ui-icon-tile mtm-h-9 mtm-w-9">
                    <FiShield size={15} />
                  </span>
                  <div>
                    <div className="mtm-text-content mtm-font-semibold">
                      {role.name}
                    </div>
                    <div className="mtm-text-xs ui-muted">{role.id}</div>
                  </div>
                </div>
                <div className="mtm-flex mtm-gap-2">
                  <button
                    className="ui-btn ui-btn-ghost ui-btn-sm"
                    onClick={() => setEditing({ id: role.id, name: role.name })}
                  >
                    <FiEdit2 size={14} /> Rename
                  </button>
                  <button
                    className="ui-btn ui-btn-danger ui-btn-sm"
                    onClick={() => handleDelete(role)}
                  >
                    <FiTrash2 size={14} /> Delete
                  </button>
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
  // Bulk mode lets an admin apply a set of role add/remove actions to many
  // users in a single submit. Selection persists across page changes.
  const [bulkMode, setBulkMode] = useState(false);
  const [bulkSelectedUsernames, setBulkSelectedUsernames] = useState(new Set());
  // Map<roleName, "add"|"remove">. Absent = no change for that role.
  const [bulkRoleActions, setBulkRoleActions] = useState(new Map());

  // Load the full role catalogue once so the editor can offer every option.
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
  // only at render time.
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

  const resetBulkState = () => {
    setBulkSelectedUsernames(new Set());
    setBulkRoleActions(new Map());
  };

  const toggleBulkMode = () => {
    setBulkMode((prev) => {
      const next = !prev;
      if (next) {
        // Entering bulk mode — cancel any in-progress per-user edit.
        cancelEdit();
      }
      resetBulkState();
      return next;
    });
  };

  const toggleBulkUser = (username) => {
    setBulkSelectedUsernames((prev) => {
      const next = new Set(prev);
      if (next.has(username)) next.delete(username);
      else next.add(username);
      return next;
    });
  };

  const selectAllOnPage = () => {
    setBulkSelectedUsernames((prev) => {
      const next = new Set(prev);
      users.forEach((u) => {
        if (u.username) next.add(u.username);
      });
      return next;
    });
  };

  const clearBulkSelection = () => setBulkSelectedUsernames(new Set());

  // Cycle: undefined -> "add" -> "remove" -> undefined.
  const cycleBulkRoleAction = (roleName) => {
    setBulkRoleActions((prev) => {
      const next = new Map(prev);
      const current = next.get(roleName);
      if (!current) next.set(roleName, "add");
      else if (current === "add") next.set(roleName, "remove");
      else next.delete(roleName);
      return next;
    });
  };

  const handleBulkApply = async () => {
    const usernames = [...bulkSelectedUsernames];
    if (usernames.length === 0) {
      showError("Select at least one user.");
      return;
    }
    const added = [];
    const removed = [];
    bulkRoleActions.forEach((action, roleName) => {
      if (action === "add") added.push(roleName);
      else if (action === "remove") removed.push(roleName);
    });
    if (added.length === 0 && removed.length === 0) {
      showError("Pick at least one role to add or remove.");
      return;
    }
    setSaving(true);
    const runAdd = () =>
      new Promise((resolve, reject) => {
        if (added.length === 0) {
          resolve();
          return;
        }
        addRolesToUsers({ roleNames: added, usernames }, (data, err) =>
          err ? reject(err) : resolve(data)
        );
      });
    const runRemove = () =>
      new Promise((resolve, reject) => {
        if (removed.length === 0) {
          resolve();
          return;
        }
        removeRolesFromUsers({ roleNames: removed, usernames }, (data, err) =>
          err ? reject(err) : resolve(data)
        );
      });
    try {
      await runAdd();
      await runRemove();
      showSuccess(
        `Updated ${usernames.length} user${usernames.length === 1 ? "" : "s"}.`
      );
      resetBulkState();
      load(pageNumber);
    } catch (err) {
      showError(errorMessageFrom(err));
    } finally {
      setSaving(false);
    }
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

  const bulkApplyDisabled =
    saving || bulkSelectedUsernames.size === 0 || bulkRoleActions.size === 0;

  return (
    <>
      <div className="mtm-flex mtm-flex-wrap mtm-gap-2 mtm-items-center mtm-mb-4">
        <button
          className={`ui-btn ui-btn-sm ${bulkMode ? "ui-btn-primary" : "ui-btn-ghost"}`}
          onClick={toggleBulkMode}
        >
          {bulkMode ? "Exit bulk edit" : "Bulk edit"}
        </button>
        {bulkMode && (
          <>
            <button
              className="ui-btn ui-btn-ghost ui-btn-sm"
              disabled={loading || users.length === 0}
              onClick={selectAllOnPage}
            >
              Select all on page
            </button>
            <button
              className="ui-btn ui-btn-ghost ui-btn-sm"
              disabled={bulkSelectedUsernames.size === 0}
              onClick={clearBulkSelection}
            >
              Clear selection
            </button>
            <span className="mtm-text-sm ui-muted mtm-ml-1">
              {bulkSelectedUsernames.size} selected
            </span>
          </>
        )}
      </div>

      {bulkMode && (
        <div className="ui-card mtm-p-4 mtm-mb-6">
          <div className="mtm-text-sm ui-muted mtm-mb-3">
            Click each role to cycle through{" "}
            <span className="mtm-text-muted">no change</span> →{" "}
            <span className="mtm-text-ok mtm-font-medium">+ add</span> →{" "}
            <span className="mtm-text-danger mtm-font-medium">− remove</span>.
          </div>
          {allRoleNames.length === 0 ? (
            <p className="ui-muted mtm-mb-3">
              No roles defined yet. Create one in the Roles tab first.
            </p>
          ) : (
            <div className="mtm-flex mtm-flex-wrap mtm-gap-2 mtm-mb-4">
              {allRoleNames.map((roleName) => {
                const action = bulkRoleActions.get(roleName);
                const styled =
                  action === "add"
                    ? "mtm-bg-ok/15 mtm-text-ok mtm-border-ok/40"
                    : action === "remove"
                    ? "mtm-bg-danger/15 mtm-text-danger mtm-border-danger/40"
                    : "mtm-bg-surface-2 mtm-text-content mtm-border-line";
                const prefix =
                  action === "add" ? "+ " : action === "remove" ? "− " : "";
                return (
                  <button
                    key={roleName}
                    type="button"
                    onClick={() => cycleBulkRoleAction(roleName)}
                    className={`mtm-text-sm mtm-px-3 mtm-py-1.5 mtm-rounded-lg mtm-border mtm-font-medium mtm-transition-colors ${styled}`}
                  >
                    {prefix}
                    {displayRoleName(roleName)}
                  </button>
                );
              })}
            </div>
          )}
          <div className="mtm-flex mtm-gap-2">
            <button
              className="ui-btn ui-btn-primary ui-btn-sm"
              disabled={bulkApplyDisabled}
              onClick={handleBulkApply}
            >
              {saving ? "Applying…" : "Apply changes"}
            </button>
            <button
              className="ui-btn ui-btn-ghost ui-btn-sm"
              disabled={saving}
              onClick={resetBulkState}
            >
              Reset
            </button>
          </div>
        </div>
      )}

      {loading && (
        <div className="ui-card mtm-p-8 mtm-text-center ui-muted">Loading users…</div>
      )}
      {!loading && users.length === 0 && (
        <div className="ui-card mtm-p-8 mtm-text-center ui-muted">No users found.</div>
      )}

      <ul className="mtm-flex mtm-flex-col mtm-gap-3 mtm-list-none mtm-p-0 mtm-m-0">
        {users.map((user) => {
          const userRoles = user.roles || [];
          const isEditing = editingUid === user.uid;
          const isBulkSelected =
            !!user.username && bulkSelectedUsernames.has(user.username);
          return (
            <li key={user.uid || user.id} className="ui-card-flat mtm-p-4">
              <div className="mtm-flex mtm-flex-col sm:mtm-flex-row sm:mtm-justify-between sm:mtm-items-start mtm-gap-3">
                <div className="mtm-flex mtm-gap-3 mtm-items-start">
                  {bulkMode && (
                    <input
                      type="checkbox"
                      className="mtm-mt-1.5 mtm-h-4 mtm-w-4 mtm-accent-primary"
                      checked={isBulkSelected}
                      disabled={!user.username}
                      onChange={() =>
                        user.username && toggleBulkUser(user.username)
                      }
                    />
                  )}
                  <div>
                    <div className="mtm-text-content mtm-font-semibold">
                      {user.firstName} {user.lastName}{" "}
                      <span className="ui-muted mtm-text-sm mtm-font-normal">
                        @{user.username}
                      </span>
                    </div>
                    <div className="mtm-text-sm ui-muted">{user.email}</div>
                    <div className="mtm-text-xs ui-muted mtm-mt-1">
                      uid: {user.uid}
                    </div>
                    <div className="mtm-flex mtm-flex-wrap mtm-gap-1.5 mtm-mt-2">
                      {userRoles.length === 0 ? (
                        <span className="mtm-text-xs ui-muted">no roles</span>
                      ) : (
                        userRoles.map((r) => (
                          <span key={r} className="ui-chip mtm-text-xs">
                            {displayRoleName(r)}
                          </span>
                        ))
                      )}
                    </div>
                  </div>
                </div>
                {!bulkMode && (
                  <div className="mtm-flex mtm-gap-2 mtm-shrink-0">
                    {!isEditing ? (
                      <button
                        className="ui-btn ui-btn-ghost ui-btn-sm"
                        onClick={() => startEditing(user)}
                      >
                        <FiEdit2 size={14} /> Edit roles
                      </button>
                    ) : (
                      <>
                        <button
                          className="ui-btn ui-btn-primary ui-btn-sm"
                          disabled={saving}
                          onClick={() => handleSaveRoles(user)}
                        >
                          {saving ? "Saving…" : "Save"}
                        </button>
                        <button
                          className="ui-btn ui-btn-ghost ui-btn-sm"
                          disabled={saving}
                          onClick={cancelEdit}
                        >
                          Cancel
                        </button>
                      </>
                    )}
                  </div>
                )}
              </div>

              {!bulkMode && isEditing && (
                <div className="mtm-mt-4 mtm-pt-4 mtm-border-t mtm-border-line mtm-grid sm:mtm-grid-cols-2 mtm-gap-2">
                  {allRoleNames.length === 0 ? (
                    <p className="ui-muted">
                      No roles defined yet. Create one in the Roles tab first.
                    </p>
                  ) : (
                    allRoleNames.map((roleName) => (
                      <label
                        key={roleName}
                        className="mtm-flex mtm-items-center mtm-gap-2 mtm-text-sm mtm-text-content"
                      >
                        <input
                          type="checkbox"
                          className="mtm-h-4 mtm-w-4 mtm-accent-primary"
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
          <button
            className="ui-btn ui-btn-ghost ui-btn-sm"
            disabled={pageNumber === 0 || loading}
            onClick={() => load(pageNumber - 1)}
          >
            <FiChevronLeft size={16} /> Prev
          </button>
          <span className="ui-muted mtm-text-sm mtm-min-w-[92px] mtm-text-center">
            Page {pageNumber + 1} of {totalPages}
          </span>
          <button
            className="ui-btn ui-btn-ghost ui-btn-sm"
            disabled={pageNumber >= totalPages - 1 || loading}
            onClick={() => load(pageNumber + 1)}
          >
            Next <FiChevronRight size={16} />
          </button>
        </div>
      )}
    </>
  );
}

function Admin({ setToastState }) {
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

  const tabs = [
    { id: "roles", label: "Roles", icon: <FiShield size={15} /> },
    { id: "users", label: "Users", icon: <FiUsers size={15} /> },
  ];

  return (
    <div className="ui-page ui-fade-in mtm-max-w-3xl">
      <p className="ui-eyebrow">Administration</p>
      <h1 className="mtm-text-3xl mtm-font-display mtm-font-bold mtm-text-content mtm-mt-1 mtm-mb-6">
        Admin
      </h1>

      <div className="mtm-inline-flex mtm-p-1 mtm-rounded-xl mtm-bg-surface-2 mtm-border mtm-border-line mtm-mb-6">
        {tabs.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`mtm-inline-flex mtm-items-center mtm-gap-2 mtm-px-4 mtm-py-1.5 mtm-rounded-lg mtm-text-sm mtm-font-semibold mtm-transition-colors ${
              tab === t.id
                ? "mtm-bg-surface mtm-text-primary mtm-shadow-sm"
                : "mtm-text-muted hover:mtm-text-content"
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {tab === "roles" ? (
        <RolesTab showSuccess={showSuccess} showError={showError} />
      ) : (
        <UsersTab showSuccess={showSuccess} showError={showError} />
      )}
    </div>
  );
}

export default Admin;
