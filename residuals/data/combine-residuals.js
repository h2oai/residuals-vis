const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const rossman05Config = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-residuals-aggregated',
  fileSuffix: '-0-05'
}

const rossman075Config = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-residuals-aggregated',
  fileSuffix: '-0-075'
}

const rossman20kConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-combined-validation-predict',
  fileSuffix: '-20k'
}

const walmartConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'walmart-trip-type',
  predictColumn: 'predict',
  responseColumn: 'TripType',
  numericColumn: ['ScanCount'],
  fileStem: '-combined-validation-predict',
  residualType: 'pearson'
}

function combineResiduals(config) {
  //
  // create the a file with all of the residuals for all algos
  //
  const algos = config.algos;
  const predictColumn = config.predictColumn;
  const responseColumn = config.responseColumn;
  const project = config.project;
  const fileStem = config.fileStem;
  const fileSuffix = config.fileSuffix;

  const inputPath = `${project}/input`;
  const outputPath = `${project}/output`;
  const algoSpecificData = {};

  // start with the original validation frame
  const csvfile2 = `${inputPath}/${algos[0]}${fileStem}${fileSuffix}.csv`;
  const baseData = d3.csv.parse(fs.readFileSync(csvfile2, 'utf8'));

  // strip out the prediction to get just the validation frame
  baseData.forEach(d => {
    delete d[predictColumn];
  })

  const allResidualsData = _.cloneDeep(baseData);

  // check the length of the arrays
  const residualsArraysLengths = _.keys(algoSpecificData).map(key => algoSpecificData[key].length)
  let lengthsMatch = true;
  residualsArraysLengths.forEach(length => {
    if(length !== allResidualsData.length) { lengthsMatch === undefined };
  });

  if (lengthsMatch) {
    allResidualsData.forEach((d, i) => {
      console.log('algoSpecificData', algoSpecificData);
      algos.forEach(algo => {
        // merge the object with the prediction and the residual
        // for the current algo for the current row
        // with the current row from the validation set 
        _.assign(d, algoSpecificData[algo][i]);
      })
    })
  }

  outputData = allResidualsData;
  // write a csv file
  var writer = csvWriter();
  writer.pipe(fs.createWriteStream(`${outputPath}/residuals${fileSuffix}.csv`));
  outputData.forEach(d => {
      writer.write(d);
  })
  writer.end();
}

combineResiduals(rossman05Config);
combineResiduals(rossman075Config);