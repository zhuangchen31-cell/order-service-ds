const CartStore = (() => {
  const KEY = 'pinia_cart_state';
  let state = { items: [] };
  const listeners = new Set();

  function load() {
    const value = localStorage.getItem(KEY);
    state = value ? JSON.parse(value) : { items: [] };
    notify();
  }

  function save() {
    localStorage.setItem(KEY, JSON.stringify(state));
    notify();
  }

  function notify() {
    listeners.forEach(listener => listener(getState()));
  }

  function getState() {
    const totalCount = state.items.reduce((sum, item) => sum + item.quantity, 0);
    const totalAmount = state.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    return { ...state, totalCount, totalAmount };
  }

  function add(product) {
    const current = state.items.find(item => item.id === product.id);
    if (current) {
      current.quantity += 1;
    } else {
      state.items.push({
        id: product.id,
        name: product.name,
        category: product.category,
        price: Number(product.price),
        imageUrl: product.imageUrl,
        quantity: 1,
      });
    }
    save();
  }

  function remove(id) {
    state.items = state.items.filter(item => item.id !== id);
    save();
  }

  function changeQuantity(id, quantity) {
    const current = state.items.find(item => item.id === id);
    if (!current) return;
    current.quantity = Math.max(1, Number(quantity) || 1);
    save();
  }

  function clear() {
    state.items = [];
    save();
  }

  function subscribe(listener) {
    listeners.add(listener);
    listener(getState());
    return () => listeners.delete(listener);
  }

  load();
  return { getState, add, remove, changeQuantity, clear, subscribe };
})();
