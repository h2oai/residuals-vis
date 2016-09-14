import { parseResponse } from './parseResponse';

import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function getResidualsDataFromh2o3(options) {
  const server = options.server;
  const port = options.port;
  const frameIDs = options.frameIDs;

  // get the number of rows in the aggregated residuals frame

  // ignore fields that are not the row count
  const getRowsFrameOptions = '?_exclude_fields=frames/__meta,frames/chunk_summary,frames/default_percentiles,frames/columns,frames/distribution_summary,frames/frame_id,__meta';

  // get the row counts for each aggregated residuals frame frameID
  const q0 = d3_queue.queue();
  Object.keys(frameIDs).forEach(key => {
    const frameID = frameIDs[key];
    const getRowsRequestURL = `${server}${port}/3/Frames/${frameID}/summary${getRowsFrameOptions}`;
    console.log('getRowsRequestURL', getRowsRequestURL);
    q0.defer(d3.request, getRowsRequestURL);
  })

  q0.awaitAll(getResidualsFrames)

  const getRowsRequestURL = `${server}${port}/3/Frames/${frameID}/summary${getRowsFrameOptions}`;
  console.log('getRowsRequestURL', getRowsRequestURL);

  // console.log('typeof d3.request', typeof d3.request);

  //
  //
  //

  // get the aggregated residuals data from h2o-3
  function getResidualsFrames(error, responses) {
    if (error) console.error(error);

    const parsedRowResponses = responses.map(d => JSON.parse(d.response));
    console.log('parsedRowRespones', parsedRowResponses);
    const rowCounts = parsedRowResponses.map(d => d.frames[0].rows);
    console.log('rowCounts', rowCounts);


    // can we trust the order?
    // how do we know which row count goes with which frameID?
    const q1 = d3_queue.queue();
    rowCounts.forEach(rowCount => {
      const frameOptions = `?column_offset=0&column_count=21&row_count=${rowCount}`;
      const getDataRequestURL = `${server}${port}/3/Frames/${frameID}${frameOptions}`;
      console.log('getDataRequestURL', getDataRequestURL);
      q1.defer(d3.request, getDataRequestURL)
    })

    q1
      .await(logResponse);
    
    function logResponse(error, response) {
      if (error) console.error(error);
      console.log('response', response);
      const parsedResponse = parseResponse(response);
      console.log('parsedResponse', parsedResponse);
      return parsedResponse;
    }
  }
}
