import * as d3 from 'd3';

export function getGlobalExtents(data, options) {
  const combined = options.combined;
  const globalExtents = [
      [0, 0], // global xVariable Predict extent
      [0, 0]  // global yVariable Residual extent 
    ];

  if (typeof combined !== 'undefined') {
    // one array-of-objects dataset 
    // with columnns (properties) from many datasets

    const algos = options.algos;
    // find max extent across all algos
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
  } else {
    // one object that contains many datasets
    // find max extent across all datasets

    const xVariable = options.xVariable;
    const yVariable = options.yVariable;
    Object.keys(datasets).forEach((key) => {
      const currentDataset = datasets[key];
      [xVariable, yVariable].forEach((variable, i) => {
        const currentExtent = d3.extent(currentDataset, d => Number(d[variable]));
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
}
