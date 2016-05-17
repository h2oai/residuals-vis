import { parseResponse } from './parseResponse';
import { drawMemberCircles } from './drawMemberCircles';

export function plotMembers(vis) {
  /* semantic zoom for members of clusters */
  const zoomLevel = vis.zoomBeh.scale();
  const zoomXDomain = vis.zoomBeh.x().domain();
  const zoomYDomain = vis.zoomBeh.y().domain();
  // const zoomThreshold = 31.8;

  console.log('zoomLevel', zoomLevel);
  console.log('zoomXDomain', zoomXDomain);
  console.log('zoomYDomain', zoomYDomain);

  // find the subset of exemplar points that we can see at the current zoom level
  const exemplarPointsVisible = vis.exemplarData.filter(d => {
    const xWithin = d[vis.xCat] > zoomXDomain[0] && d[vis.xCat] < zoomXDomain[1];
    const yWithin = d[vis.yCat] > zoomYDomain[0] && d[vis.yCat] < zoomYDomain[1];
    return xWithin && yWithin;
  });

  console.log('exemplarPointsVisible', exemplarPointsVisible);
  console.log('exemplarPointsVisible.length', exemplarPointsVisible.length);

  // get the data and show the member points
  if (exemplarPointsVisible.length === 1) {
    // call API to get detail data
    const exemplarId = exemplarPointsVisible[0].id;
    const queryUrl = `http://mr-0xc8:55555/3/Frames/members_exemplar${exemplarId}?column_offset=0&column_count=10`;

    d3.xhr(queryUrl, 'application/json', (error, response) => {
      console.log('response', response);
      vis.detailData = parseResponse(response);
      drawMemberCircles(vis);
    });
  }

  // hide the member points
  if (exemplarPointsVisible.length > 1) {
    if (d3.selectAll('.detailDot')[0].length > 0) {
      d3.selectAll('.detailDot').transition()
        .duration(500)
        .style('stroke-opacity', 0)
        .style('fill-opacity', 0)
        .remove();
    }
  }
}
