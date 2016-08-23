import { d3ExplodingBoxplot } from './d3-exploding-boxplot';
import * as d3 from 'd3';

export function drawExplodingBoxplot(selector, inputData, options) {
  // set default configuration
  const cfg = {
    margin: { left: 120, top: 20, right: 80, bottom: 20 },
    width: 1000,
    yVariable: 'residual',
    marks: {
      r: 2,
      fillOpacity: 0.3
    } 
  };

  // Put all of the options into a variable called cfg
  if (typeof options !== 'undefined') {
    for (const i in options) {
      if (typeof options[i] !== 'undefined') { cfg[i] = options[i]; }
    }// for i
  }// if

  // map variables to our dataset
  const xVariable = cfg.xVariable;
  const yVariable = cfg.yVariable;

  const div = d3.select(selector)
    .append('div')
    .attr('id', 'chart');

  const margin = cfg.margin;
  const chartDivWidth = document.getElementById('chart').offsetWidth; 
  const dynamicWidth = chartDivWidth - margin.left - margin.right;
  const width = dynamicWidth; 
  const specifiedWidth = cfg.width;
  const height = specifiedWidth * 0.25;
  const marksRadius = cfg.marks.r;
  const marksFillOpacity = cfg.marks.fillOpacity;

  console.log('calculated width for explodingBoxplot', width);
  console.log('calculated height for explodingBoxplot', height);

  // tell explodingBoxplot how to group, color, and position
  // the boxplots that represent our randomNormal data
  const explodingBoxplotOptions = {
    width: dynamicWidth,
    height,
    margin,
    y: yVariable,
    data: {
      group: xVariable,
      colorIndex: xVariable,
    },
    dataPoints: {
      radius: marksRadius,
      fillOpacity: marksFillOpacity
    },
    color: xVariable,
    label: '',
    axes: {
      x: {
        variable: xVariable,
        label: xVariable
      },
      y: {
        variable: yVariable,
        label: 'residual',
        ticks: 6
      }
    },
    boxColors: [
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
    ]
  };
  console.log('options for explodingBoxplot', explodingBoxplotOptions);

  // see what we are working with
  console.log('d3ExplodingBoxplot', d3ExplodingBoxplot);

  // assign a explodingBoxplot chart object to a variable called chart
  const chart = d3ExplodingBoxplot();
  
  // call the chart's data accessor function on our data
  console.log('inputData from drawExplodingBoxplot', inputData);
  chart.data(inputData);

  // call the chart's options accessor function on our options object
  chart.options(explodingBoxplotOptions);

  // call the explodingBoxplot `chart` function itself on a selection
  chart(div);

  // call the update function to render the exploding boxplots
  chart.update();
}