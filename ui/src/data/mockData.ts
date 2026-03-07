export type Metric = {
  label: string;
  value: string;
  change: string;
};

export type RoomItem = {
  id: string;
  name: string;
  capacity: number;
  floor: string;
  status: "Available" | "Maintenance" | "Occupied";
};

export type StudentItem = {
  id: string;
  name: string;
  program: string;
  year: string;
  bookingStatus: "Active" | "Pending" | "None";
};

export type BookingItem = {
  id: string;
  room: string;
  student: string;
  date: string;
  time: string;
  status: "Confirmed" | "Pending" | "Cancelled";
};

export type AdminUserItem = {
  id: string;
  name: string;
  email: string;
  role: AppRole;
  status: "Active" | "Invited";
};

export const dashboardMetrics: Metric[] = [
  { label: "Rooms In Use", value: "42", change: "+8% this week" },
  { label: "Today Bookings", value: "137", change: "+12% since yesterday" },
  { label: "Pending Requests", value: "19", change: "-3 resolved today" },
  { label: "Active Students", value: "684", change: "+24 this month" },
];

export const rooms: RoomItem[] = [
  { id: "RM-101", name: "Innovation Lab", capacity: 42, floor: "Block A - 1F", status: "Available" },
  { id: "RM-205", name: "Cloud Studio", capacity: 30, floor: "Block B - 2F", status: "Occupied" },
  { id: "RM-302", name: "Systems Hall", capacity: 120, floor: "Block C - 3F", status: "Maintenance" },
  { id: "RM-114", name: "Data Workshop", capacity: 24, floor: "Block A - 1F", status: "Available" },
];

export const students: StudentItem[] = [
  { id: "ST-9001", name: "Ava Patel", program: "Computer Science", year: "Year 3", bookingStatus: "Active" },
  { id: "ST-9002", name: "Noah Murphy", program: "Software Engineering", year: "Year 2", bookingStatus: "Pending" },
  { id: "ST-9003", name: "Mia O'Connor", program: "AI & Data", year: "Year 4", bookingStatus: "Active" },
  { id: "ST-9004", name: "Ethan Smith", program: "Cybersecurity", year: "Year 1", bookingStatus: "None" },
];

export const bookings: BookingItem[] = [
  { id: "BK-4401", room: "Innovation Lab", student: "Ava Patel", date: "2026-03-03", time: "09:00 - 11:00", status: "Confirmed" },
  { id: "BK-4402", room: "Cloud Studio", student: "Noah Murphy", date: "2026-03-03", time: "12:00 - 14:00", status: "Pending" },
  { id: "BK-4403", room: "Systems Hall", student: "Mia O'Connor", date: "2026-03-04", time: "10:00 - 12:00", status: "Cancelled" },
  { id: "BK-4404", room: "Data Workshop", student: "Ethan Smith", date: "2026-03-04", time: "15:00 - 17:00", status: "Confirmed" },
];

export const adminUsers: AdminUserItem[] = [
  { id: "AD-01", name: "Liam Byrne", email: "liam.byrne@tus.ie", role: "SUPER_ADMIN", status: "Active" },
  { id: "AD-02", name: "Grace Kelly", email: "grace.kelly@tus.ie", role: "ADMIN", status: "Active" },
  { id: "AD-03", name: "Sofia Ryan", email: "sofia.ryan@tus.ie", role: "TEACHER", status: "Invited" },
];
import type { AppRole } from "../types/roles";
