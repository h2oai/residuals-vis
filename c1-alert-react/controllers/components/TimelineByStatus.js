import React, { Component, PropTypes } from 'react'
import SingleAlertTooltipDetail from './SingleAlertTooltipDetail'
import MultipleAlertsTooltipDetail from './MultipleAlertsTooltipDetail'
import { PRIORITIES_MAP } from '../constants'

const INDIVIDUAL = {
  radius: 5
}

const CLUSTER = {
  radius: 10
}

class TimelineByStatus extends Component {
  render() {
    let individuals = this.props.group.individuals.map((a) => {
      let cx = this.props.timeScale(a.slaTime)
      let onMouseOver = () => {
        let content = <SingleAlertTooltipDetail alert={a} />
        this.props.setTooltip(cx, this.props.y + INDIVIDUAL.radius + 1, content)
      }

      let onMouseOut = () => {
        this.props.clearTooltip()
      }
 
      return (
        <g transform={'translate(' + cx + ',0)'} key={a.alertId}>
          <circle 
            r={INDIVIDUAL.radius} 
            fill={PRIORITIES_MAP[a.priority].color} 
            onMouseOver={onMouseOver} 
            onMouseOut={onMouseOut} />
        </g>
      )
    })

    let clusters = this.props.group.clusters.map((c, i) => {
      let cx = this.props.timeScale(c.slaTime)
      let onMouseOver = () => {
        let content = <MultipleAlertsTooltipDetail cluster={c} />
        this.props.setTooltip(cx, this.props.y + CLUSTER.radius + 1, content)
      }

      let onMouseOut = () => {
        this.props.clearTooltip()
      }

      return (
        <g transform={'translate(' + cx + ',0)'} key={i}>
          <circle 
            r={CLUSTER.radius} 
            fill={PRIORITIES_MAP[c.priority].color} />
          <text y="4" fontSize="13" textAnchor="middle" fill="#fff">{c.values.length}</text>
          <circle 
            r={CLUSTER.radius} 
            fill="transparent"
            onMouseOver={onMouseOver} 
            onMouseOut={onMouseOut} />
        </g>
      )
    })

    return (
      <g className="alerts-timeline">
        <line x1={0} x2={this.props.width} />
        <text className="alerts-status-label" x={this.props.width - 2} y="-3" textAnchor="end">{this.props.group.label}</text>
        {individuals}
        {clusters}
      </g>
    )
  }
}

export default TimelineByStatus
