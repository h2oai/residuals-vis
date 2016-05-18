import { translatePoints } from './translatePoints';
import { zoom } from './zoom';
import { zoomstart } from './zoomstart';
import { zoomend } from './zoomend';
// import { drawVoronoiPaths } from './drawVoronoiPaths';
import d3 from 'd3';
import d3Tip from 'd3-tip';
d3.tip = d3Tip;

export function plotExemplars(vis) {
  console.log('vis', vis);

  console.log('vis.exemplarData', vis.exemplarData);
  const exemplar = vis.exemplarData
    .filter(d => +d.C10 === 6279 && +d.C1 === 2596)[0];
  console.log('exemplar', exemplar);

  // TODO define this in terms of the max point radius
  const domainPaddingFactor = 0.1;

  const xMax = d3.max(vis.exemplarData, d => +d[vis.xCat]);
  const xMin = d3.min(vis.exemplarData, d => +d[vis.xCat]);
  const xExtent = xMax - xMin;
  const xDMax = xMax + (xExtent * domainPaddingFactor);
  const xDMin = xMin - (xExtent * domainPaddingFactor);

  const yMax = d3.max(vis.exemplarData, d => +d[vis.yCat]);
  const yMin = d3.min(vis.exemplarData, d => +d[vis.yCat]);
  const yExtent = yMax - yMin;
  const yDMax = yMax + (yExtent * domainPaddingFactor);
  const yDMin = yMin - (yExtent * domainPaddingFactor);

  vis.x.domain([xDMin, xDMax]);
  vis.y.domain([yDMin, yDMax]);

  console.log('vis.exemplarData', vis.exemplarData);
  console.log('vis.x.domain()', vis.x.domain());
  console.log('vis.y.domain()', vis.y.domain());

  vis.xAxis = d3.svg.axis()
    .scale(vis.x)
    .orient('bottom')
    .tickSize(-vis.height);

  vis.yAxis = d3.svg.axis()
    .scale(vis.y)
    .orient('left')
    .tickSize(-vis.width);

  const color = d3.scale.category10();

  vis.tip = d3.tip()
    .attr('class', 'd3-tip')
    .offset([-10, 0])
    .html(d => `${vis.xCat}: ${d[vis.xCat]} <br> ${vis.yCat}: ${d[vis.yCat]}`);

  // clear the page before we draw
  // find a more robust way to do this
  d3.selectAll('svg').remove();

  vis.svg = d3.select('#scatter')
    .append('svg')
    .attr('width', vis.outerWidth)
    .attr('height', vis.outerHeight)
    .append('g')
      .attr('transform', `translate(${vis.margin.left}, ${vis.margin.top})`);
      // .call(vis.zoomBeh);

  vis.svg.call(vis.tip);

  vis.svg.append('rect')
    .attr('width', vis.width)
    .attr('height', vis.height);

  vis.svg.append('g')
    .classed('x axis', true)
    .attr('transform', `translate(0, ${vis.height})`)
    .call(vis.xAxis)
    .append('text')
      .classed('label', true)
      .attr('x', vis.width)
      .attr('y', vis.margin.bottom - 10)
      .style('text-anchor', 'end')
      .text(vis.xCat);

  vis.svg.append('g')
    .classed('y axis', true)
    .call(vis.yAxis)
    .append('text')
      .classed('label', true)
      .attr('transform', 'rotate(-90)')
      .attr('y', -vis.margin.left)
      .attr('dy', '.71em')
      .style('text-anchor', 'end')
      .text(vis.yCat);

  const objects = vis.svg.append('svg')
    .classed('objects', true)
    .attr('width', vis.width)
    .attr('height', vis.height);

  objects.append('svg:line')
    .classed('axisLine hAxisLine', true)
    .attr('x1', 0)
    .attr('y1', 0)
    .attr('x2', vis.width)
    .attr('y2', 0)
    .attr('transform', `translate(0, ${vis.height})`);

  objects.append('svg:line')
    .classed('axisLine vAxisLine', true)
    .attr('x1', 0)
    .attr('y1', 0)
    .attr('x2', 0)
    .attr('y2', vis.height);

  const dots = objects.selectAll('.dot')
    .data(vis.exemplarData)
    .enter()
    .append('g')
      .attr('transform', d => translatePoints(vis, d))
      .classed('dot', true);

  dots
    .append('circle')
    .classed('dot', true)
    // .attr('r', function (d) {
    //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI);
    // })
    .attr('r', 2)
    // .attr('r', d => {
    //   if (d.C10 === exemplar[vis.xCat] && d.C1 === exemplar[vis.yCat]) { return 4; }
    //   return 2;
    // })
    .style('fill', 'darkgray')
    // .style('fill', d => color(d[colorCat]); })
    // .style('fill', d => {
    //   if (d.C10 === exemplar[vis.xCat] && d.C1 === exemplar[vis.yCat]) { return 'steelblue'; }
    //   return 'darkgray';
    // })
    .style('fill-opacity', 1)
    // .style('fill-opacity', d => {
    //   if (d.C10 === exemplar[vis.xCat] && d.C1 === exemplar[vis.yCat]) { return 1; }
    //   return 0.2;
    // })
    .on('mouseover', vis.tip.show)
    .on('mouseout', vis.tip.hide);

  dots
    .append('text')
    .style('fill', 'black')
    .style('font-size', 8)
    .text(d => d.id);

  dots.classed('aggregate', true);

  const legend = vis.svg.selectAll('.legend')
    .data(color.domain())
    .enter().append('g')
      .classed('legend', true)
      .attr('transform', (d, i) => `translate(0, ${i * 20})`);

  legend.append('circle')
    .attr('r', 3.5)
    .attr('cx', vis.width + 20)
    .attr('fill', color);

  legend.append('text')
    .attr('x', vis.width + 26)
    .attr('dy', '.35em')
    .text(d => d);

  // drawVoronoiPaths(vis, vis.exemplarData);

  vis.zoomBeh = d3.behavior.zoom()
    .x(vis.x)
    .y(vis.y)
    .scaleExtent([0, 500]);

  vis.zoomBeh
    .on('zoom', () => zoom(vis)) // this is where the action continues
    .on('zoomstart', zoomstart())
    .on('zoomend', zoomend(vis));

  vis.svg.call(vis.zoomBeh);
}
