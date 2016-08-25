import * as d3 from 'd3';

export function dropdown(selector, inputData, options) {

  const categoricalVariables = options.categoricalColumns;
  const data = categoricalVariables;
  console.log('categoricalVariables from dropdown', categoricalVariables);

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

  // populate the drop down menu of categorical columns
  d3.select('#dropdown')
    .selectAll('option')
    .data(data)
    .enter().append('option')
    .text(d  => d)
    .attr('value', (d, i) => i);

  // add `color by...` prompt
  const dropdownSelection = document.getElementById("dropdown");
  const optionElement = document.createElement("option");
  optionElement.text = 'color by...';
  optionElement.value = -1;
  dropdownSelection.add(optionElement, 0);

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
      } else { // currentLabel is undefined
        // reset the fill color
        d3.selectAll('.marks')
          .style('fill', d => color.range()[0])

        d3.selectAll('rect.box')
          .style('fill', d => color.range()[0])
          .style('fill-opacity', 1)
      }
  }

  let marksFiltered = undefined;
  function legendRectClick(d) {
    console.log('legendRectClick was called');
    if(typeof marksFiltered === 'undefined') {
      d3.selectAll('.marks')
        .filter(e => {
          return e[currentLabel] !== d;
        })
        .style('fill-opacity', 0);
      marksFiltered = true;
    } else {
      // reset the mark opacity 
      d3.selectAll('.marks')
        .style('fill-opacity', 0.3);
      marksFiltered = undefined;
    }
  }
}
