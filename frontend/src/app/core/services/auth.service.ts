import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';
import {LoginRequest, LoginResponse} from '../models/auth.model';
import {Membre} from '../models/membre.model';

@Injectable({providedIn: 'root'})
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);
  private url = `${environment.apiUrl}/auth`;

  // signal → variable réactive qui notifie Angular quand elle change
  currentAdmin = signal<LoginResponse | null>(null);
  currentMembre = signal<Membre | null>(null);

  // ─── Admin auth ───────────────────────────────────────────────

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.url}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('admin_token', response.token);
        localStorage.setItem('admin_data', JSON.stringify(response));
        this.currentAdmin.set(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_data');
    this.currentAdmin.set(null);
    this.router.navigate(['/admin/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('admin_token');
  }

  isAdminLoggedIn(): boolean {
    return !!this.getToken();
  }

  loadAdminFromStorage(): void {
    const data = localStorage.getItem('admin_data');
    if (data) {
      this.currentAdmin.set(JSON.parse(data));
    }
  }

  // ─── Membre auth ──────────────────────────────────────────────

  loginMembre(membre: Membre): void {
    // pas de token pour les membres — juste le matricule en localStorage
    localStorage.setItem('membre_data', JSON.stringify(membre));
    this.currentMembre.set(membre);
  }

  logoutMembre(): void {
    localStorage.removeItem('membre_data');
    this.currentMembre.set(null);
    this.router.navigate(['/membre/login']);
  }

  isMembreLoggedIn(): boolean {
    return !!localStorage.getItem('membre_data');
  }

  loadMembreFromStorage(): void {
    const data = localStorage.getItem('membre_data');
    if (data) {
      this.currentMembre.set(JSON.parse(data));
    }
  }
}
