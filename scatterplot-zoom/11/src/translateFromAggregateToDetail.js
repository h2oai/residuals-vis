function translateFromAggregateToDetail(d) {
  let xTranslate = x(+d[xCat]) - x(+exemplar[xCat]);
  let yTranslate = y(+d[yCat]) + y(+exemplar[yCat]);
  return `translate(${xTranslate}, ${yTranslate})`;
}