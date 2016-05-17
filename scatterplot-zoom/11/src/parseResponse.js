export function parseResponse(response) {
  const responseData = JSON.parse(response.response);
  console.log('responseData', responseData);

  const columnsData = responseData.frames[0].columns;
  const points = [];
  columnsData.forEach(d => {
    d.data.forEach((e, j) => {
      if (typeof points[j] === 'undefined') points[j] = {};
      points[j][d.label] = e;
    });
  });
  console.log('columnsData', columnsData);

  const parsedData = points;
  // console.log('exemplar', exemplar);
  console.log('parsedData', parsedData);
  return parsedData;
}
