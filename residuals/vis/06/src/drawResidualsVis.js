import { scatterplot } from './scatterplot';
import { dropdown } from './dropdown';
import { drawTitle } from './drawTitle';
import { getGlobalExtents } from './getGlobalExtents'; 
import { setModelTransition } from './setModelTransition';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 470;

  const rossmanConfig = {
    projectTitle: 'Rossman Store Sales',
    projectLink: 'https://www.kaggle.com/c/rossmann-store-sales',
    dataText: 'a 20,000 row subset of the data',
    algos: [ 'dl', 'drf', 'gbm', 'glm'],
    currentAlgo: 'glm',
    currentAlgoLabel: 'Generalized Linear Model',
    project: 'rossman-store-sales',
    predictColumn: 'glmPredict',
    responseColumn: 'Sales',
    xColumns: ['Customers', 'CompetitionDistance'],
    yColumn: 'glmResidual',
    idColumn: 'Store',
    tooltipColumns: [
      {
        name: 'Store'
      },
      {
        name: 'Date',
        type: 'time',
        format: '%B %d, %Y'
      },
      {
        name: 'Sales',
        type: 'numeric',
        format: ',.0f'
      },
      {
        name: 'glmPredict',
        type: 'numeric',
        format: ',.0f'
      }
    ],
    numericColumns: [
      'dlPredict',
      'drfPredict',
      'gbmPredict',
      'glmPredict',
      'dlResidual',
      'drfResidual',
      'gbmResidual',
      'glmResidual',
      'Sales',
      'Customers',
      'CompetitionDistance'
    ],
    categoricalColumns: [
      'Open',
      'Promo',
      'StateHoliday',
      'SchoolHoliday',
      'StoreType',
      'Assortment',
      'Promo2',
      'PromoInterval'
    ]
  }

  const citibikes20kConfig = {
    algos: ['gbm'],
    project: 'citibikes-20k',
    predictColumn: 'predict',
    responseColumn: 'Bikes',
    xColumns: ['Days']
  }

  let options;
  const cfg = rossmanConfig;
  const projectTitle = cfg.projectTitle;
  const projectLink = cfg.projectLink;
  const algos = cfg.algos;
  const currentAlgo = cfg.currentAlgo;
  const currentAlgoLabel = cfg.currentAlgoLabel;
  const project = cfg.project;
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

  const algo = algos[0];

  const path = `src/data/${project}`;
  // const dataFile = `${path}/${algo}-residuals-20k.csv`;
  const dataFile = `${path}/residuals-20k.csv`;
 
 // wait for data to load before attempting to draw
  d3_queue.queue()
    .defer(d3.csv, dataFile)
    .await(drawVisFromData);

  function drawVisFromData(error, data) {
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
    options = {
      algos
    }
    const globalExtents = getGlobalExtents(data, options);

    // residuals vs prediction
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
      globalExtents
    }
    scatterplot('.flex-container', data, options);

    // residuals vs dependent variables
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
        globalExtents: undefined
      }
    scatterplot('.flex-container', data, options);
    })

    // create the dropdown menu
    const dropdownOptions = {
      categoricalColumns
    }
    dropdown('.nav', data, dropdownOptions);

    // setup transition event listeners
    options = {
      margin,
      width,
      responseColumn,
      tooltipColumns,
      projectTitle,
      projectLink,
      dataText,
      algos,
      globalExtents,
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
      projectTitle,
      projectLink,
      dataText,
      algos,
      globalExtents,
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
      projectTitle,
      projectLink,
      dataText,
      algos,
      globalExtents,
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
      projectTitle,
      projectLink,
      dataText,
      algos,
      globalExtents,
      idVariable: idColumn,
      xVariable: 'glmPredict',
      yVariable: 'glmResidual',
      currentAlgo: 'glm',
      currentAlgoLabel: 'Generalized Linear Model',
    }
    setModelTransition('#glmButton', data, options);
  }
}
