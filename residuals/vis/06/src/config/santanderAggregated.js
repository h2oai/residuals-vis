export const santanderAggregatedConfig = {
  projectTitle: 'Santander Customer Satisfaction',
  projectLink: 'https://www.kaggle.com/c/santander-customer-satisfaction',
  projectTitleNote: 'Plots are shown for the 10 most important independent variables.',
  dataText: 'data',
  models: [ 'dl', 'drf', 'gbm', 'glm'],
  currentAlgo: 'dl',
  currentAlgoLabel: 'Deep Learning',
  project: 'santander-customer-satisfaction',
  fileSuffix: '-aggregated-0-002',
  aggregated: true,
  predictColumn: 'predict',
  responseColumn: 'TARGET',
  xColumns: [
    "var15",
    "var38",
    "saldo_var5",
    "num_meses_var5_ult3",
    "ind_var13_largo_0",
    "ind_var13_corto_0",
    "ind_var13",
    "num_var20",
    "ind_var20",
    "ind_var13_largo"
  ],
  yColumn: 'residual',
  idColumn: 'ID',
  tooltipColumns: [
    {
      name: 'ID',
      type: 'numeric',
      format: ',.0f'
    },
    {
      name: 'TARGET',
      type: 'numeric',
      format: ',.0f'
    },
    {
      name: 'predict',
      type: 'numeric',
      format: ',.4f'
    },
    {
      name: 'residual',
      type: 'numeric',
      format: ',.4f'
    }
  ],
  numericColumns: [
    'residual',
    "predict",
    "TARGET",
    "var15",
    "var38",
    "saldo_var5",
    "num_meses_var5_ult3",
    "ind_var13_largo_0",
    "ind_var13_corto_0",
    "ind_var13",
    "num_var20",
    "ind_var20",
    "ind_var13_largo"
  ],
  categoricalColumns: [],
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
    'dl': 'aggregated_santander_Key_Frame__dl_residuals1.hex_by_aggregator-7fb7bebd-d002-425f-a3ef-235cc97a1dca',
    'drf': 'aggregated_Key_Frame__drf_residuals1.hex_by_aggregator-80cb903b-cc2c-41af-bada-25a2a742f5f5',
    'gbm': 'aggregated_santander_Key_Frame__gbm_residuals1.hex_by_aggregator-ed6a9f50-c98b-48d6-a5e2-b21916161cce',
    'glm': 'aggregated_Key_Frame__glm_residuals.hex_by_aggregator-457a81f9-6ca5-4ebd-8f0e-5cbc67766c8e',
  }
}
