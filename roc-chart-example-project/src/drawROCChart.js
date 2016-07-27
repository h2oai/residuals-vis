import rocChart from 'roc-chart';

export function drawROCChart() { 
  const sideLength = 6.25;
  const margin = {top: 0, right: 0, bottom: 0, left: 0};
  const width = '100%'; // sideLength - margin.left - margin.right;
  const height = '100%'; // sideLength - margin.top - margin.bottom;

  // fpr for 'false positive rate'
  // tpr for 'true positive rate'

  const rocChartOptions = {
    'margin': margin,
    'width': width,
    'height': height,
    'interpolationMode': 'basis',
    'fpr': 'X',
    'tprVariables': [
      {
        'name': 'BPC',
        'label': 'Break Points'
      },
      {
        'name': 'WNR',
        'label': 'Winners'
      },
      {
        'name': 'FSP',
        'label': 'First Serve %',
      },
      {
        'name': 'NPW',
        'label': 'Net Points Won'
      }
    ], 
    'animate': true,
    'smooth': true,
    'hideAxes': true,
    'hideBoundaries': true
  }

  d3.json('src/data.json', function(error, data) {
    rocChart.plot('#root', data, rocChartOptions)
  })
}