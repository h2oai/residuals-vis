export function parseResponse(response) {
  const responseData = JSON.parse(response.response);
  const columnsData = responseData.frames[0].columns;
  const points = [];
  columnsData.forEach(d => {
    // console.log('d.label', d.label);
    // console.log('d from columnsData', d);
    if (Object.prototype.toString.call(d.data) === '[object Array]') {
      // if the current column is an enum or a category
      // recognize that the value in the data is acually a index for the
      // domain array
      // the value at that index position in the domain array
      // is actually the datum that we want
      d.data.forEach((e, j) => {
        if (typeof points[j] === 'undefined') points[j] = {};
          let value;
          if (d.type === "enum" && d.domain !== null) {
            value = d.domain[e];
          } else {
            value = e;
          }
          points[j][d.label] = value;
      });
    }
  });
  console.log('columnsData', columnsData);
  console.log('points', points);

  points.forEach((d, i) => {
    d.id = i;
  });

  const parsedData = points;

  // console.log('parsedData', parsedData);
  return parsedData;
}