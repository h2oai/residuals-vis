import * as d3 from 'd3';

export function setModelTransition(selector, data, options) {
  const yVariable = options.yVariable;
  const width = options.width;
  const height = width * 0.25;

  // Set the new y axis range
  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(d3.extent(data, d => d[yVariable]))
    .nice();

  d3.select(selector)
    .on('click', click);

  function click() {
    d3.select('g.predict').selectAll('.marks')
      .transition()
      .duration(2000)
      .attr('cy', yScale(100));
  }
}
