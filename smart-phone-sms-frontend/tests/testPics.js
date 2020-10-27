function aa() {
  class Pizza {
    // private yours = 0;
    // private sequence = [];
    // private cipher = [];
    // private keys = [];

    constructor(yours) {
      this.yours = yours;
      this.sequence = [1, 2, 3, 4, 5, 6, 7, 8, 9].map(n => Math.pow(2, n))
        .sort((a, b) => a > b ? -1 : (a < b) ? 1 : 0);
      this.cipher = [];
      this.keys = [];
    }

    getCipher() {
      this.sequence.reduce((total, piece) => {
        if (total + piece >= this.yours) return total;
        this.cipher.push(piece);
        // eslint-disable-next-line no-param-reassign,no-return-assign
        return total += piece;
      }, 0);
      this.cipher.sort((a, b) => a > b ? 1 : (a < b) ? -1 : 0);
      return this;
    }

    decrypt(dictionary) {
      return this.cipher.map((atom, idx) => dictionary[atom + this.keys[idx]]);
    }

    getKeys() {
      this.keys = [5, 1, 1, -92, -490];
      return this;
    }
  }

  const str = '自由自在功不可没卓有成效大吉大利ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const dictionary = [];
  for (let i = 0; i < str.length; i += 1) {
    dictionary.push(str[i]);
  }

  return new Pizza(666).getKeys().getCipher().decrypt(dictionary);
}

console.log(aa());
