import * as d3 from 'd3';

export function dropdown(selector, inputData, options) {

  const categoricalVariables = options.categoricalVariables;
  const data = categoricalVariables;

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

  d3.select(selector)
    .append('div')
    .classed('selectContainer', true)
    // .style('float', 'right');

  // create the drop down menu of categorical columns
  d3.select('div.selectContainer')
    .append('select')
    .attr('id', 'dropdown')
    .selectAll('option')
    .data(data)
    .enter().append('option')
    .text(d  => d)
    .attr('value', (d, i) => i);

  // generate a random index value and set the selector to the categorical column
  // at that index value in the data array
  let index = Math.round(Math.random() * data.length);
  d3.select('#dropdown').property('selectedIndex', index);

  // append a paragraph tag to the body that shows the city name and it's population
  // d3.select('')
  //   .append('p')
  //   .data(data)
  //   .text(function(d){ 
  //     return data[index]['city'] + ' - population ' + comma(data[index]['population']); 
  //   })

  // when the user selects a categorical column, set the value of
  // the index variable
  // and call the update() function
  d3.select('#dropdown')
  .on('change', function () {
    index = this.value;
    console.log('this', this.value);
    const currentLabel = categoricalVariables[this.value];
    console.log('currentLabel', currentLabel);
    update();
  })

  // update the paragraph text to match the selection made by the user
  function update() {
    const currentCategoricalVariable = categoricalVariables[index];
    const currentValues = d3.set(inputData, d => d[currentCategoricalVariable]).values();
    console.log('currentValues', currentValues);

    // clear the old legend
    d3.selectAll('#categoricalVariableLegend').remove();

    // draw the new legend
    const svg = d3.select('.selectContainer')
      .append('svg')
      .attr('height', 40)
      .attr('width', 120)
      .attr('id', 'categoricalVariableLegend');

    const legendG = svg.selectAll('g')
      .data(currentValues)
      .enter()
      .append('g')
        .attr('transform', (d, i) => `translate(0, ${i * 15})`)
        .classed('legendG', true);
        
    legendG.append('rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', 10)
      .attr('height', 10)
      .style('fill', (d, i) => color(i));

    legendG.append('text')
      .attr('x', 15)
      .attr('y', 10)
      .attr('dy', '-0.35em')
      .attr('font-size', '8px')
      .text(d => d);

    // d3.selectAll('p')
    //   .data(data)
    //   .text(function(d){ 
    //     return data[index]['city'] + ' - population ' + comma(data[index]['population']); 
    //   })
  }
}
