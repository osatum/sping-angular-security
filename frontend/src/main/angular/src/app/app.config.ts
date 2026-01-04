import {APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners} from '@angular/core';
import {provideRouter} from '@angular/router';
import {routes} from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {authInterceptor} from './core/interceptors/auth.interceptor';
import {authInitializer} from './core/auth.initializer';
import {BASE_PATH, Configuration} from './core/backend';



export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: authInitializer
    },

    // basePath pusty => idzie na ten sam origin (4200) i proxy działa
    { provide: BASE_PATH, useValue: '' },

    // cookie ma być wysyłane
    { provide: Configuration, useValue: new Configuration({ basePath: '', withCredentials: true }) }
  ]
};
