import { drawVoronoiScatterplot } from './d3-voronoi-scatterplot';
import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { dropdown } from './dropdown';
import { drawTitle } from './drawTitle';
import { getGlobalExtents } from './getGlobalExtents'; 
import { setModelTransition } from './setModelTransition';
import { setModelTransitionAggregated } from './setModelTransitionAggregated';
import { parseData } from './parseData';
import { parseResponse } from './parseResponse';
import { getResidualsDataFromh2o3 } from './getResidualsDataFromh2o3';

import { rossmanConfig } from './config/rossman';
import { rossmanAggregatedConfig } from './config/rossmanAggregated';
import { walmartTripTypeConfig } from './config/walmartTripType';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 1000;

  let options;
  // const cfg = rossmanConfig;
  const cfg = rossmanAggregatedConfig;
  // const cfg = walmartTripTypeConfig;
  const projectTitle = cfg.projectTitle;
  const projectLink = cfg.projectLink;
  const algos = cfg.algos;
  const models = cfg.models;
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
  const q = d3_queue.queue();

  let dataFile;
  if (typeof aggregated !== 'undefined') {
    const dataFiles = {};

    //
    // make API calls to h2o-3 backend
    //

    // /3/Frames/
    const server = 'http://172.16.2.141/:';
    const port = '55555';
    const frameIDs = {
      'dl': 'aggregated_Key_Frame__dl_residuals.hex_by_aggregator-fcc6084e-0fab-46b5-b55d-1ac4b8ff5390',
      'drf': 'aggregated_Key_Frame__drf_residuals.hex_by_aggregator-d1eb4a7c-af23-4227-a4c7-47bb4ac251c3',
      'gbm': 'aggregated_Key_Frame__gbm_residuals.hex_by_aggregator-a3b7770a-e5ff-4010-a73a-05137effafdd',
      'glm': 'aggregated_Key_Frame__glm_residuals.hex_by_aggregator-06a69797-ccf8-4d71-bc48-5dab239be01e',
    }

    const getResidualsDataFromh2o3Options = {
      server,
      port,
      frameIDs
    };

    console.log('getResidualsDataFromh2o3', getResidualsDataFromh2o3);
    console.log('getResidualsDataFromh2o3(getResidualsDataFromh2o3Options)', getResidualsDataFromh2o3(getResidualsDataFromh2o3Options));

    // const inputDatasets = []; 
    // const q0 = d3_queue.queue();
    // models.forEach(model => {
    //   q0.defer(getResidualsDataFromh2o3.call(this, getResidualsDataFromh2o3Options));
    // })
    // q0.await(drawVisFromData);

    //
    //
    //

    //
    // get data from csv files on disk
    //
/*
    // construct file names for each model
    models.forEach(model => {
      dataFiles[model] = `${path}/${model}-residuals${fileSuffix}.csv`;
    })

    const q = d3_queue.queue();
    // add data for each model to the queue
    models.forEach(model => {
      // q.defer(d3.request, requestURL);
      q.defer(d3.csv, dataFiles[model]);
    })

    // when all data has loaded, call `drawVisFromData()`
    q.await(drawVisFromData);
*/
  } else {
    dataFile = `${path}/residuals${fileSuffix}.csv`;

    // wait for data to load before attempting to draw
    d3_queue.queue()
      .defer(d3.csv, dataFile)
      .await(drawVisFromData);
  }
  function drawVisFromData(error, ...args) {
    console.log('args', args);
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
      xVariable: predictColumn,
      yVariable: yColumn,
      idVariable: idColumn,
      tooltipColumns,
      numericColumns,
      xLabelDetail: responseColumn,
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
      scatterplotUpdateFunctions[predictColumn] = drawVoronoiScatterplot('.dependent-variable-plot-container', data, options);
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
        globalExtents: globalExtents[x],
        marks
      }
      // comment out for now
      scatterplotUpdateFunctions[x] = drawVoronoiScatterplot('.scatterplot-container', data, options);
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
}
