const LEGACY_AUTH_KEYS = ['accessToken', 'auth-storage', 'app-storage'];

export const cleanupLegacyAuthStorage = (): void => {
  for (const key of LEGACY_AUTH_KEYS) {
    globalThis.localStorage?.removeItem(key);
  }
};
