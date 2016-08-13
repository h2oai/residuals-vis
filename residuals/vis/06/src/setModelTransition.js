import * as d3 from 'd3';

export function setModelTransition(selector, data, options) {
  const yVariable = options.yVariable;
  const width = options.width;
  const height = width * 0.25;

  // Set the new y axis range
  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(d3.extent(data, d => Number(d[yVariable])))
    .nice();

  console.log('yScale range', yScale.range());
  console.log('yScale domain', yScale.domain());

  d3.select(selector)
    .on('click', click);

  let collapsed = undefined;
  function click() {
    if (typeof collapsed === 'undefined') {
      // then collapse the points to zero
      d3.select('g.independent').selectAll('.marks')
        .transition()
        .duration(2000)
        .attr('cy', yScale(0));
      collapsed = true;
    } else {
      // then expand the points to the residual value
      d3.select('g.independent').selectAll('.marks')
        .transition()
        .duration(2000)
        .attr('cy', d => yScale(Number(d[yVariable])));
      collapsed = undefined;
    }
  }
}
