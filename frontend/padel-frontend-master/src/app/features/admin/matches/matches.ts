import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTableModule} from '@angular/material/table';
import {MatChipsModule} from '@angular/material/chips';
import {MatSelectModule} from '@angular/material/select';
import {MatchService} from '../../../core/services/match.service';
import {SiteService} from '../../../core/services/site.service';
import {AuthService} from '../../../core/services/auth.service';
import {Match, StatutMatch, TypeMatch} from '../../../core/models/match.model';
import {Site} from '../../../core/models/site.model';
import {MatchFilterPipe} from '../../../shared/pipes/match-filter.pipe';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatChipsModule,
    MatSelectModule,
    MatchFilterPipe
  ],
  templateUrl: './matches.html',
  styleUrl: './matches.css'
})
export class Matches implements OnInit {

  private matchService = inject(MatchService);
  private siteService = inject(SiteService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  matches = signal<Match[]>([]);
  sites = signal<Site[]>([]);
  loading = signal(true);
  converting = signal<number | null>(null);
  selectedSiteId = signal<number | null>(null);
  isAdminSite = signal(false);

  readonly StatutMatch = StatutMatch;
  readonly TypeMatch = TypeMatch;

  columns = ['terrain', 'site', 'date', 'horaire', 'type', 'statut', 'joueurs', 'prix', 'actions'];

  ngOnInit(): void {
    const admin = this.authService.currentAdmin();

    if (admin?.role === 'SITE' && admin.siteId) {
      // admin SITE → forcer son site, bloquer le filtre
      this.isAdminSite.set(true);
      this.selectedSiteId.set(admin.siteId);

      // charger uniquement son site dans le select
      this.siteService.getById(admin.siteId).subscribe({
        next: (site) => this.sites.set([site])
      });

    } else {
      // admin GLOBAL → charger tous les sites pour le filtre
      this.siteService.getAll().subscribe({
        next: (data) => this.sites.set(data)
      });
    }

    this.loadMatches();
  }

  loadMatches(): void {
    this.loading.set(true);
    const siteId = this.selectedSiteId();

    const obs$ = siteId
      ? this.matchService.getBySiteId(siteId)
      : this.matchService.getAll();

    obs$.subscribe({
      next: (data) => {
        this.matches.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  convertToPublic(match: Match): void {
    if (!confirm(`Convertir le match du ${match.date} en public ?`)) return;

    this.converting.set(match.id);
    this.matchService.convertToPublic(match.id).subscribe({
      next: () => {
        this.snackBar.open('Match converti en public', 'OK', {duration: 3000});
        this.loadMatches();
        this.converting.set(null);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la conversion';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.converting.set(null);
      }
    });
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

  onSiteChange(): void {
    this.loadMatches();
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
