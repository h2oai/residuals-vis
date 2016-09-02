import { d3DistanceLimitedVoronoi } from './distance-limited-voronoi';
import { tooltip } from './tooltip';
import * as d3 from 'd3';
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
  const idVariable = options.idVariable || 'id';
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
    .data(limitedVoronoiCells) // Use Voronoi() with your dataset inside
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
      .style('stroke', 'lightblue') // I use this to look at how the cells are dispersed as a check
      // .style('stroke', 'none')
      .style('fill', 'none')
      .style('pointer-events', 'all')
      .on('mouseover', showTooltip)
      .on('mouseout', removeTooltip);

  // Show the tooltip on the hovered over circle
  function showTooltip(d) {
    // Save the circle element (so not the voronoi which is triggering the hover event)
    // in a variable by using the unique class of the voronoi (idVariable)
    var element = d3.selectAll(`.marks.id${d.datum[idVariable]}`);
    console.log('element from showTooltip', element);
    
    // skip tooltip creation if already defined
    // existingTooltip = $(".popover");
    // if (existingTooltip !== null 
    //     && existingTooltip.length >0
    //     && existingTooltip.text()===d.Country) {
    //   return;
    // }
    
    // Define and show the tooltip 
    element.each(function () {
      tip.show;
    })

    // use bootstrap popover
    // But you can use whatever you prefer
    // $(element).popover({
    //   placement: 'auto top', // place the tooltip above the item
    //   container: '#chart', // the name (class or id) of the container
    //   trigger: 'manual',
    //   html : true,
    //   content: function() { // the html content to show inside the tooltip
    //     return "<span style='font-size: 11px; text-align: center;'>" + d.Country + "</span>"; }
    // });
    // $(element).popover('show');

    // Make chosen circle more visible
    element.style("opacity", 1);
            
  }// function showTooltip

  // Hide the tooltip when the mouse moves away
  function removeTooltip(d) {

    // Save the circle element (so not the voronoi which is triggering the hover event)
    // in a variable by using the unique class of the voronoi (idVariable)
    var element = d3.selectAll(`.marks.id${d.datum[idVariable]}`);
    console.log('element from removeTooltip', element);
    
    // Hide the tooltip
    element.each(function () {
      tip.hide;
    })

    // $('.popover').each(function() {
    //   $(this).remove();
    // }); 
    
    // Fade out the bright circle again
    element.style("opacity", 0.3);
    
  }// function removeTooltip
}
