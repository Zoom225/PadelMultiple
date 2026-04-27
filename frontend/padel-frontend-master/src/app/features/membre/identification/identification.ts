import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MembreService} from '../../../core/services/membre.service';
import {AuthService} from '../../../core/services/auth.service';

@Component({
  selector: 'app-identification',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './identification.html',
  styleUrl: './identification.css'
})
export class Identification {

  private membreService = inject(MembreService);
  private authService = inject(AuthService);
  private router = inject(Router);

  matricule = '';
  loading = false;
  error = '';

  identify(): void {
    if (!this.matricule.trim()) {
      this.error = 'Veuillez entrer votre matricule';
      return;
    }

    this.loading = true;
    this.error = '';

    this.membreService.getByMatricule(this.matricule.trim()).subscribe({
      next: (membre) => {
        this.authService.loginMembre(membre);
        this.router.navigate(['/membre/matches']);
      },
      error: () => {
        this.error = 'Matricule introuvable. Vérifiez votre matricule.';
        this.loading = false;
      }
    });
  }
}
