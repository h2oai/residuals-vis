import { tooltip } from './tooltip';
import { drawTitle } from './drawTitle';
import { drawVoronoiOverlay } from './drawVoronoiOverlay';
import * as d3 from 'd3';

export function setModelTransition(selector, data, options) {
  const xVariable = options.xVariable;
  const yVariable = options.yVariable;
  const idVariable = options.idVariable;
  const responseVariable = options.responseColumn;
  const tooltipVariables = options.tooltipColumns;
  const algos = options.algos;
  const currentAlgo = options.currentAlgo;
  const currentAlgoLabel = options.currentAlgoLabel;
  const projectTitle = options.projectTitle;
  const projectLink = options.projectLink;
  const dataText = options.dataText;
  const globalExtents = options.globalExtents;
  const margin = options.margin;
  const chartWidth = document.getElementById('chart').offsetWidth;
  const width = chartWidth - margin.left - margin.right;
  // const width = options.width;
  const height = options.width * 0.25;

  // retrieve global extents
  const xExtent = globalExtents[0];
  const yExtent = globalExtents[1];

  const xScale = d3.scaleLinear()
    .range([0, width])
    .domain(xExtent);

  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(yExtent)
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

    // transition marks
    d3.select('g.independent').selectAll('.marks')
      .transition()
      .delay(marksDelay)
      .duration(2000)
      .on('start', moveToNewPosition);

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

    // update the subtitle
    const subtitleOptions = {
      projectTitle,
      projectLink,
      currentAlgoLabel,
      dataText
    }
    drawTitle('p#subTitle', subtitleOptions);

    function moveToNewPosition() {
      d3.active(this)
        .attr('cx', d => xScale(d[xVariable]))
        .attr('cy', d => yScale(d[yVariable]));
    }   
  }
}
