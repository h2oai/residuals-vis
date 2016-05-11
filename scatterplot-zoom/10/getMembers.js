export function getMembers(d) {
  let row = d.id;
  // call API to get detail data
  // let queryUrl = 'http://mr-0xc8:55555/3/Frames/members_exemplar0?column_offset=0&column_count=10';
  let queryUrl = `http://mr-0xc8:55555/3/Frames/members_exemplar${row}?column_offset=0&column_count=10`;
  console.log('queryUrl', queryUrl);
  d3.xhr(queryUrl, "application/json", (error, response) => {
    responseData = JSON.parse(response.response);
    console.log('response', response);
    console.log('responseData', responseData);
    updateDetailData(responseData);
    drawMembers();
  });
}