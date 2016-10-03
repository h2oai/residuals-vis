export function translateToAggregate(vis, d) {
  let xTranslate = vis.x(+exemplar[vis.xCat]);
  let yTranslate = vis.y(+exemplar[vis.yCat]);
  console.log('xTranslate', xTranslate);
  console.log('yTranslate', yTranslate);
  return `translate(${xTranslate}, ${yTranslate})`;
}