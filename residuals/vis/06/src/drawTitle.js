import * as d3 from 'd3';

export function drawTitle(selector, options) {
  const projectTitle = options.projectTitle;
  const projectLink = options.projectLink;
  const algos = options.algos;
  const currentAlgo = algos[0];
  let dataText;

  if (typeof options.dataText !== 'undefined') {
    dataText = options.dataText;
  } else { // default text
    dataText = 'data';
  }

  const titleHTML = `calcuated from a ${currentAlgo} model trained with <a href='https://github.com/h2oai/h2o-3'>h2o-3</a> on ${dataText} 
    from the <a href='${projectLink}'>${projectTitle}</a> project`

  d3.select(selector)
    .html(titleHTML);
}
