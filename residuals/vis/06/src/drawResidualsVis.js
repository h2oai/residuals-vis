import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { setModelTransition } from './setModelTransition';
import { getResidualsDataFromh2o3 } from './getResidualsDataFromh2o3';
import { drawVisFromData } from './drawVisFromData';

import { rossmanAggregatedConfig } from './config/rossmanAggregated';
import { santanderAggregatedConfig } from './config/santanderAggregated';
import { rossmanConfig } from './config/rossman';
import { walmartTripTypeConfig } from './config/walmartTripType';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 1000;

  let options;
  const cfg = rossmanAggregatedConfig;
  // const cfg = santanderAggregatedConfig;
  // const cfg = rossmanConfig;
  // const cfg = walmartTripTypeConfig; 
  cfg.width = width;
  const chartOptions = cfg;

  const aggregated = cfg.aggregated;
  const models = cfg.models;
  const project = cfg.project;
  const fileSuffix = cfg.fileSuffix;


  const path = `src/data/${project}`;
  const q = d3_queue.queue();

  let dataFile;
  if (typeof aggregated !== 'undefined') {
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
    // if configured to use multiple local csv files
    } else if (dataSource === 'localCsv') {
      //
      // get data from csv files on disk
      //

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
  } else {
    dataFile = `${path}/residuals${fileSuffix}.csv`;

    // wait for data to load before attempting to draw
    d3_queue.queue()
      .defer(d3.csv, dataFile)
      .await(drawVisFromData);
  }
}
