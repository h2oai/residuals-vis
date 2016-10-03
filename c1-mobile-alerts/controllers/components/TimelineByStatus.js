import React, { Component, PropTypes } from 'react'
import SingleAlertTooltipDetail from './SingleAlertTooltipDetail'
import MultipleAlertsTooltipDetail from './MultipleAlertsTooltipDetail'
import { PRIORITIES_MAP } from '../constants'

const INDIVIDUAL = {
  radius: 16
}

const CLUSTER = {
  radius: 28
}

const HOLD_TIME = 1500

class TimelineByStatus extends Component {
  constructor() {
    super()

    this.state = {
      lastTap: new Date()
    }
  }

  render() {
    let individuals = this.props.group.individuals.map((a) => {
      let cy = this.props.timeScale(a.slaTime)
      let onTouchStart = (e) => {
        e.preventDefault()
        let currentTime = new Date()
        let delta = currentTime.getTime() - this.state.lastTap.getTime()

        if (delta < 250) {
          window.location.href = 'http://h2o.ai'
        } else {
          let content = <SingleAlertTooltipDetail alert={a} />
          this.props.setTooltip(this.props.x, cy - INDIVIDUAL.radius - 1, content)
        }

        this.setState({
          lastTap: currentTime
        })

        // window.location.href = 'http://h2o.ai'
      }

      let onTouchEnd = () => {
        this.props.clearTooltip()

      }
 
      return (
        <g transform={'translate(0, ' + cy + ')'} key={a.alertId}>
          <circle 
            r={INDIVIDUAL.radius} 
            fill={PRIORITIES_MAP[a.priority].color} 
            onTouchStart={onTouchStart} 
            onTouchEnd={onTouchEnd} />
        </g>
      )
    })

    let clusters = this.props.group.clusters.map((c, i) => {
      let cy = this.props.timeScale(c.slaTime)

      let onTouchStart = (e) => {
        e.preventDefault()

        let currentTime = new Date()
        let delta = currentTime.getTime() - this.state.lastTap.getTime()

        if (delta < 250) {
          window.location.href = 'http://h2o.ai'
        } else {
          let content = <MultipleAlertsTooltipDetail cluster={c} />
          this.props.setTooltip(this.props.x, cy - CLUSTER.radius - 1, content)
        }

        this.setState({
          lastTap: currentTime
        })
      }

      let onTouchEnd = () => {
        this.props.clearTooltip()
      }

      return (
        <g transform={'translate(0, ' + cy + ')'} key={i}>
          <circle 
            r={CLUSTER.radius} 
            fill={PRIORITIES_MAP[c.priority].color} />
          <text y="8" fontSize="24" fontWeight="bold" textAnchor="middle" fill="#fff">{c.values.length}</text>
          <circle 
            r={CLUSTER.radius} 
            fill="transparent"
            onTouchStart={onTouchStart} 
            onTouchEnd={onTouchEnd} />
        </g>
      )
    })

    return (
      <g className="alerts-timeline">
        <line y1={0} y2={this.props.height} />
        <g transform="rotate(90)">
          <text className="alerts-status-label" y="-6" x="6">{this.props.group.label}</text>
        </g>
        {individuals}
        {clusters}
      </g>
    )
  }
}

export default TimelineByStatus
