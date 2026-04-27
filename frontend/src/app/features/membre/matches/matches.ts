import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatChipsModule} from '@angular/material/chips';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import {MatchService} from '../../../core/services/match.service';
import {ReservationService} from '../../../core/services/reservation.service';
import {AuthService} from '../../../core/services/auth.service';
import {Match} from '../../../core/models/match.model';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './matches.html',
  styleUrl: './matches.css'
})
export class Matches implements OnInit {

  private matchService = inject(MatchService);
  private reservationService = inject(ReservationService);
  protected authService = inject(AuthService);
  protected router = inject(Router);
  private snackBar = inject(MatSnackBar);

  matches = signal<Match[]>([]);
  loading = signal(true);
  joining = signal<number | null>(null);
  reservationStatuts = signal<{matchId: number, statut: string}[]>([]);

  ngOnInit(): void {
    this.loadMatches();
    this.loadMesReservations();
  }

  loadMatches(): void {
    this.loading.set(true);
    this.matchService.getPublicAvailable().subscribe({
      next: (data) => {
        this.matches.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des matchs', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  loadMesReservations(): void {
    const membre = this.authService.currentMembre();
    if (!membre) return;

    this.reservationService.getByMembreId(membre.id).subscribe({
      next: (reservations) => {
        const data = reservations
          .filter(r => r.statut !== 'ANNULEE')
          .map(r => ({ matchId: r.matchId, statut: r.statut }));
        this.reservationStatuts.set(data);
      },
      error: () => {}
    });
  }

  joinMatch(match: Match): void {
    const membre = this.authService.currentMembre();
    if (!membre) return;

    this.joining.set(match.id);

    this.reservationService.create(match.id, membre.id, membre.id).subscribe({
      next: () => {
        this.snackBar.open('Réservation créée ! Pensez à payer.', 'OK', {duration: 4000});
        this.reservationStatuts.update(items => [
          ...items,
          { matchId: match.id, statut: 'EN_ATTENTE' }
        ]);
        this.loadMatches();
        this.joining.set(null);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la réservation';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.joining.set(null);
      }
    });
  }

  isAlreadyRegistered(match: Match): boolean {
    const membre = this.authService.currentMembre();
    if (!membre) return false;
    if (match.organisateurId === membre.id) return true;
    return this.reservationStatuts().some(r => r.matchId === match.id);
  }

  getReservationStatut(matchId: number): string {
    return this.reservationStatuts().find(r => r.matchId === matchId)?.statut || '';
  }

  goToReservations(): void {
    this.router.navigate(['/membre/reservations']);
  }

  logout(): void {
    this.authService.logoutMembre();
  }

  getSpotsLeft(match: Match): number {
    return 4 - match.nbJoueursActuels;
  }

  getSpotColor(match: Match): string {
    const spots = this.getSpotsLeft(match);
    if (spots === 1) return 'warn';
    if (spots <= 2) return 'accent';
    return 'primary';
  }

  goToCreateMatch(): void {
    this.router.navigate(['/membre/create-match']);
  }

  goToMatches(): void {
    this.router.navigate(['/membre/matches']);
  }

  goToMesMatches(): void {
    this.router.navigate(['/membre/mes-matches']);
  }
}
