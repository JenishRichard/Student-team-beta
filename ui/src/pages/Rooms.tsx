import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import PageHeader from "../components/PageHeader";
import { createRoom, deleteRoom, getRooms, type RoomApi } from "../services/room.service";
import { useAuth } from "../store/auth";
import { isAdminRole } from "../types/roles";

export default function Rooms() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);

  const [rooms, setRooms] = useState<RoomApi[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [roomNumber, setRoomNumber] = useState("");
  const [building, setBuilding] = useState("");
  const [capacity, setCapacity] = useState("30");
  const [type, setType] = useState("Lecture");
  const [available, setAvailable] = useState(true);
  const [createMessage, setCreateMessage] = useState<string | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [isDeletingId, setIsDeletingId] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);

  const loadRooms = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const result = await getRooms();
      setRooms(result);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? "Failed to load rooms");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadRooms();
  }, [loadRooms]);

  const onCreateRoom = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCreateError(null);
    setCreateMessage(null);

    if (!roomNumber.trim() || !building.trim() || !type.trim()) {
      setCreateError("Room number, building, and type are required.");
      return;
    }

    const parsedCapacity = Number(capacity);
    if (!Number.isFinite(parsedCapacity) || parsedCapacity <= 0) {
      setCreateError("Capacity must be greater than 0.");
      return;
    }

    try {
      setIsCreating(true);
      await createRoom({
        roomNumber: roomNumber.trim(),
        building: building.trim(),
        capacity: parsedCapacity,
        type: type.trim(),
        available,
      });
      setCreateMessage("Room created successfully.");
      setRoomNumber("");
      setBuilding("");
      setCapacity("30");
      setType("Lecture");
      setAvailable(true);
      await loadRooms();
    } catch (e: any) {
      setCreateError(e?.response?.data?.message ?? e?.message ?? "Failed to create room");
    } finally {
      setIsCreating(false);
    }
  };

  const onDeleteRoom = async (room: RoomApi) => {
    setDeleteError(null);
    setDeleteMessage(null);

    const confirmed = window.confirm(`Delete room ${room.roomNumber}?`);
    if (!confirmed) return;

    try {
      setIsDeletingId(room.id);
      await deleteRoom(room.id);
      setDeleteMessage(`Room ${room.roomNumber} deleted.`);
      await loadRooms();
    } catch (e: any) {
      setDeleteError(e?.response?.data?.message ?? e?.message ?? "Failed to delete room");
    } finally {
      setIsDeletingId(null);
    }
  };

  return (
    <div className="page-wrap">
      <PageHeader title="Rooms" subtitle="Monitor room inventory, capacity, and current availability." />

      {isAdmin ? (
        <section className="panel">
          <h2>Create Room</h2>
          <form className="entity-form" onSubmit={onCreateRoom}>
            <label>
              Room Number
              <input
                value={roomNumber}
                onChange={(e) => setRoomNumber(e.target.value)}
                placeholder="e.g. A-101"
              />
            </label>

            <label>
              Building
              <input
                value={building}
                onChange={(e) => setBuilding(e.target.value)}
                placeholder="e.g. Main Block"
              />
            </label>

            <label>
              Capacity
              <input
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
                type="number"
                min={1}
              />
            </label>

            <label>
              Type
              <input value={type} onChange={(e) => setType(e.target.value)} placeholder="e.g. Lab" />
            </label>

            <label className="checkbox-field">
              <input
                checked={available}
                onChange={(e) => setAvailable(e.target.checked)}
                type="checkbox"
              />
              Available
            </label>

            <button className="primary-btn" type="submit" disabled={isCreating}>
              {isCreating ? "Creating..." : "Create Room"}
            </button>
          </form>

          {createMessage ? <p className="panel-success">{createMessage}</p> : null}
          {createError ? <p className="panel-error">{createError}</p> : null}
        </section>
      ) : null}

      <section className="panel">
        {isLoading ? <p className="panel-message">Loading rooms...</p> : null}
        {error ? <p className="panel-error">{error}</p> : null}
        {deleteMessage ? <p className="panel-success">{deleteMessage}</p> : null}
        {deleteError ? <p className="panel-error">{deleteError}</p> : null}

        {!isLoading && !error ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Room Number</th>
                  <th>Building</th>
                  <th>Type</th>
                  <th>Capacity</th>
                  <th>Status</th>
                  {isAdmin ? <th>Action</th> : null}
                </tr>
              </thead>
              <tbody>
                {rooms.map((room) => {
                  const uiStatus = room.available ? "Available" : "Occupied";
                  return (
                    <tr key={room.id}>
                      <td>{room.id}</td>
                      <td>{room.roomNumber}</td>
                      <td>{room.building}</td>
                      <td>{room.type}</td>
                      <td>{room.capacity}</td>
                      <td>
                        <span className={`badge badge-${uiStatus.toLowerCase()}`}>{uiStatus}</span>
                      </td>
                      {isAdmin ? (
                        <td>
                          <button
                            type="button"
                            className="danger-btn"
                            onClick={() => onDeleteRoom(room)}
                            disabled={isDeletingId === room.id}
                          >
                            {isDeletingId === room.id ? "Deleting..." : "Delete"}
                          </button>
                        </td>
                      ) : null}
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {rooms.length === 0 ? <p className="panel-message">No rooms found.</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
