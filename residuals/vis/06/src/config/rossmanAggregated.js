export const rossmanAggregatedConfig = {
  projectTitle: 'Rossman Store Sales',
  projectLink: 'https://www.kaggle.com/c/rossmann-store-sales',
  dataText: 'a 20,000 row subset of the data',
  algos: [ 'dl', 'drf', 'gbm', 'glm'],
  models: [ 'dl', 'drf', 'gbm', 'glm'],
  currentAlgo: 'dl',
  currentAlgoLabel: 'Deep Learning',
  project: 'rossman-store-sales',
  fileSuffix: '-aggregated-0-125',
  aggregated: true,
  predictColumn: 'predict',
  responseColumn: 'Sales',
  xColumns: ['Customers', 'CompetitionDistance'],
  yColumn: 'residual',
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
      name: 'predict',
      type: 'numeric',
      format: ',.0f'
    }
  ],
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
  },
  sortBoxplots: 'rootMeanSquaredValue',
  skeletonBox: true,
  server: 'http://172.16.2.141',
  port: '55555',
  frameIDs: {
    'dl': 'aggregated_Key_Frame__dl_residuals.hex_by_aggregator-fcc6084e-0fab-46b5-b55d-1ac4b8ff5390',
    'drf': 'aggregated_Key_Frame__drf_residuals.hex_by_aggregator-d1eb4a7c-af23-4227-a4c7-47bb4ac251c3',
    'gbm': 'aggregated_Key_Frame__gbm_residuals.hex_by_aggregator-a3b7770a-e5ff-4010-a73a-05137effafdd',
    'glm': 'aggregated_Key_Frame__glm_residuals.hex_by_aggregator-06a69797-ccf8-4d71-bc48-5dab239be01e',
  }
}
