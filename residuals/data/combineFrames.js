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

function combineFrames(options, ...args) {
  console.log('args', args);
  const frameIDs = args;
  const a = frameIDs[0];
  const b = frameIDs[1];
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
        if (frameIDs.length > 2) {
          let newFrameIDs = [];
          const remainingFrameIDs = frameIDs.slice(2,frameIDs.length);
          console.log('remainingFrameIDs', remainingFrameIDs);
          newFrameIDs.push(combinedFrameKey);
          newFrameIDs = newFrameIDs.concat(remainingFrameIDs);
          console.log('newFrameIDs', newFrameIDs);

          // recursion!
          combineFrames(options, ...newFrameIDs);
        }
    });
}

// combineFrames('valid_rossman_frame_0.250', 'predictions_8174_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', rossmanOptions);
// combineFrames('combined-valid_rossman_frame_0.250-predictions_8174_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', 'deviances_8586_glm-07e61c42-9e3d-40bd-a288-60b76a53e91e_on_valid_rossman_frame_0.250', rossmanOptions);
combineFrames(grupoBimboOptions, 'gb_validation_frame_0.250', 'predictions_bca0_glm-f52fe8cb-3aad-4eb0-b0cb-36ec16ae58a3_on_gb_validation_frame_0.250', 'deviances_a106_glm-f52fe8cb-3aad-4eb0-b0cb-36ec16ae58a3_on_gb_validation_frame_0.250');