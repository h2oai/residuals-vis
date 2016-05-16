import { parseResponse } from './parseResponse';
import { plotExemplars } from './plotExemplars';
import d3 from 'd3';

export function drawScatterplot() {
  const vis = {};
  vis.margin = { top: 50, right: 300, bottom: 50, left: 60 };
  vis.outerWidth = 960; // 3648
  vis.outerHeight = 500; // 1900
  vis.width = vis.outerWidth - vis.margin.left - vis.margin.right;
  vis.height = vis.outerHeight - vis.margin.top - vis.margin.bottom;

  vis.x = d3.scale.linear()
    .range([0, vis.width]).nice();

  vis.y = d3.scale.linear()
    .range([vis.height, 0]).nice();

  // const rScale = d3.scale.linear()
  //   .range([0, 3]);

  vis.xCat = 'C10';
  vis.yCat = 'C1';
  // const rCat = 'C2';
  // const colorCat = 'C3';

  // call API to get exemplar data
  const queryUrl = 'http://mr-0xc8:55555/3/Frames/aggregated_covtype_20k_data.hex_by_aggregatormodel?column_offset=0&column_count=10';

  d3.xhr(queryUrl, 'application/json', (error, response) => {
    console.log('response', response);
    vis.exemplarData = parseResponse(response);
    plotExemplars(vis);
  });
}
