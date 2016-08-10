import * as d3 from 'd3';

export function drawFlexContainer(selector) {
  d3.select(selector)
    .append('div')
    .classed('flex-container', true);
}
