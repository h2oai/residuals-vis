import { drawVoronoiScatterplot } from './d3-voronoi-scatterplot';
import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { dropdown } from './dropdown';
import { drawTitle } from './drawTitle';
import { getGlobalExtents } from './getGlobalExtents'; 
import { setModelTransition } from './setModelTransition';
import { parseData } from './parseData';
import { rossmanConfig } from './config/rossman';
import { rossmanAggregatedConfig } from './config/rossmanAggregated';
import { walmartTripTypeConfig } from './config/walmartTripType';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 1000;

  let options;
  const cfg = rossmanConfig;
  // const cfg = rossmanAggregatedConfig;
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
    // dataFile = `${path}/${algo}-residuals${fileSuffix}.csv`;
    // hard code the algo names for now
    const dlDataFile = `${path}/dl-residuals${fileSuffix}.csv`;
    const drfDataFile = `${path}/drf-residuals${fileSuffix}.csv`;
    const gbmDataFile = `${path}/gbm-residuals${fileSuffix}.csv`;
    const glmDataFile = `${path}/glm-residuals${fileSuffix}.csv`;

    // TODO figure out how to have a dynamic number of defers
    d3_queue.queue()
      .defer(d3.csv, dlDataFile)
      .defer(d3.csv, drfDataFile)
      .defer(d3.csv, gbmDataFile)
      .defer(d3.csv, glmDataFile)
      .await(drawVisFromData);
  } else {
    dataFile = `${path}/residuals${fileSuffix}.csv`;

    // wait for data to load before attempting to draw
    d3_queue.queue()
      .defer(d3.csv, dataFile)
      .await(drawVisFromData);
  }

  // TODO figure out how to accept a dynamic number of input parameters
  function drawVisFromData(error, inputData, inputData2, inputData3, inputData4) {
    let data;
    let datasets;
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
      options.idPrefix = 'dl';
      const dlData = parseData(inputData, options);
      options.idPrefix = 'drf';
      const drfData = parseData(inputData2, options);
      options.idPrefix = 'gbm';
      const gbmData = parseData(inputData3, options);
      options.idPrefix = 'glm';
      const glmData = parseData(inputData4, options);

      datasets = {
        'dl': dlData,
        'drf': drfData,
        'gbm': gbmData,
        'glm': glmData
      }
      console.log('datasets object', datasets);

      data = datasets['dl'];
    }


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

    // calcuate global extents, if not specified
    let globalExtents = chartOptions.globalExtents; 
    if (typeof globalExtents === 'undefined') {
      if (typeof aggregated !== 'undefined') {
        const datasets = {
          0: data
        }
        options = {
          xVariable: predictColumn,
          yVariable: yColumn,
          combined: undefined
        }
        globalExtents = getGlobalExtents(datasets, options);
      } else {
        options = {
          algos,
          combined: true
        }
        globalExtents = getGlobalExtents(data, options);
      }
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
      xLabelDetail: responseColumn,
      wrapperId: currentAlgo,
      wrapperLabel: currentAlgoLabel,
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
        wrapperId: currentAlgo,
        wrapperLabel: currentAlgoLabel,
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
  }
}
