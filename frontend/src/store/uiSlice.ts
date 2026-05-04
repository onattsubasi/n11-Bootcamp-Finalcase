import { StoreSetter, StoreGetter } from './types';

export interface UIState {
  isDrawerOpen: boolean;
}

export class UIActionImpl {
  readonly #set: StoreSetter<UIState>;
  readonly #get: StoreGetter<UIState>;

  constructor(set: any, get: any) {
    this.#set = set;
    this.#get = get;
  }

  toggleDrawer = () => this.#set((state) => ({ isDrawerOpen: !state.isDrawerOpen }));
}

export type UIAction = Pick<UIActionImpl, keyof UIActionImpl>;

export const createUISlice = (set: any, get: any) => new UIActionImpl(set, get);
