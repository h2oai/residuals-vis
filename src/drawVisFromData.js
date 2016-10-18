import { parseData } from './parseData';
import { getGlobalExtents } from './getGlobalExtents';
import { drawVoronoiScatterplot } from './d3-voronoi-scatterplot';
import { dropdown } from './dropdown';
import { setModelTransitionAggregated } from './setModelTransitionAggregated';
import { setModelTransition } from './setModelTransition';

import * as d3 from 'd3';

export function drawVisFromData(error, chartOptions, ...args) {
  console.log('args', args);
  console.log('arguments', arguments);

  const numericColumns = chartOptions.numericColumns;
  const idColumn = chartOptions.idColumn;
  const aggregated = chartOptions.aggregated;
  // const models = chartOptions.models;
  const models = chartOptions.modelIDs;
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
  const yScaleType = chartOptions.yScaleType;
  const yScaleExponent = chartOptions.yScaleExponent;

  console.log('models', models);

  let options;
  let data;
  let datasets = {};
  if (typeof aggregated === 'undefined') {
    options = {
      numericColumns,
      idColumn
    }
    const inputData = args[0];
    data = parseData(inputData, options); 
  } else {
    options = {
      numericColumns,
      idColumn
    }

    models.forEach((model, i) => {
      options.idPrefix = model;
      const currentData = args[i];
      console.log('currentData', currentData);
      const parsedData = parseData(currentData, options)
      datasets[model] = parsedData;
    })

    console.log('datasets object', datasets);

    // set a default value for `data`
    data = datasets[models[0]];
  }

  // calcuate global extents, if not specified
  let globalExtents = {};

  // if a global extent is specified
  // use it for the predictColumn
  globalExtents[predictColumn] = chartOptions.globalExtents;

  const scatterplotXVariables = xColumns.concat([predictColumn]);
  console.log('scatterplotXVariables', scatterplotXVariables);

  scatterplotXVariables.forEach(x => {
    if (typeof globalExtents[x] === 'undefined') {
      if (typeof aggregated !== 'undefined') {
        options = {
          xVariable: x,
          yVariable: yColumn,
          combined: undefined,
          models
        }
        globalExtents[x] = getGlobalExtents(datasets, options);
      } else {
        options = {
          xVariable: x,
          yVariable: yColumn,
          models,
          combined: true
        }
        globalExtents[x] = getGlobalExtents(data, options);
      }
    }
  })
  console.log('globalExtents', globalExtents);

  //
  // residuals vs prediction plot
  //
  options = {
    width,
    dynamicWidth: true,
    xVariable: predictColumn,
    yVariable: yColumn,
    idVariable: idColumn,
    tooltipColumns,
    numericColumns,
    xLabelDetail: responseColumn,
    // xLabelTransform: 'top',
    // yLabelTransform: 'left',
    yLabelTransform: [-10, -7, 0],
    wrapperId: currentAlgo,
    wrapperLabel: currentAlgoLabel,
    dependent: true,
    globalExtents: globalExtents[predictColumn],
    marks,
    categoricalColumns,
    sortBoxplots,
    chartOptions,
    voronoiStroke: 'none',
    yScaleType,
    yScaleExponent
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
  
  //
  // residuals vs independent variables scatterplots
  //
  xColumns.forEach(x => {
    // remove spaces
    const xNoSpaces = x.split(' ').join('');
    const card = d3.select(`#${xNoSpaces}Card`);

    // description
    const textBox = card.append('div')
      .attr('id', `${xNoSpaces}Text`)
      // .style('border', '1px solid lightgray')
      .style('padding', '5px')
      .style('margin-left', '5px')
      .style('margin-top', '5px')
      .style('margin-bottom', '5px')
      .style('width', `${basisWidth * 0.20}px`);

    textBox.append('p')
      // .style('white-space', 'nowrap')
      .style('text-align', 'right')
      .style('font-weight', '600')
      .style('padding', '5px')
      .html(`${x}<br>numeric`);

    // plot
    card.append('div')
      .attr('id', `${xNoSpaces}Plot`)
      // .style('border', '1px solid lightgray')
      .style('padding', '5px')
      .style('margin-right', '5px')
      .style('margin-top', '5px')
      .style('margin-bottom', '5px')
      .style('width', `${basisWidth * 0.80}px`);

    const plotWidth = document.getElementById(`${xNoSpaces}Card`).clientWidth;
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
      // hideXLabel: true,
      // yLabelTransform: 'left',
      yLabelTransform: [-10, -7, 0],
      globalExtents: globalExtents[x],
      marks,
      yScaleType,
      yScaleExponent
    }
    console.log('data passed to drawVoronoiScatterplot for independent variable plot', data);
    scatterplotUpdateFunctions[x] = drawVoronoiScatterplot(`#${xNoSpaces}Plot`, data, options);
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

  const algoLabels = {
    glm: 'Generalized Linear Model',
    drf: 'Distributed Random Forest',
    gbm: 'Gradient Boosting Method',
    dl: 'Deep Learning'
  };

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

    models.forEach(model => {
      const modelPrefix = model.split('-', 1)[0];
      options.currentAlgo = model;
      options.currentAlgoLabel = algoLabels[modelPrefix];
      const buttonID = `#${model}Button`;
      setModelTransitionAggregated(buttonID, datasets[model], options);
    })
  }

}