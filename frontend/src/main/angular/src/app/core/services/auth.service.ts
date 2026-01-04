import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of, tap} from 'rxjs';
import {AuthApplicationService, AuthResponse, LoginRequest} from '../backend';
import {catchError, mapTo} from 'rxjs/operators';


@Injectable({providedIn: 'root'})
export class AuthService {

  private accessToken$ = new BehaviorSubject<string | undefined | null>(null);
  private refreshing = false;

  constructor(private http: HttpClient, private authApi: AuthApplicationService) {
  }

  token(): string | undefined | null {
    console.log(this.accessToken$.value)
    return this.accessToken$.value;
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  silentRefresh(): Observable<boolean> {
    return this.refresh().pipe(
      mapTo(true),
      catchError(() => of(false)) // brak cookie / 401 / 403 -> traktuj jako "niezalogowany"
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    const body: LoginRequest = {email, password};

    // observe domyślnie 'body' => dostajesz AuthResponse
    return this.authApi.login(body).pipe(
      tap(r => this.accessToken$.next(r.accessToken))
    );
  }

  refresh(): Observable<AuthResponse> {
    return this.authApi.refresh().pipe(
      tap(r => this.accessToken$.next(r.accessToken))
    );
  }

  logout(): Observable<void> {
    // generator zwraca Observable<any> - mapujemy na void
    return this.authApi.logout().pipe(
      tap(() => this.accessToken$.next(null)),
      // opcjonalnie: wymuś typ void
      // map(() => void 0)
    ) as unknown as Observable<void>;
  }

  setRefreshing(v: boolean) {
    this.refreshing = v;
  }

  isRefreshing(): boolean {
    return this.refreshing;
  }
}
