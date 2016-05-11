/* eslint-disable */
import { translatePoints } from './translatePoints';
import { updateDetailData } from './updateDetailData';
import d3 from 'd3';
import d3Tip from "d3-tip";
d3.tip = d3Tip;

export function drawScatterplot() {

const margin = { top: 50, right: 300, bottom: 50, left: 60 };
const outerWidth = 960; // 3648
const outerHeight = 500; // 1900
const width = outerWidth - margin.left - margin.right;
const height = outerHeight - margin.top - margin.bottom;

let x = d3.scale.linear()
  .range([0, width]).nice();

let y = d3.scale.linear()
  .range([height, 0]).nice();

let rScale = d3.scale.linear()
  .range([0, 3])

let xCat = 'C10',
  yCat = 'C1',
  rCat = 'C2',
  colorCat = 'C3';

let fileName = 'zoom0.csv'
d3.csv(fileName, function(data) {

  let exemplar = data
    .filter(function (d) {
      return +d.C10 === 6279 && +d.C1 === 2596;
    })[0];
  console.log('exemplar', exemplar);

  // TODO define this in terms of the max point radius
  let domainPaddingFactor = 0.1;

  let xMax = d3.max(data, d => +d[xCat]);
  let xMin = d3.min(data, d => +d[xCat]);
  let xExtent = xMax - xMin;
  let xDMax = xMax + (xExtent * domainPaddingFactor);
  let xDMin = xMin - (xExtent * domainPaddingFactor);

  let yMax = d3.max(data, d => +d[yCat]);
  let yMin = d3.min(data, d => +d[yCat]);
  let yExtent = yMax - yMin;
  let yDMax = yMax + (yExtent * domainPaddingFactor);
  let yDMin = yMin - (yExtent * domainPaddingFactor);

  x.domain([xDMin, xDMax]);
  y.domain([yDMin, yDMax]);

  console.log('data', data);
  console.log('x.domain()', x.domain());
  console.log('y.domain()', y.domain());

  let xAxis = d3.svg.axis()
    .scale(x)
    .orient('bottom')
    .tickSize(-height);

  let yAxis = d3.svg.axis()
    .scale(y)
    .orient('left')
    .tickSize(-width);

  let color = d3.scale.category10();

  let tip = d3.tip()
    .attr('class', 'd3-tip')
    .offset([-10, 0])
    .html(d => `${xCat}: ${d[xCat]} <br> ${yCat}: ${d[yCat]}`);

  let zoomBeh = d3.behavior.zoom()
    .x(x)
    .y(y)
    .scaleExtent([0, 500])
    .on('zoom', zoom)
    .on('zoomend', zoomend);

  let svg = d3.select('#scatter')
  .append('svg')
    .attr('width', outerWidth)
    .attr('height', outerHeight)
  .append('g')
    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
    .call(zoomBeh);

  svg.call(tip);

  svg.append('rect')
    .attr('width', width)
    .attr('height', height);

  svg.append('g')
    .classed('x axis', true)
    .attr('transform', 'translate(0,' + height + ')')
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

  let objects = svg.append('svg')
    .classed('objects', true)
    .attr('width', width)
    .attr('height', height);

  objects.append('svg:line')
    .classed('axisLine hAxisLine', true)
    .attr('x1', 0)
    .attr('y1', 0)
    .attr('x2', width)
    .attr('y2', 0)
    .attr('transform', 'translate(0,' + height + ')');

  objects.append('svg:line')
    .classed('axisLine vAxisLine', true)
    .attr('x1', 0)
    .attr('y1', 0)
    .attr('x2', 0)
    .attr('y2', height);

  let dots = objects.selectAll('.dot')
    .data(data)
  .enter().append('circle')
    .classed('dot', true)
    // .attr('r', function (d) { 
    //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI); 
    // })
    .attr('r', d => {
      if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 4 };
      return 2;
    })
    .attr('transform', function (d) {
      return translatePoints(d, x, xCat, y, yCat);
    })
    // .style('fill', d => color(d[colorCat]); })
    .style('fill', function(d) {
    if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 'steelblue' };
    return 'darkgray';
    })
    .style('fill-opacity', function(d) {
    if (d.C10 === exemplar[xCat] && d.C1 === exemplar[yCat]) { return 1 };
    return 0.2;
    })
    .on('mouseover', tip.show)
    .on('mouseout', tip.hide);

  dots.classed('aggregate', true);

  let legend = svg.selectAll('.legend')
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
 
  // declare some global variables
  let responseData;
  let detailData;

  // call API to get detail data
  let queryUrl = 'http://mr-0xc8:55555/3/Frames/members_exemplar0?column_offset=0&column_count=10';

  let xhr = d3.xhr(queryUrl, "application/json", (error, response) => {
    responseData = JSON.parse(response.response);
    console.log('response', response);
    console.log('responseData', responseData);
    detailData = updateDetailData(responseData);
  });

  function zoom() {
    svg.select('.x.axis').call(xAxis);
    svg.select('.y.axis').call(yAxis);

    svg.selectAll('.dot')
      .attr('transform', function (d) {
        return translatePoints(d, x, xCat, y, yCat);
      })

    let zoomLevel = zoomBeh.scale();
    let zoomThreshold = 31.8;

    console.log('zoomLevel', zoomLevel);
    if (zoomLevel > zoomThreshold) {
      if (d3.selectAll('.detailDot')[0].length === 0) {
      let detailDots = objects.selectAll('.detailDot')
        .data(detailData)
      .enter().append('circle')
        .classed('dot', true)
        .classed('detailDot', true)
        // .attr('r', function (d) { 
        //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI); 
        // })
        .attr('r', 2)
        .attr('transform', function (d) {
          return translatePoints(d, x, xCat, y, yCat);
        }) // translateFromAggregateToDetail
        .style('fill', 'darkorange')
        .style('fill-opacity', 0)
        .style('stroke-opacity', 0)
        //.style('stroke', d => color(d[colorCat]); })
        .style('stroke', 'darkorange')
        // .style('stroke-width', function (d) { 
        //   return 3 * Math.sqrt(d[rCat] / Math.PI); 
        // })
        .style('stroke-width', 1)
        
      detailDots.transition()
        .duration(2000)
        //.attr('transform', translatePoints)
        .style('fill-opacity', 0.4)
        //.style('stroke-opacity', 0.8);
       
      d3.selectAll('.detailDot') 
        .on('mouseover', tip.show)
        .on('mouseout', tip.hide);
      }
    }

    if (zoomLevel < zoomThreshold) {
      if (d3.selectAll('.detailDot')[0].length > 0) {
      d3.selectAll('.detailDot').transition()
        .duration(2000)
        //.attr('transform', translatePoints) // translateToAggregate
        .style('stroke-opacity', 0)
        .style('fill-opacity', 0)
        .remove();
      }
      
    }
  }

  function zoomend() {
  
  }

  function translateToAggregate(d) {
    let xTranslate = x(+exemplar[xCat]);
    let yTranslate = y(+exemplar[yCat]);
    console.log('xTranslate', xTranslate);
    console.log('yTranslate', yTranslate);
    return `translate(${xTranslate}, ${yTranslate})`;
  }

  function translateFromAggregateToDetail(d) {
    let xTranslate = x(+d[xCat]) - x(+exemplar[xCat]);
    let yTranslate = y(+d[yCat]) + y(+exemplar[yCat]);
    return `translate(${xTranslate}, ${yTranslate})`;
  }
});
}