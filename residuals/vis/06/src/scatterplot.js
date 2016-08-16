import { tooltip } from './tooltip'; 
import { drawVoronoiOverlay } from './drawVoronoiOverlay'; 
import * as d3 from 'd3';
import _ from 'lodash';

export function scatterplot(selector, inputData, options) {
  //
  // Set-up
  //

  // vanilla JS window width and height
  const wV = window;
  const dV = document;
  const eV = dV.documentElement;
  const gV = dV.getElementsByTagName('body')[0];
  const xV = wV.innerWidth || eV.clientWidth || gV.clientWidth;
  const yV = wV.innerHeight || eV.clientHeight || gV.clientHeight;

  // Quick fix for resizing some things for mobile-ish viewers
  const mobileScreen = (xV < 500);

  // set default configuration
  const cfg = {
    margin: { left: 120, top: 20, right: 80, bottom: 20 },
    width: 1000,
    animateFromZero: undefined
  };

  // Put all of the options into a variable called cfg
  if (typeof options !== 'undefined') {
    for (const i in options) {
      if (typeof options[i] !== 'undefined') { cfg[i] = options[i]; }
    }// for i
  }// if

  // map variables to our dataset
  const xVariable = cfg.xVariable;
  const yVariable = cfg.yVariable || 'residual';
  const rVariable = undefined;
  const idVariable = cfg.idVariable;
  const groupByVariable = undefined;
  const currentAlgo = cfg.currentAlgo;
  const currentAlgoLabel = cfg.currentAlgoLabel;
  const tooltipVariables = cfg.tooltipColumns;
  const numericVariables = cfg.numericColumns;
  const responseVariable = cfg.responseColumn;
  const independent = cfg.independent;
  const globalExtents = cfg.globalExtents;
  const animateFromZero = cfg.animateFromZero;

  // labels
  let xLabel = cfg.xLabel || xVariable;
  if (typeof responseVariable !== 'undefined') { 
    xLabel = `${xLabel} (${responseVariable})` 
  }
  const yLabel = 'residual';
  // const xLabel = 'y\u{0302}'; // y-hat for the prediction
  // const yLabel = 'r\u{0302}'; // r-hat for the residual

  const div = d3.select(selector)
    .append('div')
    .attr('id', 'chart');

  // Scatterplot
  const margin = cfg.margin;
  const chartWidth = document.getElementById('chart').offsetWidth; 
  const width = chartWidth - margin.left - margin.right;
  const height = cfg.width * 0.25;
  // const maxDistanceFromPoint = 50;

  const svg = div
    .append('svg')
      .attr('width', (width + margin.left + margin.right))
      .attr('height', (height + margin.top + margin.bottom));

  const wrapper = svg.append('g')
    .classed('chartWrapper', true)
    .classed(`${xVariable}`, true)
    .attr('transform', `translate(${margin.left}, ${margin.top})`);

  if (independent) {
    svg.classed('independent', true);
    wrapper.classed('independent', true);
    wrapper.attr('id', currentAlgo);

    // draw model label
    wrapper.append('g')
      .attr('transform', `translate(${20}, ${45})`)
      .append('text')
      .classed('modelLabel', true)
      .style('font-size', '40px')
      .style('font-weight', 400)
      .style('opacity', 0.15)
      .style('fill', 'gray')
      .style('font-family', 'Work Sans, sans-serif')
      .text(`${currentAlgoLabel}`);
  }

  //
  // Initialize Axes & Scales
  //

  const opacityCircles = 0.3; // 0.7;

  // Set the color for each region
  const color = d3.scaleOrdinal()
    .range([
      '#1f78b4',
      '#ff7f00',
      '#33a02c',
      '#e31a1c',
      '#6a3d9a',
      '#b15928',
      '#a6cee3',
      '#fdbf6f',
      '#b2df8a',
      '#fb9a99',
      '#cab2d6',
      '#ffff99'
    ]);

  // parse strings to numbers
  let data = _.cloneDeep(inputData);

  data.forEach(d => {
    numericVariables.forEach(e => {
      d[e] = Number(d[e]);
    })
  })

  // Set the new x axis range
  const xScale = d3.scaleLinear()
    .range([0, width]);

  // Set the new y axis range
  const yScale = d3.scaleLinear()
    .range([height, 0]);
 
  if (typeof globalExtents !== 'undefined') {
    // retrieve global extents
    const xExtent = globalExtents[0];
    const yExtent = globalExtents[1];

    // set scale domains with global extents
    xScale.domain(xExtent);
    yScale
      .domain(yExtent)
      .nice();
  } else {
    // set scale domains from the local extent
    xScale
      .domain(d3.extent(data, d => d[xVariable]))
      // .nice();
    yScale
      .domain(d3.extent(data, d => d[yVariable]))
      .nice();
  }

  // Set new x-axis
  const xAxis = d3.axisBottom()
    .ticks(4)
    .tickSizeOuter(0)
    // .tickFormat(d => // Difficult function to create better ticks
    //   xScale.tickFormat((mobileScreen ? 4 : 8), e => {
    //     const prefix = d3.format(',.0s');
    //     return `${prefix(e)}`;
    //   })(d))
    .scale(xScale);

  // Append the x-axis
  wrapper.append('g')
    .attr('class', 'x axis')
    .attr('transform', `translate(${0}, ${yScale(0)})`)
    .call(xAxis);

  const yAxis = d3.axisLeft()
    .ticks(6)  // Set rough # of ticks
    .scale(yScale);

  // Append the y-axis
  wrapper.append('g')
      .attr('class', 'y axis')
      .attr('transform', `translate(${0}, ${0})`)
      .call(yAxis);

  // Scale for the bubble size
  if(typeof rVariable !== 'undefined') {
    const rScale = d3.scaleSqrt()
      .range([
        mobileScreen ? 1 : 2,
        mobileScreen ? 10 : 16
      ])
      .domain(d3.extent(data, d => d[rVariable]));
  }

  //
  // Scatterplot Circles
  //

  // Initiate a group element for the circles
  const circleGroup = wrapper.append('g')
    .attr('class', 'circleWrapper');

  // Place the country circles
  const circles = circleGroup.selectAll('marks')
    .data(() => {
        if (typeof rVariable !== 'undefined') {
          // Sort so the biggest circles are below
          return data.sort((a, b) => b[rVariable] > a[rVariable]);
        }
        return data;
      }
    )
    .enter().append('circle')
      .attr('class', (d) => `marks ${d[idVariable]}`)
      .style('fill-opacity', opacityCircles)
      .style('fill', d => {
        if (typeof groupByVariable !== 'undefined') {
          return color(d[groupByVariable]);
        } 
        return color.range()[0];
      })
      .attr('cx', d => {
        return xScale(d[xVariable]);
      })
      .attr('cy', d => {
        if (typeof animateFromZero !== 'undefined') {
          return yScale(0);
        } else {
          return yScale(d[yVariable]);
        }
        
      })
      .attr('r', d => {
        if (typeof rVariable !== 'undefined') {
          return rScale(d[rVariable])
        } 
        return '2'; 
      });

  if (typeof animateFromZero !== 'undefined') {
    circles
      .transition()
      .delay(2000)
      .duration(2000)
      .attr('cy', d => yScale(d[yVariable]));
  }

  //
  // Tooltips
  //

  const tip = tooltip(tooltipVariables);
  svg.call(tip);

  //
  // distance-limited Voronoi overlay
  //

  const voronoiOptions = {
    xVariable,
    yVariable,
    idVariable,
    xScale,
    yScale,
    width,
    height,
    tip
  }
  drawVoronoiOverlay(wrapper, data, voronoiOptions);

  //
  // Initialize Labels
  //

  const xlabelText = xLabel || xVariable;
  const yLabelText = yLabel || yVariable;

  // Set up X axis label
  wrapper.append('g')
    .append('text')
    .attr('class', 'x title')
    .attr('text-anchor', 'start')
    .style('font-size', `${mobileScreen ? 8 : 12}px`)
    .style('font-weight', 600)
    .attr('transform', `translate(${30}, ${-10})`)
    .text(`${xlabelText}`);

  // Set up y axis label
  wrapper.append('g')
    .append('text')
    .attr('class', 'y title')
    .attr('text-anchor', 'end')
    .attr('dy', '0.35em')
    .style('font-size', `${mobileScreen ? 8 : 12}px`)
    // .attr('transform', 'translate(18, 0) rotate(-90)')
    .attr('transform', `translate(${-30}, ${yScale(0)})`)
    .text(`${yLabelText}`);

  //
  // Hide axes on click
  //
  let axisVisible = true;

  function click() {
    if (axisVisible) {
      d3.selectAll('.y.axis')
        .style('opacity', 0);
      d3.selectAll('.x.axis text')
        .style('opacity', 0);
      d3.selectAll('.x.axis .tick')
        .style('opacity', 0);
      axisVisible = false;
    } else {
      d3.selectAll('.axis')
        .style('opacity', 1);
      d3.selectAll('.x.axis text')
        .style('opacity', 1);
      d3.selectAll('.x.axis .tick')
        .style('opacity', 1);
      axisVisible = true;
    }
  }

  d3.selectAll('.chartWrapper')
    .on('click', () => {
      click();
    });

}