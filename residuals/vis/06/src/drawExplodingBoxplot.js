import { d3ExplodingBoxplot } from './d3-exploding-boxplot';
import * as d3 from 'd3';

export function drawExplodingBoxplot(selector, inputData, options) {
  // set default configuration
  const cfg = {
    yVariable: 'residual'
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

  // tell explodingBoxplot how to group, color, and position
  // the boxplots that represent our randomNormal data
  const explodingBoxplotOptions = {
    y: yVariable,
    data: {
      group: xVariable,
      colorIndex: xVariable,
    },
    color: xVariable,
    label: '',
    axes: {
      x: {
        label: xVariable
      },
      y: {
        label: yVariable
      }
    }
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