import * as d3 from 'd3';

export function getGlobalExtents(data, options) {
  const algos = options.algos;

  // find max extent across all algos
  const globalExtents = [
    [0, 0], // global xVariable Predict extent
    [0, 0]  // global yVariable Residual extent 
  ];
  ['Predict', 'Residual'].forEach((variable, i) => {
    algos.forEach(algo => {
      const currentExtent = d3.extent(data, d => Number(d[`${algo}${variable}`]));
      // update global min
      if (currentExtent[0] < globalExtents[i][0]) {
        globalExtents[i][0] = currentExtent[0];
      }
      // update the global max
      if (currentExtent[1] > globalExtents[i][1]) {
        globalExtents[i][1] = currentExtent[1];
      }
    })
  })
  return globalExtents;
}
