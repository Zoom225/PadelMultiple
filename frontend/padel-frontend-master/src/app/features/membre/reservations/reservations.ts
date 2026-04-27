import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatChipsModule} from '@angular/material/chips';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {ReservationService} from '../../../core/services/reservation.service';
import {PaiementService} from '../../../core/services/paiement.service';
import {AuthService} from '../../../core/services/auth.service';
import {Reservation, StatutPaiement, StatutReservation} from '../../../core/models/reservation.model';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './reservations.html',
  styleUrl: './reservations.css'
})
export class Reservations implements OnInit {

  private reservationService = inject(ReservationService);
  private paiementService = inject(PaiementService);
  private authService = inject(AuthService);
  protected router = inject(Router);
  private snackBar = inject(MatSnackBar);
  protected readonly StatutReservation = StatutReservation;
  protected readonly StatutPaiement = StatutPaiement;


  reservations = signal<Reservation[]>([]);
  loading = signal(true);
  paying = signal<number | null>(null);

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    const membre = this.authService.currentMembre();
    if (!membre) return;

    this.loading.set(true);
    this.reservationService.getByMembreId(membre.id).subscribe({
      next: (data) => {
        this.reservations.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  pay(reservation: Reservation): void {
    const membre = this.authService.currentMembre();
    if (!membre) return;

    this.paying.set(reservation.id);

    this.paiementService.pay(reservation.id, membre.id).subscribe({
      next: () => {
        this.snackBar.open('Paiement effectué avec succès !', 'OK', {duration: 4000});
        this.loadReservations();
        this.paying.set(null);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors du paiement';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.paying.set(null);
      }
    });
  }

  cancel(reservation: Reservation): void {
    this.reservationService.cancel(reservation.id).subscribe({
      next: () => {
        this.snackBar.open('Réservation annulée', 'OK', {duration: 3000});
        this.loadReservations();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de l\'annulation';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
      }
    });
  }

  goToMatches(): void {
    this.router.navigate(['/membre/matches']);
  }

  goToCreateMatch(): void {
    this.router.navigate(['/membre/create-match']);
  }

  goToReservations(): void {
    this.router.navigate(['/membre/reservations']);
  }

  goToMesMatches(): void {
    this.router.navigate(['/membre/mes-matches']);
  }

  logout(): void {
    this.authService.logoutMembre();
  }

  getStatutColor(statut: string): string {
    switch (statut) {
      case 'CONFIRMEE':
        return 'primary';
      case 'EN_ATTENTE':
        return 'accent';
      case 'ANNULEE':
        return 'warn';
      default:
        return '';
    }
  }

  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'CONFIRMEE':
        return 'Confirmée';
      case 'EN_ATTENTE':
        return 'En attente de paiement';
      case 'ANNULEE':
        return 'Annulée';
      default:
        return statut;
    }
  }

  getPaiementLabel(statut: string): string {
    switch (statut) {
      case 'PAYE':
        return 'Payé';
      case 'EN_ATTENTE':
        return 'Non payé';
      case 'REMBOURSE':
        return 'Remboursé';
      default:
        return statut;
    }
  }

}
