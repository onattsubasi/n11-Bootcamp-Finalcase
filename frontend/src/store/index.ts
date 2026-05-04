import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { createAuthSlice, AuthState, AuthAction } from './authSlice';
import { createUISlice, UIState, UIAction } from './uiSlice';
import { flattenActions } from './utils';

export type StoreState = AuthState & UIState;
export type StoreActions = AuthAction & UIAction;
export type GlobalStore = StoreState & StoreActions;

const initialState: StoreState = {
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

export const useStore = create<GlobalStore>()(
  persist(
    (set, get, api) => ({
      ...initialState,
      ...flattenActions<StoreActions>([
        createAuthSlice(set, get),
        createUISlice(set, get),
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
