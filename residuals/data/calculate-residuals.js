const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const rossman20kConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-combined-validation-predict',
  fileSuffix: '-20k'
}

const rossmanConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-combined-validation-predict',
  fileSuffix: ''
}

const walmart20kConfig = {
  algos: ['dl'], //, 'drf',  'gbm', 'glm'],
  project: 'walmart-trip-type',
  predictColumn: 'predict',
  responseColumn: 'TripType',
  numericColumn: ['ScanCount'],
  residualType: 'pearson',
  fileStem: '-combined-validation-predict',
  fileSuffix: '-20k'
}

const adultConfig = {
  project: 'adult',
  residualType: 'pearsonAdvisor',
  fileStem: 'Logit',
  fileSuffix: ''
}

function calculateResiduals(config) {
  const algos = config.algos;
  const predictColumn = config.predictColumn;
  const responseColumn = config.responseColumn;
  const project = config.project;
  const fileSuffix = config.fileSuffix;
  const fileStem = config.fileStem;
  const residualType = config.residualType;
  const algo = algos[0];

  const inputPath = `${project}/input`;
  const outputPath = `${project}/output`;
  const csvfile1 = `${inputPath}/${algo}${fileStem}${fileSuffix}.csv`;
  const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));

  const categories = d3.set(data.map(d => d[responseColumn]))
    .values()
    .sort(); 

  console.log('unique categories from responseColumn', categories);

  data.forEach((d, i) => {
    const predicted = Number(d[predictColumn]);
    const actual = Number(d[responseColumn]);
    const actualString = d[responseColumn];

    let residual;
    if (residualType === 'pearson') {
      const estimate = Number(d[`p${actual}`]); // p40 posterior value etc
      const currentCategoryIndex = categories.indexOf(actualString); // '40' the category string
      const numberOfCategories = categories.length;
      const observed = numberOfCategories - currentCategoryIndex;

      console.log('predicted', predicted);
      console.log('actual', actual);
      console.log('estimate', estimate);
      console.log('currentCategoryIndex', currentCategoryIndex);
      console.log('numberOfCategories', numberOfCategories);
      console.log('observed', observed);

      residual = (observed - estimate) / (Math.sqrt(estimate * (1 - estimate)));
    } else if (residualType === 'pearsonAdvisor') {
      const estimate = Number(d.Estimates);
      const observed = Number(d.Observed);

      console.log('estimate', estimate);
      console.log('observed', observed);

      residual = (observed - estimate) / (Math.sqrt(estimate * (1 - estimate)));
    } else {
      residual = actual - predicted;
    }
    d.residual = residual;
  })
  let outputData = data;

  // write a csv file
  var writer = csvWriter();
  writer.pipe(fs.createWriteStream(`${outputPath}/residuals${fileSuffix}.csv`));
  outputData.forEach(d => {
      writer.write(d);
  })
  writer.end();

}

calculateResiduals(walmart20kConfig);
