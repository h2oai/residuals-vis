import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';
import d3 from 'd3';
import d3_request from 'd3-request';
import d3_queue from 'd3-queue';
d3.request = d3_request.request;
d3.queue = d3_queue.queue;

export function getMembersData(vis) {
  // call the h2o-3 API to get members data
  // for the specified exemplar

  const exemplarId = vis.exemplarPointsVisible[0].id;
  const membersFrame = `members_exemplar${exemplarId}`;

  const server = vis.apiConfig.server;
  const port = vis.apiConfig.port;
  const columnOffset = vis.apiConfig.columnOffset;
  const columnCount = vis.apiConfig.columnCount;

  const getMemberFrameUrl = `http://${server}:${port}/3/Frames/${membersFrame}?column_offset=${columnOffset}&column_count=${columnCount}`;

  function callback(error, response) {
    console.log('response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  }

  d3.queue()
    .defer(d3.request, getMemberFrameUrl)
    // .defer(d3.request, "http://www.google.com:81")
    // .defer(d3.request, "http://www.google.com:81")
    .awaitAll((error, results) => {
      if (error) throw error;
      console.log(results);
      callback(error, results[0]);
    });
}
