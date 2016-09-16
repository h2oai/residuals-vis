import { parseResponse } from './parseResponse';
import { drawVisFromData } from './drawVisFromData';

import * as d3 from 'd3';
import * as d3_queue from 'd3-queue';

export function getResidualsDataFromh2o3(options) {
  const server = options.server;
  const port = options.port;
  const frameIDs = options.frameIDs;

  // get the number of rows in the aggregated residuals frame
  // ignore fields that are not the row count
  const getRowsFrameOptions = '?_exclude_fields=frames/__meta,frames/chunk_summary,frames/default_percentiles,frames/columns,frames/distribution_summary,__meta';

  // get the row counts for each aggregated residuals frame frameID
  const q0 = d3_queue.queue();

  // console.log('options from getResidualsDataFromh2o3', options);

  Object.keys(frameIDs).forEach(key => {
    const frameID = frameIDs[key];
    const getRowsRequestURL = `${server}:${port}/3/Frames/${frameID}/summary${getRowsFrameOptions}`;
    console.log('getRowsRequestURL', getRowsRequestURL);
    q0.defer(d3.request, getRowsRequestURL);
  })

  q0.awaitAll(getResidualsFrames)

  // get the aggregated residuals data from h2o-3
  function getResidualsFrames(error, responses) {
    if (error) console.error(error);
    console.log('arguments from getResidualsFrames', arguments);

    const parsedRowResponses = responses.map(d => JSON.parse(d.response));
    const frames = parsedRowResponses.map(d => ({
      rows: d.frames[0].rows,
      frameID: d.frames[0].frame_id.name
    }))
    console.log('parsedRowRespones', parsedRowResponses);
    console.log('frames from getResidualsFrames', frames);

    // can we trust the order?
    // how do we know which row count goes with which frameID?
    const q1 = d3_queue.queue();
    frames.forEach(frame => {
      const rowCount = frame.rows;
      const frameID = frame.frameID;
      // TODO: generalize the `column_count` parameter
      const frameOptions = `?column_offset=0&column_count=373&row_count=${rowCount}`;
      const getDataRequestURL = `${server}:${port}/3/Frames/${frameID}${frameOptions}`;
      console.log('getDataRequestURL', getDataRequestURL);
      q1.defer(d3.request, getDataRequestURL)
    })

    q1
      .awaitAll(logResponse);
    
    function logResponse(error, responses) {
      if (error) console.error(error);
      console.log('logResponse was called');
      console.log('arguments from logResponse', arguments);

      const parsedResponses = [];
      responses.forEach(response => {
        console.log('response', response);
        const parsedResponse = parseResponse(response);
        parsedResponses.push(parsedResponse);
        console.log('parsedResponse', parsedResponse);
      })
      drawVisFromData(null, options, ...parsedResponses);
    }
  }
}
