export const rossmanAggregatedConfig = {
  projectTitle: 'Rossman Store Sales',
  projectLink: 'https://www.kaggle.com/c/rossmann-store-sales',
  dataText: 'a 20,000 row subset of the data',
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
  server: 'http://172.16.2.141/',
  port: '55555',
  frameIDs: {
    'dl': 'aggregated_Key_Frame__dl_residuals.hex_by_aggregator-5c06ef26-8836-43eb-98c3-7d91cd29ee66',
    'drf': 'aggregated_Key_Frame__drf_residuals.hex_by_aggregator-ae9eeb80-ba4c-4950-b2ea-a72102330c52',
    'gbm': 'aggregated_rossman_Key_Frame__gbm_residuals.hex_by_aggregator-92deae3c-68bf-4481-9318-9bf10a37d947',
    'glm': 'aggregated_rossman_Key_Frame__glm_residuals.hex_by_aggregator-38fb1e92-226f-49a8-beaf-fca7b87b32fd',
  }
}
