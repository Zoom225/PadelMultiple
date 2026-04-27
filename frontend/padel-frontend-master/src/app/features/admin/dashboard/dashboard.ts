import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {SiteService} from '../../../core/services/site.service';
import {MembreService} from '../../../core/services/membre.service';
import {MatchService} from '../../../core/services/match.service';
import {TerrainService} from '../../../core/services/terrain.service';
import {AuthService} from '../../../core/services/auth.service';
import {StatutMatch} from '../../../core/models/match.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private siteService = inject(SiteService);
  private membreService = inject(MembreService);
  private matchService = inject(MatchService);
  private terrainService = inject(TerrainService);
  protected authService = inject(AuthService);
  protected router = inject(Router);

  loading = signal(true);
  isAdminSite = signal(false);
  nbSites = signal(0);
  nbMembres = signal(0);
  nbMatchs = signal(0);
  nbTerrains = signal(0);
  nbMatchsComplets = signal(0);
  chiffreAffaires = signal(0);

  ngOnInit(): void {
    const admin = this.authService.currentAdmin();

    if (admin?.role === 'SITE' && admin.siteId) {
      this.isAdminSite.set(true);
      this.loadStatsSite(admin.siteId);
    } else {
      this.loadStatsGlobal();
    }
  }

  loadStatsGlobal(): void {
    this.loading.set(true);

    this.siteService.getAll().subscribe({
      next: (sites) => this.nbSites.set(sites.length)
    });

    this.membreService.getAll().subscribe({
      next: (membres) => this.nbMembres.set(membres.length)
    });

    this.terrainService.getAll().subscribe({
      next: (terrains) => this.nbTerrains.set(terrains.length)
    });

    this.matchService.getAll().subscribe({
      next: (matches) => {
        this.nbMatchs.set(matches.length);
        const complets = matches.filter(m => m.statut === StatutMatch.COMPLET);
        this.nbMatchsComplets.set(complets.length);
        this.chiffreAffaires.set(complets.length * 60);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadStatsSite(siteId: number): void {
    this.loading.set(true);

    // 1 site uniquement
    this.nbSites.set(1);

    // membres → tous visibles selon cahier des charges
    this.membreService.getAll().subscribe({
      next: (membres) => this.nbMembres.set(membres.length)
    });

    // terrains du site
    this.terrainService.getBySiteId(siteId).subscribe({
      next: (terrains) => this.nbTerrains.set(terrains.length)
    });

    // matchs du site
    this.matchService.getBySiteId(siteId).subscribe({
      next: (matches) => {
        this.nbMatchs.set(matches.length);
        const complets = matches.filter(m => m.statut === StatutMatch.COMPLET);
        this.nbMatchsComplets.set(complets.length);
        this.chiffreAffaires.set(complets.length * 60);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
