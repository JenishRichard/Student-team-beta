import { useCallback, useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import PageHeader from "../components/PageHeader";
import {
  createUser,
  deleteUser,
  getUsers,
  updateUser,
  type UserApi,
} from "../services/auth.service";
import { APP_ROLES, roleLabel, type AppRole } from "../types/roles";

type UserStatusUi = "ACTIVE" | "INVITED";

export default function AdminUsers() {
  const [users, setUsers] = useState<UserApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [userId, setUserId] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<AppRole>("ADMIN");
  const [isCreating, setIsCreating] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const [editingUserId, setEditingUserId] = useState<string | null>(null);
  const [editRole, setEditRole] = useState<AppRole>("ADMIN");
  const [editStatus, setEditStatus] = useState<UserStatusUi>("ACTIVE");
  const [isSavingId, setIsSavingId] = useState<string | null>(null);
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null);

  const reload = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await getUsers();
      setUsers(data);
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setError(backendMessage ?? e?.message ?? "Failed to load users");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const sortedUsers = useMemo(() => {
    return [...users].sort((a, b) => a.userId.localeCompare(b.userId));
  }, [users]);

  const onAddUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setMessage(null);

    const normalizedId = userId.trim().toUpperCase();
    const normalizedEmail = email.trim().toLowerCase();
    if (!normalizedId) {
      setError("User ID is required.");
      return;
    }
    if (!normalizedEmail) {
      setError("User email is required.");
      return;
    }
    if (!password.trim()) {
      setError("Password is required.");
      return;
    }

    try {
      setIsCreating(true);
      await createUser({
        userId: normalizedId,
        email: normalizedEmail,
        password: password.trim(),
        roles: [role],
      });
      setMessage(`User ${normalizedId} (${normalizedEmail}) created successfully.`);
      setUserId("");
      setEmail("");
      setPassword("");
      setRole("ADMIN");
      setShowForm(false);
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setError(backendMessage ?? e?.message ?? "Failed to add user");
    } finally {
      setIsCreating(false);
    }
  };

  const onStartEdit = (row: UserApi) => {
    setEditingUserId(row.userId);
    setEditRole(row.role);
    setEditStatus(row.status);
    setError(null);
    setMessage(null);
  };

  const onSaveEdit = async (userIdValue: string) => {
    setError(null);
    setMessage(null);
    try {
      setIsSavingId(userIdValue);
      await updateUser(userIdValue, { role: editRole, status: editStatus });
      setMessage(`User ${userIdValue} updated.`);
      setEditingUserId(null);
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setError(backendMessage ?? e?.message ?? "Failed to update user");
    } finally {
      setIsSavingId(null);
    }
  };

  const onDeleteUser = async (userIdValue: string) => {
    setError(null);
    setMessage(null);
    const confirmed = window.confirm(`Delete user ${userIdValue}?`);
    if (!confirmed) return;
    try {
      setIsDeletingId(userIdValue);
      await deleteUser(userIdValue);
      setMessage(`User ${userIdValue} deleted.`);
      if (editingUserId === userIdValue) {
        setEditingUserId(null);
      }
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setError(backendMessage ?? e?.message ?? "Failed to delete user");
    } finally {
      setIsDeletingId(null);
    }
  };

  return (
    <div className="page-wrap">
      <PageHeader
        title="Users"
        subtitle="Create and manage platform users."
        action={
          <button className="primary-btn" onClick={() => setShowForm((prev) => !prev)} type="button">
            {showForm ? "Cancel" : "Add User"}
          </button>
        }
      />

      {showForm ? (
        <section className="panel">
          <h2>Create User</h2>
          <form className="entity-form" onSubmit={onAddUser}>
            <label>
              User ID
              <input
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="e.g. AD-10"
              />
            </label>

            <label>
              User Email
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. admin@tus.ie"
              />
            </label>

            <label>
              Password
              <input
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                type="password"
              />
            </label>

            <label>
              Role
              <select value={role} onChange={(e) => setRole(e.target.value as AppRole)}>
                {APP_ROLES.map((item) => (
                  <option key={item} value={item}>
                    {roleLabel(item)}
                  </option>
                ))}
              </select>
            </label>

            <button className="primary-btn" type="submit" disabled={isCreating}>
              {isCreating ? "Creating..." : "Create User"}
            </button>
          </form>
        </section>
      ) : null}

      <section className="panel">
        {isLoading ? <p className="panel-message">Loading users...</p> : null}
        {message ? <p className="panel-success">{message}</p> : null}
        {error ? <p className="panel-error">{error}</p> : null}

        {!isLoading ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {sortedUsers.map((row) => {
                  const isEditing = editingUserId === row.userId;
                  return (
                    <tr key={row.userId}>
                      <td>{row.userId}</td>
                      <td>{row.email}</td>
                      <td>
                        {isEditing ? (
                          <select value={editRole} onChange={(e) => setEditRole(e.target.value as AppRole)}>
                            {APP_ROLES.map((item) => (
                              <option key={`edit-role-${row.userId}-${item}`} value={item}>
                                {roleLabel(item)}
                              </option>
                            ))}
                          </select>
                        ) : (
                          roleLabel(row.role)
                        )}
                      </td>
                      <td>
                        {isEditing ? (
                          <select
                            value={editStatus}
                            onChange={(e) => setEditStatus(e.target.value as UserStatusUi)}
                          >
                            <option value="ACTIVE">Active</option>
                            <option value="INVITED">Invited</option>
                          </select>
                        ) : (
                          <span className={`badge badge-${row.status.toLowerCase()}`}>
                            {row.status === "ACTIVE" ? "Active" : "Invited"}
                          </span>
                        )}
                      </td>
                      <td>
                        {isEditing ? (
                          <>
                            <button
                              type="button"
                              className="secondary-btn"
                              onClick={() => onSaveEdit(row.userId)}
                              disabled={isSavingId === row.userId}
                            >
                              {isSavingId === row.userId ? "Saving..." : "Save"}
                            </button>
                            <button
                              type="button"
                              className="secondary-btn"
                              onClick={() => setEditingUserId(null)}
                              style={{ marginLeft: 8 }}
                            >
                              Cancel
                            </button>
                          </>
                        ) : (
                          <>
                            <button
                              type="button"
                              className="secondary-btn"
                              onClick={() => onStartEdit(row)}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="danger-btn"
                              onClick={() => onDeleteUser(row.userId)}
                              disabled={isDeletingId === row.userId}
                              style={{ marginLeft: 8 }}
                            >
                              {isDeletingId === row.userId ? "Deleting..." : "Delete"}
                            </button>
                          </>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {sortedUsers.length === 0 ? <p className="panel-message">No users found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
