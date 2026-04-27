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
import {MatTableModule} from '@angular/material/table';
import {TerrainService} from '../../../core/services/terrain.service';
import {SiteService} from '../../../core/services/site.service';
import {AuthService} from '../../../core/services/auth.service';
import {Terrain} from '../../../core/models/terrain.model';
import {Site} from '../../../core/models/site.model';

@Component({
  selector: 'app-terrains',
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
    MatTableModule
  ],
  templateUrl: './terrains.html',
  styleUrl: './terrains.css'
})
export class Terrains implements OnInit {

  private terrainService = inject(TerrainService);
  private siteService = inject(SiteService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  terrains = signal<Terrain[]>([]);
  sites = signal<Site[]>([]);
  loading = signal(true);
  saving = signal(false);
  showForm = signal(false);
  editingTerrain = signal<Terrain | null>(null);
  isAdminSite = signal(false);
  adminSiteId = signal<number | null>(null);

  newTerrain: Partial<Terrain> & { siteId?: number } = {
    nom: '',
    siteId: undefined
  };

  columns = ['nom', 'siteNom', 'actions'];

  ngOnInit(): void {
    const admin = this.authService.currentAdmin();

    if (admin?.role === 'SITE' && admin.siteId) {
      // admin SITE → uniquement son site
      this.isAdminSite.set(true);
      this.adminSiteId.set(admin.siteId);
      this.newTerrain.siteId = admin.siteId; // pré-remplir le site

      // charger uniquement son site dans le select
      this.siteService.getById(admin.siteId).subscribe({
        next: (site) => this.sites.set([site])
      });

      // charger uniquement ses terrains
      this.terrainService.getBySiteId(admin.siteId).subscribe({
        next: (data) => {
          this.terrains.set(data);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
          this.loading.set(false);
        }
      });

    } else {
      // admin GLOBAL → tout charger
      this.loadTerrains();
      this.loadSites();
    }
  }

  loadTerrains(): void {
    this.loading.set(true);
    this.terrainService.getAll().subscribe({
      next: (data) => {
        this.terrains.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  loadSites(): void {
    this.siteService.getAll().subscribe({
      next: (data) => this.sites.set(data)
    });
  }

  create(): void {
    if (!this.newTerrain.nom?.trim() || !this.newTerrain.siteId) {
      this.snackBar.open('Nom et site obligatoires', 'Fermer', {duration: 3000});
      return;
    }

    this.saving.set(true);
    this.terrainService.create(this.newTerrain, this.newTerrain.siteId!).subscribe({
      next: () => {
        this.snackBar.open('Terrain créé avec succès', 'OK', {duration: 3000});
        this.showForm.set(false);
        this.newTerrain = {
          nom: '',
          siteId: this.isAdminSite() ? this.adminSiteId()! : undefined
        };
        this.loadTerrains();
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la création';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  startEdit(terrain: Terrain): void {
    this.editingTerrain.set({...terrain});
  }

  cancelEdit(): void {
    this.editingTerrain.set(null);
  }

  update(): void {
    const terrain = this.editingTerrain();
    if (!terrain) return;

    this.saving.set(true);
    this.terrainService.update(terrain.id, terrain).subscribe({
      next: () => {
        this.snackBar.open('Terrain modifié', 'OK', {duration: 3000});
        this.editingTerrain.set(null);
        this.loadTerrains();
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la modification';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  delete(terrain: Terrain): void {
    if (!confirm(`Supprimer le terrain "${terrain.nom}" ?`)) return;

    this.terrainService.delete(terrain.id).subscribe({
      next: () => {
        this.snackBar.open('Terrain supprimé', 'OK', {duration: 3000});
        this.loadTerrains();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
