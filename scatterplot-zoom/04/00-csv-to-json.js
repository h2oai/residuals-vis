var fs = require('fs');
var _ = require('lodash');
var jsonfile = require('jsonfile');
var d3 = require('d3');
var inputFile = "cereal.csv"
var data = d3.csv.parse(fs.readFileSync(inputFile, 'utf8'))

// the goal of this script is to convert cereal.csv to json

// console.log(data);

var outputData = data;
var outputFile = "data.json";
jsonfile.spaces = 2;

jsonfile.writeFile(outputFile, outputData, function (err) {
  console.error(err)
})