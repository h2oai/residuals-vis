import { scatterplot } from './scatterplot';
import { dropdown } from './dropdown';
import * as d3 from 'd3';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 470;

  const rossmanConfig = {
    algos: ['gbm'], // ['drf', 'dl', 'gbm', 'glm'];
    project: 'rossman-store-sales',
    predictColumn: 'predict',
    responseColumn: 'Sales',
    xColumns: ['Customers', 'CompetitionDistance'],
    idColumn: 'Store',
    tooltipColumns: ['Store', 'Date'],
    tooltipFormats: [undefined, '']
    numericColumns: [
      'predict',
      'residual',
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
  const algos = cfg.algos;
  const project = cfg.project;
  const predictColumn = cfg.predictColumn;
  const responseColumn = cfg.responseColumn;
  const xColumns = cfg.xColumns;
  const idColumn = cfg.idColumn;
  const tooltipColumns = cfg.tooltipColumns;
  const numericVariables = cfg.numericColumns;
  const categoricalVariables = cfg.categoricalColumns;

  const algo = algos[0]; // gbm

  const path = `src/data/${project}`;
  const dataFile = `${path}/${algo}-residuals-20k.csv`;
 
  d3.csv(dataFile, function(error, data) {
    // residuals vs prediction
    let options = {
      width,
      xVariable: predictColumn,
      yVariable: 'residual',
      idVariable: idColumn,
      tooltipVariables: tooltipColumns,
      numericVariables
    }
    scatterplot('.flex-container', data, options);

    // residuals vs independent variables
    xColumns.forEach(x => {
      options = {
        width,
        xVariable: x,
        yVariable: 'residual',
        idVariable: idColumn,
        tooltipVariables: tooltipColumns,
        numericVariables
      }
    scatterplot('.flex-container', data, options);
    })
    // create the dropdown menu
    const dropdownOptions = {
      categoricalVariables
    }
    dropdown('.nav', data, dropdownOptions);
  })
}
