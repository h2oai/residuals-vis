import * as d3 from 'd3';

export function setModelTransition(selector, data, options) {
  const xVariable = options.xVariable;
  const yVariable = options.yVariable;
  const responseVariable = options.responseColumn;
  const width = options.width;
  const height = width * 0.25;

  const xScale = d3.scaleLinear()
    .range([0, width])
    .domain(d3.extent(data, d =>  Number(d[xVariable])));

  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(d3.extent(data, d => Number(d[yVariable])))
    .nice();

  console.log('yScale range', yScale.range());
  console.log('yScale domain', yScale.domain());

  d3.select(selector)
    .on('click', click);

  function click() {
    d3.select('g.independent').select('text.x.title')
      .transition()
      .duration(1000)
      .style('opacity', 0)
      .transition()
      .duration(0)
      .delay(6000)
      .text(`${xVariable} (${responseVariable})`)
      .transition()
      .duration(1000)
      .style('opacity', 1);

    d3.select('g.independent').selectAll('.marks')
      .transition()
      .duration(2000)
      .on('start', moveToNewPosition);

  function moveToNewPosition() {
    d3.active(this)
      .attr('cy', yScale(0))
      .transition()
      .delay(1000)
      .attr('cx', d => xScale(d[xVariable]))
      .transition()
      .delay(1000)
      .attr('cy', d => yScale(d[yVariable]));
}
    
  }
}
