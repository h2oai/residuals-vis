import { tooltip } from './tooltip';
import { drawVoronoiOverlay } from './drawVoronoiOverlay';
import * as d3 from 'd3';

export function setModelTransition(selector, data, options) {
  const xVariable = options.xVariable;
  const yVariable = options.yVariable;
  const idVariable = options.idVariable;
  const responseVariable = options.responseColumn;
  const tooltipVariables = options.tooltipColumns;
  const currentAlgo = options.currentAlgo;
  const currentAlgoLabel = options.currentAlgoLabel;
  const margin = options.margin;
  const chartWidth = document.getElementById('chart').offsetWidth;
  const width = chartWidth - margin.left - margin.right;
  // const width = options.width;
  const height = options.width * 0.25;

  const xScale = d3.scaleLinear()
    .range([0, width])
    .domain(d3.extent(data, d =>  Number(d[xVariable])));

  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(d3.extent(data, d => Number(d[yVariable])))
    .nice();

  const xAxis = d3.axisBottom()
    .ticks(4)
    .tickSizeOuter(0)
    .scale(xScale);

  const yAxis = d3.axisLeft()
    .ticks(6)
    .scale(yScale);

  // console.log('yScale range', yScale.range());
  // console.log('yScale domain', yScale.domain());

  d3.select(selector)
    .on('click', click);

  function click() {
    const wrapperId = d3.select('g.independent').attr('id');
    if (wrapperId === currentAlgo) { return; };

    const marksDelay = 1000;

    // set new id
    d3.select('g.independent')
      .attr('id', currentAlgo);

    // transition y-axis
    d3.select('g.independent').select('g.axis.y')
      .transition()
      .delay(0)
      .duration(1000)
      .call(yAxis);

    // transition y-axis label vertical position
    d3.select('g.independent').select('text.y.title')
      .transition()
      .delay(0)
      .duration(1000)
      .attr('transform', `translate(${-30}, ${yScale(0)})`);

    // transition x-axis vertical position
    d3.select('g.independent').select('g.axis.x')
      .transition()
      .delay(0)
      .duration(1000)
      .attr('transform', `translate(${0}, ${yScale(0)})`);

    // transition marks
    d3.select('g.independent').selectAll('.marks')
      .transition()
      .delay(marksDelay)
      .duration(2000)
      .on('start', moveToNewPosition);

    // transition x-axis label
    d3.select('g.independent').select('text.x.title')
      .transition()
      .duration(1000)
      .style('opacity', 0)
      .transition()
      .duration(0)
      .delay(5000 + marksDelay)
      .text(`${xVariable} (${responseVariable})`)
      .transition()
      .duration(1000)
      .style('opacity', 1);

    // transition model label
    d3.select('g.independent').select('text.modelLabel')
      .transition()
      .duration(1000)
      .style('opacity', 0)
      .transition()
      .duration(0)
      .delay(5000 + marksDelay)
      .text(`${currentAlgoLabel}`)
      .transition()
      .duration(1000)
      .style('opacity', 0.15);

    // transition x-axis
    d3.select('g.independent').select('g.axis.x')
      .transition()
      .delay(2000 + marksDelay)
      .duration(1000)
      .call(xAxis);

    // set the tooltip for with new tooltipVariables
    tooltipVariables[3].name = xVariable;
    const tip = tooltip(tooltipVariables);
    d3.select('svg.independent').call(tip);

    // update Voronoi overlay for tooltips
    const wrapper = d3.select('g.independent');

    // remove the existing Voronoi overlay
    wrapper.selectAll('g.voronoiWrapper').remove();

    const voronoiOptions = {
      xVariable,
      yVariable,
      idVariable,
      xScale,
      yScale,
      width,
      height,
      tip
    };

    // draw a new Voronoi overlay
    drawVoronoiOverlay(wrapper, data, voronoiOptions);

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
