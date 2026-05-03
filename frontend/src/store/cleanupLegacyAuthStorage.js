const LEGACY_AUTH_KEYS = ['accessToken', 'auth-storage', 'app-storage'];

export const cleanupLegacyAuthStorage = () => {
  for (const key of LEGACY_AUTH_KEYS) {
    localStorage.removeItem(key);
  }
};
