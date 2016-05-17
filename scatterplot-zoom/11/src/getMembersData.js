import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';

export function getMembersData(vis) {
  // call the h2o-3 API to get members data
  // for the specified exemplar
  const exemplarId = vis.exemplarPointsVisible[0].id;
  const queryUrl = `http://mr-0xc8:55555/3/Frames/members_exemplar${exemplarId}?column_offset=0&column_count=10`;

  d3.xhr(queryUrl, 'application/json', (error, response) => {
    console.log('response', response);
    vis.detailData = parseResponse(response);
    drawMemberCircles(vis);
  });
}
