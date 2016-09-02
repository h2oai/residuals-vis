import * as d3 from 'd3';

export function updateMarksStyles(inputData, options) {
  const marksColors = options.chartOptions.marks.colors;
  const categoricalVariables = options.chartOptions.categoricalColumns;
  const index = options.index;
  const currentLabel = options.currentLabel;
  const chartOptions = options.chartOptions;

  // update the paragraph text to match the selection made by the user
  const currentCategoricalVariable = categoricalVariables[index];
  const currentValues = d3.set(inputData, d => d[currentCategoricalVariable]).values();
  console.log('currentValues', currentValues);

  // update the domain of the color scale
  const color = d3.scaleOrdinal()
    .domain(currentValues)
    .range(marksColors);

  // clear the old legend
  d3.selectAll('#categoricalVariableLegend')
    .selectAll('.legendG')
    .remove();

  if (typeof currentLabel !== 'undefined') {
    const svg = d3.select('#categoricalVariableLegend');

    const legendG = svg.selectAll('g')
      .data(currentValues)
      .enter()
      .append('g')
        .attr('transform', (d, i) => `translate(0, ${i * 16})`)
        .classed('legendG', true);
      
    legendG.append('rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', 12)
      .attr('height', 12)
      .style('fill', (d, i) => color(d))
      .style('pointer-events', 'all')
      .on('click', (d) => {
        legendRectClick(d);
      });

    legendG.append('text')
      .attr('x', 17)
      .attr('y', 12)
      .attr('dy', '-0.35em')
      .attr('font-size', '10px')
      .attr('font-family', 'Open Sans, sans-serif')
      .text(d => d);

    d3.selectAll('.marks')
      .style('fill', d => {
        return color(d[currentLabel]);
      })

    if (typeof chartOptions.skeletonBox === 'undefined') {
      let boxFillOpacity;
      // style boxplot boxes
      d3.selectAll('rect.box')
        .style('fill', d => {
          // console.log('d from box style', d);
          // console.log('d.classProportions[currentLabel] from box style', d.classProportions[currentLabel]);
          const currentClassProportions = d.classProportions[currentLabel];
          const dominantClass = Object.keys(currentClassProportions).reduce((a, b) => {
            return currentClassProportions[a] > currentClassProportions[b] ? a : b;
          });
          // boxFillOpacity = d.classProportions[currentLabel][dominantClass];
          console.log('currentLabel', currentLabel);
          console.log('dominantClass', dominantClass);
          // console.log('boxFillOpacity', boxFillOpacity);
          return color(dominantClass);
        })
        .style('fill-opacity', d => {
          const currentClassProportions = d.classProportions[currentLabel];
          const dominantClass = Object.keys(currentClassProportions).reduce((a, b) => {
            return currentClassProportions[a] > currentClassProportions[b] ? a : b;
          });
          const boxFillOpacity = d.classProportions[currentLabel][dominantClass];
          return boxFillOpacity;
        });
    }
  } else { // currentLabel is undefined
    // reset the fill color of the points
    d3.selectAll('.marks')
      .style('fill', d => color.range()[0])

    // reset the fill color of the boxplot boxes
    d3.selectAll('rect.box')
      .style('fill', d => color.range()[0])
      .style('fill-opacity', () => {
        if (typeof chartOptions.skeletonBox !== 'undefined') {
          return 0;
        } else {
          return 1;
        }
      })
  }
}
