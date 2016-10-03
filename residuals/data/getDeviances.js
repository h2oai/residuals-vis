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

const rossmanOptions = {
  server: 'http://172.16.2.27',
  port: '54321',
  modelIDs: [
    'deeplearning-a2a9fef1-10d0-4cfb-a8dd-96e99dca161c'
  ],
  project: 'rossman-store-sales'
};

const grupoBimboOptions = {
  server: 'http://172.16.2.141',
  port: '54321',
  validationFrame: 'gb_validation_frame_0.250',
  modelIDs: [
    'glm-f52fe8cb-3aad-4eb0-b0cb-36ec16ae58a3',
    'drf-c6daf49d-dd1f-43b8-9eeb-99bb828d2a25',
    'gbm-ef176351-e583-4484-9a08-0f47dc10d4e1',
  ]
}

// predict
// cbind valid and predict 
// cbind combined and deviances
// get combined-all

// WIP, not working now
/*
function getDeviances(options) {
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
          const output = json.models[0].output;
          const deviancesObject = json.models[0].output.deviances;
          const deviances = parseResponseData(deviancesObject);

          // write out the variable importance data
          // console.log('variable importances from the model', deviances);
          let outputFile = `${project}/output/${modelID}-deviances.json`;
          let outputData = deviances;
          // human readable json
          jsonfile.spaces = 2;
          jsonfile.writeFile(outputFile, outputData, function (err) {
            console.error(err)
          })

          outputFile = `${project}/output/${modelID}-responseOutput.json`;
          outputData = output;
          // human readable json
          jsonfile.spaces = 2;
          jsonfile.writeFile(outputFile, outputData, function (err) {
            console.error(err)
          })
      });
  })
}
*/

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

function predict(model, validationFrame, options) {
  const server = options.server;
  const port = options.port;
  const fetchOptions = { 
    method: 'POST',
    body: 'deviances=true',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  };
  fetch(`${server}:${port}/3/Predictions/models/${model}/frames/${validationFrame}`, fetchOptions)
    .then(function(res) {
        return res.json();
    }).then(function(json) {
        console.log(json);
    });
}

function combineFrames(a, b, options) {
  const server = options.server;
  const port = options.port;
  const combinedFrameKey = `combined-${a}-${b}`;
  const rapidsExpression = `(assign ${combinedFrameKey} (cbind ${a} ${b}))`
  const fetchOptions = { 
    method: 'POST',
    body: `ast=${rapidsExpression}`,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  };
  fetch(`${server}:${port}/99/Rapids`, fetchOptions)
    .then(function(res) {
        return res.json();
    }).then(function(json) {
        console.log(json);
    });
}

// call the function to make an API call
// getDeviances(rossmanOptions);

// predict('glm-07e61c42-9e3d-40bd-a288-60b76a53e91e', 'valid_rossman_frame_0.250', rossmanOptions);
// combineFrames('valid_rossman_frame_0.250', 'predictions_8174_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', rossmanOptions);
// combineFrames('combined-valid_rossman_frame_0.250-predictions_8174_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', 'deviances_8586_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', rossmanOptions);

// more examples
// this time with Grupo Bimbo data

// predict(grupoBimboOptions.modelIDs[0], grupoBimboOptions.validationFrame, grupoBimboOptions);
predict(grupoBimboOptions.modelIDs[1], grupoBimboOptions.validationFrame, grupoBimboOptions);
predict(grupoBimboOptions.modelIDs[2], grupoBimboOptions.validationFrame, grupoBimboOptions);