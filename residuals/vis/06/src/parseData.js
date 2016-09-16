export function parseData(inputData, options) {
  const numericColumns = options.numericColumns;
  const idColumn = options.idColumn;
  const idPrefix = options.idPrefix || '';
  console.log('idPrefix from parseData', idPrefix);

  // parse strings to numbers for numeric columns
  const data = [];
  inputData.forEach((d, i) => {
    data.push(d);
    // numericColumns.forEach(e => {
    //   data[i][e] = Number(d[e]);
    // })
    // if there is no idColumn, assign the index as an id
    if (typeof idColumn === 'undefined') {
      data[i].id = `${idPrefix}${i}`;
    }
  });
  console.log('data after parsing strings to numbers', data)
  return data;
}