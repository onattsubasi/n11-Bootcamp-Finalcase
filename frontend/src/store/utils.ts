export const flattenActions = <T>(actions: any[]): T => {
  const flattened: any = {};
  
  actions.forEach((actionImpl) => {
    // Get all property names including from prototype
    let obj = actionImpl;
    while (obj && obj !== Object.prototype) {
      Object.getOwnPropertyNames(obj).forEach((key) => {
        if (key === 'constructor') return;
        
        const descriptor = Object.getOwnPropertyDescriptor(obj, key);
        if (descriptor && typeof descriptor.value === 'function') {
          flattened[key] = actionImpl[key].bind(actionImpl);
        }
      });
      obj = Object.getPrototypeOf(obj);
    }
  });

  return flattened as T;
};
