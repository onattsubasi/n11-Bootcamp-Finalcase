export class AuthActionImpl {
  #set; #get;

  constructor(set, get) {
    this.#set = set;
    this.#get = get;
  }

  setAuth = (authState) =>
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

export const createAuthSlice = (set, get) => new AuthActionImpl(set, get);
