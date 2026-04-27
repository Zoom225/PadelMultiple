import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Terrain} from '../models/terrain.model';

@Injectable({providedIn: 'root'})
export class TerrainService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/terrains`;

  getAll(): Observable<Terrain[]> {
    return this.http.get<Terrain[]>(this.url);
  }

  getBySiteId(siteId: number): Observable<Terrain[]> {
    return this.http.get<Terrain[]>(`${this.url}/site/${siteId}`);
  }

  getById(id: number): Observable<Terrain> {
    return this.http.get<Terrain>(`${this.url}/${id}`);
  }

  create(terrain: Partial<Terrain>, siteId: number): Observable<Terrain> {
    return this.http.post<Terrain>(this.url, { ...terrain, siteId });
  }

  update(id: number, terrain: Partial<Terrain>): Observable<Terrain> {
    return this.http.put<Terrain>(`${this.url}/${id}`, terrain);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
