const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const adultConfig = {
  project: 'adult',
  residualType: 'pearsonAdvisor',
  fileStem: 'residuals',
  fileSuffix: '',
  source: 'Pearson Residuals',
  target: 'residual'
 }

function checkEquality(config) {
  const source = config.source;
  const target = config.target;

  const project = config.project;
  const fileSuffix = config.fileSuffix;
  const fileStem = config.fileStem;

  const outputPath = `${project}/output`;
  const csvfile1 = `${outputPath}/${fileStem}${fileSuffix}.csv`;
  const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));

  let equalityBoolean;
  data.forEach((d, i) => {
    if (d[source] === d[target]) {
      equalityBoolean = true;
    } else {
      equalityBoolean = false;
    }
    d.equal = equalityBoolean;
  })

  let outputData = data;

  // write a csv file
  var writer = csvWriter();
  writer.pipe(fs.createWriteStream(`${outputPath}/${fileStem}-check-equality${fileSuffix}.csv`));
  outputData.forEach(d => {
      writer.write(d);
  })
  writer.end();
}

checkEquality(adultConfig);