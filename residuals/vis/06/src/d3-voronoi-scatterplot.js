// https://github.com/micahstubbs/d3-voronoi-scatterplot Version 0.1.0. Copyright 2016 Contributors.
(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports, require('d3'), require('lodash')) :
  typeof define === 'function' && define.amd ? define(['exports', 'd3', 'lodash'], factory) :
  (factory((global.d3VoronoiScatterplot = global.d3VoronoiScatterplot || {}),global.d3,global._));
}(this, function (exports,d3,_) { 'use strict';

  _ = 'default' in _ ? _['default'] : _;

  function d3Tip () {

    // Mappings to new version of d3
    var d3$$ = {
      select: d3.select,
      event: function event() {
        return d3.event;
      },
      selection: d3.selection,
      functor: function functor(v) {
        return typeof v === "function" ? v : function () {
          return v;
        };
      }
    };

    var direction = d3_tip_direction,
        offset = d3_tip_offset,
        html = d3_tip_html,
        node = initNode(),
        svg = null,
        point = null,
        target = null,
        parent = null;

    function tip(vis) {
      svg = getSVGNode(vis);
      point = svg.createSVGPoint();
    }

    // Public - show the tooltip on the screen
    //
    // Returns a tip
    tip.show = function () {
      if (!parent) tip.parent(document.body);
      var args = Array.prototype.slice.call(arguments);
      // console.log('args from tip.show', args);
      if (args[args.length - 1] instanceof SVGElement) target = args.pop();

      var content = html.apply(this, args),
          poffset = offset.apply(this, args),
          dir = direction.apply(this, args),
          nodel = getNodeEl(),
          i = directions.length,
          coords,
          parentCoords = node.offsetParent.getBoundingClientRect();

      nodel.html(content).style('position', 'absolute').style('opacity', 1).style('pointer-events', 'all');

      while (i--) {
        nodel.classed(directions[i], false);
      }coords = direction_callbacks[dir].apply(this);
      nodel.classed(dir, true).style('top', coords.top + poffset[0] - parentCoords.top + 'px').style('left', coords.left + poffset[1] - parentCoords.left + 'px');
      // .style('top', (coords.top - parentCoords.top + 'px')
      // .style('left', (coords.left - parentCoords.left) + 'px')

      return tip;
    };

    // Public - hide the tooltip
    //
    // Returns a tip
    tip.hide = function () {
      var nodel = getNodeEl();
      nodel.style('opacity', 0).style('pointer-events', 'none');
      return tip;
    };

    // Public: Proxy attr calls to the d3 tip container.  Sets or gets attribute value.
    //
    // n - name of the attribute
    // v - value of the attribute
    //
    // Returns tip or attribute value
    tip.attr = function (n, v) {
      if (arguments.length < 2 && typeof n === 'string') {
        return getNodeEl().attr(n);
      } else {
        var args = Array.prototype.slice.call(arguments);
        d3$$.selection.prototype.attr.apply(getNodeEl(), args);
      }

      return tip;
    };

    // Public: Proxy style calls to the d3 tip container.  Sets or gets a style value.
    //
    // n - name of the property
    // v - value of the property
    //
    // Returns tip or style property value
    tip.style = function (n, v) {
      // debugger;
      if (arguments.length < 2 && typeof n === 'string') {
        return getNodeEl().style(n);
      } else {
        var args = Array.prototype.slice.call(arguments);
        if (args.length === 1) {
          var styles = args[0];
          Object.keys(styles).forEach(function (key) {
            d3$$.selection.prototype.style.apply(getNodeEl(), [key, styles[key]]);
          });
        }
      }

      return tip;
    };

    // Public: Sets or gets the parent of the tooltip element
    //
    // v - New parent for the tip
    //
    // Returns parent element or tip
    tip.parent = function (v) {
      if (!arguments.length) return parent;
      parent = v || document.body;
      // console.log('parent from tip.parent', parent);
      parent.appendChild(node);

      // Make sure offsetParent has a position so the tip can be
      // based from it. Mainly a concern with <body>.
      var offsetParent = d3$$.select(node.offsetParent);
      if (offsetParent.style('position') === 'static') {
        offsetParent.style('position', 'relative');
      }

      return tip;
    };

    // Public: Set or get the direction of the tooltip
    //
    // v - One of n(north), s(south), e(east), or w(west), nw(northwest),
    //     sw(southwest), ne(northeast) or se(southeast)
    //
    // Returns tip or direction
    tip.direction = function (v) {
      if (!arguments.length) return direction;
      direction = v == null ? v : d3$$.functor(v);

      return tip;
    };

    // Public: Sets or gets the offset of the tip
    //
    // v - Array of [x, y] offset
    //
    // Returns offset or
    tip.offset = function (v) {
      if (!arguments.length) return offset;
      offset = v == null ? v : d3$$.functor(v);

      return tip;
    };

    // Public: sets or gets the html value of the tooltip
    //
    // v - String value of the tip
    //
    // Returns html value or tip
    tip.html = function (v) {
      if (!arguments.length) return html;
      html = v == null ? v : d3$$.functor(v);

      return tip;
    };

    // Public: destroys the tooltip and removes it from the DOM
    //
    // Returns a tip
    tip.destroy = function () {
      if (node) {
        getNodeEl().remove();
        node = null;
      }
      return tip;
    };

    function d3_tip_direction() {
      return 'n';
    }
    function d3_tip_offset() {
      return [0, 0];
    }
    function d3_tip_html() {
      return ' ';
    }

    var direction_callbacks = {
      n: direction_n,
      s: direction_s,
      e: direction_e,
      w: direction_w,
      nw: direction_nw,
      ne: direction_ne,
      sw: direction_sw,
      se: direction_se
    };

    var directions = Object.keys(direction_callbacks);

    function direction_n() {
      var bbox = getScreenBBox();
      return {
        top: bbox.n.y - node.offsetHeight,
        left: bbox.n.x - node.offsetWidth / 2
      };
    }

    function direction_s() {
      var bbox = getScreenBBox();
      return {
        top: bbox.s.y,
        left: bbox.s.x - node.offsetWidth / 2
      };
    }

    function direction_e() {
      var bbox = getScreenBBox();
      return {
        top: bbox.e.y - node.offsetHeight / 2,
        left: bbox.e.x
      };
    }

    function direction_w() {
      var bbox = getScreenBBox();
      return {
        top: bbox.w.y - node.offsetHeight / 2,
        left: bbox.w.x - node.offsetWidth
      };
    }

    function direction_nw() {
      var bbox = getScreenBBox();
      return {
        top: bbox.nw.y - node.offsetHeight,
        left: bbox.nw.x - node.offsetWidth
      };
    }

    function direction_ne() {
      var bbox = getScreenBBox();
      return {
        top: bbox.ne.y - node.offsetHeight,
        left: bbox.ne.x
      };
    }

    function direction_sw() {
      var bbox = getScreenBBox();
      return {
        top: bbox.sw.y,
        left: bbox.sw.x - node.offsetWidth
      };
    }

    function direction_se() {
      var bbox = getScreenBBox();
      return {
        top: bbox.se.y,
        left: bbox.e.x
      };
    }

    function initNode() {
      var node = d3$$.select(document.createElement('div'));
      node.style('position', 'absolute').style('top', 0).style('opacity', 0).style('pointer-events', 'none').style('box-sizing', 'border-box');

      return node.node();
    }

    function getSVGNode(el) {
      el = el.node();
      if (el.tagName.toLowerCase() === 'svg') return el;

      return el.ownerSVGElement;
    }

    function getNodeEl() {
      if (node === null) {
        node = initNode();
        // re-add node to DOM
        document.body.appendChild(node);
      };
      return d3$$.select(node);
    }

    // Private - gets the screen coordinates of a shape
    //
    // Given a shape on the screen, will return an SVGPoint for the directions
    // n(north), s(south), e(east), w(west), ne(northeast), se(southeast), nw(northwest),
    // sw(southwest).
    //
    //    +-+-+
    //    |   |
    //    +   +
    //    |   |
    //    +-+-+
    //
    // Returns an Object {n, s, e, w, nw, sw, ne, se}
    function getScreenBBox() {
      var targetel = target || d3$$.event().target;

      while ('undefined' === typeof targetel.getScreenCTM && 'undefined' === targetel.parentNode) {
        targetel = targetel.parentNode;
      }

      var bbox = {},
          matrix = targetel.getScreenCTM(),
          tbbox = targetel.getBBox(),
          width = tbbox.width,
          height = tbbox.height,
          x = tbbox.x,
          y = tbbox.y;

      point.x = x;
      point.y = y;
      bbox.nw = point.matrixTransform(matrix);
      point.x += width;
      bbox.ne = point.matrixTransform(matrix);
      point.y += height;
      bbox.se = point.matrixTransform(matrix);
      point.x -= width;
      bbox.sw = point.matrixTransform(matrix);
      point.y -= height / 2;
      bbox.w = point.matrixTransform(matrix);
      point.x += width;
      bbox.e = point.matrixTransform(matrix);
      point.x -= width / 2;
      point.y -= height / 2;
      bbox.n = point.matrixTransform(matrix);
      point.y += height;
      bbox.s = point.matrixTransform(matrix);

      return bbox;
    }

    return tip;
  };

  function tooltip(tooltipVariables) {
    var tip = d3Tip().parent(document.getElementById('chart')).attr('class', 'd3-tip').html(function (d) {
      // console.log('d from tooltip html function', d);
      var allRows = '';
      tooltipVariables.forEach(function (e) {
        var currentValue = void 0;
        if (typeof e.format !== 'undefined') {
          if (e.type === 'time') {
            // time formatting
            var inputValue = new Date(Number(d.datum[e.name]));
            // TODO: handle case where date values are strings
            var currentFormat = d3.timeFormat(e.format);
            currentValue = currentFormat(inputValue);
          } else {
            // number formatting
            var _inputValue = Number(d.datum[e.name]);
            var _currentFormat = d3.format(e.format);
            currentValue = _currentFormat(_inputValue);
          }
        } else {
          // no formatting
          currentValue = d.datum[e.name];
        }
        var currentRow = '<span style=\'font-size: 11px; display: block; text-align: center;\'>' + e.name + ' ' + currentValue + '</span>';
        allRows = allRows.concat(currentRow);
      });
      return '<div style=\'background-color: white; padding: 5px; border-radius: 6px;\n        border-style: solid; border-color: #D1D1D1; border-width: 1px;\'>\n        ' + allRows + '\n        </div>';
    });

    return tip;
  }

  function d3DistanceLimitedVoronoi() {
    /////// Internals ///////
    var voronoi = d3.voronoi().extent([[-1e6, -1e6], [1e6, 1e6]]);
    var limit = 20; // default limit
    var context = null; // set it to render to a canvas' 2D context

    function _distanceLimitedVoronoi(data) {
      if (context != null) {
        //renders into a Canvas
        context.beginPath();
        voronoi.polygons(data).forEach(function (cell) {
          distanceLimitedCell(cell, limit, context);
        });
        return true;
      } else {
        //final viz is an SVG
        return voronoi.polygons(data).map(function (cell) {
          return {
            path: distanceLimitedCell(cell, limit, d3.path()).toString(),
            datum: cell.data
          };
        });
      }
    }

    ///////////////////////
    ///////// API /////////
    ///////////////////////

    _distanceLimitedVoronoi.limit = function (_) {
      if (!arguments.length) {
        return limit;
      }
      if (typeof _ === "number") {
        limit = Math.abs(_);
      }

      return _distanceLimitedVoronoi;
    };

    _distanceLimitedVoronoi.x = function (_) {
      if (!arguments.length) {
        return voronoi.x();
      }
      voronoi.x(_);

      return _distanceLimitedVoronoi;
    };

    _distanceLimitedVoronoi.y = function (_) {
      if (!arguments.length) {
        return voronoi.y();
      }
      voronoi.y(_);

      return _distanceLimitedVoronoi;
    };

    _distanceLimitedVoronoi.extent = function (_) {
      if (!arguments.length) {
        return voronoi.extent();
      }
      voronoi.extent(_);

      return _distanceLimitedVoronoi;
    };

    //exposes the underlying d3.geom.voronoi
    //eg. allows to code 'limitedVoronoi.voronoi().triangle(data)'
    _distanceLimitedVoronoi.voronoi = function (_) {
      if (!arguments.length) {
        return voronoi;
      }
      voronoi = _;

      return _distanceLimitedVoronoi;
    };

    _distanceLimitedVoronoi.context = function (_) {
      if (!arguments.length) {
        return context;
      }
      context = _;

      return _distanceLimitedVoronoi;
    };

    ///////////////////////
    /////// Private ///////
    ///////////////////////

    function distanceLimitedCell(cell, r, context) {
      var seed = [voronoi.x()(cell.data), voronoi.y()(cell.data)];
      if (allVertecesInsideMaxDistanceCircle(cell, seed, r)) {
        context.moveTo(cell[0][0], cell[0][1]);
        for (var j = 1, m = cell.length; j < m; ++j) {
          context.lineTo(cell[j][0], cell[j][1]);
        }
        context.closePath();
        return context;
      } else {
        var pathNotYetStarted = true;
        var firstPointTooFar = pointTooFarFromSeed(cell[0], seed, r);
        var p0TooFar = firstPointTooFar;
        var p0, p1, intersections;
        var openingArcPoint, lastClosingArcPoint;
        var startAngle, endAngle;

        //begin: loop through all segments to compute path
        for (var iseg = 0; iseg < cell.length; iseg++) {
          p0 = cell[iseg];
          p1 = cell[(iseg + 1) % cell.length];
          // compute intersections between segment and maxDistance circle
          intersections = segmentCircleIntersections(p0, p1, seed, r);
          // complete the path (with lines or arc) depending on:
          // intersection count (0, 1, or 2)
          // if the segment is the first to start the path
          // if the first point of the segment is inside or outside of the maxDistance circle
          if (intersections.length === 2) {
            if (p0TooFar) {
              if (pathNotYetStarted) {
                pathNotYetStarted = false;
                // entire path will finish with an arc
                // store first intersection to close last arc
                lastClosingArcPoint = intersections[0];
                // init path at 1st intersection
                context.moveTo(intersections[0][0], intersections[0][1]);
              } else {
                //draw arc until first intersection
                startAngle = angle(seed, openingArcPoint);
                endAngle = angle(seed, intersections[0]);
                context.arc(seed[0], seed[1], r, startAngle, endAngle, 1);
              }
              // then line to 2nd intersection, then initiliaze an arc
              context.lineTo(intersections[1][0], intersections[1][1]);
              openingArcPoint = intersections[1];
            } else {
              // THIS CASE IS IMPOSSIBLE AND SHOULD NOT ARISE
              console.error("What's the f**k");
            }
          } else if (intersections.length === 1) {
            if (p0TooFar) {
              if (pathNotYetStarted) {
                pathNotYetStarted = false;
                // entire path will finish with an arc
                // store first intersection to close last arc
                lastClosingArcPoint = intersections[0];
                // init path at first intersection
                context.moveTo(intersections[0][0], intersections[0][1]);
              } else {
                // draw an arc until intersection
                startAngle = angle(seed, openingArcPoint);
                endAngle = angle(seed, intersections[0]);
                context.arc(seed[0], seed[1], r, startAngle, endAngle, 1);
              }
              // then line to next point (1st out, 2nd in)
              context.lineTo(p1[0], p1[1]);
            } else {
              if (pathNotYetStarted) {
                pathNotYetStarted = false;
                // init path at p0
                context.moveTo(p0[0], p0[1]);
              }
              // line to intersection, then initiliaze arc (1st in, 2nd out)
              context.lineTo(intersections[0][0], intersections[0][1]);
              openingArcPoint = intersections[0];
            }
            p0TooFar = !p0TooFar;
          } else {
            if (p0TooFar) {
              // entire segment too far, nothing to do
              true;
            } else {
              // entire segment in maxDistance
              if (pathNotYetStarted) {
                pathNotYetStarted = false;
                // init path at p0
                context.moveTo(p0[0], p0[1]);
              }
              // line to next point
              context.lineTo(p1[0], p1[1]);
            }
          }
        } //end: loop through all segments

        if (pathNotYetStarted) {
          // special case: no segment intersects the maxDistance circle
          // cell perimeter is entirely outside the maxDistance circle
          // path is the maxDistance circle
          pathNotYetStarted = false;
          context.moveTo(seed[0] + r, seed[1]);
          context.arc(seed[0], seed[1], r, 0, 2 * Math.PI, false);
        } else {
          // if final segment ends with an opened arc, close it
          if (firstPointTooFar) {
            startAngle = angle(seed, openingArcPoint);
            endAngle = angle(seed, lastClosingArcPoint);
            context.arc(seed[0], seed[1], r, startAngle, endAngle, 1);
          }
          context.closePath();
        }

        return context;
      }

      function allVertecesInsideMaxDistanceCircle(cell, seed, r) {
        var result = true;
        var p;
        for (var ip = 0; ip < cell.length; ip++) {
          result = result && !pointTooFarFromSeed(cell[ip], seed, r);
        }
        return result;
      }

      function pointTooFarFromSeed(p, seed, r) {
        return Math.pow(p[0] - seed[0], 2) + Math.pow(p[1] - seed[1], 2) > Math.pow(r, 2);
      }

      function angle(seed, p) {
        var v = [p[0] - seed[0], p[1] - seed[1]];
        // from http://stackoverflow.com/questions/2150050/finding-signed-angle-between-vectors, with v1 = horizontal radius = [seed[0]+r - seed[0], seed[0] - seed[0]]
        return Math.atan2(v[1], v[0]);
      }
    }

    function segmentCircleIntersections(A, B, C, r) {
      /*
      from http://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm
      */
      var Ax = A[0],
          Ay = A[1],
          Bx = B[0],
          By = B[1],
          Cx = C[0],
          Cy = C[1];

      // compute the euclidean distance between A and B
      var LAB = Math.sqrt(Math.pow(Bx - Ax, 2) + Math.pow(By - Ay, 2));

      // compute the direction vector D from A to B
      var Dx = (Bx - Ax) / LAB;
      var Dy = (By - Ay) / LAB;

      // Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.

      // compute the value t of the closest point to the circle center (Cx, Cy)
      var t = Dx * (Cx - Ax) + Dy * (Cy - Ay);

      // This is the projection of C on the line from A to B.

      // compute the coordinates of the point E on line and closest to C
      var Ex = t * Dx + Ax;
      var Ey = t * Dy + Ay;

      // compute the euclidean distance from E to C
      var LEC = Math.sqrt(Math.pow(Ex - Cx, 2) + Math.pow(Ey - Cy, 2));

      // test if the line intersects the circle
      if (LEC < r) {
        // compute distance from t to circle intersection point
        var dt = Math.sqrt(Math.pow(r, 2) - Math.pow(LEC, 2));
        var tF = t - dt; // t of first intersection point
        var tG = t + dt; // t of second intersection point

        var result = [];
        if (tF > 0 && tF < LAB) {
          // test if first intersection point in segment
          // compute first intersection point
          var Fx = (t - dt) * Dx + Ax;
          var Fy = (t - dt) * Dy + Ay;
          result.push([Fx, Fy]);
        }
        if (tG > 0 && tG < LAB) {
          // test if second intersection point in segment
          // compute second intersection point
          var Gx = (t + dt) * Dx + Ax;
          var Gy = (t + dt) * Dy + Ay;
          result.push([Gx, Gy]);
        }
        return result;
      } else {
        // either (LEC === r), tangent point to circle is E
        // or (LEC < r), line doesn't touch circle
        // in both cases, returning nothing is OK
        return [];
      }
    }

    return _distanceLimitedVoronoi;
  };

  function drawVoronoiOverlay(selector, data, options) {
    /*
      Initiate the Voronoi function
      Use the same variables of the data in the .x and .y as used
      in the cx and cy of the circle call
      The clip extent will make the boundaries end nicely along
      the chart area instead of splitting up the entire SVG
      (if you do not do this it would mean that you already see
      a tooltip when your mouse is still in the axis area, which
      is confusing)
    */

    var xVariable = options.xVariable;
    var yVariable = options.yVariable;
    var idVariable = options.idVariable || 'id';
    var xScale = options.xScale;
    var yScale = options.yScale;
    var width = options.width;
    var height = options.height;
    var tip = options.tip;

    var xAccessor = function xAccessor(d) {
      return xScale(d[xVariable]);
    };
    var yAccessor = function yAccessor(d) {
      return yScale(d[yVariable]);
    };

    var limitedVoronoi = d3DistanceLimitedVoronoi().x(xAccessor).y(yAccessor).limit(50).extent([[0, 0], [width, height]]);

    // console.log('data[0]', data[0]);
    var limitedVoronoiCells = limitedVoronoi(data);

    // create a group element to place the Voronoi diagram in
    var limitedVoronoiGroup = selector.append('g').attr('class', 'voronoiWrapper');

    // Create the distance-limited Voronoi diagram
    limitedVoronoiGroup.selectAll('path').data(limitedVoronoiCells) // Use Voronoi() with your dataset inside
    .enter().append('path')
    // .attr("d", function(d, i) { return "M" + d.join("L") + "Z"; })
    .attr('d', function (d) {
      // console.log('d from limitedVoronoiGroup', d);
      if (typeof d !== 'undefined') {
        return d.path;
      }
      return '';
    })
    // Give each cell a unique class where the unique part corresponds to the circle classes
    // .attr('class', d => `voronoi ${d.datum[idVariable]}`)
    .attr('class', function (d) {
      if (typeof d !== 'undefined') {
        return 'voronoi ' + d.datum[idVariable];
      }
      return 'voronoi';
    })
    // .style('stroke', 'lightblue') // I use this to look at how the cells are dispersed as a check
    .style('stroke', 'none').style('fill', 'none').style('pointer-events', 'all')
    // .on('mouseover', tip.show)
    // .on('mouseout', tip.hide);
    .on('mouseover', function (d, i, nodes) {
      // console.log('d from mouseover', d);
      // console.log('i from mouseover', i);
      // console.log('nodes from mouseover', nodes);
      // console.log('this from mouseover', this);
      showTooltip(d, i, nodes);
    }).on('mouseout', function (d, i, nodes) {
      // console.log('this from mouseout', this);
      removeTooltip(d, i, nodes);
    });

    // Show the tooltip on the hovered over circle
    function showTooltip(d, i, nodes) {
      // Save the circle element (so not the voronoi which is triggering the hover event)
      // in a variable by using the unique class of the voronoi (idVariable)
      var element = d3.selectAll('.marks.id' + d.datum[idVariable]);
      // console.log('element from showTooltip', element);
      // console.log('d from showTooltip', d);
      var pathStartX = Number(d.path.split('M')[1].split(',')[0]);
      var pathStartY = Number(d.path.split(',')[1].split('L')[0]);
      // console.log('pathStartX', pathStartX);
      // console.log('pathStartY', pathStartY);
      // console.log('element.nodes()[0] from removeTooltip', element.nodes()[0]);
      var currentDOMNode = element.nodes()[0];
      var cx = currentDOMNode.cx.baseVal.value;
      var cy = currentDOMNode.cy.baseVal.value;

      tip.show(d, i, nodes);
      // const tipTop = tip.style('top');
      // const tipLeft = tip.style('left');
      // const tipTopValue = Number(tipTop.slice(0, -2));
      // const tipLeftValue = Number(tipLeft.slice(0, -2));

      // const offsetX = tipLeftValue - cx;
      // const offsetY = tipTopValue - cy;
      var offsetX = 0; // pathStartX + (pathStartX - cx);
      var offsetY = pathStartY + (pathStartY - cy);
      // console.log('cx', cx);
      // console.log('tipLeft', tipLeft);
      // console.log('tipLeftValue', tipLeftValue);
      // console.log('calculated offsetX', offsetX);
      // console.log('cy', cy);
      // console.log('tipTop', tipTop);
      // console.log('tipTopValue', tipTopValue);
      // console.log('calculated offsetY', offsetY);
      // tip.offset([offsetX,offsetY]);
      // tip.offset([150, 150]);

      // Make chosen circle more visible
      element.style('fill-opacity', 1);
    } // function showTooltip

    // Hide the tooltip when the mouse moves away
    function removeTooltip(d, i, nodes) {

      // Save the circle element (so not the voronoi which is triggering the hover event)
      // in a variable by using the unique class of the voronoi (idVariable)
      var element = d3.selectAll('.marks.id' + d.datum[idVariable]);
      // console.log('element from removeTooltip', element);
      // console.log('element.nodes()[0] from removeTooltip', element.nodes()[0]);
      var currentDOMNode = element.nodes()[0];

      tip.hide(d, i, nodes);

      // Fade out the bright circle again
      element.style('fill-opacity', 0.3);
    } // function removeTooltip
  }

  function drawVoronoiScatterplot(selector, inputData, options) {
    //
    // Set-up
    //

    // vanilla JS window width and height
    var wV = window;
    var dV = document;
    var eV = dV.documentElement;
    var gV = dV.getElementsByTagName('body')[0];
    var xV = wV.innerWidth || eV.clientWidth || gV.clientWidth;
    var yV = wV.innerHeight || eV.clientHeight || gV.clientHeight;

    // Quick fix for resizing some things for mobile-ish viewers
    var mobileScreen = xV < 500;

    // set default configuration
    var cfg = {
      margin: { left: 120, top: 20, right: 80, bottom: 20 },
      width: 1000,
      animateFromZero: undefined,
      yVariable: 'residual',
      idVariable: 'id',
      marks: {
        r: 2,
        fillOpacity: 0.3
      }
    };

    // Put all of the options into a variable called cfg
    if (typeof options !== 'undefined') {
      for (var i in options) {
        if (typeof options[i] !== 'undefined') {
          cfg[i] = options[i];
        }
      } // for i
    } // if
    console.log('options passed in to scatterplot', options);
    console.log('cfg from scatterplot', cfg);

    // map variables to our dataset
    var xVariable = cfg.xVariable;
    var yVariable = cfg.yVariable;
    var rVariable = undefined;
    var idVariable = cfg.idVariable;
    var groupByVariable = undefined;
    var currentAlgo = cfg.currentAlgo;
    var currentAlgoLabel = cfg.currentAlgoLabel;
    var tooltipVariables = cfg.tooltipColumns;
    var numericVariables = cfg.numericColumns;
    var responseVariable = cfg.responseColumn;
    var dependent = cfg.dependent;
    var globalExtents = cfg.globalExtents;
    var animateFromZero = cfg.animateFromZero;
    var opacityCircles = cfg.marks.fillOpacity;
    var marksRadius = cfg.marks.r;

    // labels
    var xLabel = cfg.xLabel || xVariable;
    if (typeof responseVariable !== 'undefined') {
      xLabel = xLabel + ' (' + responseVariable + ')';
    }
    var yLabel = 'residual';
    // const xLabel = 'y\u{0302}'; // y-hat for the prediction
    // const yLabel = 'r\u{0302}'; // r-hat for the residual

    var div = d3.select(selector).append('div').attr('id', 'chart');

    // Scatterplot
    var margin = cfg.margin;
    var chartWidth = document.getElementById('chart').offsetWidth;
    var width = chartWidth - margin.left - margin.right;
    var height = cfg.width * 0.25;
    // const maxDistanceFromPoint = 50;

    var svg = div.append('svg').attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom);

    var wrapper = svg.append('g').classed('chartWrapper', true).classed('' + xVariable, true).attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

    if (typeof dependent !== 'undefined') {
      svg.classed('dependent', true);
      wrapper.classed('dependent', true);
      wrapper.attr('id', currentAlgo);

      // draw model label
      wrapper.append('g').attr('transform', 'translate(' + 20 + ', ' + 45 + ')').append('text').classed('modelLabel', true).style('font-size', '40px').style('font-weight', 400).style('opacity', 0.15).style('fill', 'gray').style('font-family', 'Work Sans, sans-serif').text('' + currentAlgoLabel);
    } else {
      svg.classed('independent', true);
      wrapper.classed('independent', true);
      wrapper.attr('id', currentAlgo);
    }

    //
    // Initialize Axes & Scales
    //

    // Set the color for each region
    var color = d3.scaleOrdinal().range(['#1f78b4', '#ff7f00', '#33a02c', '#e31a1c', '#6a3d9a', '#b15928', '#a6cee3', '#fdbf6f', '#b2df8a', '#fb9a99', '#cab2d6', '#ffff99']);

    // parse strings to numbers
    var data = _.cloneDeep(inputData);
    // console.log('data from scatterplot', data);

    data.forEach(function (d) {
      numericVariables.forEach(function (e) {
        d[e] = Number(d[e]);
      });
    });

    // Set the new x axis range
    var xScale = d3.scaleLinear().range([0, width]);

    // Set the new y axis range
    var yScale = d3.scaleLinear().range([height, 0]);

    if (typeof globalExtents !== 'undefined') {
      // retrieve global extents
      var xExtent = globalExtents[0];
      var yExtent = globalExtents[1];

      // set scale domains with global extents
      xScale.domain(xExtent);
      yScale.domain(yExtent).nice();
    } else {
      // set scale domains from the local extent
      xScale.domain(d3.extent(data, function (d) {
        return d[xVariable];
      }));
      // .nice();
      yScale.domain(d3.extent(data, function (d) {
        return d[yVariable];
      })).nice();
    }

    // Set new x-axis
    var xAxis = d3.axisBottom().ticks(4).tickSizeOuter(0)
    // .tickFormat(d => // Difficult function to create better ticks
    //   xScale.tickFormat((mobileScreen ? 4 : 8), e => {
    //     const prefix = d3.format(',.0s');
    //     return `${prefix(e)}`;
    //   })(d))
    .scale(xScale);

    // Append the x-axis
    wrapper.append('g').attr('class', 'x axis').attr('transform', 'translate(' + 0 + ', ' + yScale(0) + ')').call(xAxis);

    var yAxis = d3.axisLeft().ticks(6) // Set rough # of ticks
    .scale(yScale);

    // Append the y-axis
    wrapper.append('g').attr('class', 'y axis').attr('transform', 'translate(' + 0 + ', ' + 0 + ')').call(yAxis);

    // Scale for the bubble size
    if (typeof rVariable !== 'undefined') {
      var _rScale = d3.scaleSqrt().range([mobileScreen ? 1 : 2, mobileScreen ? 10 : 16]).domain(d3.extent(data, function (d) {
        return d[rVariable];
      }));
    }

    //
    // Scatterplot Circles
    //

    // Initiate a group element for the circles
    var circleGroup = wrapper.append('g').attr('class', 'circleWrapper');

    // Place the country circles
    var circles = circleGroup.selectAll('marks').data(function () {
      if (typeof rVariable !== 'undefined') {
        // Sort so the biggest circles are below
        return data.sort(function (a, b) {
          return b[rVariable] > a[rVariable];
        });
      }
      return data;
    }).enter().append('circle').attr('class', function (d) {
      return 'marks id' + d[idVariable];
    }).style('fill-opacity', opacityCircles).style('fill', function (d) {
      if (typeof groupByVariable !== 'undefined') {
        return color(d[groupByVariable]);
      }
      return color.range()[0];
    }).attr('cx', function (d) {
      return xScale(d[xVariable]);
    }).attr('cy', function (d) {
      if (typeof animateFromZero !== 'undefined') {
        return yScale(0);
      } else {
        return yScale(d[yVariable]);
      }
    }).attr('r', function (d) {
      if (typeof rVariable !== 'undefined') {
        return rScale(d[rVariable]);
      }
      return marksRadius;
    });

    if (typeof animateFromZero !== 'undefined') {
      circles.transition().delay(2000).duration(2000).attr('cy', function (d) {
        return yScale(d[yVariable]);
      });
    }

    //
    // Tooltips
    //

    var tip = tooltip(tooltipVariables);
    svg.call(tip);

    //
    // distance-limited Voronoi overlay
    //

    var voronoiOptions = {
      xVariable: xVariable,
      yVariable: yVariable,
      idVariable: idVariable,
      xScale: xScale,
      yScale: yScale,
      width: width,
      height: height,
      tip: tip
    };
    drawVoronoiOverlay(wrapper, data, voronoiOptions);

    //
    // Initialize Labels
    //

    var xlabelText = xLabel || xVariable;
    var yLabelText = yLabel || yVariable;

    // Set up X axis label
    wrapper.append('g').append('text').attr('class', 'x title').attr('text-anchor', 'start').style('font-size', (mobileScreen ? 8 : 12) + 'px').style('font-weight', 600).attr('transform', 'translate(' + 30 + ',' + -10 + ')').text('' + xlabelText);

    // Set up y axis label
    wrapper.append('g').append('text').attr('class', 'y title').attr('text-anchor', 'end').attr('dy', '0.35em').style('font-size', (mobileScreen ? 8 : 12) + 'px')
    // .attr('transform', 'translate(18, 0) rotate(-90)')
    .attr('transform', 'translate(' + -(margin.left / 4) + ',' + yScale(0) + ')').text('' + yLabelText);

    //
    // Hide axes on click
    //
    var axisVisible = true;

    function click() {
      if (axisVisible) {
        d3.selectAll('.y.axis').style('opacity', 0);
        d3.selectAll('.x.axis text').style('opacity', 0);
        d3.selectAll('.x.axis .tick').style('opacity', 0);
        axisVisible = false;
      } else {
        d3.selectAll('.axis').style('opacity', 1);
        d3.selectAll('.x.axis text').style('opacity', 1);
        d3.selectAll('.x.axis .tick').style('opacity', 1);
        axisVisible = true;
      }
    }

    d3.selectAll('.chartWrapper').on('click', function () {
      click();
    });
  }

  exports.drawVoronoiScatterplot = drawVoronoiScatterplot;

  Object.defineProperty(exports, '__esModule', { value: true });

}));