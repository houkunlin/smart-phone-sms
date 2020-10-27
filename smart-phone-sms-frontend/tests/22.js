function test() {
  const yours = 666;
  const sequence = [1, 2, 3, 4, 5, 6, 7, 8, 9]
    .map(n => Math.pow(2, n))
    .sort((a, b) => a > b ? -1 : (a < b) ? 1 : 0);
  const cipher = [];

  const keys = [5, 1, 1, -92, -490];


  const str = '自由自在功不可没卓有成效大吉大利ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const dictionary = [];
  for (let i = 0; i < str.length; i += 1) {
    dictionary.push(str[i]);
  }

  console.log(dictionary)

  sequence.reduce((total, piece) => {
    if (total + piece >= yours) return total;
    cipher.push(piece);
    // eslint-disable-next-line no-param-reassign,no-return-assign
    return total += piece;
  }, 0);
  cipher.sort((a, b) => a > b ? 1 : (a < b) ? -1 : 0);
  return cipher.map((atom, idx) => dictionary[atom + keys[idx]]);
}


console.log(test());
