const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const inputPath = `input`;
const outputPath = `output`;

const csvfile1 = `${inputPath}/train.csv`;
const csvfile2 = `${inputPath}/store.csv`;
const joinByColumn = 'Store'

const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));
const metadata = d3.csv.parse(fs.readFileSync(csvfile2, 'utf8'));

const metadataColumns = Object.keys(metadata[0]);
console.log('metadataColumns', metadataColumns);

const metadataColumnsToAdd = _.pull(metadataColumns, joinByColumn);
console.log('metadataColumnsToAdd', metadataColumnsToAdd);

// add the store metadata properties to each record from the training set
data.forEach(d => {
  const currentMetadatum = metadata.filter(e => e[joinByColumn] === d[joinByColumn])[0];
  Object.keys(currentMetadatum).forEach(key => {
    d[key] = currentMetadatum[key];
  })
})

const outputData = data;

// write a csv file
var writer = csvWriter();
writer.pipe(fs.createWriteStream(`${inputPath}/train-processed.csv`));
outputData.forEach(d => {
    writer.write(d);
})
writer.end();
