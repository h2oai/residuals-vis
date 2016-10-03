import * as d3 from 'd3';

export function drawTitle(selector, options) {
  const projectTitle = options.projectTitle;
  const projectLink = options.projectLink;
  const algo = options.currentAlgo;
  const algoLabel = options.currentAlgoLabel.toLowerCase();
  
  let dataText;
  if (typeof options.dataText !== 'undefined') {
    dataText = options.dataText;
  } else { // default text
    dataText = 'data';
  }

  let projectTitleNote;
  if (typeof options.projectTitleNote !== 'undefined') {
    projectTitleNote = options.projectTitleNote;
  } else {
    projectTitleNote = '';
  }

  // clear any existing html
  d3.select(selector)
    .html('');

  const titleHTML = `calcuated from a ${algoLabel} model trained with <a href='https://github.com/h2oai/h2o-3'>h2o-3</a> on ${dataText} 
    from the <a href='${projectLink}'>${projectTitle}</a> project. ${projectTitleNote}`;

  // set the new title
  d3.select(selector)
    .html(titleHTML);
}
