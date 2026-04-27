export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  nom: string;
  prenom: string;
  role: 'GLOBAL' | 'SITE';
  siteId: number | null;
}
