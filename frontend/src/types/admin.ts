export interface User {
  id: string;
  username: string;
  email: string;
  first_name: string;
  last_name: string;
  role_id?: string;
  role: 'administrator' | 'integrator' | 'viewer';
  status: 'active' | 'inactive' | 'pending';
  permissions?: Record<string, string[]>;
  email_verified: boolean;
  created_at: string;
  updated_at: string;
  last_login_at?: string;
  email_verification_token?: string;
  password_reset_token?: string;
  password_reset_expires_at?: string;
}

export interface Role {
  id: string;
  name: string;
  description: string;
  permissions: string[];
  userCount: number;
}

export interface Certificate {
  id: string;
  name: string;
  type: string;
  issuer: string;
  validFrom: string;
  validTo: string;
  status: string;
  usage: string;
}

export interface JarFile {
  id: string;
  name: string;
  version?: string;
  description?: string;
  file_name: string;
  file_path?: string;
  size_bytes?: number;
  driver_type?: string;
  upload_date: string;
  checksum?: string;
  is_active: boolean;
  created_at: string;
  updated_at: string;
  uploaded_by?: string;
}