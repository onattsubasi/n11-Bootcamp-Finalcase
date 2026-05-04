import { StoreSetter, StoreGetter } from './types';

export interface AuthState {
  accessToken: string | null;
  accessTokenExpiresAt: string | null;
  userId: string | null;
  email: string | null;
  roles: string[];
  isAuthenticated: boolean;
}

export class AuthActionImpl {
  readonly #set: StoreSetter<AuthState>;
  readonly #get: StoreGetter<AuthState>;

  constructor(set: any, get: any) {
    this.#set = set;
    this.#get = get;
  }

  setAuth = (authState: Partial<AuthState>) =>
    this.#set({
      accessToken: authState?.accessToken ?? null,
      accessTokenExpiresAt: authState?.accessTokenExpiresAt ?? null,
      userId: authState?.userId ?? null,
      email: authState?.email ?? null,
      roles: Array.isArray(authState?.roles) ? authState.roles : [],
      isAuthenticated: Boolean(authState?.accessToken),
    });

  clearAuth = () =>
    this.#set({
      accessToken: null,
      accessTokenExpiresAt: null,
      userId: null,
      email: null,
      roles: [],
      isAuthenticated: false,
    });
}

export type AuthAction = Pick<AuthActionImpl, keyof AuthActionImpl>;

export const createAuthSlice = (set: any, get: any) => new AuthActionImpl(set, get);
