// import { plotMembers } from './plotMembers';

export function drawVoronoiPaths(vis, data) {
  // Initiate the voronoi function
  // Use the same variables of the data in the .x and .y as used in the cx and cy of the circle call
  // The clip extent will make the boundaries end nicely along the chart area instead of splitting up the entire SVG
  // (if you do not do this it would mean that you already see a tooltip when your mouse is still in the axis area, which is confusing)
  const voronoi = d3.geom.voronoi()
    .x(d => vis.x(d[vis.xCat]))
    .y(d => vis.y(d[vis.yCat]))
    .clipExtent([[0, 0], [vis.width, vis.height]]);

  // Initiate a group element to place the voronoi diagram in
  const voronoiGroup = vis.svg.append('g')
    .attr('class', 'voronoiG');

  // Create the Voronoi diagram
  vis.voronoiPaths = voronoiGroup.selectAll('path')
    .data(voronoi(data).filter(d => d.length > 0)) // Use voronoi() with your dataset inside
    .enter().append('path')
    .attr('d', d => `M${d.join('L')}Z`)
    .datum(d => d.point)
    // Give each cell a unique class where the unique part corresponds to the circle classes
    .attr('class', d => `voronoi ${d.id}`)
    .style('stroke', '#2074A0') // I use this to look at how the cells are dispersed as a check
    .style('stroke-opacity', 0.3)
    .style('fill', 'none')
    .style('pointer-events', 'all');

  /*
  function mouseover() {
    d3.select(this)
      .style('fill', 'lightgray')
      .style('fill-opacity', 0.7);

    plotMembers(vis);
  }

  function mouseout() {
    d3.select(this)
      .style('fill', 'none');
  }

  vis.voronoiPaths
    .on('mouseover', mouseover)
    .on('mouseout', mouseout);
  */
}
