import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Site} from '../models/site.model';

@Injectable({providedIn: 'root'})
export class SiteService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/sites`;

  getAll(): Observable<Site[]> {
    return this.http.get<Site[]>(this.url);
  }

  getById(id: number): Observable<Site> {
    return this.http.get<Site>(`${this.url}/${id}`);
  }

  create(site: Partial<Site>): Observable<Site> {
    return this.http.post<Site>(this.url, site);
  }

  update(id: number, site: Partial<Site>): Observable<Site> {
    return this.http.put<Site>(`${this.url}/${id}`, site);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
