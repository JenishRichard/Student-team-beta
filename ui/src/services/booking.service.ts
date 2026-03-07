import coreClient from "./coreClient";

export type BookingStatusApi = "CONFIRMED" | "CANCELLED";
export type BookingIdentityApi = "TEACHER" | "STUDENT";
export type ParticipantApi = {
  id: number;
  participantId: string;
  email: string;
  identity: BookingIdentityApi;
};

export type BookingApi = {
  id: number;
  roomId: number;
  bookedBy: string;
  bookedByIdentity: BookingIdentityApi;
  bookingDate: string;
  bookingTime: string;
  status: BookingStatusApi;
};

export type CreateBookingPayload = {
  roomId: number;
  bookedBy: string;
  bookedByIdentity: BookingIdentityApi;
  bookingDate: string;
  bookingTime: string;
};

export async function getBookings(): Promise<BookingApi[]> {
  const { data } = await coreClient.get<BookingApi[]>("/bookings");
  return data;
}

export async function createBooking(payload: CreateBookingPayload): Promise<BookingApi> {
  const { data } = await coreClient.post<BookingApi>("/bookings", payload);
  return data;
}

export async function deleteBooking(bookingId: number): Promise<void> {
  await coreClient.delete(`/bookings/${bookingId}`);
}

export async function cancelBooking(bookingId: number): Promise<BookingApi> {
  const { data } = await coreClient.put<BookingApi>(`/bookings/${bookingId}/cancel`);
  return data;
}

export async function deleteParticipantBookings(
  bookedBy: string,
  identity: BookingIdentityApi
): Promise<void> {
  await coreClient.delete("/bookings/participants", {
    params: { bookedBy, identity },
  });
}

export async function getParticipants(identity: BookingIdentityApi): Promise<ParticipantApi[]> {
  try {
    const { data } = await coreClient.get<ParticipantApi[]>("/bookings/participants", {
      params: { identity },
    });
    return data;
  } catch (err: any) {
    // Backward compatibility: older booking-service builds don't expose participants API.
    if (err?.response?.status === 404) return [];
    throw err;
  }
}

export async function addParticipant(
  participantId: string,
  email: string,
  identity: BookingIdentityApi
): Promise<ParticipantApi> {
  try {
    const { data } = await coreClient.post<ParticipantApi>("/bookings/participants", {
      participantId,
      email,
      identity,
    });
    return data;
  } catch (err: any) {
    if (err?.response?.status === 404) {
      throw new Error(
        "Participants API is not available. Restart booking-service on the latest code."
      );
    }
    throw err;
  }
}
