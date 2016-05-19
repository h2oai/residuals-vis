import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';
import d3 from 'd3';
import d3_request from 'd3-request';
// import d3_queue from 'd3-queue';
d3.request = d3_request.request;
// d3.queue = d3_queue.queue;

export function getMembersData(vis) {
  // call the h2o-3 API to get members data
  // for the specified exemplar

  const exemplarId = vis.exemplarPointsVisible[0].id;
  const membersFrame = `members_exemplar${exemplarId}`;

  const server = vis.apiConfig.server;
  const port = vis.apiConfig.port;
  const columnOffset = vis.apiConfig.columnOffset;
  const columnCount = vis.apiConfig.columnCount;
  const baseUrl = `http://${server}:${port}/3`;

  const getMemberFrameUrl = `${baseUrl}/Frames/${membersFrame}?column_offset=${columnOffset}&column_count=${columnCount}`;
  function getMemberFrameCallback(error, response) {
    console.log('getMemberFrameCallback response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  }

  const generateMemberFrameUrl = `${baseUrl}/Predictions/models/aggregatormodel/frames/null`;
  const generateMemberFrameData = `predictions_frame=members_exemplar${exemplarId}&exemplar_index=${exemplarId}`;
  function generateMemberFrameCallback(error, response) {
    console.log('generateMemberFrameCallback response', response);
    d3.request(getMemberFrameUrl)
      .get(getMemberFrameCallback);
  }

  // const generateMemberFrameRequest =
  d3.request(generateMemberFrameUrl)
    .header('Content-Type', 'application/x-www-form-urlencoded')
    .post(generateMemberFrameData, generateMemberFrameCallback);

  // function generateMemberFrameRequestWrapper() {
  //   generateMemberFrameRequest.post(generateMemberFrameData, generateMemberFrameCallback);
  // }

  // console.log('type of generateMemberFrameRequest...', typeof generateMemberFrameRequest.post(generateMemberFrameData, generateMemberFrameCallback));

  /* abandon d3.queue approach for now */
  // d3.queue(1)
  //   .defer(generateMemberFrameRequestWrapper)
  //   .defer(d3.request, getMemberFrameUrl)
  //   // .defer(d3.request, "http://www.google.com:81")
  //   .awaitAll((error, results) => {
  //     if (error) {
  //       console.log('error from awaitAll', error);
  //       throw error;
  //     }
  //     console.log(results);
  //     // generateMemberFrameCallback(error, results[0]);
  //     getMemberFrameCallback(error, results[0]);
  //   });
}
