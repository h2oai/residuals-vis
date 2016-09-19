const fetch = require('node-fetch');
const jsonfile = require('jsonfile');

const santanderOptions = {
  server: 'http://172.16.2.141',
  port: '55555',
  modelIDs: [
    'deeplearning-22a36d2a-9737-46fa-ab3f-0ff9f5a49f28'
  ],
  project: 'santander-customer-satisfaction'

};

function getVariableImportance(options) {
  const server = options.server;
  const port = options.port;
  const modelIDs = options.modelIDs;
  const project = options.project;

  // arrays are objects too deep down, right? ;-)
  Object.keys(modelIDs).forEach(key => {
    const modelID = modelIDs[key];
    const getModelURL = `${server}:${port}/3/Models/${modelID}`;
    console.log('getModelURL', getModelURL);
    fetch(getModelURL)
      .then(function(res) {
          return res.json();
      }).then(function(json) {
          const variableImportancesObject = json.models[0].output.variable_importances;
          const variableImportances = parseResponseData(variableImportancesObject);

          // write out the variable importance data
          // console.log('variable importances from the model', variableImportances);
          let outputFile = `${project}/output/${modelID}-variable-importances.json`;
          let outputData = variableImportances;
          // human readable json
          jsonfile.spaces = 2;
          jsonfile.writeFile(outputFile, outputData, function (err) {
            console.error(err)
          })

          // write out the top 10 variables
          const top10Variables = variableImportances.map(d => d.variable).slice(0, 10);
          outputFile = `${project}/output/${modelID}-top10-important-variables.json`;
          outputData = top10Variables;
          // human readable json
          jsonfile.spaces = 2;
          jsonfile.writeFile(outputFile, outputData, function (err) {
            console.error(err)
          })
      });
  })
}

function parseResponseData(responseData) {
  const columnNames = responseData.columns.map(d => d.name);
  console.log('columnNames', columnNames);
  const points = [];

  responseData.data.forEach((d, i) => {
    if (Object.prototype.toString.call(d) === '[object Array]') {
      d.forEach((e, j) => {
        if (typeof points[j] === 'undefined') points[j] = {};
        points[j][columnNames[i]] = e;
      });
    }
  });
  // console.log('columnsData', columnsData);
  // console.log('points', points);

  // 1-indexed ranks
  points.forEach((d, i) => {
    d.rank = i + 1;
  });

  const parsedData = points;

  // console.log('parsedData', parsedData);
  return parsedData;
}

// call the function to make an API call
getVariableImportance(santanderOptions);