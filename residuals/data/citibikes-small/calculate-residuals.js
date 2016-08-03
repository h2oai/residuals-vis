const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const algos = ['drf', 'dl', 'gbm', 'glm'];

algos.forEach(algo => {
  const csvfile1 = `input/${algo}-combined-hold-predict.csv`;

  const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));

  const predictColumn = 'predict';
  const responseColumn = 'bikes';

  data.forEach(d => {
    const predicted = Number(d[predictColumn]);
    const actual = Number(d[responseColumn]);
    d.residual = actual - predicted;
  })

  const outputData = data;

  // write a csv file
  var writer = csvWriter();
  writer.pipe(fs.createWriteStream(`output/${algo}-residuals.csv`));
  outputData.forEach(d => {
      writer.write(d);
  })
  writer.end();
})
