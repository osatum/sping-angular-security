import {inject} from '@angular/core';
import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {catchError, switchMap, throwError} from 'rxjs';
import {AuthService} from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);

  const token = auth.token();
  const isAuth = req.url.includes('/api/auth/');

  const authReq = (!isAuth && token)
    ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}})
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || isAuth) {
        return throwError(() => err);
      }


      return auth.refreshOnce().pipe(
        switchMap(() => {
          const newToken = auth.token();
          if (!newToken) {
            return throwError(() => err);
          }
          return next(
            req.clone({
              setHeaders: {Authorization: `Bearer ${newToken}`}
            })
          );
        })
      );
    })
  );
};
