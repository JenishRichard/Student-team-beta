import { useEffect, useMemo, useState } from "react";
import PageHeader from "../components/PageHeader";
import { getBookings, type BookingApi } from "../services/booking.service";
import { getRooms, type RoomApi } from "../services/room.service";

export default function Dashboard() {
  const [bookings, setBookings] = useState<BookingApi[]>([]);
  const [rooms, setRooms] = useState<RoomApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const [bookingData, roomData] = await Promise.all([getBookings(), getRooms()]);
        setBookings(bookingData);
        setRooms(roomData);
      } catch (e: any) {
        setError(e?.response?.data?.message ?? e?.message ?? "Failed to load dashboard");
      } finally {
        setIsLoading(false);
      }
    };

    void load();
  }, []);

  const roomNumberById = useMemo(() => {
    return new Map(rooms.map((room) => [room.id, room.roomNumber]));
  }, [rooms]);

  const metrics = useMemo(() => {
    const confirmed = bookings.filter((b) => b.status === "CONFIRMED").length;
    const cancelled = bookings.filter((b) => b.status === "CANCELLED").length;
    const availableRooms = rooms.filter((r) => r.available).length;

    return [
      { label: "Total Rooms", value: String(rooms.length), change: `${availableRooms} available` },
      { label: "Total Bookings", value: String(bookings.length), change: `${confirmed} confirmed` },
      { label: "Cancelled", value: String(cancelled), change: "Needs rescheduling" },
      {
        label: "Active Teachers",
        value: String(new Set(bookings.map((b) => b.bookedBy)).size),
        change: "Based on bookings",
      },
    ];
  }, [bookings, rooms]);

  return (
    <div className="page-wrap">
      <PageHeader
        title="Dashboard"
        subtitle="Track room usage, occupancy, and booking activity in one place."
      />

      {isLoading ? <p className="panel-message">Loading dashboard...</p> : null}
      {error ? <p className="panel-error">{error}</p> : null}

      {!isLoading && !error ? (
        <>
          <section className="metrics-grid" aria-label="Summary metrics">
            {metrics.map((item) => (
              <article key={item.label} className="metric-card">
                <p className="metric-label">{item.label}</p>
                <p className="metric-value">{item.value}</p>
                <p className="metric-change">{item.change}</p>
              </article>
            ))}
          </section>

          <section className="panel">
            <h2>Recent Bookings</h2>
            <p className="panel-subtitle">Latest booking entries from booking-service</p>
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
                  </tr>
                </thead>
                <tbody>
                  {bookings.slice(0, 8).map((row) => {
                    const roomLabel = roomNumberById.get(row.roomId) ?? `Room #${row.roomId}`;
                    const uiStatus = row.status === "CONFIRMED" ? "Confirmed" : "Cancelled";

                    return (
                      <tr key={row.id}>
                        <td>{row.id}</td>
                        <td>{roomLabel}</td>
                        <td>{row.bookedByIdentity}</td>
                        <td>{row.bookedBy}</td>
                        <td>{row.bookingDate}</td>
                        <td>{row.bookingTime}</td>
                        <td>
                          <span className={`badge badge-${uiStatus.toLowerCase()}`}>{uiStatus}</span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </section>
        </>
      ) : null}
    </div>
  );
}
