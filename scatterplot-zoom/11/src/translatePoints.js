export function translatePoints(d, x, xCat, y, yCat) {
  const translateString = `translate(${x(+d[xCat])}, ${y(+d[yCat])})`;
  return translateString;
}
