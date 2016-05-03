import React, { Component, PropTypes } from 'react'

const INDIVIDUAL = {
  radius: 5
}

const CLUSTER = {
  radius: 8
}

export class TimelineByPriority extends Component {
  render() {
    let circles = this.props.group.alerts.map((a) => {
      let cx = this.props.timeScale(a.slaTime)

      return (
        <g transform={'translate(' + cx + ',0)'} key={a.alertId}>
          <circle r={INDIVIDUAL.radius} fill={this.props.priorityColor(a.priority)} />
        </g>
      )
    })

    return (
      <g className="alerts-timeline">
        <line x1={0} x2={this.props.width} />
        <text x={this.props.width - 10} y="-3" textAnchor="end">{this.props.group.label}</text>
        {circles}
      </g>
    )
  }
}
