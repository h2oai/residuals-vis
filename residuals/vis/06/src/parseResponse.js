export function parseResponse(response) {

  const responseData = JSON.parse(response.response);
  const columnsData = responseData.frames[0].columns;
  const points = [];
  columnsData.forEach(d => {
    if (Object.prototype.toString.call(d.data) === '[object Array]') {
      d.data.forEach((e, j) => {
        if (typeof points[j] === 'undefined') points[j] = {};
        points[j][d.label] = e;
      });
    }
  });
  console.log('columnsData', columnsData);
  console.log('points', points);

  points.forEach((d, i) => {
    d.id = i;
  });

  const parsedData = points;

  console.log('parsedData', parsedData);
  return parsedData;
}