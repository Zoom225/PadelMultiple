import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Reservation} from '../models/reservation.model';

@Injectable({providedIn: 'root'})
export class ReservationService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/reservations`;

  getById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.url}/${id}`);
  }

  getByMatchId(matchId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.url}/match/${matchId}`);
  }

  getByMembreId(membreId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.url}/membre/${membreId}`);
  }

  create(matchId: number, membreId: number, requesterId: number): Observable<Reservation> {
    return this.http.post<Reservation>(this.url, {
      matchId,
      membreId,
      requesterId
    });
  }

  cancel(id: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/cancel`, {});
  }
}
