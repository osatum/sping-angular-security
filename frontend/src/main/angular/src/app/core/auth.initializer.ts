import { inject } from '@angular/core';

import { firstValueFrom } from 'rxjs';
import {AuthService} from './services/auth.service';

export function authInitializer() {
  const auth = inject(AuthService);

  return async () => {
    // próba odzyskania sesji po reloadzie
    await firstValueFrom(auth.silentRefresh());
  };
}
