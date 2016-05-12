export function translatePoints(vis, d) {
  const translateString = `translate(${vis.x(+d[vis.xCat])}, ${vis.y(+d[vis.yCat])})`;
  return translateString;
}
