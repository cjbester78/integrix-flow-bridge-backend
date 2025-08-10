export interface Customer {
  id: string;
  name: string;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  name: string;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
}

export interface UpdateCustomerRequest extends Partial<CreateCustomerRequest> {
  id: string;
}