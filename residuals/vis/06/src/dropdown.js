import * as d3 from 'd3';

export function dropdown(selector, inputData, options) {

  const categoricalVariables = options.categoricalVariables;
  const data = categoricalVariables;

  // insert an empty state menu selection
  // data.splice(0, 0, 'filter...');

  let currentLabel;

  const color = d3.scaleOrdinal()
    .range([
      '#1f78b4',
      '#ff7f00',
      '#33a02c',
      '#e31a1c',
      '#6a3d9a',
      '#b15928',
      '#a6cee3',
      '#fdbf6f',
      '#b2df8a',
      '#fb9a99',
      '#cab2d6',
      '#ffff99'
    ]);

  // create the drop down menu of categorical columns
  d3.select('#dropdown')
    .selectAll('option')
    .data(data)
    .enter().append('option')
    .text(d  => d)
    .attr('value', (d, i) => i);

  // set the selector to the categorical column
  // to the index value in the data array
  // let index = Math.round(Math.random() * data.length);
  let index = 0;
  d3.select('#dropdown').property('selectedIndex', index);

  // when the user selects a categorical column, set the value of
  // the index variable
  // and call the update() function
  d3.select('#dropdown')
  .on('change', function () {
    index = this.value;
    console.log('this', this.value);
    currentLabel = categoricalVariables[this.value];
    console.log('currentLabel', currentLabel);
    update();
  })

  // update the paragraph text to match the selection made by the user
  function update() {
    const currentCategoricalVariable = categoricalVariables[index];
    const currentValues = d3.set(inputData, d => d[currentCategoricalVariable]).values();
    console.log('currentValues', currentValues);

    // update the domain of the color scale
    color.domain(currentValues);

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
        .style('fill', (d, i) => {
          console.log('d from legend rect', d);
          return color(d)
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
      }
  }
}
