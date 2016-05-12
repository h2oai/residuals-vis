export function parseResponse(response) {
  const responseData = JSON.parse(response.response);
  console.log('responseData', responseData);

  const columnsData = responseData.frames[0].columns;
  const exemplarMembers = [];
  columnsData.forEach(d => {
    d.data.forEach((e, j) => {
      if (typeof exemplarMembers[j] === 'undefined') exemplarMembers[j] = {};
      exemplarMembers[j][d.label] = e;
    });
  });
  console.log('columnsData', columnsData);

  const detailData = exemplarMembers;
  // console.log('exemplar', exemplar);
  console.log('detailData', detailData);
  return detailData;
}
