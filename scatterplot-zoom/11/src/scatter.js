import { parseResponse } from './parseResponse';
import { plotExemplars } from './plotExemplars';
import d3 from 'd3';

export function drawScatterplot() {
  const vis = {};
  vis.margin = { top: 50, right: 300, bottom: 50, left: 60 };
  vis.outerWidth = 960; // 3648
  vis.outerHeight = 500; // 1900
  vis.width = vis.outerWidth - vis.margin.left - vis.margin.right;
  vis.height = vis.outerHeight - vis.margin.top - vis.margin.bottom;

  vis.x = d3.scale.linear()
    .range([0, vis.width]).nice();

  vis.y = d3.scale.linear()
    .range([vis.height, 0]).nice();

  // const rScale = d3.scale.linear()
  //   .range([0, 3]);


  /* call API to get exemplar data */
  vis.coverTypeConfig = {
    server: 'mr-0xc8',
    port: '55555',
    exemplarFrame: 'aggregated_covtype_20k_data.hex_by_aggregatormodel',
    columnOffset: '0',
    columnCount: '10',
    defaultXVariable: 'C10',
    defaultYVariable: 'C1'
  };

  vis.pcaConfig = {
    server: 'mr-0xc8',
    port: '54321',
    exemplarFrame: 'aggregated_pca_processed_events_sql_to_hex_by_aggregatormodel',
    columnOffset: '0',
    columnCount: '10',
    defaultXVariable: 'PC1',
    defaultYVariable: 'PC2'
  };

  vis.apiConfig = vis.pcaConfig;

  vis.xCat = vis.apiConfig.defaultXVariable;
  vis.yCat = vis.apiConfig.defaultYVariable;
  // const rCat = 'C2';
  // const colorCat = 'C3';

  const server = vis.apiConfig.server;
  const port = vis.apiConfig.port;
  const exemplarsFrame = vis.apiConfig.exemplarFrame;
  const columnOffset = vis.apiConfig.columnOffset;
  const columnCount = vis.apiConfig.columnCount;

  const queryUrl = `http://${server}:${port}/3/Frames/${exemplarsFrame}?column_offset=${columnOffset}&column_count=${columnCount}`;

  d3.xhr(queryUrl, 'application/json', (error, response) => {
    console.log('response', response);
    vis.exemplarData = parseResponse(response);
    plotExemplars(vis);
  });
}
