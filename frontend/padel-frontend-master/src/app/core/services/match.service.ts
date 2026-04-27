import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Match} from '../models/match.model';

@Injectable({providedIn: 'root'})
export class MatchService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/matches`;

  getAll(): Observable<Match[]> {
    return this.http.get<Match[]>(this.url);
  }

  getById(id: number): Observable<Match> {
    return this.http.get<Match>(`${this.url}/${id}`);
  }

  getPublicAvailable(): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/public`);
  }

  getBySiteId(siteId: number): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/site/${siteId}`);
  }

  getByOrganisateurId(organisateurId: number): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.url}/organisateur/${organisateurId}`);
  }

  create(match: Partial<Match> & { terrainId: number; organisateurId: number }): Observable<Match> {
    return this.http.post<Match>(this.url, match);
  }

  convertToPublic(id: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/convert-public`, {});
  }
}
