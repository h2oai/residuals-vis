export function translateToAggregate(d) {
  let xTranslate = x(+exemplar[xCat]);
  let yTranslate = y(+exemplar[yCat]);
  console.log('xTranslate', xTranslate);
  console.log('yTranslate', yTranslate);
  return `translate(${xTranslate}, ${yTranslate})`;
}