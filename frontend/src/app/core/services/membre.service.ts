import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Membre} from '../models/membre.model';

@Injectable({providedIn: 'root'})
export class MembreService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/membres`;

  getAll(): Observable<Membre[]> {
    return this.http.get<Membre[]>(this.url);
  }

  getById(id: number): Observable<Membre> {
    return this.http.get<Membre>(`${this.url}/${id}`);
  }

  getByMatricule(matricule: string): Observable<Membre> {
    return this.http.get<Membre>(`${this.url}/matricule/${matricule}`);
  }

  hasActivePenalty(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.url}/${id}/penalty`);
  }

  hasOutstandingBalance(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.url}/${id}/balance`);
  }

  create(membre: Partial<Membre>): Observable<Membre> {
    return this.http.post<Membre>(this.url, membre);
  }

  update(id: number, membre: Partial<Membre>): Observable<Membre> {
    return this.http.put<Membre>(`${this.url}/${id}`, membre);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
