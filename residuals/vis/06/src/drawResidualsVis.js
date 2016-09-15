import { drawExplodingBoxplot } from './drawExplodingBoxplot';
import { setModelTransition } from './setModelTransition';
import { setModelTransitionAggregated } from './setModelTransitionAggregated';
import { getResidualsDataFromh2o3 } from './getResidualsDataFromh2o3';
import { drawVisFromData } from './drawVisFromData';

import { rossmanConfig } from './config/rossman';
import { rossmanAggregatedConfig } from './config/rossmanAggregated';
import { walmartTripTypeConfig } from './config/walmartTripType';
import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function drawResidualsVis(width) {
  // set defaults
  if (typeof width === 'undefined') width = 1000;

  let options;
  // const cfg = rossmanConfig;
  const cfg = rossmanAggregatedConfig;
  // const cfg = walmartTripTypeConfig; 
  cfg.width = width;
  const chartOptions = cfg;

  const aggregated = cfg.aggregated;
  const algos = cfg.algos;
  const models = cfg.models;
  const margin = { left: 120, top: 20, right: 80, bottom: 20 };
  const project = cfg.project;
  const fileSuffix = cfg.fileSuffix;
  const algo = algos[0];

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

      const server = 'http://172.16.2.141:';
      const port = '55555';

      const frameIDs = {
        'dl': 'aggregated_Key_Frame__dl_residuals.hex_by_aggregator-fcc6084e-0fab-46b5-b55d-1ac4b8ff5390',
        'drf': 'aggregated_Key_Frame__drf_residuals.hex_by_aggregator-d1eb4a7c-af23-4227-a4c7-47bb4ac251c3',
        'gbm': 'aggregated_Key_Frame__gbm_residuals.hex_by_aggregator-a3b7770a-e5ff-4010-a73a-05137effafdd',
        'glm': 'aggregated_Key_Frame__glm_residuals.hex_by_aggregator-06a69797-ccf8-4d71-bc48-5dab239be01e',
      }

    const getResidualsDataFromh2o3Options = {
      server,
      port,
      frameIDs,
      chartOptions
    };

    // make the API calls and log out the responses
    // then call drawVisFromData from this function
    getResidualsDataFromh2o3(getResidualsDataFromh2o3Options);

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
