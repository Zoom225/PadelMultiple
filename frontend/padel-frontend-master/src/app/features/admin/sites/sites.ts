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
import {MatTableModule} from '@angular/material/table';
import {SiteService} from '../../../core/services/site.service';
import {AuthService} from '../../../core/services/auth.service';
import {Site} from '../../../core/models/site.model';

@Component({
  selector: 'app-sites',
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
    MatTableModule
  ],
  templateUrl: './sites.html',
  styleUrl: './sites.css'
})
export class Sites implements OnInit {

  private siteService = inject(SiteService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  sites = signal<Site[]>([]);
  loading = signal(true);
  saving = signal(false);
  showForm = signal(false);
  editingSite = signal<Site | null>(null);
  isAdminSite = signal(false);

  newSite: Partial<Site> = {
    nom: '',
    adresse: '',
    heureOuverture: '08:00',
    heureFermeture: '22:00',
    dureeMatchMinutes: 90,
    dureeEntreMatchMinutes: 15,
    anneeCivile: new Date().getFullYear()
  };

  columns = ['nom', 'adresse', 'heureOuverture', 'heureFermeture', 'actions'];

  ngOnInit(): void {
    const admin = this.authService.currentAdmin();

    if (admin?.role === 'SITE' && admin.siteId) {
      // admin SITE → uniquement son site
      this.isAdminSite.set(true);

      this.siteService.getById(admin.siteId).subscribe({
        next: (site) => {
          this.sites.set([site]);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
          this.loading.set(false);
        }
      });

    } else {
      // admin GLOBAL → tous les sites
      this.loadSites();
    }
  }

  loadSites(): void {
    this.loading.set(true);
    this.siteService.getAll().subscribe({
      next: (data) => {
        this.sites.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  create(): void {
    if (!this.newSite.nom?.trim() || !this.newSite.adresse?.trim()) {
      this.snackBar.open('Nom et adresse obligatoires', 'Fermer', {duration: 3000});
      return;
    }

    this.saving.set(true);
    this.siteService.create(this.newSite).subscribe({
      next: () => {
        this.snackBar.open('Site créé avec succès', 'OK', {duration: 3000});
        this.showForm.set(false);
        this.resetForm();
        this.loadSites();
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la création';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  startEdit(site: Site): void {
    this.editingSite.set({...site});
  }

  cancelEdit(): void {
    this.editingSite.set(null);
  }

  update(): void {
    const site = this.editingSite();
    if (!site) return;

    if (!site.nom?.trim() || !site.adresse?.trim()) {
      this.snackBar.open('Nom et adresse obligatoires', 'Fermer', {duration: 3000});
      return;
    }

    this.saving.set(true);
    this.siteService.update(site.id, site).subscribe({
      next: () => {
        this.snackBar.open('Site modifié avec succès', 'OK', {duration: 3000});
        this.editingSite.set(null);
        this.ngOnInit(); // recharger selon le rôle
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la modification';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  delete(site: Site): void {
    if (!confirm(`Supprimer le site "${site.nom}" ?`)) return;

    this.siteService.delete(site.id).subscribe({
      next: () => {
        this.snackBar.open('Site supprimé', 'OK', {duration: 3000});
        this.loadSites();
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

  resetForm(): void {
    this.newSite = {
      nom: '',
      adresse: '',
      heureOuverture: '08:00',
      heureFermeture: '22:00',
      dureeMatchMinutes: 90,
      dureeEntreMatchMinutes: 15,
      anneeCivile: new Date().getFullYear()
    };
  }
}
