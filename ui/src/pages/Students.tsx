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

type StudentRow = {
  id: string;
  email: string;
  totalBookings: number;
  activeBookings: number;
  status: "Active" | "None";
};

export default function Students() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);

  const [bookings, setBookings] = useState<BookingApi[]>([]);
  const [studentsDirectory, setStudentsDirectory] = useState<ParticipantApi[]>([]);
  const [studentUsers, setStudentUsers] = useState<UserApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isDeletingEmail, setIsDeletingEmail] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);
  const [newStudentEmail, setNewStudentEmail] = useState("");
  const [newStudentId, setNewStudentId] = useState("");
  const [isAdding, setIsAdding] = useState(false);
  const [addError, setAddError] = useState<string | null>(null);
  const [addMessage, setAddMessage] = useState<string | null>(null);

  const reload = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [bookingData, participants] = await Promise.all([
        getBookings(),
        getParticipants("STUDENT"),
      ]);
      const users = await getUsers();
      setBookings(bookingData);
      setStudentsDirectory(participants);
      setStudentUsers(users.filter((user) => user.role === "STUDENT"));
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? "Failed to load students");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void reload();
  }, []);

  const students = useMemo<StudentRow[]>(() => {
    const byEmail = new Map<string, BookingApi[]>();

    for (const booking of bookings) {
      if (booking.bookedByIdentity !== "STUDENT") continue;
      const key = booking.bookedBy || "unknown@tus.ie";
      const list = byEmail.get(key) ?? [];
      list.push(booking);
      byEmail.set(key, list);
    }

    const allEmails = new Set<string>(studentUsers.map((item) => item.email));
    for (const email of studentsDirectory.map((item) => item.email)) allEmails.add(email);
    for (const email of byEmail.keys()) allEmails.add(email);
    const participantIdByEmail = new Map(
      studentsDirectory.map((item) => [item.email, item.participantId] as const)
    );
    const userIdByEmail = new Map(
      studentUsers.map((item) => [item.email, item.userId] as const)
    );

    return Array.from(allEmails)
      .sort((a, b) => a.localeCompare(b))
      .map((email, index) => {
        const items = byEmail.get(email) ?? [];
        const activeBookings = items.filter((item) => item.status === "CONFIRMED").length;
        return {
          id: userIdByEmail.get(email) ?? participantIdByEmail.get(email) ?? `ST-${index + 1}`,
          email,
          totalBookings: items.length,
          activeBookings,
          status: activeBookings > 0 ? "Active" : "None",
        };
      });
  }, [bookings, studentsDirectory, studentUsers]);

  const onAddStudent = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAddError(null);
    setAddMessage(null);

    const participantId = newStudentId.trim().toUpperCase();
    const email = newStudentEmail.trim().toLowerCase();
    if (!participantId) {
      setAddError("Student ID is required.");
      return;
    }
    if (!email) {
      setAddError("Student email is required.");
      return;
    }

    try {
      setIsAdding(true);
      await addParticipant(participantId, email, "STUDENT");
      setAddMessage("Student added successfully.");
      setNewStudentId("");
      setNewStudentEmail("");
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setAddError(backendMessage ?? e?.message ?? "Failed to add student");
    } finally {
      setIsAdding(false);
    }
  };

  const onDeleteStudent = async (email: string) => {
    if (!isAdmin) return;
    setDeleteError(null);
    setDeleteMessage(null);

    const confirmed = window.confirm(`Delete student ${email} and all related bookings?`);
    if (!confirmed) return;

    try {
      setIsDeletingEmail(email);
      await deleteParticipantBookings(email, "STUDENT");
      setDeleteMessage(`Deleted student ${email}.`);
      await reload();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string" ? e.response.data : e?.response?.data?.message;
      setDeleteError(backendMessage ?? e?.message ?? "Failed to delete student");
    } finally {
      setIsDeletingEmail(null);
    }
  };

  return (
    <div className="page-wrap">
      <PageHeader
        title="Students"
        subtitle="Student directory with booking activity and admin actions."
      />

      {isAdmin ? (
        <section className="panel">
          <h2>Add Student</h2>
          <form className="entity-form" onSubmit={onAddStudent}>
            <label>
              Student ID
              <input
                value={newStudentId}
                onChange={(e) => setNewStudentId(e.target.value)}
                placeholder="e.g. STD-001"
              />
            </label>

            <label>
              Student Email
              <input
                value={newStudentEmail}
                onChange={(e) => setNewStudentEmail(e.target.value)}
                placeholder="e.g. student@tus.ie"
              />
            </label>

            <button className="primary-btn" type="submit" disabled={isAdding}>
              {isAdding ? "Adding..." : "Add Student"}
            </button>
          </form>

          {addMessage ? <p className="panel-success">{addMessage}</p> : null}
          {addError ? <p className="panel-error">{addError}</p> : null}
        </section>
      ) : null}

      <section className="panel">
        {isLoading ? <p className="panel-message">Loading students...</p> : null}
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
                {students.map((student) => (
                  <tr key={student.id}>
                    <td>{student.id}</td>
                    <td>{student.email}</td>
                    <td>{student.totalBookings}</td>
                    <td>{student.activeBookings}</td>
                    <td>
                      <span className={`badge badge-${student.status.toLowerCase()}`}>{student.status}</span>
                    </td>
                    <td>
                      {isAdmin ? (
                        <button
                          type="button"
                          className="danger-btn"
                          onClick={() => onDeleteStudent(student.email)}
                          disabled={isDeletingEmail === student.email}
                        >
                          {isDeletingEmail === student.email ? "Deleting..." : "Delete"}
                        </button>
                      ) : (
                        "-"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {students.length === 0 ? <p className="panel-message">No student records found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
