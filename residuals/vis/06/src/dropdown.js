import { updateMarksStyles } from './updateMarksStyles';
import * as d3 from 'd3';

export function dropdown(selector, inputData, options) {
  const categoricalVariables = options.chartOptions.categoricalColumns;
  const data = categoricalVariables;
  const marksColors = options.chartOptions.marks.colors;
  console.log('categoricalVariables from dropdown', categoricalVariables);

  // insert an empty state menu selection
  // data.splice(0, 0, 'filter...');

  let currentLabel;

  const color = d3.scaleOrdinal()
    .range(marksColors);

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
    const updateMarksStylesOptions = {
      color,
      categoricalVariables,
      index,
      currentLabel,
      chartOptions: options.chartOptions
    };
    updateMarksStyles(inputData, updateMarksStylesOptions);
  })

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
