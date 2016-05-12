import { translatePoints } from './translatePoints';

export function zoom(xAxis, yAxis, x, y, xCat, yCat, zoomBeh, detailData, tip) {
  const svg = d3.select('svg');
  const objects = d3.selectAll('.objects');
  svg.select('.x.axis').call(xAxis);
  svg.select('.y.axis').call(yAxis);

  svg.selectAll('.dot')
    .attr('transform', d => translatePoints(d, x, xCat, y, yCat));

  const zoomLevel = zoomBeh.scale();
  const zoomThreshold = 31.8;

  console.log('zoomLevel', zoomLevel);
  if (zoomLevel > zoomThreshold) {
    if (d3.selectAll('.detailDot')[0].length === 0) {
      const detailDots = objects.selectAll('.detailDot')
        .data(detailData)
      .enter().append('circle')
        .classed('dot', true)
        .classed('detailDot', true)
      // .attr('r', function (d) {
      //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI);
      // })
      .attr('r', 2)
      .attr('transform', d => translatePoints(d, x, xCat, y, yCat)) // translateFromAggregateToDetail
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
        .on('mouseover', tip.show)
        .on('mouseout', tip.hide);
    }
  }

  if (zoomLevel < zoomThreshold) {
    if (d3.selectAll('.detailDot')[0].length > 0) {
      d3.selectAll('.detailDot').transition()
        .duration(2000)
        // .attr('transform', translatePoints) // translateToAggregate
        .style('stroke-opacity', 0)
        .style('fill-opacity', 0)
        .remove();
    }
  }
}
