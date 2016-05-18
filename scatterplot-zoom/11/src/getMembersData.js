import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';

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

  d3.xhr(queryUrl, 'application/json', (error, response) => {
    console.log('response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  });
}
