import { d3DistanceLimitedVoronoi } from './distance-limited-voronoi';
import d3Tip from './d3-tip';
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
    width: 1000
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
  const tooltipVariable = cfg.tooltipVariable;
  const numericVariables = cfg.numericVariables;

  // labels
  const xLabel = cfg.xLabel || xVariable;
  const yLabel = 'residual';
  // const xLabel = 'y\u{0302}'; // y-hat for the prediction
  // const yLabel = 'r\u{0302}'; // r-hat for the residual

  const div = d3.select(selector)
    .append('div')
    .attr('id', 'chart')
    .style('z-index', 1);

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

  const wrapper = svg.append('g').attr('class', 'chartWrapper')
    .attr('transform', `translate(${margin.left}, ${margin.top})`);

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
    .range([0, width])
    //.domain([100, 2e5]);
    // I prefer this exact scale over the true range and then using "nice"
    .domain(d3.extent(data, function(d) { return d[xVariable]; }))
    // .nice();

  // Set the new y axis range
  const yScale = d3.scaleLinear()
    .range([height, 0])
    .domain(d3.extent(data, d => d[yVariable]))
    .nice();

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
  circleGroup.selectAll('marks')
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
      .attr('cy', d => yScale(d[yVariable]))
      .attr('r', d => {
        if (typeof rVariable !== 'undefined') {
          return rScale(d[rVariable])
        } 
        return '2'; 
      });

  //
  // Tooltips
  //

  const tip = d3Tip()
    .attr('class', 'd3-tip')
    .html(d => {
      return `<div style='background-color: white; padding: 5px; border-radius: 6px;
        border-style: solid; border-color: #D1D1D1; border-width: 1px;'>
        <span style='font-size: 11px; text-align: center;'>${tooltipVariable} ${d.datum[tooltipVariable]}</span>
        </div>`
    });

  svg.call(tip);

  // // Show the tooltip on the hovered over circle
  // function showTooltip(d) {
  //   // Save the circle element (so not the voronoi which is triggering the hover event)
  //   // in a variable by using the unique class of the voronoi (CountryCode)
  //   const element = d3.selectAll(`.marks.${d.CountryCode}`);
  //   // skip tooltip creation if already defined
  //   const existingTooltip = $('.popover');
  //   if (existingTooltip !== null
  //       && existingTooltip.length > 0
  //       && existingTooltip.text() === d.Country) {
  //     return;
  //   }
  //   // Define and show the tooltip using bootstrap popover
  //   // But you can use whatever you prefer
  //   $(element).popover({
  //     placement: 'auto top', // place the tooltip above the item
  //     container: '#chart', // the name (class or id) of the container
  //     trigger: 'manual',
  //     html: true,
  //     content() { // the html content to show inside the tooltip
  //       return `<span style='font-size: 11px; text-align: center;'>${d.Country}</span>`;
  //     }
  //   });
  //   $(element).popover('show');
  //   // Make chosen circle more visible
  //   element.style('opacity', 1);
  // }// function showTooltip

  // // Hide the tooltip when the mouse moves away
  // function removeTooltip(d) {
  //   // Save the circle element (so not the voronoi which is triggering the hover event)
  //   // in a variable by using the unique class of the voronoi (CountryCode)
  //   const element = d3.selectAll(`.marks.${d.CountryCode}`);
  //   // Hide the tooltip
  //   $('.popover').each(function () {
  //     $(this).remove();
  //   });
  //   // Fade out the bright circle again
  //   element.style('opacity', opacityCircles);
  // }// function removeTooltip

  //
  // distance-limited Voronoi
  //

  /*
    Initiate the voronoi function
    Use the same variables of the data in the .x and .y as used
    in the cx and cy of the circle call
    The clip extent will make the boundaries end nicely along
    the chart area instead of splitting up the entire SVG
    (if you do not do this it would mean that you already see
    a tooltip when your mouse is still in the axis area, which
    is confusing)
  */

  const xAccessor = d => xScale(d[xVariable]);
  const yAccessor = d => yScale(d[yVariable]);

  const limitedVoronoi = d3DistanceLimitedVoronoi()
    .x(xAccessor)
    .y(yAccessor)
    .limit(50)
    .extent([[0, 0], [width, height]]);

  // console.log('data[0]', data[0]);
  const limitedVoronoiCells = limitedVoronoi(data);


  // Initiate a group element to place the voronoi diagram in
  const limitedVoronoiGroup = wrapper.append('g')
    .attr('class', 'voronoiWrapper');

  // Create the distance-limited Voronoi diagram
  limitedVoronoiGroup.selectAll('path')
    .data(limitedVoronoiCells) // Use vononoi() with your dataset inside
    .enter().append('path')
      // .attr("d", function(d, i) { return "M" + d.join("L") + "Z"; })
      .attr('d', d => {
        // console.log('d from limitedVoronoiGroup', d);
        if (typeof d !== 'undefined') {
          return d.path;
        }
        return '';
      })
      // Give each cell a unique class where the unique part corresponds to the circle classes
      // .attr('class', d => `voronoi ${d.datum[idVariable]}`)
      .attr('class', d => {
        if (typeof d !== 'undefined') {
          return `voronoi ${d.datum[idVariable]}`;
        }
        return 'voronoi';
      })
      // .style('stroke', 'lightblue') // I use this to look at how the cells are dispersed as a check
      .style('stroke', 'none')
      .style('fill', 'none')
      .style('pointer-events', 'all')
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide);

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
    .attr('transform', `translate(${0}, ${-10})`)
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