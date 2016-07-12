// https://github.com/h2oai/vis-components#readme Version 0.1.0. Copyright 2016 undefined.
(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (factory());
}(this, function () { 'use strict';

  function curve (data, tpr, fpr, interpolationMode, xScale, yScale) {
    var lineGenerator = d3.svg.line()
     .interpolate(interpolationMode)
     .x(function(d) { return x(d[fpr]); })
     .y(function(d) { return y(d[tpr]); });
    return lineGenerator(data);
  }

  var d3$1 = require('d3');

  module.exports = {
    plot: function(selector, data, options) {
    // set default configuration
    var cfg = {
      "margin": {top: 30, right: 20, bottom: 70, left: 61},
      "width": 470,
      "height": 450,
      "interpolationMode": "basis",
      "ticks": undefined,
      "tickValues": [0, .1, .25, .5, .75, .9, 1],
      "fpr": "fpr",
      "tprVariables": [{
        "name": "tpr0",
      }], 
      "animate": true
    }

    console.log('options passed to rocChart.plot', options);

    //Put all of the options into a variable called cfg
    if('undefined' !== typeof options){
      for(var i in options){
        if('undefined' !== typeof options[i]){ cfg[i] = options[i]; }
      }//for i
    }//if

    var tprVariables = cfg["tprVariables"];
    // if values for labels are not specified
    // set the default values for the labels to the corresponding
    // true positive rate variable name
    tprVariables.forEach(function(d, i) {
      if('undefined' === typeof d.label){
        d.label = d.name;
      }

    })

    console.log("tprVariables", tprVariables);

    const interpolationMode = cfg["interpolationMode"];
    const fpr = cfg["fpr"];
    const width = cfg["width"];
    const height = cfg["height"];
    const animate = cfg["animate"];
    const margin = cfg["margin"];

    var format = d3$1.format('.2');
    var aucFormat = d3$1.format('.4r');
    
    var x = d3$1.scale.linear().range([0, width]);
    var y = d3$1.scale.linear().range([height, 0]);
    var color = d3$1.scale.category10() // d3.scale.ordinal().range(["steelblue", "red", "green", "purple"]);

    var xAxis = d3$1.svg.axis()
      .scale(x)
      .orient("top")
      .outerTickSize(0);

    var yAxis = d3$1.svg.axis()
      .scale(y)
      .orient("right")
      .outerTickSize(0);

    // set the axis ticks based on input parameters,
    // if ticks or tickValues are specified
    if('undefined' !== typeof cfg["ticks"]) {
      xAxis.ticks(cfg["ticks"]);
      yAxis.ticks(cfg["ticks"]);
    } else if ('undefined' !== typeof cfg["tickValues"]) {
      xAxis.tickValues(cfg["tickValues"]);
      yAxis.tickValues(cfg["tickValues"]);
    } else {
      xAxis.ticks(5);
      yAxis.ticks(5);
    }

    // apply the format to the ticks we chose
    xAxis.tickFormat(format);
    yAxis.tickFormat(format);
    
    // a function that returns a line generator
    /*
    function curve(data, tpr) {

       var lineGenerator = d3.svg.line()
        .interpolate(interpolationMode)
        .x(function(d) { return x(d[fpr]); })
        .y(function(d) { return y(d[tpr]); });

      return lineGenerator(data);
    }
    */

    // a function that returns an area generator
    function areaUnderCurve(data, tpr) {

      var areaGenerator = d3$1.svg.area()
        .x(function(d) { return x(d[fpr]); })
        .y0(height)
        .y1(function(d) { return y(d[tpr]); });

      return areaGenerator(data);
    }

    var svg = d3$1.select(selector)
      .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    x.domain([0, 1]);
    y.domain([0, 1]);

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .append("text")            
          .attr("x", width / 2)
          .attr("y", 40 )
          .style("text-anchor", "middle")
          .text("False Positive Rate")
            
    var xAxisG = svg.select("g.x.axis");
              
    // draw the top boundary line
    xAxisG.append("line")
      .attr({
        "x1": -1,
        "x2": width + 1,
        "y1": -height,
        "y2": -height
      });

    // draw a bottom boundary line over the existing
    // x-axis domain path to make even corners
    xAxisG.append("line")
      .attr({
        "x1": -1,
        "x2": width + 1,
        "y1": 0,
        "y2": 0
      });


    // position the axis tick labels below the x-axis
    xAxisG.selectAll('.tick text')
      .attr('transform', 'translate(0,' + 25 + ')');

    // hide the y-axis ticks for 0 and 1
    xAxisG.selectAll("g.tick line")
      .style("opacity", function(d) {
        // if d is an integer
        return d % 1 === 0 ? 0 : 1;
      });

    svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)
      .append("text")            
        .attr("transform", "rotate(-90)")
        .attr("y", -35)
        // manually configured so that the label is centered vertically 
        .attr("x", 0 - height/1.56)  
        .style("font-size","12px")              
        .style("text-anchor", "left")
        .text("True Positive Rate");

    yAxisG = svg.select("g.y.axis");

    // add the right boundary line
    yAxisG.append("line")
      .attr({
        "x1": width,
        "x2": width,
        "y1": 0,
        "y2": height
      })

    // position the axis tick labels to the right of 
    // the y-axis and
    // translate the first and the last tick labels
    // so that they are right aligned
    // or even with the 2nd digit of the decimal number
    // tick labels
    yAxisG.selectAll("g.tick text")
      .attr('transform', function(d) {
        if(d % 1 === 0) { // if d is an integer
          return 'translate(' + -22 + ',0)';
        } else if((d*10) % 1 === 0) { // if d is a 1 place decimal
          return 'translate(' + -32 + ',0)';
        } else {
          return 'translate(' + -42 + ',0)';
        }
    })

    // hide the y-axis ticks for 0 and 1
    yAxisG.selectAll("g.tick line")
      .style("opacity", function(d) {
        // if d is an integer
        return d % 1 === 0 ? 0 : 1;
      });

    // draw the random guess line
    svg.append("line") 
      .attr("class", "curve")         
      .style("stroke", "black")
      .attr({
        "x1": 0,
        "x2": width,
        "y1": height,
        "y2": 0
      })
      .style({
        "stroke-width": 2,
        "stroke-dasharray": "8",
        "opacity": 0.4
      })

    // draw the ROC curves
    function drawCurve(data, tpr, stroke){

      svg.append("path")
        .attr("class", "curve")
        .style("stroke", stroke)
        .attr("d", curve(data, tpr, fpr, interpolationMode, x, y))
        .on('mouseover', function(d) {

          var areaID = "#" + tpr + "Area";
          svg.select(areaID)
            .style("opacity", .4)

          var aucText = "." + tpr + "text"; 
          svg.selectAll(aucText)
            .style("opacity", .9)
        })
        .on('mouseout', function(){
          var areaID = "#" + tpr + "Area";
          svg.select(areaID)
            .style("opacity", 0)

          var aucText = "." + tpr + "text"; 
          svg.selectAll(aucText)
            .style("opacity", 0)


        });
    }

    // draw the area under the ROC curves
    function drawArea(data, tpr, fill) {
      svg.append("path")
        .attr("class", "area")
        .attr("id", tpr + "Area")
        .style({
          "fill": fill,
          "opacity": 0
        })
        .attr("d", areaUnderCurve(data, tpr))
    }

    function drawAUCText(auc, tpr, label) {

      svg.append("g")
        .attr("class", tpr + "text")
        .style("opacity", 0)
        .attr("transform", "translate(" + .5*width + "," + .79*height + ")")
        .append("text")
          .text(label)
          .style({
            "fill": "white",
            "font-size": 18
          });

      svg.append("g")
        .attr("class", tpr + "text")
        .style("opacity", 0)
        .attr("transform", "translate(" + .5*width + "," + .84*height + ")")
        .append("text")
          .text("AUC = " + aucFormat(auc))
          .style({
            "fill": "white",
            "font-size": 18
          });

    }
    
    // calculate the area under each curve
    tprVariables.forEach(function(d){
      var tpr = d.name;
      var points = generatePoints(data, fpr, tpr);
      var auc = calculateArea(points);
      d["auc"] = auc;
    })

    console.log("tprVariables", tprVariables);

    // draw curves, areas, and text for each 
    // true-positive rate in the data
    tprVariables.forEach(function(d, i){
      console.log("drawing the curve for", d.label)
      console.log("color(", i, ")", color(i));
      var tpr = d.name;
      drawArea(data, tpr, color(i))
      drawCurve(data, tpr, color(i));
      drawAUCText(d.auc, tpr, d.label);
    })

    ///////////////////////////////////////////////////
    ////// animate through areas for each curve ///////
    ///////////////////////////////////////////////////

    if(animate && animate !== "false") {
      //sort tprVariables ascending by AUC
      var tprVariablesAscByAUC = tprVariables.sort(function(a, b) {
        return a.auc - b.auc;
      })

      console.log("tprVariablesAscByAUC", tprVariablesAscByAUC);
      
      for(var i = 0; i < tprVariablesAscByAUC.length; i++) {
        areaID = "#" + tprVariablesAscByAUC[i]["name"] + "Area";
        svg.select(areaID)
          .transition()
            .delay(2000 * (i+1))
            .duration(250)
            .style("opacity", .4)
          .transition()
            .delay(2000 * (i+2))
            .duration(250)
            .style("opacity", 0)

        textClass = "." + tprVariablesAscByAUC[i]["name"] + "text";
        svg.selectAll(textClass)
          .transition()
            .delay(2000 * (i+1))
            .duration(250)
            .style("opacity", .9)
          .transition()
            .delay(2000 * (i+2))
            .duration(250)
            .style("opacity", 0)
      }  
    }

    // styles
    d3$1.select('body')
      .style({ 
        'font-size': '12px', 
        'font-family': 'Open Sans'
      });

    d3$1.selectAll('path.curve')
      .style({
        'stroke-width': 3,
        'fill': 'none',
        'opacity': 0.7
      });

    d3$1.selectAll('.axis path')
      .style({
        fill: 'none',
        stroke: 'grey',
        'stroke-width': 2,
        'shape-rendering': 'crispEdges',
        opacity: 1
      });

    d3$1.selectAll('.axis line')
      .style({
        fill: 'none',
        stroke: 'grey',
        'stroke-width': 2,
        'shape-rendering': 'crispEdges',
        opacity: 1
      });

    d3$1.selectAll('.d3-tip')
      .style({
      'font-family': 'Verdana',
      background: rgba(0, 0, 0, 0.8),
      padding: '8px',
      color: 'white', 
      'z-index': 5070
    })

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////

    function generatePoints(data, x, y) {
      var points = [];
      data.forEach(function(d){
        points.push([ Number(d[x]), Number(d[y]) ])
      })
      return points;
    }

    // numerical integration
    function calculateArea(points) {
      var area = 0.0;
      var length = points.length;
      if (length <= 2) {
        return area;
      }
      points.forEach(function(d, i) {
        var x = 0,
            y = 1;

        if('undefined' !== typeof points[i-1]){
          area += (points[i][x] - points[i-1][x]) * (points[i-1][y] + points[i][y]) / 2;
        }
        
      });
      return area;
    }

    }
  }

}));