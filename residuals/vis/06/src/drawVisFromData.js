import { parseData } from './parseData';
import { drawTitle } from './drawTitle';
import { getGlobalExtents } from './getGlobalExtents';
import { drawVoronoiScatterplot } from './d3-voronoi-scatterplot';
import { dropdown } from './dropdown';
import { setModelTransitionAggregated } from './setModelTransitionAggregated';

import * as d3 from 'd3';

export function drawVisFromData(error, chartOptions, ...args) {
  console.log('args', args);
  console.log('arguments', arguments);

  const numericColumns = chartOptions.numericColumns;
  const idColumn = chartOptions.idColumn;
  const aggregated = chartOptions.aggregated;
  const models = chartOptions.models;
  const projectTitle = chartOptions.projectTitle;
  const projectLink = chartOptions.projectLink;
  const currentAlgo = chartOptions.currentAlgo;
  const currentAlgoLabel = chartOptions.currentAlgoLabel;
  const dataText = chartOptions.dataText;
  const predictColumn = chartOptions.predictColumn;
  const xColumns = chartOptions.xColumns;
  const yColumn = chartOptions.yColumn;
  const width = chartOptions.width;
  const tooltipColumns = chartOptions.tooltipColumns;
  const responseColumn = chartOptions.responseColumn;
  const marks = chartOptions.marks;
  const categoricalColumns = chartOptions.categoricalColumns;
  const sortBoxplots = chartOptions.sortBoxplots;
  const problemType = chartOptions.problemType;
  const margin = { left: 120, top: 20, right: 80, bottom: 20 }; 
  const algos = chartOptions.algos;
  const projectTitleNote = chartOptions.projectTitleNote;

  console.log('models', models);

  let options;
  let data;
  let datasets = {};
  if (typeof aggregated === 'undefined') {
    options = {
      numericColumns,
      idColumn
    }
    data = parseData(inputData, options); 
  } else {
    options = {
      numericColumns,
      idColumn
    }

    models.forEach((model, i) => {
      options.idPrefix = model;
      const currentData = args[i];
      const parsedData = parseData(currentData, options)
      datasets[model] = parsedData;
    })

    console.log('datasets object', datasets);

    // set a default value for `data`
    data = datasets[models[0]];
  }

  // draw the title text
  options = {
    projectTitle,
    projectLink,
    currentAlgo,
    currentAlgoLabel,
    dataText,
    projectTitleNote
  }
  drawTitle('p#subTitle', options);

  // calcuate global extents, if not specified
  let globalExtents = {};

  // if a global extent is specified
  // use it for the predictColumn
  globalExtents[predictColumn] = chartOptions.globalExtents;

  const scatterplotXVariables = xColumns.concat([predictColumn]);
  console.log('scatterplotXVariables', scatterplotXVariables);

  scatterplotXVariables.forEach(d => {
    if (typeof globalExtents[d] === 'undefined') {
      if (typeof aggregated !== 'undefined') {
        options = {
          xVariable: d,
          yVariable: yColumn,
          combined: undefined
        }
        globalExtents[d] = getGlobalExtents(datasets, options);
      } else {
        options = {
          algos,
          combined: true
        }
        globalExtents[d] = getGlobalExtents(data, options);
      }
    }
  })
  console.log('globalExtents', globalExtents);

  // residuals vs prediction plot
  options = {
    width,
    dynamicWidth: true,
    xVariable: predictColumn,
    yVariable: yColumn,
    idVariable: idColumn,
    tooltipColumns,
    numericColumns,
    xLabelDetail: responseColumn,
    xLabelTransform: 'top',
    yLabelTransform: 'left',
    wrapperId: currentAlgo,
    wrapperLabel: currentAlgoLabel,
    dependent: true,
    globalExtents: globalExtents[predictColumn],
    marks,
    categoricalColumns,
    sortBoxplots,
    chartOptions
  }
  const scatterplotUpdateFunctions = {};
  if (problemType === 'classification') {
    drawExplodingBoxplot('.dependent-variable-plot-container', data, options);
  } else {
    d3.select('.dependent-variable-plot-container')
      .append('div')
      .attr('id', `${predictColumn}`);
    scatterplotUpdateFunctions[predictColumn] = drawVoronoiScatterplot(`#${predictColumn}`, data, options);
  }

  // get the width of the independent variable plot at the top
  const topPlotWidth = document.getElementById(`${predictColumn}`).clientWidth;
  const leftPadding = 120;
  const rightPadding = 80;
  const basisWidth = topPlotWidth - leftPadding - rightPadding;
  console.log('topPlotWidth', topPlotWidth);
  
  // residuals vs independent variables scatterplots
  xColumns.forEach(x => {
    const card = d3.select('.scatterplot-container')
      .append('div')
      .classed('card', true)
      .style('display', 'flex')
      .style('flexDirection', 'row')
      .style('alignItems', 'flex-start')
      .style('justifyContent', 'space-between');
    
    // description
    const textBox = card.append('div')
      .attr('id', `${x}Text`)
      .style('border', '1px solid lightgray')
      .style('padding', '5px')
      .style('margin-left', '5px')
      .style('margin-top', '5px')
      .style('margin-bottom', '5px')
      .style('width', `${basisWidth * 0.2}px`);

    textBox.append('p')
      // .style('white-space', 'nowrap')
      .style('font-weight', '600')
      .style('padding', '5px')
      .html(`${x}<br>(type)<br>(common values)`);

    // plot
    card.append('div')
      .attr('id', `${x}`)
      .style('border', '1px solid lightgray')
      .style('padding', '5px')
      .style('margin-right', '5px')
      .style('margin-top', '5px')
      .style('margin-bottom', '5px')
      .style('width', `${basisWidth * 0.8}px`);

    const plotWidth = document.getElementById(`${x}`).clientWidth;
    console.log('width of top plot', width);
    console.log('plotWidth', plotWidth);

    options = {
      plotWidth,
      xVariable: x,
      yVariable: yColumn,
      idVariable: idColumn,
      tooltipColumns,
      numericColumns,
      wrapperId: currentAlgo,
      wrapperLabel: currentAlgoLabel,
      hideXLabel: true,
      yLabelTransform: 'left',
      globalExtents: globalExtents[x],
      marks
    }

    scatterplotUpdateFunctions[x] = drawVoronoiScatterplot(`#${x}`, data, options);
  })

  // draw exploding boxplots for categorical independent variables
  // const testArray = [];
  // testArray.push(categoricalColumns[1]);
  // testArray.push(categoricalColumns[2]);
  // testArray.forEach(x => {
  categoricalColumns.forEach(x => {
    options = {
      xVariable: x,
      yVariable: yColumn,
      marks,
      categoricalColumns,
      globalExtents,
      sortBoxplots,
      chartOptions
    }
    // comment out for now
    // drawExplodingBoxplot('.boxplot-container', data, options);
  })


  // create the dropdown menu
  const dropdownOptions = {
    chartOptions
  }
  dropdown('.nav', data, dropdownOptions);

  //
  // setup transition event listeners
  //

  // options common to all algos
  options = {
    margin,
    width,
    responseColumn,
    tooltipColumns,
    categoricalColumns,
    projectTitle,
    projectLink,
    dataText,
    algos,
    globalExtents,
    marks,
    chartOptions,
    idVariable: idColumn,
  }

  if (typeof aggregated === 'undefined') {
    // deep learning button
    options.xVariable = 'dlPredict';
    options.yVariable = 'dlResidual';
    options.currentAlgo = 'dl';
    options.currentAlgoLabel = 'Deep Learning';
    setModelTransition('#dlButton', data, options);

    // distributed random forest button
    options.xVariable = 'drfPredict';
    options.yVariable = 'drfResidual';
    options.currentAlgo = 'drf';
    options.currentAlgoLabel = 'Distributed Random Forest';
    setModelTransition('#drfButton', data, options);

    // gradient boosting method button
    options.xVariable = 'gbmPredict';
    options.yVariable = 'gbmResidual';
    options.currentAlgo = 'gbm';
    options.currentAlgoLabel = 'Gradient Boosting Method';
    setModelTransition('#gbmButton', data, options);

    // generalized linear model button
    options.xVariable = 'glmPredict';
    options.yVariable = 'glmResidual';
    options.currentAlgo = 'glm';
    options.currentAlgoLabel = 'Generalized Linear Model';
    setModelTransition('#glmButton', data, options);
  } else {
    options.scatterplotUpdateFunctions = scatterplotUpdateFunctions;
    options.predictVariable = predictColumn;
    options.xVariables = xColumns;
    options.xVariable = predictColumn;
    options.yVariable = yColumn;

    // deep learning button
    options.currentAlgo = 'dl';
    options.currentAlgoLabel = 'Deep Learning';
    setModelTransitionAggregated('#dlButton', datasets['dl'], options);

    // distributed random forest button
    options.currentAlgo = 'drf';
    options.currentAlgoLabel = 'Distributed Random Forest';
    setModelTransitionAggregated('#drfButton', datasets['drf'], options);

    // gradient boosting method button
    options.currentAlgo = 'gbm';
    options.currentAlgoLabel = 'Gradient Boosting Method';
    setModelTransitionAggregated('#gbmButton', datasets['gbm'], options);

    // generalized linear model button
    options.currentAlgo = 'glm';
    options.currentAlgoLabel = 'Generalized Linear Model';
    setModelTransitionAggregated('#glmButton', datasets['glm'], options);
  }

}