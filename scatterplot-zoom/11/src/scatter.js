import { translatePoints } from './translatePoints';
import { updateDetailData } from './updateDetailData';
import { zoom } from './zoom';
import { zoomend } from './zoomend';
import d3 from 'd3';
import d3Tip from 'd3-tip';
d3.tip = d3Tip;

export function drawScatterplot() {
  const margin = { top: 50, right: 300, bottom: 50, left: 60 };
  const outerWidth = 960; // 3648
  const outerHeight = 500; // 1900
  const width = outerWidth - margin.left - margin.right;
  const height = outerHeight - margin.top - margin.bottom;

  const x = d3.scale.linear()
    .range([0, width]).nice();

  const y = d3.scale.linear()
    .range([height, 0]).nice();

  // const rScale = d3.scale.linear()
  //   .range([0, 3]);

  const xCat = 'C10';
  const yCat = 'C1';
  // const rCat = 'C2';
  // const colorCat = 'C3';

  const fileName = 'zoom0.csv';
  d3.csv(fileName, data => {
    const exemplar = data
      .filter(d => +d.C10 === 6279 && +d.C1 === 2596)[0];
    console.log('exemplar', exemplar);

    // TODO define this in terms of the max point radius
    const domainPaddingFactor = 0.1;

    const xMax = d3.max(data, d => +d[xCat]);
    const xMin = d3.min(data, d => +d[xCat]);
    const xExtent = xMax - xMin;
    const xDMax = xMax + (xExtent * domainPaddingFactor);
    const xDMin = xMin - (xExtent * domainPaddingFactor);

    const yMax = d3.max(data, d => +d[yCat]);
    const yMin = d3.min(data, d => +d[yCat]);
    const yExtent = yMax - yMin;
    const yDMax = yMax + (yExtent * domainPaddingFactor);
    const yDMin = yMin - (yExtent * domainPaddingFactor);

    x.domain([xDMin, xDMax]);
    y.domain([yDMin, yDMax]);

    console.log('data', data);
    console.log('x.domain()', x.domain());
    console.log('y.domain()', y.domain());

    // declare some global variables
    let responseData;
    let detailData;
    console.log('detailData', detailData);

    const xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom')
      .tickSize(-height);

    const yAxis = d3.svg.axis()
      .scale(y)
      .orient('left')
      .tickSize(-width);

    const color = d3.scale.category10();

    const tip = d3.tip()
      .attr('class', 'd3-tip')
      .offset([-10, 0])
      .html(d => `${xCat}: ${d[xCat]} <br> ${yCat}: ${d[yCat]}`);

    const zoomBeh = d3.behavior.zoom()
      .x(x)
      .y(y)
      .scaleExtent([0, 500]);

    zoomBeh
      .on('zoom', () => zoom(xAxis, yAxis, x, y, xCat, yCat, zoomBeh, detailData, tip))
      .on('zoomend', zoomend);

    const svg = d3.select('#scatter')
    .append('svg')
      .attr('width', outerWidth)
      .attr('height', outerHeight)
    .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`)
      .call(zoomBeh);

    svg.call(tip);

    svg.append('rect')
      .attr('width', width)
      .attr('height', height);

    svg.append('g')
      .classed('x axis', true)
      .attr('transform', `translate(0, ${height})`)
      .call(xAxis)
    .append('text')
      .classed('label', true)
      .attr('x', width)
      .attr('y', margin.bottom - 10)
      .style('text-anchor', 'end')
      .text(xCat);

    svg.append('g')
      .classed('y axis', true)
      .call(yAxis)
    .append('text')
      .classed('label', true)
      .attr('transform', 'rotate(-90)')
      .attr('y', -margin.left)
      .attr('dy', '.71em')
      .style('text-anchor', 'end')
      .text(yCat);

    const objects = svg.append('svg')
      .classed('objects', true)
      .attr('width', width)
      .attr('height', height);

    objects.append('svg:line')
      .classed('axisLine hAxisLine', true)
      .attr('x1', 0)
      .attr('y1', 0)
      .attr('x2', width)
      .attr('y2', 0)
      .attr('transform', `translate(0, ${height})`);

    objects.append('svg:line')
      .classed('axisLine vAxisLine', true)
      .attr('x1', 0)
      .attr('y1', 0)
      .attr('x2', 0)
      .attr('y2', height);

    const dots = objects.selectAll('.dot')
      .data(data)
    .enter().append('circle')
      .classed('dot', true)
      // .attr('r', function (d) {
      //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI);
      // })
      .attr('r', d => {
        if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 4; }
        return 2;
      })
      .attr('transform', d => translatePoints(d, x, xCat, y, yCat))
      // .style('fill', d => color(d[colorCat]); })
      .style('fill', d => {
        if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 'steelblue'; }
        return 'darkgray';
      })
      .style('fill-opacity', d => {
        if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 1; }
        return 0.2;
      })
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide);

    dots.classed('aggregate', true);

    const legend = svg.selectAll('.legend')
      .data(color.domain())
    .enter().append('g')
      .classed('legend', true)
      .attr('transform', (d, i) => `translate(0, ${i * 20})`);

    legend.append('circle')
      .attr('r', 3.5)
      .attr('cx', width + 20)
      .attr('fill', color);

    legend.append('text')
      .attr('x', width + 26)
      .attr('dy', '.35em')
      .text(d => d);

    // call API to get detail data
    const queryUrl = 'http://mr-0xc8:55555/3/Frames/members_exemplar0?column_offset=0&column_count=10';

    d3.xhr(queryUrl, 'application/json', (error, response) => {
      responseData = JSON.parse(response.response);
      console.log('response', response);
      console.log('responseData', responseData);
      detailData = updateDetailData(responseData);
    });
  });
}
