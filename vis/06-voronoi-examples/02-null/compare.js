const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

// a small script to test the hypothesis that the data is 
// somehow transformed inside of the residuals vis app
// and that this causes the Voronoi overlay to fail to render

// the data from the h2o-3 frame downloaded directly from h2o-3
const csvfile1 = 'rawData.csv';
const data = d3.csvParse(fs.readFileSync(csvfile1, 'utf8'));

// the data from the browser console that we pass to the 
// Voronoi overlay inside of the residuals vis app
const jsonFile = 'observed-data.json';
const observedData = JSON.parse(fs.readFileSync(jsonFile, 'utf8'));

const observedDataCleaned = [];
observedData.forEach((d, i) => {
  let keys = Object.keys(d);
  _.pull(keys, 'id');
  observedDataCleaned[i] = {};
  keys.forEach(key => {
    observedDataCleaned[i][key] = d[key];
  })
});

console.log('data.length', data.length);
console.log('observedData.length', observedData.length); 
console.log('observedDataCleaned.length', observedDataCleaned.length);

const unmatched = [];

data.forEach((d, i) => {
  if(d !== observedDataCleaned[i]) {
    console.log('records do not match!');
    console.log('datum from h2o-3 data', data[i]);
    console.log('datum from observed app data', observedDataCleaned[i]);
    unmatched.push({
      h2o3: data[i],
      app: observedData[i]
    })
  }
})

console.log('unmatched.length', unmatched.length);
// outputData = subset;

// // write a csv file
// var writer = csvWriter();
// writer.pipe(fs.createWriteStream('data.csv'));
// outputData.forEach(d => {
//     writer.write(d);
// })
// writer.end();

// var outputFile = "01.json";
// jsonfile.spaces = 2;
// 
// jsonfile.writeFile(outputFile, results, function (err) {
//   console.error(err)
// })