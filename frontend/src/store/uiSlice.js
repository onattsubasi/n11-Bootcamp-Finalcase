export class UIActionImpl {
  #set; #get;

  constructor(set, get) {
    this.#set = set;
    this.#get = get;
  }

  toggleDrawer = () => this.#set((state) => ({ isDrawerOpen: !state.isDrawerOpen }));
}

export const createUISlice = (set, get) => new UIActionImpl(set, get);
