export type Role = 'ROLE_MEMBER' | 'ROLE_CURATOR' | 'ROLE_LIBRARIAN' | 'ROLE_ADMIN';
export type UserTier = 'REGULAR' | 'CAPSTONE' | 'FACULTY';
export type ResourceType = 'BOOK' | 'LAB_KIT' | 'HARDWARE' | 'SEMINAR_ROOM';
export type ResourceStatus = 'AVAILABLE' | 'BORROWED' | 'RESERVED' | 'MAINTENANCE';
export type LoanStatus = 'ACTIVE' | 'RETURNED' | 'OVERDUE';
export type QueueStatus = 'WAITING' | 'FULFILLED' | 'CANCELLED' | 'EXPIRED';
export type UrgencyLevel = 'STANDARD' | 'HIGH' | 'CRITICAL';
export type FineStatus = 'PENDING' | 'PAID' | 'WAIVED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  userTier?: UserTier;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: string;
  email: string;
  fullName: string;
  role: Role;
  userTier?: UserTier;
}

export interface ResourceDTO {
  id: string;
  barcode: string;
  title: string;
  authorOrBrand: string;
  description?: string;
  coverImageUrl?: string;
  type: ResourceType;
  totalQuantity: number;
  availableQuantity: number;
  location: string;
  tags: string[];
  status: ResourceStatus;
  activeQueueCount: number;
  createdAt: string;
}

export interface LoanDTO {
  id: string;
  userId: string;
  userFullName: string;
  resourceId: string;
  resourceTitle: string;
  resourceBarcode: string;
  borrowDate: string;
  dueDate: string;
  returnDate?: string;
  status: LoanStatus;
  calculatedFine: number;
}

export interface QueueEntryDTO {
  id: string;
  resourceId: string;
  resourceTitle: string;
  userId: string;
  userFullName: string;
  requestedAt: string;
  urgencyLevel: UrgencyLevel;
  calculatedPriorityScore: number;
  queuePosition: number;
  status: QueueStatus;
}

export interface FineDTO {
  id: string;
  loanId: string;
  resourceTitle: string;
  userId: string;
  userFullName: string;
  amount: number;
  reason: string;
  status: FineStatus;
  createdAt: string;
  waivedBy?: string;
}

export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export interface ShelfPositionDTO {
  resourceId: string;
  barcode: string;
  title: string;
  authorOrBrand: string;
  type: ResourceType;
  status: ResourceStatus;
  clusterId: number;
  aisleId: string;
  shelfLevel: number;
  posX: number;
  posY: number;
  posZ: number;
  spineColorHex: string;
  isEyeLevel: boolean;
}
