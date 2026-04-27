import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatChipsModule} from '@angular/material/chips';
import {MatchService} from '../../../core/services/match.service';
import {MembreService} from '../../../core/services/membre.service';
import {ReservationService} from '../../../core/services/reservation.service';
import {AuthService} from '../../../core/services/auth.service';
import {Match, StatutMatch, TypeMatch} from '../../../core/models/match.model';
import {Reservation} from '../../../core/models/reservation.model';

@Component({
  selector: 'app-mes-matches',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule
  ],
  templateUrl: './mes-matches.html',
  styleUrl: './mes-matches.css'
})
export class MesMatches implements OnInit {

  private matchService = inject(MatchService);
  private membreService = inject(MembreService);
  private reservationService = inject(ReservationService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  matches = signal<Match[]>([]);
  reservations = signal<Map<number, Reservation[]>>(new Map());
  loading = signal(true);

  // pour ajouter un joueur à un match privé
  matriculeInputs = signal<Map<number, string>>(new Map());
  searchingFor = signal<number | null>(null);

  readonly StatutMatch = StatutMatch;
  readonly TypeMatch = TypeMatch;

  ngOnInit(): void {
    this.loadMesMatches();
  }

  loadMesMatches(): void {
    const membre = this.authService.currentMembre();
    if (!membre) return;

    this.loading.set(true);
    this.matchService.getByOrganisateurId(membre.id).subscribe({
      next: (matches) => {
        this.matches.set(matches);

        // charger les réservations de chaque match
        matches.forEach(match => {
          this.reservationService.getByMatchId(match.id).subscribe({
            next: (reservations) => {
              this.reservations.update(map => {
                map.set(match.id, reservations);
                return new Map(map);
              });
            }
          });
        });

        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  getMatriculeInput(matchId: number): string {
    return this.matriculeInputs().get(matchId) || '';
  }

  setMatriculeInput(matchId: number, value: string): void {
    this.matriculeInputs.update(map => {
      map.set(matchId, value);
      return new Map(map);
    });
  }

  addJoueur(match: Match): void {
    const matricule = this.getMatriculeInput(match.id).trim();
    if (!matricule) return;

    const organisateur = this.authService.currentMembre();
    if (!organisateur) return;

    this.searchingFor.set(match.id);

    this.membreService.getByMatricule(matricule).subscribe({
      next: (membre) => {
        if (membre.id === organisateur.id) {
          this.snackBar.open('Vous êtes déjà l\'organisateur', 'Fermer', {duration: 3000});
          this.searchingFor.set(null);
          return;
        }

        this.reservationService.create(match.id, membre.id, organisateur.id).subscribe({
          next: () => {
            this.snackBar.open(`${membre.prenom} ${membre.nom} ajouté !`, 'OK', {duration: 3000});
            this.setMatriculeInput(match.id, '');
            this.searchingFor.set(null);
            this.loadMesMatches();
          },
          error: (err) => {
            const msg = err.error?.message || 'Erreur lors de l\'ajout';
            this.snackBar.open(msg, 'Fermer', {duration: 4000});
            this.searchingFor.set(null);
          }
        });
      },
      error: () => {
        this.snackBar.open('Matricule introuvable', 'Fermer', {duration: 3000});
        this.searchingFor.set(null);
      }
    });
  }

  getSpotsLeft(match: Match): number {
    return 4 - match.nbJoueursActuels;
  }

  getStatutColor(statut: StatutMatch): string {
    switch (statut) {
      case StatutMatch.COMPLET:
        return 'primary';
      case StatutMatch.PLANIFIE:
        return 'accent';
      case StatutMatch.ANNULE:
        return 'warn';
      default:
        return '';
    }
  }

  getTypeColor(type: TypeMatch): string {
    return type === TypeMatch.PUBLIC ? 'primary' : 'accent';
  }

  goToMatches(): void {
    this.router.navigate(['/membre/matches']);
  }

  goToReservations(): void {
    this.router.navigate(['/membre/reservations']);
  }

  goToCreateMatch(): void {
    this.router.navigate(['/membre/create-match']);
  }

  logout(): void {
    this.authService.logoutMembre();
  }
}
