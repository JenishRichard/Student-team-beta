import { useCallback, useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import PageHeader from "../components/PageHeader";
import {
  cancelBooking,
  createBooking,
  deleteBooking,
  getBookings,
  type BookingApi,
  type BookingIdentityApi,
} from "../services/booking.service";
import { getRooms, type RoomApi } from "../services/room.service";
import { useAuth } from "../store/auth";
import { isAdminRole, normalizeRole } from "../types/roles";

const TIME_OPTIONS = ["09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

export default function Bookings() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);
  const userRole = normalizeRole(user?.role);
  const currentUserEmail = user?.username?.trim().toLowerCase() ?? "";

  const [bookings, setBookings] = useState<BookingApi[]>([]);
  const [rooms, setRooms] = useState<RoomApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [roomId, setRoomId] = useState("");
  const [bookedBy, setBookedBy] = useState("");
  const [bookedByIdentity, setBookedByIdentity] = useState<BookingIdentityApi>("TEACHER");
  const [bookingDate, setBookingDate] = useState("");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("10:00");
  const [createMessage, setCreateMessage] = useState<string | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [isDeletingId, setIsDeletingId] = useState<number | null>(null);
  const [isCancellingId, setIsCancellingId] = useState<number | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [bookingData, roomData] = await Promise.all([getBookings(), getRooms()]);
      setBookings(bookingData);
      setRooms(roomData);
      setRoomId((prev) => {
        if (prev || roomData.length === 0) return prev;
        return String(roomData[0].id);
      });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? "Failed to load bookings");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  useEffect(() => {
    if (isAdmin) return;
    if (currentUserEmail) setBookedBy(currentUserEmail);
    if (userRole === "STUDENT") setBookedByIdentity("STUDENT");
    if (userRole === "TEACHER") setBookedByIdentity("TEACHER");
  }, [currentUserEmail, isAdmin, userRole]);

  const roomNumberById = useMemo(() => {
    return new Map(rooms.map((room) => [room.id, room.roomNumber]));
  }, [rooms]);

  const visibleBookings = useMemo(() => {
    if (isAdmin) return bookings;
    return bookings.filter((booking) => booking.bookedBy?.toLowerCase() === currentUserEmail);
  }, [bookings, currentUserEmail, isAdmin]);

  const onCreateBooking = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCreateError(null);
    setCreateMessage(null);

    const parsedRoomId = Number(roomId);
    if (!Number.isFinite(parsedRoomId) || parsedRoomId <= 0) {
      setCreateError("Please select a valid room.");
      return;
    }

    if (!bookedBy.trim()) {
      setCreateError("Email is required.");
      return;
    }

    if (!bookingDate) {
      setCreateError("Booking date is required.");
      return;
    }

    const startIdx = TIME_OPTIONS.indexOf(startTime);
    const endIdx = TIME_OPTIONS.indexOf(endTime);
    if (startIdx === -1 || endIdx === -1 || endIdx <= startIdx) {
      setCreateError("End time must be after start time.");
      return;
    }

    try {
      setIsCreating(true);
      await createBooking({
        roomId: parsedRoomId,
        bookedBy: bookedBy.trim(),
        bookedByIdentity,
        bookingDate,
        bookingTime: `${startTime}-${endTime}`,
      });
      setCreateMessage("Booking created successfully.");
      setBookedBy("");
      setBookedByIdentity("TEACHER");
      setBookingDate("");
      setStartTime("09:00");
      setEndTime("10:00");
      await loadData();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string"
          ? e.response.data
          : e?.response?.data?.message;
      setCreateError(backendMessage ?? e?.message ?? "Failed to create booking");
    } finally {
      setIsCreating(false);
    }
  };

  const onDeleteBooking = async (booking: BookingApi) => {
    setDeleteMessage(null);
    setDeleteError(null);

    const confirmed = window.confirm(`Delete booking #${booking.id}?`);
    if (!confirmed) return;

    try {
      setIsDeletingId(booking.id);
      await deleteBooking(booking.id);
      setDeleteMessage(`Booking #${booking.id} deleted.`);
      await loadData();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string"
          ? e.response.data
          : e?.response?.data?.message;
      setDeleteError(backendMessage ?? e?.message ?? "Failed to delete booking");
    } finally {
      setIsDeletingId(null);
    }
  };

  const onCancelBooking = async (booking: BookingApi) => {
    setDeleteMessage(null);
    setDeleteError(null);

    const isOwner = booking.bookedBy?.toLowerCase() === currentUserEmail;
    if (!isAdmin && !isOwner) {
      setDeleteError("You can cancel only your own bookings.");
      return;
    }
    if (booking.status === "CANCELLED") {
      setDeleteError("Booking is already cancelled.");
      return;
    }

    const confirmed = window.confirm(`Cancel booking #${booking.id}?`);
    if (!confirmed) return;

    try {
      setIsCancellingId(booking.id);
      await cancelBooking(booking.id);
      setDeleteMessage(`Booking #${booking.id} cancelled.`);
      await loadData();
    } catch (e: any) {
      const backendMessage =
        typeof e?.response?.data === "string"
          ? e.response.data
          : e?.response?.data?.message;
      setDeleteError(backendMessage ?? e?.message ?? "Failed to cancel booking");
    } finally {
      setIsCancellingId(null);
    }
  };

  return (
    <div className="page-wrap">
      <PageHeader title="Bookings" subtitle="Teachers can book rooms and manage booking status." />

      <section className="panel">
        <h2>Create Booking</h2>
        <form className="entity-form" onSubmit={onCreateBooking}>
          <label>
            Room
            <select value={roomId} onChange={(e) => setRoomId(e.target.value)} disabled={rooms.length === 0}>
              {rooms.map((room) => (
                <option key={room.id} value={room.id}>
                  {room.roomNumber} ({room.building})
                </option>
              ))}
            </select>
          </label>

          <label>
            Identity
            <select
              value={bookedByIdentity}
              onChange={(e) => setBookedByIdentity(e.target.value as BookingIdentityApi)}
              disabled={!isAdmin}
            >
              <option value="TEACHER">Teacher</option>
              <option value="STUDENT">Student</option>
            </select>
          </label>

          <label>
            Email
            <input
              value={bookedBy}
              onChange={(e) => setBookedBy(e.target.value)}
              placeholder="e.g. person@tus.ie"
              disabled={!isAdmin}
            />
          </label>

          <label>
            Booking Date
            <input value={bookingDate} onChange={(e) => setBookingDate(e.target.value)} type="date" />
          </label>

          <label>
            Start Time
            <select value={startTime} onChange={(e) => setStartTime(e.target.value)}>
              {TIME_OPTIONS.map((slot) => (
                <option key={`start-${slot}`} value={slot}>
                  {slot}
                </option>
              ))}
            </select>
          </label>

          <label>
            End Time
            <select value={endTime} onChange={(e) => setEndTime(e.target.value)}>
              {TIME_OPTIONS.map((slot) => (
                <option key={`end-${slot}`} value={slot}>
                  {slot}
                </option>
              ))}
            </select>
          </label>

          <button className="primary-btn" type="submit" disabled={isCreating || rooms.length === 0}>
            {isCreating ? "Creating..." : "Create Booking"}
          </button>
        </form>

        {rooms.length === 0 ? (
          <p className="panel-message">Create at least one room before creating bookings.</p>
        ) : null}
        {createMessage ? <p className="panel-success">{createMessage}</p> : null}
        {createError ? <p className="panel-error">{createError}</p> : null}
      </section>

      <section className="panel">
        {isLoading ? <p className="panel-message">Loading bookings...</p> : null}
        {error ? <p className="panel-error">{error}</p> : null}
        {deleteMessage ? <p className="panel-success">{deleteMessage}</p> : null}
        {deleteError ? <p className="panel-error">{deleteError}</p> : null}

        {!isLoading && !error ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Room</th>
                  <th>Identity</th>
                  <th>Email</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {visibleBookings.map((booking) => {
                  const roomLabel = roomNumberById.get(booking.roomId) ?? `Room #${booking.roomId}`;
                  const uiStatus = booking.status === "CONFIRMED" ? "Confirmed" : "Cancelled";
                  const isOwner = booking.bookedBy?.toLowerCase() === currentUserEmail;

                  return (
                    <tr key={booking.id}>
                      <td>{booking.id}</td>
                      <td>{roomLabel}</td>
                      <td>{booking.bookedByIdentity}</td>
                      <td>{booking.bookedBy}</td>
                      <td>{booking.bookingDate}</td>
                      <td>{booking.bookingTime}</td>
                      <td>
                        <span className={`badge badge-${uiStatus.toLowerCase()}`}>{uiStatus}</span>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="secondary-btn"
                          onClick={() => onCancelBooking(booking)}
                          disabled={
                            isCancellingId === booking.id ||
                            booking.status === "CANCELLED" ||
                            (!isAdmin && !isOwner)
                          }
                        >
                          {isCancellingId === booking.id ? "Cancelling..." : "Cancel"}
                        </button>
                        {isAdmin ? (
                          <button
                            type="button"
                            className="danger-btn"
                            onClick={() => onDeleteBooking(booking)}
                            disabled={isDeletingId === booking.id}
                            style={{ marginLeft: 8 }}
                          >
                            {isDeletingId === booking.id ? "Deleting..." : "Delete"}
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {visibleBookings.length === 0 ? <p className="panel-message">No bookings found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
