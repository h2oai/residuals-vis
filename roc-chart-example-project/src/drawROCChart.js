import rocChart from 'rocChart';
import d3 from 'd3';

export function drawROCChart(width, height) {

  // set defaults
  if (typeof width === 'undefined') width = 470;
  if (typeof height === 'undefined') height = 450; 

  // const margin = {top: 30, right: 61, bottom: 70, left: 61}; 
  const margin = {top: 1, right: 1, bottom: 1, left: 1};
  const chartWidth = width - margin.left - margin.right;
  const chartHeight = height - margin.top - margin.bottom;

  // fpr for 'false positive rate'
  // tpr for 'true positive rate'

  const rocChartOptions = {
    'margin': margin,
    'width': chartWidth,
    'height': chartHeight,
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
    'animate': false,
    'smooth': true,
    'hideTicks': true
  }

  d3.json('src/data.json', function(error, data) {
    rocChart.plot('#root', data, rocChartOptions)
  })
}