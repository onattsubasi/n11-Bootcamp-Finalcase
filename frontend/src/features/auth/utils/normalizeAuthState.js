export const normalizeAuthState = (payload) => {
  const source = payload?.data ?? payload ?? {};

  return {
    accessToken: source.accessToken ?? source.token ?? null,
    accessTokenExpiresAt: source.expiresInSeconds
      ? Date.now() + source.expiresInSeconds * 1000
      : (source.accessTokenExpiresAt ?? null),
    userId: source.userId ?? source.id ?? null,
    email: source.email ?? null,
    roles: Array.isArray(source.roles) ? source.roles : [],
  };
};
