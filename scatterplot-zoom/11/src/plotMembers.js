import { parseResponse } from './parseResponse';
import { translatePoints } from './translatePoints';

export function plotMembers(vis) {
  /* semantic zoom for members of clusters */
  const objects = d3.selectAll('.objects');
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

  if (exemplarPointsVisible.length === 1) {
    // call API to get detail data
    const queryUrl = 'http://mr-0xc8:55555/3/Frames/members_exemplar0?column_offset=0&column_count=10';

    d3.xhr(queryUrl, 'application/json', (error, response) => {
      console.log('response', response);
      vis.detailData = parseResponse(response);
    });

    if (d3.selectAll('.detailDot')[0].length === 0) {
      const detailDots = objects.selectAll('.detailDot')
        .data(vis.detailData)
      .enter().append('circle')
        .classed('dot', true)
        .classed('detailDot', true)
      // .attr('r', function (d) {
      //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI);
      // })
      .attr('r', 2)
      .attr('transform', d => translatePoints(vis, d))
      .style('fill', 'darkorange')
      .style('fill-opacity', 0)
      .style('stroke-opacity', 0)
      // .style('stroke', d => color(d[colorCat]); })
      .style('stroke', 'darkorange')
      // .style('stroke-width', function (d) {
      //   return 3 * Math.sqrt(d[rCat] / Math.PI);
      // })
      .style('stroke-width', 1);

      detailDots.transition()
        .duration(2000)
        // .attr('transform', translatePoints)
        .style('fill-opacity', 0.4);
        // .style('stroke-opacity', 0.8);

      d3.selectAll('.detailDot')
        .on('mouseover', vis.tip.show)
        .on('mouseout', vis.tip.hide);
    }
  }

  if (exemplarPointsVisible.length > 1) {
    if (d3.selectAll('.detailDot')[0].length > 0) {
      d3.selectAll('.detailDot').transition()
        .duration(2000)
        .style('stroke-opacity', 0)
        .style('fill-opacity', 0)
        .remove();
    }
  }
}
