import React, { Component, PropTypes } from 'react'
import d3 from 'd3'

const HEIGHT = 20
const WIDTH = HEIGHT * 4

const strokeWidth = 2.5
const r = HEIGHT / 2
const ir = r - strokeWidth

const ringPath = 'M ' + r + ' 0 '
 + 'A ' + r + ' ' + r + ' 0 0 1 ' + r + ' ' + HEIGHT + ' '
 + 'A ' + r + ' ' + r + ' 0 0 1 ' + r + ' 0 Z'

 + 'M ' + r + ' ' + strokeWidth + ' '
 + 'A ' + ir + ' ' + ir + ' 0 0 0 ' + r + ' ' + (HEIGHT - strokeWidth) + ' '
 + 'A ' + ir + ' ' + ir + ' 0 0 0 ' + r + ' ' + strokeWidth + ' Z'

const unitWidth = strokeWidth
const barWidth = HEIGHT - 4 * unitWidth
const offsetBar = (HEIGHT - barWidth) / 2
const offsetUnit = (HEIGHT - unitWidth) / 2

const arc = d3.svg.arc()
      .outerRadius(r - 1.5)
      .innerRadius(ir - 1.5)
      .startAngle(Math.PI * -0.25)
      .endAngle(Math.PI * 1.375)()

const triangle = 'M ' + (unitWidth * 1) + ' ' + (HEIGHT / 2 - unitWidth) + ' '
 + 'L ' + (unitWidth * 1) + ' ' + (HEIGHT / 2 - unitWidth * 3.5) + ' '
 + 'L ' + (unitWidth * 3.5) + ' ' + (HEIGHT / 2 - unitWidth) + ' Z'

class AlertZoomControls extends Component {
  render() {
    return (
      <svg className="alerts-zoom-controls" width={WIDTH} height={HEIGHT}>
        <g transform={'translate(' + (HEIGHT * 0.2) + ',0)'}>
          <path d={ringPath} fill="#ddd" />
          <rect fill="#ddd" x={offsetBar} y={offsetUnit} width={barWidth} height={unitWidth} />
          <rect fill="#ddd" x={offsetUnit} y={offsetBar} width={unitWidth} height={barWidth} />
          <circle cx={HEIGHT / 2} cy={HEIGHT / 2} r={r} opacity="0" fill="#fff" onMouseDown={this.props.onZoomIn.mouseDown} onMouseUp={this.props.onZoomIn.mouseUp} />
        </g>

        <g transform={'translate(' + (HEIGHT * 1.5) + ',0)'}>
          <path d={ringPath} fill="#ddd" />
          <rect fill="#ddd" x={offsetBar} y={offsetUnit} width={barWidth} height={unitWidth} />
          <circle cx={HEIGHT / 2} cy={HEIGHT / 2} r={r} opacity="0" fill="#fff" onMouseDown={this.props.onZoomOut.mouseDown} onMouseUp={this.props.onZoomOut.mouseUp} />
        </g>

        <g transform={'translate(' + (HEIGHT * 2.8) + ',0)'}>
          <g transform={'translate(' + r + ',' + r + ')'}>
            <path d={arc} fill="#ddd" />
          </g>
          <path d={triangle} fill="#ddd" />
          <rect width={HEIGHT} height={HEIGHT} opacity="0" fill="#ddd" onClick={this.props.onResetClick} />
        </g>
      </svg>
    )
  }
}

export default AlertZoomControls