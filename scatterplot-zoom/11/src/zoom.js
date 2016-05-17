import { translatePoints } from './translatePoints';
import { drawVoronoiPaths } from './drawVoronoiPaths';
import { plotMembers } from './plotMembers';

export function zoom(vis) {
  const svg = d3.select('svg');
  svg.select('.x.axis').call(vis.xAxis);
  svg.select('.y.axis').call(vis.yAxis);

  svg.selectAll('.dot')
    .attr('transform', d => translatePoints(vis, d));

  d3.selectAll('path.voronoi')
    .remove();

  drawVoronoiPaths(vis, vis.exemplarData);

  plotMembers(vis);
}
