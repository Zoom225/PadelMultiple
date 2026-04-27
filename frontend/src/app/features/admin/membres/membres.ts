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
import {MatChipsModule} from '@angular/material/chips';
import {MembreService} from '../../../core/services/membre.service';
import {SiteService} from '../../../core/services/site.service';
import {Membre, TypeMembre} from '../../../core/models/membre.model';
import {Site} from '../../../core/models/site.model';

@Component({
  selector: 'app-membres',
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
    MatTableModule,
    MatChipsModule
  ],
  templateUrl: './membres.html',
  styleUrl: './membres.css'
})
export class Membres implements OnInit {

  private membreService = inject(MembreService);
  private siteService = inject(SiteService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  membres = signal<Membre[]>([]);
  sites = signal<Site[]>([]);
  loading = signal(true);
  saving = signal(false);
  showForm = signal(false);
  editingMembre = signal<Membre | null>(null);

  newMembre: any = {
    matricule: '',
    nom: '',
    prenom: '',
    email: '',
    typeMembre: TypeMembre.GLOBAL,
    siteId: null
  };

  readonly TypeMembre = TypeMembre;
  columns = ['matricule', 'nom', 'prenom', 'type', 'site', 'solde', 'penalite', 'actions'];

  ngOnInit(): void {
    this.loadSites();
    this.loadMembres();
  }

  loadSites(): void {
    this.siteService.getAll().subscribe({
      next: (data) => this.sites.set(data)
    });
  }

  loadMembres(): void {
    this.loading.set(true);
    this.membreService.getAll().subscribe({
      next: (data) => {
        this.membres.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', {duration: 3000});
        this.loading.set(false);
      }
    });
  }

  create(): void {
    if (!this.newMembre.matricule?.trim() || !this.newMembre.nom?.trim()) {
      this.snackBar.open('Matricule et nom obligatoires', 'Fermer', {duration: 3000});
      return;
    }
    if (this.newMembre.typeMembre === TypeMembre.SITE && !this.newMembre.siteId) {
      this.snackBar.open('Un membre SITE doit avoir un site', 'Fermer', {duration: 3000});
      return;
    }

    this.saving.set(true);
    this.membreService.create(this.newMembre).subscribe({
      next: () => {
        this.snackBar.open('Membre créé avec succès', 'OK', {duration: 3000});
        this.showForm.set(false);
        this.resetForm();
        this.loadMembres();
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la création';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  startEdit(membre: Membre): void {
    this.editingMembre.set({...membre});
  }

  cancelEdit(): void {
    this.editingMembre.set(null);
  }

  update(): void {
    const membre = this.editingMembre();
    if (!membre) return;

    this.saving.set(true);
    this.membreService.update(membre.id, membre).subscribe({
      next: () => {
        this.snackBar.open('Membre modifié', 'OK', {duration: 3000});
        this.editingMembre.set(null);
        this.loadMembres();
        this.saving.set(false);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la modification';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
        this.saving.set(false);
      }
    });
  }

  delete(membre: Membre): void {
    if (!confirm(`Supprimer le membre "${membre.matricule}" ?`)) return;

    this.membreService.delete(membre.id).subscribe({
      next: () => {
        this.snackBar.open('Membre supprimé', 'OK', {duration: 3000});
        this.loadMembres();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression';
        this.snackBar.open(msg, 'Fermer', {duration: 4000});
      }
    });
  }

  getTypeColor(type: TypeMembre): string {
    switch (type) {
      case TypeMembre.GLOBAL:
        return 'primary';
      case TypeMembre.SITE:
        return 'accent';
      case TypeMembre.LIBRE:
        return 'warn';
      default:
        return '';
    }
  }

  resetForm(): void {
    this.newMembre = {
      matricule: '',
      nom: '',
      prenom: '',
      email: '',
      typeMembre: TypeMembre.GLOBAL,
      siteId: null
    };
  }

  goBack(): void {
    this.router.navigate(['/admin/dashboard']);
  }
}
