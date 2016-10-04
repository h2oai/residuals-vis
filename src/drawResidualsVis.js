import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { setModelTransition } from './setModelTransition';
import { getResidualsDataFromh2o3 } from './getResidualsDataFromh2o3';
import { drawVisFromData } from './drawVisFromData';

import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(props) {
  console.log('props', props);
  let options;
  const cfg = props.config;
  // set default width
  if (typeof cfg.width === 'undefined') cfg.width = 1000;
  const chartOptions = cfg;

  const aggregated = cfg.aggregated;
  const models = cfg.models;
  const project = cfg.project;
  const fileSuffix = cfg.fileSuffix;

  const path = `src/data/${project}`;
  const q = d3_queue.queue();

  //
  // data passed in on the props from the parent component 
  // in the React app
  //
  if (typeof props.datasets !== 'undefined') {
    drawVisFromData(null, chartOptions, ...props.datasets);
  //  
  // data retrieved directly from the h2o-3 REST API
  //
  } else if (typeof aggregated !== 'undefined') {
    const dataFiles = {};
    // if configured to use the h2o-3 API
    // TODO add configuration check to if statement 
    if (typeof dataSource === 'undefined' || dataSource === 'api') {
      //
      // make API calls to h2o-3 backend
      //
      const server = cfg.server;
      const port = cfg.port;
      const frameIDs = cfg.frameIDs

      // make the API calls and log out the responses
      // then call drawVisFromData from this function
      getResidualsDataFromh2o3(chartOptions);
    //
    // data read in from multiple local csv files
    //
    } else if (dataSource === 'localCsv') {
      // construct file names for each model
      models.forEach(model => {
        dataFiles[model] = `${path}/${model}-residuals${fileSuffix}.csv`;
      })

      const q = d3_queue.queue();
      // add data for each model to the queue
      models.forEach(model => {
        // q.defer(d3.request, requestURL);
        q.defer(d3.csv, dataFiles[model]);
      })

      // when all data has loaded, call `drawVisFromData()`
      q.await(drawVisFromData);
    } else {
      console.error(`${dataSource} is not a valid dataSource`);
    }
  //
  // data read in from a single local csv file
  //
  } else {
    const dataFile = `${path}/residuals${fileSuffix}.csv`;

    // wait for data to load before attempting to draw
    d3_queue.queue()
      .defer(d3.csv, dataFile)
      .await(drawVisFromData);
  }
}
