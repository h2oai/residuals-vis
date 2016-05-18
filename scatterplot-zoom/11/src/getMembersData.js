import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';
import d3 from 'd3';
import d3_request from 'd3-request';
d3.request = d3_request.request;

// console.log('d3_request', d3_request);
// console.log('d3.request', d3.request);

export function getMembersData(vis) {
  // call the h2o-3 API to get members data
  // for the specified exemplar

  const exemplarId = vis.exemplarPointsVisible[0].id;
  const membersFrame = `members_exemplar${exemplarId}`;

  const server = vis.apiConfig.server;
  const port = vis.apiConfig.port;
  const columnOffset = vis.apiConfig.columnOffset;
  const columnCount = vis.apiConfig.columnCount;

  const queryUrl = `http://${server}:${port}/3/Frames/${membersFrame}?column_offset=${columnOffset}&column_count=${columnCount}`;

  function callback(error, response) {
    console.log('response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  }

  d3.request(queryUrl)
    .get(callback);

  /*
  d3.xhr(queryUrl, 'application/json', (error, response) => {
    console.log('response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  });
  */
}
