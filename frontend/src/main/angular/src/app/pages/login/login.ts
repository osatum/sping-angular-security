import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../core/services/auth.service';
import {Router} from '@angular/router';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    NgIf
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loading = false;
  error: string | null = null;

  form = new FormGroup({
    email: new FormControl('user@test.com', [Validators.required, Validators.email]),
    password: new FormControl('password', Validators.required)
  });

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading = true;
    this.error = null;

    const { email, password } = this.form.getRawValue();

    this.auth.login(email!, password!).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: e => {
        this.error = e?.error?.detail ?? 'Login failed';
        this.loading = false;
      },
      complete: () => this.loading = false
    });
  }
}
