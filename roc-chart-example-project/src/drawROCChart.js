import rocChart from 'rocChart';
import d3 from 'd3';

export function drawROCChart() {
  const margin = {top: 30, right: 61, bottom: 70, left: 61};
  const width = 470 - margin.left - margin.right;
  const height = 450 - margin.top - margin.bottom;

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
    'smooth': true
  }

  d3.json('src/data.json', function(error, data) {
    rocChart.plot('#root', data, rocChartOptions)
  })
}