import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { catchError, switchMap, throwError, finalize } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);

  const token = auth.token();
  const isAuth = req.url.includes('/api/auth/');

  const authReq = (!isAuth && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {

      if (err.status !== 401 || isAuth || auth.isRefreshing()) {
        return throwError(() => err);
      }

      auth.setRefreshing(true);

      return auth.refresh().pipe(
        switchMap(() => {
          const newToken = auth.token();
          if (!newToken) {
            return throwError(() => err);
          }

          return next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` }
            })
          );
        }),
        finalize(() => auth.setRefreshing(false))
      );
    })
  );
};
