import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { createAuthSlice } from './authSlice';
import { createUISlice } from './uiSlice';
import { flattenActions } from './utils';

const initialState = {
  // Auth
  accessToken: null,
  accessTokenExpiresAt: null,
  userId: null,
  email: null,
  roles: [],
  isAuthenticated: false,
  // UI
  isDrawerOpen: false,
};

export const useStore = create(
  persist(
    (set, get, api) => ({
      ...initialState,
      ...flattenActions([
        createAuthSlice(set, get, api),
        createUISlice(set, get, api),
      ]),
    }),
    {
      name: 'zustand-auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        accessTokenExpiresAt: state.accessTokenExpiresAt,
        userId: state.userId,
        email: state.email,
        roles: state.roles,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
