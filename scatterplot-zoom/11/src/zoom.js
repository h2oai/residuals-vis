import { parseResponse } from './parseResponse';
import { translatePoints } from './translatePoints';
import { drawVoronoiPaths } from './drawVoronoiPaths';

export function zoom(vis) {
  const svg = d3.select('svg');
  const objects = d3.selectAll('.objects');
  svg.select('.x.axis').call(vis.xAxis);
  svg.select('.y.axis').call(vis.yAxis);

  svg.selectAll('.dot')
    .attr('transform', d => translatePoints(vis, d));

  d3.selectAll('path.voronoi')
    .remove();

  drawVoronoiPaths(vis, vis.exemplarData);

  /* semantic zoom for members of clusters */
  const zoomLevel = vis.zoomBeh.scale();
  const zoomThreshold = 31.8;

  console.log('zoomLevel', zoomLevel);
  if (zoomLevel > zoomThreshold) {
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

  if (zoomLevel < zoomThreshold) {
    if (d3.selectAll('.detailDot')[0].length > 0) {
      d3.selectAll('.detailDot').transition()
        .duration(2000)
        .style('stroke-opacity', 0)
        .style('fill-opacity', 0)
        .remove();
    }
  }
}
