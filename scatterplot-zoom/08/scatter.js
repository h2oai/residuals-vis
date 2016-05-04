var margin = { top: 50, right: 300, bottom: 50, left: 60 },
    outerWidth = 960 // 3648,
    outerHeight = 500 // 1900,
    width = outerWidth - margin.left - margin.right,
    height = outerHeight - margin.top - margin.bottom;

var x = d3.scale.linear()
    .range([0, width]).nice();

var y = d3.scale.linear()
    .range([height, 0]).nice();

var rScale = d3.scale.linear()
  .range([
    0,
    3
  ])

var xCat = 'C10',
    yCat = 'C1',
    rCat = 'C2',
    colorCat = 'C3';

var fileName = 'zoom0.csv'
d3.csv(fileName, function(data) {

  // TODO define this in terms of the max point radius
  var domainPaddingFactor = 0.1;

  var xMax = d3.max(data, function (d) { return +d[xCat]; });
  var xMin = d3.min(data, function (d) { return +d[xCat]; });
  var xExtent = xMax - xMin;
  var xDMax = xMax + (xExtent * domainPaddingFactor);
  var xDMin = xMin - (xExtent * domainPaddingFactor);

  var yMax = d3.max(data, function (d) { return +d[yCat]; });
  var yMin = d3.min(data, function (d) { return +d[yCat]; });
  var yExtent = yMax - yMin;
  var yDMax = yMax + (yExtent * domainPaddingFactor);
  var yDMin = yMin - (yExtent * domainPaddingFactor);

  x.domain([xDMin, xDMax]);
  y.domain([yDMin, yDMax]);

  console.log('data', data);
  console.log('x.domain()', x.domain());
  console.log('y.domain()', y.domain());

  var xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom')
      .tickSize(-height);

  var yAxis = d3.svg.axis()
      .scale(y)
      .orient('left')
      .tickSize(-width);

  var color = d3.scale.category10();

  var tip = d3.tip()
      .attr('class', 'd3-tip')
      .offset([-10, 0])
      .html(function (d) {
        return xCat + ': ' + d[xCat] + '<br>' + yCat + ': ' + d[yCat];
      });

  var zoomBeh = d3.behavior.zoom()
      .x(x)
      .y(y)
      .scaleExtent([0, 500])
      .on('zoom', zoom)
      .on('zoomend', zoomend);

  var svg = d3.select('#scatter')
    .append('svg')
      .attr('width', outerWidth)
      .attr('height', outerHeight)
    .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
      .call(zoomBeh);

  svg.call(tip);

  svg.append('rect')
      .attr('width', width)
      .attr('height', height);

  svg.append('g')
      .classed('x axis', true)
      .attr('transform', 'translate(0,' + height + ')')
      .call(xAxis)
    .append('text')
      .classed('label', true)
      .attr('x', width)
      .attr('y', margin.bottom - 10)
      .style('text-anchor', 'end')
      .text(xCat);

  svg.append('g')
      .classed('y axis', true)
      .call(yAxis)
    .append('text')
      .classed('label', true)
      .attr('transform', 'rotate(-90)')
      .attr('y', -margin.left)
      .attr('dy', '.71em')
      .style('text-anchor', 'end')
      .text(yCat);

  var objects = svg.append('svg')
      .classed('objects', true)
      .attr('width', width)
      .attr('height', height);

  objects.append('svg:line')
      .classed('axisLine hAxisLine', true)
      .attr('x1', 0)
      .attr('y1', 0)
      .attr('x2', width)
      .attr('y2', 0)
      .attr('transform', 'translate(0,' + height + ')');

  objects.append('svg:line')
      .classed('axisLine vAxisLine', true)
      .attr('x1', 0)
      .attr('y1', 0)
      .attr('x2', 0)
      .attr('y2', height);

  var dots = objects.selectAll('.dot')
      .data(data)
    .enter().append('circle')
      .classed('dot', true)
      // .attr('r', function (d) { 
      //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI); 
      // })
      .attr('r', function(d) {
        if (+d.C10 === 6279 && +d.C1 === 2596) { return 4 };
        return 2;
      })
      .attr('transform', transform)
      // .style('fill', function (d) { return color(d[colorCat]); })
      .style('fill', function(d) {
        if (+d.C10 === 6279 && +d.C1 === 2596) { return 'steelblue' };
        return 'darkgray';
      })
      .style('fill-opacity', function(d) {
        if (+d.C10 === 6279 && +d.C1 === 2596) { return 1 };
        return 0.2;
      })
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide);

  dots.classed('aggregate', true);

  var legend = svg.selectAll('.legend')
      .data(color.domain())
    .enter().append('g')
      .classed('legend', true)
      .attr('transform', function (d, i) {
        return 'translate(0,' + i * 20 + ')';
      });

  legend.append('circle')
      .attr('r', 3.5)
      .attr('cx', width + 20)
      .attr('fill', color);

  legend.append('text')
      .attr('x', width + 26)
      .attr('dy', '.35em')
      .text(function (d) { return d; });

  // 
  var testRecord = data
    .filter(function (d) {
      return +d.C10 === 6279 && +d.C1 === 2596;
    })

  // call API to get detail data
  queryUrl = 'http://127.0.0.1/:3000/3/Frames/members_exemplar0?column_offset=0&column_count=10';

  //var xhr = d3.xhr(queryUrl, "application/json", function (error, response) {
  //  console.log('response', response);
  //});

  // add the responseData inline until we get the API call working
  var responseData = {"__meta": {}, "_exclude_fields": "", "row_offset": 0, "row_count": 0, "column_offset": 0, "column_count": 10, "job": null, "frames": [{"__meta": {}, "frame_id": {}, "byte_size": 4619, "is_text": false, "row_offset": 0, "row_count": 25, "column_offset": 0, "column_count": 10, "total_column_count": 55, "checksum": 848678444177561500, "rows": 25, "num_columns": 55, "default_percentiles": [], "columns": [{"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C1", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [2589, 2589, 2590, 2592, 2593 ], "maxs": [2614, 2609, 2607, 2606, 2605 ], "mean": 2599.7200000000003, "sigma": 6.636515149785545, "type": "int", "domain": null, "domain_cardinality": 0, "data": [2596, 2590, 2595, 2606, 2605, 2589, 2601, 2600, 2595, 2593, 2592, 2594, 2589, 2605, 2602, 2601, 2595, 2607, 2603, 2605, 2603, 2600, 2604, 2614, 2609 ], "string_data": null, "precision": 0, "histogram_bins": null, "histogram_base": 2589, "histogram_stride": 1, "percentiles": [2589, 2589, 2590.8, 2593.8, 2595, 2595, 2595, 2598.4, 2601, 2602.4, 2603, 2603.8, 2605, 2605, 2606.6, 2612.8, 2613.88 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C2", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [21, 22, 25, 27, 27 ], "maxs": [69, 62, 62, 61, 57 ], "mean": 44.8, "sigma": 13.892443989449804, "type": "int", "domain": null, "domain_cardinality": 0, "data": [51, 56, 45, 45, 49, 37, 54, 57, 62, 69, 36, 27, 25, 62, 45, 27, 31, 45, 21, 34, 53, 61, 56, 50, 22 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 21, "histogram_stride": 1, "percentiles": [21.024, 21.24, 25.8, 30.200000000000003, 34, 36.2, 37, 45, 45, 50.4, 53, 53.8, 56, 56.2, 61.6, 67.32, 68.832 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C3", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [2, 2, 2, 3, 3 ], "maxs": [7, 6, 5, 5, 5 ], "mean": 4.12, "sigma": 1.2355835328567093, "type": "int", "domain": null, "domain_cardinality": 0, "data": [3, 2, 2, 7, 4, 2, 4, 5, 5, 3, 4, 4, 5, 5, 5, 5, 4, 4, 3, 3, 5, 6, 5, 4, 4 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 2, "histogram_stride": 1, "percentiles": [2, 2, 2.4000000000000004, 3, 3, 4, 4, 4, 4, 4.399999999999999, 5, 5, 5, 5, 5, 6.759999999999998, 6.975999999999999 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C4", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [150, 153, 153, 162, 170 ], "maxs": [277, 277, 270, 258, 255 ], "mean": 210.83999999999997, "sigma": 41.47858885417069, "type": "int", "domain": null, "domain_cardinality": 0, "data": [258, 212, 153, 270, 234, 210, 277, 255, 234, 201, 190, 180, 180, 277, 255, 234, 162, 255, 212, 192, 170, 150, 182, 175, 153 ], "string_data": null, "precision": 0, "histogram_bins": null, "histogram_base": 150, "histogram_stride": 1, "percentiles": [150.072, 150.72, 156.6, 174, 180, 180.4, 182, 191.2, 210, 220.79999999999995, 234, 234, 255, 255, 265.20000000000005, 277, 277 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C5", "missing_count": 0, "zero_count": 1, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [-7, -7, -7, -6, -4 ], "maxs": [8, 7, 7, 6, 6 ], "mean": 0.9199999999999998, "sigma": 4.795136424892761, "type": "int", "domain": null, "domain_cardinality": 0, "data": [0, -6, -1, 5, 7, -7, 4, 3, -1, -3, -4, -2, -7, 7, 4, 4, -1, 6, 5, 8, 6, 3, -7, 2, -2 ], "string_data": null, "precision": 0, "histogram_bins": null, "histogram_base": -7, "histogram_stride": 1, "percentiles": [-7, -7, -6.6, -3.1999999999999993, -2, -1.8000000000000007, -1, -1, 2, 3.3999999999999986, 4, 4, 5, 5.200000000000003, 6.600000000000001, 7.759999999999998, 7.975999999999999 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C6", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [330, 360, 360, 390, 391 ], "maxs": [684, 633, 626, 618, 603 ], "mean": 507.87999999999994, "sigma": 98.10552142123977, "type": "int", "domain": null, "domain_cardinality": 0, "data": [510, 390, 391, 633, 573, 360, 570, 540, 510, 450, 420, 360, 330, 601, 571, 541, 421, 603, 543, 514, 484, 454, 618, 684, 626 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 330, "histogram_stride": 1, "percentiles": [330.72, 337.2, 372, 414.20000000000005, 421, 450.8, 454, 499.6, 514, 541.8, 570, 570.8, 573, 601.4, 622.8, 671.7599999999999, 682.776 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C7", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [217, 217, 218, 218, 218 ], "maxs": [225, 224, 224, 223, 223 ], "mean": 220.80000000000004, "sigma": 2.3273733406281583, "type": "int", "domain": null, "domain_cardinality": 0, "data": [221, 220, 220, 222, 222, 219, 222, 223, 224, 223, 219, 218, 218, 224, 221, 218, 219, 221, 217, 219, 223, 225, 223, 222, 217 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 217, "histogram_stride": 1, "percentiles": [217, 217, 218, 218.8, 219, 219, 219, 220, 221, 222, 222, 222, 223, 223, 223.6, 224.76, 224.976 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C8", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [225, 227, 228, 229, 229 ], "maxs": [235, 234, 234, 233, 232 ], "mean": 230.36, "sigma": 2.233830790368868, "type": "int", "domain": null, "domain_cardinality": 0, "data": [232, 235, 234, 225, 230, 234, 231, 229, 230, 233, 230, 230, 229, 230, 229, 228, 230, 230, 232, 232, 229, 227, 229, 231, 230 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 225, "histogram_stride": 1, "percentiles": [225.048, 225.48, 228.4, 229, 229, 229.2, 230, 230, 230, 230, 231, 231, 232, 232, 233.6, 234.76, 234.976 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C9", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [136, 138, 141, 142, 142 ], "maxs": [152, 151, 151, 150, 150 ], "mean": 145.6, "sigma": 4.193248541803041, "type": "int", "domain": null, "domain_cardinality": 0, "data": [148, 151, 150, 138, 144, 151, 144, 142, 142, 145, 148, 149, 148, 142, 143, 147, 148, 145, 152, 149, 142, 136, 141, 145, 150 ], "string_data": null, "precision": -1, "histogram_bins": null, "histogram_base": 136, "histogram_stride": 1, "percentiles": [136.048, 136.48, 141.4, 142, 142, 143.2, 144, 144.6, 145, 148, 148, 148, 149, 149.2, 150.6, 151.76, 151.976 ] }, {"__meta": {"schema_version": 3, "schema_name": "ColV3", "schema_type": "Vec"}, "label": "C10", "missing_count": 0, "zero_count": 0, "positive_infinity_count": 0, "negative_infinity_count": 0, "mins": [6137, 6163, 6166, 6172, 6172 ], "maxs": [6281, 6279, 6268, 6267, 6256 ], "mean": 6215.759999999999, "sigma": 40.794484921371165, "type": "int", "domain": null, "domain_cardinality": 0, "data": [6279, 6225, 6172, 6256, 6228, 6212, 6281, 6267, 6253, 6225, 6212, 6185, 6172, 6268, 6254, 6240, 6185, 6242, 6214, 6200, 6186, 6172, 6163, 6166, 6137 ], "string_data": null, "precision": 0, "histogram_bins": null, "histogram_base": 6137, "histogram_stride": 1, "percentiles": [6137.624, 6143.24, 6168.4, 6172, 6185, 6185.2, 6186, 6207.2, 6214, 6226.2, 6240, 6241.6, 6253, 6254.4, 6267.6, 6280.52, 6280.952 ] } ], "compatible_models": null, "chunk_summary": {}, "distribution_summary": {} } ], "compatible_models": null, "domain": null };
  var columnsData = responseData.frames[0].columns;
  var exemplarMembers = [];
  columnsData.forEach(function (d) {
    d.data.forEach(function (e, j) {
      if (typeof exemplarMembers[j] === 'undefined') exemplarMembers[j] = {};
      exemplarMembers[j][d.label] = e; 
    })
  })

  console.log('responseData', responseData);
  console.log('columnsData', columnsData);

  var detailData = exemplarMembers;
  console.log('testRecord', testRecord)
  console.log('detailData', detailData);
  

  function zoom() {
    svg.select('.x.axis').call(xAxis);
    svg.select('.y.axis').call(yAxis);

    svg.selectAll('.dot')
        .attr('transform', transform);

    var zoomLevel = zoomBeh.scale();
    var zoomThreshold = 31.8;

    console.log('zoomLevel', zoomLevel);
    if (zoomLevel > zoomThreshold) {
      if (d3.selectAll('.detailDot')[0].length === 0) {
        var detailDots = objects.selectAll('.detailDot')
          .data(detailData)
        .enter().append('circle')
          .classed('dot', true)
          .classed('detailDot', true)
          // .attr('r', function (d) { 
          //   return 1 * Math.sqrt(rScale(d[rCat]) / Math.PI); 
          // })
          .attr('r', 2)
          .attr('transform', translateToAggregate)
          .style('fill', 'darkgray')
          .style('fill-opacity', 0)
          .style('stroke-opacity', 0)
          //.style('stroke', function (d) { return color(d[colorCat]); })
          .style('stroke', 'orange')
          // .style('stroke-width', function (d) { 
          //   return 3 * Math.sqrt(d[rCat] / Math.PI); 
          // })
          .style('stroke-width', 1)
          
        detailDots.transition()
            .duration(2000)
            .attr('transform', transform)
            .style('fill-opacity', 0.2)
            //.style('stroke-opacity', 0.8);
         
        d3.selectAll('.detailDot') 
          .on('mouseover', tip.show)
          .on('mouseout', tip.hide);
      }
    }

    if (zoomLevel < zoomThreshold) {
      if (d3.selectAll('.detailDot')[0].length > 0) {
        d3.selectAll('.detailDot').transition()
          .duration(2000)
          .attr('transform', translateToAggregate)
          .style('stroke-opacity', 0)
          .remove();
      }
      
    }
  }

  function zoomend() {
    
  }

  function transform(d) {
    return 'translate(' + x(d[xCat]) + ',' + y(d[yCat]) + ')';
  }

  function translateToAggregate (d) {
    return 'translate(' + 
      (x(testRecord[0][xCat])) + ',' +
      (y(testRecord[0][yCat])) +
    ')';
  }

  function translateFromAggregateToDetail (d) {
    return 'translate(' + 
      (x(d[xCat]) - x(testRecord[0][xCat])) + ',' +
      (y(d[yCat]) + y(testRecord[0][yCat])) +
    ')';
  }
});
