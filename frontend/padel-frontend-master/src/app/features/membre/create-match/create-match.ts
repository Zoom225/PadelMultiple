import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatRadioModule} from '@angular/material/radio';
import {MatchService} from '../../../core/services/match.service';
import {SiteService} from '../../../core/services/site.service';
import {TerrainService} from '../../../core/services/terrain.service';
import {MembreService} from '../../../core/services/membre.service';
import {ReservationService} from '../../../core/services/reservation.service';
import {AuthService} from '../../../core/services/auth.service';
import {Site} from '../../../core/models/site.model';
import {Terrain} from '../../../core/models/terrain.model';
import {Match, TypeMatch} from '../../../core/models/match.model';
import {Membre} from '../../../core/models/membre.model';

@Component({
  selector: 'app-create-match',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatRadioModule
  ],
  templateUrl: './create-match.html',
  styleUrl: './create-match.css'
})
export class CreateMatch implements OnInit {

  private matchService = inject(MatchService);
  private siteService = inject(SiteService);
  private terrainService = inject(TerrainService);
  private membreService = inject(MembreService);
  private reservationService = inject(ReservationService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  sites = signal<Site[]>([]);
  terrains = signal<Terrain[]>([]);
  loading = signal(false);
  saving = signal(false);

  // formulaire création match
  selectedSiteId = '';
  selectedTerrain = '';
  date = '';
  heureDebut = '';
  typeMatch = TypeMatch.PUBLIC;

  readonly TypeMatch = TypeMatch;

  // pour match privé — ajouter des joueurs par matricule
  createdMatch = signal<Match | null>(null);
  matriculeJoueur = '';
  searchingJoueur = false;
  joueursTrouves = signal<Membre[]>([]);

  ngOnInit(): void {
    this.siteService.getAll().subscribe({
      next: (data) => this.sites.set(data)
    });
  }

  onSiteChange(): void {
    if (!this.selectedSiteId) return;
    this.terrainService.getBySiteId(Number(this.selectedSiteId)).subscribe({
      next: (data) => this.terrains.set(data)
    });
  }

  createMatch(): void {
    const membre = this.authService.currentMembre();
    console.log(membre);
    if (!membre) return;

    if (!this.selectedTerrain || !this.date || !this.heureDebut) {
      this.snackBar.open('Veuillez remplir tous les champs', 'Fermer', {duration: 3000});
      return;
    }

    this.saving.set(true);

    this.matchService.create({
      terrainId: Number(this.selectedTerrain),
      organisateurId: membre.id,
      date: this.date,
      heureDebut: this.heureDebut + ':00',
      typeMatch: this.typeMatch
    }).subscribe({
      next: (match) => {
        this.createdMatch.set(match);
        this.saving.set(false);

        if (this.typeMatch === TypeMatch.PUBLIC) {
          this.snackBar.open('Match public créé !', 'OK', {duration: 3000});
        } else {
          this.snackBar.open('Match privé créé ! Ajoutez vos 3 joueurs.', 'OK', {duration: 4000});
        }
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la création';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  searchJoueur(): void {
    if (!this.matriculeJoueur.trim()) return;

    this.searchingJoueur = true;
    this.membreService.getByMatricule(this.matriculeJoueur.trim()).subscribe({
      next: (membre) => {
        this.searchingJoueur = false;
        const match = this.createdMatch();
        if (!match) return;

        // vérifier que ce n'est pas l'organisateur
        const organisateur = this.authService.currentMembre();
        if (membre.id === organisateur?.id) {
          this.snackBar.open('Vous êtes déjà l\'organisateur', 'Fermer', {duration: 3000});
          return;
        }

        // ajouter le joueur via une réservation
        this.reservationService.create(match.id, membre.id, organisateur!.id).subscribe({
          next: () => {
            this.joueursTrouves.update(j => [...j, membre]);
            this.matriculeJoueur = '';
            this.snackBar.open(`${membre.prenom} ${membre.nom} ajouté !`, 'OK', {duration: 3000});
          },
          error: (err) => {
            const msg = err.error?.message || 'Erreur lors de l\'ajout';
            this.snackBar.open(msg, 'Fermer', {duration: 4000});
          }
        });
      },
      error: () => {
        this.searchingJoueur = false;
        this.snackBar.open('Membre introuvable avec ce matricule', 'Fermer', {duration: 3000});
      }
    });
  }

  goToMatches(): void {
    this.router.navigate(['/membre/matches']);
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

  getSpotsLeft(): number {
    const match = this.createdMatch();
    if (!match) return 3;
    return 4 - match.nbJoueursActuels;
  }
}
