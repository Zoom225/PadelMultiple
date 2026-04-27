import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {Paiement} from '../models/reservation.model';

@Injectable({providedIn: 'root'})
export class PaiementService {

  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/paiements`;

  getById(id: number): Observable<Paiement> {
    return this.http.get<Paiement>(`${this.url}/${id}`);
  }

  getByReservationId(reservationId: number): Observable<Paiement> {
    return this.http.get<Paiement>(`${this.url}/reservation/${reservationId}`);
  }

  getByMembreId(membreId: number): Observable<Paiement[]> {
    return this.http.get<Paiement[]>(`${this.url}/membre/${membreId}`);
  }

  pay(reservationId: number, membreId: number): Observable<Paiement> {
    return this.http.post<Paiement>(
      `${this.url}/reservation/${reservationId}/membre/${membreId}`,
      {}
    );
  }
}
