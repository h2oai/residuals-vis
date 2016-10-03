const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const inputPath = `input`;
const outputPath = `output`;

const csvfile1 = `${inputPath}/train.csv`;

const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));

const rowCount = 20000;
const subset = data.slice(0, rowCount);

const outputData = subset;

// write a csv file
var writer = csvWriter();
writer.pipe(fs.createWriteStream(`${inputPath}/train-subset-20k.csv`));
outputData.forEach(d => {
    writer.write(d);
})
writer.end();



