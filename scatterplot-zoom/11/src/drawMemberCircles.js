import { translatePoints } from './translatePoints';

export function drawMemberCircles(vis) {
  console.log("d3.selectAll('.detailDot')[0].length", d3.selectAll('.detailDot')[0].length);
  if (d3.selectAll('.detailDot')[0].length === 0) {
    const objects = d3.selectAll('.objects');
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
      .duration(500)
      // .attr('transform', translatePoints)
      .style('fill-opacity', 0.4);
      // .style('stroke-opacity', 0.8);

    d3.selectAll('.detailDot')
      .on('mouseover', vis.tip.show)
      .on('mouseout', vis.tip.hide);
  }
}
