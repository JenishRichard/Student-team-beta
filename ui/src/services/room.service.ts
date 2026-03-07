import coreClient from "./coreClient";

export type RoomApi = {
  id: number;
  roomNumber: string;
  building: string;
  capacity: number;
  type: string;
  available: boolean;
};

export type CreateRoomPayload = {
  roomNumber: string;
  building: string;
  capacity: number;
  type: string;
  available: boolean;
};

export async function getRooms(): Promise<RoomApi[]> {
  const { data } = await coreClient.get<RoomApi[]>("/rooms");
  return data;
}

export async function createRoom(payload: CreateRoomPayload): Promise<RoomApi> {
  const { data } = await coreClient.post<RoomApi>("/rooms", payload);
  return data;
}

export async function deleteRoom(roomId: number): Promise<void> {
  await coreClient.delete(`/rooms/${roomId}`);
}
