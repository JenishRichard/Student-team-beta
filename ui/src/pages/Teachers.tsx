import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import PageHeader from "../components/PageHeader";
import {
  addParticipant,
  deleteParticipantBookings,
  getBookings,
  getParticipants,
  type BookingApi,
  type ParticipantApi,
} from "../services/booking.service";
import { getUsers, type UserApi } from "../services/auth.service";
import { useAuth } from "../store/auth";
import { isAdminRole } from "../types/roles";

type TeacherRow = {
  id: string;
  email: string;
  totalBookings: number;
  activeBookings: number;
  status: "Active" | "None";
};

export default function Teachers() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);

  const [bookings, setBookings] = useState<BookingApi[]>([]);
  const [teachersDirectory, setTeachersDirectory] = useState<ParticipantApi[]>([]);
  const [teacherUsers, setTeacherUsers] = useState<UserApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDeletingEmail, setIsDeletingEmail] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);
  const [newTeacherEmail, setNewTeacherEmail] = useState("");
  const [newTeacherId, setNewTeacherId] = useState("");
  const [isAdding, setIsAdding] = useState(false);
  const [addError, setAddError] = useState<string | null>(null);
  const [addMessage, setAddMessage] = useState<string | null>(null);

  const reload = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [bookingData, participants] = await Promise.all([
        getBookings(),
        getParticipants("TEACHER"),
      ]);
      const users = await getUsers();
      setBookings(bookingData);
      setTeachersDirectory(participants);
      setTeacherUsers(users.filter((user) => user.role === "TEACHER"));
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? "Failed to load teachers");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void reload();
  }, []);

  const teachers = useMemo<TeacherRow[]>(() => {
    const byEmail = new Map<string, BookingApi[]>();

    for (const booking of bookings) {
      if (booking.bookedByIdentity !== "TEACHER") continue;
      const key = booking.bookedBy || "unknown@tus.ie";
      const list = byEmail.get(key) ?? [];
      list.push(booking);
      byEmail.set(key, list);
    }

    const allEmails = new Set<string>(teacherUsers.map((item) => item.email));
    for (const email of teachersDirectory.map((item) => item.email)) allEmails.add(email);
    for (const email of byEmail.keys()) allEmails.add(email);
    const participantIdByEmail = new Map(
      teachersDirectory.map((item) => [item.email, item.participantId] as const)
    );
    const userIdByEmail = new Map(
      teacherUsers.map((item) => [item.email, item.userId] as const)
    );

    return Array.from(allEmails)
      .sort((a, b) => a.localeCompare(b))
      .map((email, index) => {
        const items = byEmail.get(email) ?? [];
        const activeBookings = items.filter((item) => item.status === "CONFIRMED").length;
        return {
          id: userIdByEmail.get(email) ?? participantIdByEmail.get(email) ?? `TC-${index + 1}`,
          email,
          totalBookings: items.length,
          activeBookings,
          status: activeBookings > 0 ? "Active" : "None",
        };
      });
  }, [bookings, teachersDirectory, teacherUsers]);

  const onAddTeacher = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAddError(null);
    setAddMessage(null);

    const participantId = newTeacherId.trim().toUpperCase();
    const email = newTeacherEmail.trim().toLowerCase();
    if (!participantId) {
      setAddError("Teacher ID is required.");
      return;
    }
    if (!email) {
      setAddError("Teacher email is required.");
      return;
    }

    try {
      setIsAdding(true);
      await addParticipant(participantId, email, "TEACHER");
      setAddMessage("Teacher added successfully.");
      setNewTeacherId("");
      setNewTeacherEmail("");
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setAddError(backendMessage ?? e?.message ?? "Failed to add teacher");
    } finally {
      setIsAdding(false);
    }
  };

  const onDeleteTeacher = async (email: string) => {
    if (!isAdmin) return;
    setDeleteError(null);
    setDeleteMessage(null);

    const confirmed = window.confirm(`Delete teacher ${email} and all related bookings?`);
    if (!confirmed) return;

    try {
      setIsDeletingEmail(email);
      await deleteParticipantBookings(email, "TEACHER");
      setDeleteMessage(`Deleted teacher ${email}.`);
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setDeleteError(backendMessage ?? e?.message ?? "Failed to delete teacher");
    } finally {
      setIsDeletingEmail(null);
    }
  };

  return (
    <div className="page-wrap">
      <PageHeader
        title="Teachers"
        subtitle="Teacher directory with booking activity and admin actions."
      />

      {isAdmin ? (
        <section className="panel">
          <h2>Add Teacher</h2>
          <form className="entity-form" onSubmit={onAddTeacher}>
            <label>
              Teacher ID
              <input
                value={newTeacherId}
                onChange={(e) => setNewTeacherId(e.target.value)}
                placeholder="e.g. TCH-001"
              />
            </label>

            <label>
              Teacher Email
              <input
                value={newTeacherEmail}
                onChange={(e) => setNewTeacherEmail(e.target.value)}
                placeholder="e.g. teacher@tus.ie"
              />
            </label>

            <button className="primary-btn" type="submit" disabled={isAdding}>
              {isAdding ? "Adding..." : "Add Teacher"}
            </button>
          </form>

          {addMessage ? <p className="panel-success">{addMessage}</p> : null}
          {addError ? <p className="panel-error">{addError}</p> : null}
        </section>
      ) : null}

      <section className="panel">
        {isLoading ? <p className="panel-message">Loading teachers...</p> : null}
        {error ? <p className="panel-error">{error}</p> : null}
        {deleteMessage ? <p className="panel-success">{deleteMessage}</p> : null}
        {deleteError ? <p className="panel-error">{deleteError}</p> : null}

        {!isLoading && !error ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Teacher / Student Email</th>
                  <th>Total Bookings</th>
                  <th>Active Booking</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {teachers.map((teacher) => (
                  <tr key={teacher.id}>
                    <td>{teacher.id}</td>
                    <td>{teacher.email}</td>
                    <td>{teacher.totalBookings}</td>
                    <td>{teacher.activeBookings}</td>
                    <td>
                      <span className={`badge badge-${teacher.status.toLowerCase()}`}>{teacher.status}</span>
                    </td>
                    <td>
                      {isAdmin ? (
                        <button
                          type="button"
                          className="danger-btn"
                          onClick={() => onDeleteTeacher(teacher.email)}
                          disabled={isDeletingEmail === teacher.email}
                        >
                          {isDeletingEmail === teacher.email ? "Deleting..." : "Delete"}
                        </button>
                      ) : (
                        "-"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {teachers.length === 0 ? <p className="panel-message">No teacher records found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
