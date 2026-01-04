import { Routes } from '@angular/router';
import {Secured} from './pages/secured/secured';
import {AuthGuard} from './core/guards/auth.guard';
import {Login} from './pages/login/login';
import {Home} from './pages/home/home';

export const routes: Routes = [
  {
    path: '',
    component: Home
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'secured',
    component: Secured,
    canActivate: [AuthGuard],
  }
];
