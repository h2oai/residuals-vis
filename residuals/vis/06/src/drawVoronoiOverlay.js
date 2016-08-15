import { d3DistanceLimitedVoronoi } from './distance-limited-voronoi';
import { tooltip } from './tooltip';
export function drawVoronoiOverlay(selector, data, options) {
  /*
    Initiate the Voronoi function
    Use the same variables of the data in the .x and .y as used
    in the cx and cy of the circle call
    The clip extent will make the boundaries end nicely along
    the chart area instead of splitting up the entire SVG
    (if you do not do this it would mean that you already see
    a tooltip when your mouse is still in the axis area, which
    is confusing)
  */

  const xVariable = options.xVariable;
  const yVariable = options.yVariable;
  const idVariable = options.idVariable;
  const xScale = options.xScale;
  const yScale = options.yScale;
  const width = options.width;
  const height = options.height;
  const tip = options.tip;

  const xAccessor = d => xScale(d[xVariable]);
  const yAccessor = d => yScale(d[yVariable]);

  const limitedVoronoi = d3DistanceLimitedVoronoi()
    .x(xAccessor)
    .y(yAccessor)
    .limit(50)
    .extent([[0, 0], [width, height]]);

  // console.log('data[0]', data[0]);
  const limitedVoronoiCells = limitedVoronoi(data);

  // create a group element to place the Voronoi diagram in
  const limitedVoronoiGroup = selector.append('g')
    .attr('class', 'voronoiWrapper');

  // Create the distance-limited Voronoi diagram
  limitedVoronoiGroup.selectAll('path')
    .data(limitedVoronoiCells) // Use Voonoi() with your dataset inside
    .enter().append('path')
      // .attr("d", function(d, i) { return "M" + d.join("L") + "Z"; })
      .attr('d', d => {
        // console.log('d from limitedVoronoiGroup', d);
        if (typeof d !== 'undefined') {
          return d.path;
        }
        return '';
      })
      // Give each cell a unique class where the unique part corresponds to the circle classes
      // .attr('class', d => `voronoi ${d.datum[idVariable]}`)
      .attr('class', d => {
        if (typeof d !== 'undefined') {
          return `voronoi ${d.datum[idVariable]}`;
        }
        return 'voronoi';
      })
      // .style('stroke', 'lightblue') // I use this to look at how the cells are dispersed as a check
      .style('stroke', 'none')
      .style('fill', 'none')
      .style('pointer-events', 'all')
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide);
}
