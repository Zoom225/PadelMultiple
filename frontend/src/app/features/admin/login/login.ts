import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '../../../core/services/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  loading = false;
  error = '';
  showPassword = false;

  login(): void {
    if (!this.email.trim() || !this.password.trim()) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login({email: this.email, password: this.password}).subscribe({
      next: () => {
        this.router.navigate(['/admin/dashboard']);
      },
      error: () => {
        this.error = 'Email ou mot de passe incorrect';
        this.loading = false;
      }
    });
  }
}
