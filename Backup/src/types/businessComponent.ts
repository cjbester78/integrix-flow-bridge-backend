export interface BusinessComponent {
  id: string;
  name: string;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBusinessComponentRequest {
  name: string;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
}

export interface UpdateBusinessComponentRequest extends Partial<CreateBusinessComponentRequest> {
  id: string;
}