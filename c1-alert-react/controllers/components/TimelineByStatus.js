import React, { Component, PropTypes } from 'react'

const INDIVIDUAL = {
  radius: 5
}

const CLUSTER = {
  radius: 12
}

export class TimelineByStatus extends Component {
  render() {
    let individuals = this.props.group.individuals.map((a) => {
      let cx = this.props.timeScale(a.slaTime)

      return (
        <g transform={'translate(' + cx + ',0)'} key={a.alertId}>
          <circle r={INDIVIDUAL.radius} fill={this.props.priorityColor(a.priority)} />
        </g>
      )
    })

    let clusters = this.props.group.clusters.map((c, i) => {
      let cx = this.props.timeScale(c.slaTime)

      return (
        <g transform={'translate(' + cx + ',0)'} key={i}>
          <circle r={CLUSTER.radius} fill={this.props.priorityColor(c.priority)} />
          <text y="4" textAnchor="middle" fill="#fff">{c.values.length}</text>
        </g>
      )
    })

    return (
      <g className="alerts-timeline">
        <line x1={0} x2={this.props.width} />
        <text x={this.props.width - 10} y="-3" textAnchor="end">{this.props.group.label}</text>
        {individuals}
        {clusters}
      </g>
    )
  }
}
