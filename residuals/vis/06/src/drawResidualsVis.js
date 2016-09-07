import { drawVoronoiScatterplot } from './d3-voronoi-scatterplot';
import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { dropdown } from './dropdown';
import { drawTitle } from './drawTitle';
import { getGlobalExtents } from './getGlobalExtents'; 
import { setModelTransition } from './setModelTransition';
import { rossmanConfig } from './config/rossman';
import { rossmanAggregatedConfig } from './config/rossmanAggregated';
import { walmartTripTypeConfig } from './config/walmartTripType';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 1000;

  let options;
  const cfg = rossmanAggregatedConfig;
  // const cfg = walmartTripTypeConfig;
  const projectTitle = cfg.projectTitle;
  const projectLink = cfg.projectLink;
  const algos = cfg.algos;
  const currentAlgo = cfg.currentAlgo;
  const currentAlgoLabel = cfg.currentAlgoLabel;
  const predictColumn = cfg.predictColumn;
  const responseColumn = cfg.responseColumn;
  const xColumns = cfg.xColumns;
  const yColumn = cfg.yColumn;
  const idColumn = cfg.idColumn;
  const tooltipColumns = cfg.tooltipColumns;
  const numericColumns = cfg.numericColumns;
  const categoricalColumns = cfg.categoricalColumns;
  const dataText = cfg.dataText;
  const margin = { left: 120, top: 20, right: 80, bottom: 20 };
  const marks = cfg.marks;
  const chartOptions = cfg;
  const problemType = cfg.problemType;
  const sortBoxplots = cfg.sortBoxplots;

  const project = cfg.project;
  const fileSuffix = cfg.fileSuffix;
  const aggregated = cfg.aggregated;

  const algo = algos[0];

  const path = `src/data/${project}`;

  let dataFile;
  if (typeof aggregated !== 'undefined') {
    dataFile = `${path}/${algo}-residuals${fileSuffix}.csv`;
  } else {
    dataFile = `${path}/residuals${fileSuffix}.csv`;
  }
 
 // wait for data to load before attempting to draw
  d3_queue.queue()
    .defer(d3.csv, dataFile)
    .await(drawVisFromData);

  function drawVisFromData(error, inputData) {
    // parse strings to numbers for numeric columns
    const data = [];
    inputData.forEach((d, i) => {
      data.push(d);
      numericColumns.forEach(e => {
        data[i][e] = Number(d[e]);
      })
      if (typeof idColumn === 'undefined') {
        data[i].id = i;
      }
    });
    console.log('data after parsing strings to numbers', data);

    // draw the title text
    let options;
    options = {
      projectTitle,
      projectLink,
      currentAlgo,
      currentAlgoLabel,
      dataText
    }
    drawTitle('p#subTitle', options);

    // get global extents
    let globalExtents;
    if (typeof aggregated !== 'undefined') {
      globalExtents = undefined;
    } else {
      options = {
        algos
      }
      const globalExtents = getGlobalExtents(data, options);
    }
    console.log('globalExtents', globalExtents);

    // residuals vs prediction scatterplot
    options = {
      width,
      xVariable: predictColumn,
      yVariable: yColumn,
      idVariable: idColumn,
      tooltipColumns,
      numericColumns,
      responseColumn,
      currentAlgo,
      currentAlgoLabel,
      dependent: true,
      globalExtents,
      marks,
      categoricalColumns,
      sortBoxplots,
      chartOptions
    }
    if (problemType === 'classification') {
      drawExplodingBoxplot('.dependent-variable-plot-container', data, options);
    } else {
      drawVoronoiScatterplot('.dependent-variable-plot-container', data, options);
    }
    

    // residuals vs independent variables scatterplots
    xColumns.forEach(x => {
      options = {
        width,
        xVariable: x,
        yVariable: yColumn,
        idVariable: idColumn,
        tooltipColumns,
        numericColumns,
        currentAlgo,
        currentAlgoLabel,
        globalExtents: undefined,
        marks
      }
      // comment out for now
      // drawVoronoiScatterplot('.scatterplot-container', data, options);
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
      drawExplodingBoxplot('.boxplot-container', data, options);
    })


    // create the dropdown menu
    const dropdownOptions = {
      chartOptions
    }
    dropdown('.nav', data, dropdownOptions);

    // setup transition event listeners
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
      xVariable: 'dlPredict',
      yVariable: 'dlResidual',
      currentAlgo: 'dl',
      currentAlgoLabel: 'Deep Learning'
    }
    setModelTransition('#dlButton', data, options);
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
      xVariable: 'drfPredict',
      yVariable: 'drfResidual',
      currentAlgo: 'drf',
      currentAlgoLabel: 'Distributed Random Forest'
    }
    setModelTransition('#drfButton', data, options);
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
      xVariable: 'gbmPredict',
      yVariable: 'gbmResidual',
      currentAlgo: 'gbm',
      currentAlgoLabel: 'Gradient Boosting Method'
    }
    setModelTransition('#gbmButton', data, options);
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
      xVariable: 'glmPredict',
      yVariable: 'glmResidual',
      currentAlgo: 'glm',
      currentAlgoLabel: 'Generalized Linear Model',
    }
    setModelTransition('#glmButton', data, options);
  }
}
