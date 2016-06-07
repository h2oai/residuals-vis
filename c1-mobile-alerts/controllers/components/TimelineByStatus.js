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
      let cy = this.props.timeScale(a.slaTime)
      let onMouseOver = () => {
        let content = <SingleAlertTooltipDetail alert={a} />
        this.props.setTooltip(cy, this.props.y + INDIVIDUAL.radius + 1, content)
      }

      let onMouseOut = () => {
        this.props.clearTooltip()
      }
 
      return (
        <g transform={'translate(0, ' + cy + ')'} key={a.alertId}>
          <circle 
            r={INDIVIDUAL.radius} 
            fill={PRIORITIES_MAP[a.priority].color} 
            onMouseOver={onMouseOver} 
            onMouseOut={onMouseOut} />
        </g>
      )
    })

    let clusters = this.props.group.clusters.map((c, i) => {
      let cy = this.props.timeScale(c.slaTime)
      let onMouseOver = () => {
        let content = <MultipleAlertsTooltipDetail cluster={c} />
        this.props.setTooltip(cy, this.props.x + CLUSTER.radius + 1, content)
      }

      let onMouseOut = () => {
        this.props.clearTooltip()
      }

      return (
        <g transform={'translate(0, ' + cy + ')'} key={i}>
          <circle 
            r={CLUSTER.radius} 
            fill={PRIORITIES_MAP[c.priority].color} />
          <text y="4" fontSize="13" textAnchor="end" fill="#fff">{c.values.length}</text>
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
        <line y1={0} y2={this.props.height} />
        <text className="alerts-status-label" y={this.props.height - 2} y="-3" textAnchor="end">{this.props.group.label}</text>
        {individuals}
        {clusters}
      </g>
    )
  }
}

export default TimelineByStatus
