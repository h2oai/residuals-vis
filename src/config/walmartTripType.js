export const walmartTripTypeConfig = {
  projectTitle: 'WalMart Trip Type',
  projectLink: 'https://www.kaggle.com/c/walmart-recruiting-trip-type-classification',
  problemType: 'classification',
  dataText: 'data',
  models: ['dl'], // , 'drf', 'gbm', 'glm'],
  currentAlgo: 'dl',
  currentAlgoLabel: 'Deep Learning',

  project: 'walmart-trip-type',
  predictColumn: 'predict',
  responseColumn: 'TripType',
  xColumns: ['ScanCount'],
  yColumn: 'residual',
  idColumn: undefined,
  tooltipColumns: [
    {
      name: 'TripType',
    },
    {
      name: 'predict',
    }
  ],
  numericColumns: [
    'predict',
    'residual',
    'ScanCount'
  ],
  categoricalColumns: [
    'VisitNumber',
    'Weekday',
    'UPC',
    'DepartmentDescription',
    'FinelineNumber'
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