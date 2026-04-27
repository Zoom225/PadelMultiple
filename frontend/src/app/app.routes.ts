import {Routes} from '@angular/router';
import {adminGuard} from './core/guards/admin.guard';
import {membreGuard} from './core/guards/membre.guard';

export const routes: Routes = [

  // redirect par défaut
  {path: '', redirectTo: 'membre/login', pathMatch: 'full'},

  // ─── Interface Membre ────────────────────────────────────────
  {
    path: 'membre',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/membre/identification/identification')
            .then(m => m.Identification)
      },
      {
        path: 'matches',
        canActivate: [membreGuard],
        loadComponent: () =>
          import('./features/membre/matches/matches')
            .then(m => m.Matches)
      },
      {
        path: 'reservations',
        canActivate: [membreGuard],
        loadComponent: () =>
          import('./features/membre/reservations/reservations')
            .then(m => m.Reservations)
      },
      {
        path: 'create-match',          // ← ajouter uniquement ça
        canActivate: [membreGuard],
        loadComponent: () =>
          import('./features/membre/create-match/create-match')
            .then(m => m.CreateMatch)
      },
      {
        path: 'mes-matches',
        canActivate: [membreGuard],
        loadComponent: () =>
          import('./features/membre/mes-matches/mes-matches')
            .then(m => m.MesMatches)
      }
    ]
  },

  // ─── Interface Admin ─────────────────────────────────────────
  {
    path: 'admin',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/admin/login/login')
            .then(m => m.Login)
      },
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard')
            .then(m => m.Dashboard)
      },
      {
        path: 'sites',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/sites/sites')
            .then(m => m.Sites)
      },
      {
        path: 'terrains',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/terrains/terrains')
            .then(m => m.Terrains)
      },
      {
        path: 'membres',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/membres/membres')
            .then(m => m.Membres)
      },
      {
        path: 'matches',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/matches/matches')
            .then(m => m.Matches)
      }
    ]
  },

  // route inconnue → redirect
  {path: '**', redirectTo: 'membre/login'}
];
