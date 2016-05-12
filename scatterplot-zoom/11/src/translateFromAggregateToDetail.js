function translateFromAggregateToDetail(vis, d) {
  let xTranslate = vis.x(+d[vis.xCat]) - vis.x(+exemplar[vis.xCat]);
  let yTranslate = vis.y(+d[vis.yCat]) + vis.y(+exemplar[vis.yCat]);
  return `translate(${xTranslate}, ${yTranslate})`;
}