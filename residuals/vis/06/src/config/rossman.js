export const rossmanConfig = {
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
  idColumn: undefined,
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
  ],
  marks: {
    r: 2,
    fillOpacity: 0.3,
    colors: [
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
  }
}