import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import d3 from 'd3'
import _ from 'underscore'
import moment from 'moment'

import { computeSLATime } from '../SLAutils'
import { fetchAlerts, setSLATimeWindow, setNow } from '../actions/alertsActions'
import { SLA_TIMEWINDOW } from '../constants'

import TimelineByStatus from '../components/TimelineByStatus'
import AlertTooltipWrapper from '../components/AlertTooltipWrapper'
import AlertZoomControls from '../components/AlertZoomControls'
import AlertFilters from '../containers/AlertFilters'

const WIDTH = 900 // Default Width
const HEIGHT = 160

const REFRESH_RATE = 15 * 1000 // Every 15 seconds

class AlertsTimeline extends Component {
  constructor() {
    super()

    this.tick = this.tick.bind(this)
    this.setTooltip = this.setTooltip.bind(this)
    this.clearTooltip = this.clearTooltip.bind(this)

    this.zoomIn = this.zoomIn.bind(this)
    this.zoomOut = this.zoomOut.bind(this)

    this.state = {
      groups: [],
      width: WIDTH
    }
  }

  clusterAlerts(alerts, threshold) {
    let lastOpener = null
    let categorizedAlerts = alerts.map((a, i) => {
      let type = 'isolated'
      let opener = i

      let closeToPrevious = false
      let closeToNext = false

      if (i !== 0) {
        closeToPrevious = (Math.abs(alerts[i].slaTime - alerts[i - 1].slaTime) < threshold)
      }

      if (i < alerts.length - 1) {
        closeToNext = (Math.abs(alerts[i].slaTime - alerts[i + 1].slaTime) < threshold) 
      }

      if (closeToPrevious && closeToNext) {
        type = 'contained'
        opener = lastOpener
      } else {
        if (closeToNext) {
          type = 'opener'
          opener = i
          lastOpener = i
        }

        if (closeToPrevious) {
          type = 'closer'
          opener = lastOpener
          lastOpener = null
        }
      }

      return {
        key: i,
        type: type,
        opener: opener
      }
    })

    let isolatedAlerts = categorizedAlerts
      .filter((ca) => {
        return ca.type === 'isolated'
      })
      .map((ca) => {
        return alerts[ca.key]
      })

    let clusteredAlerts = categorizedAlerts
      .filter((ca) => {
        return ca.type === 'closer'
      })
      .map((ca) => {
        let values = alerts.slice(ca.opener, ca.key + 1)
        let priority = d3.min(values, (a) => { return a.priority })

        return {
          values: values,
          priority: priority,
          slaTime: (alerts[ca.opener].slaTime + alerts[ca.key].slaTime) / 2
        }
      })

    return {
      individuals: isolatedAlerts,
      clusters: clusteredAlerts
    }
  }

  setTooltip(x, y, component) {
    this.setState({
      tooltipX: x,
      tooltipY: y,
      tooltipContent: component
    })
  }

  clearTooltip() {
    this.setState({
      tooltipX: null,
      tooltipY: null,
      tooltipContent: null
    })
  }

  zoomIn() {
    let middle = (this.props.startTime + this.props.endTime) / 2
    let length = (this.props.endTime - this.props.startTime) * 0.97

    let newStart = middle - length / 2
    let newEnd = middle + length / 2

    this.props.setSLATimeWindow(newStart, newEnd)
  }

  zoomOut() {
    let middle = (this.props.startTime + this.props.endTime) / 2
    let length = (this.props.endTime - this.props.startTime) * 1.02

    let newStart = middle - length / 2
    let newEnd = middle + length / 2

    this.props.setSLATimeWindow(newStart, newEnd)
  }

  setSVGWidth() {
    let box = this.refs.alertsSVGContainer.getBoundingClientRect()

    this.setState({
      width: Math.floor(box.width)
    })
  }

  componentWillMount() {
    this.props.fetchAlerts()
    this.tick()
  }

  componentDidMount() {
    this.setSVGWidth()

    window.onresize = () => {
      this.setSVGWidth()
    }
  }

  componentWillReceiveProps(nextProps) {
    // 1) Filter by visible priorities group and is open

    // 2) Compute Time to/since SLA

    // 3) Filter by visibility in zoom area bounds
    // -  i.e. filter where x(d.SLAdiff) > 0 and < width

    // 4) Group by status

    // 5) Apply agglomerative clustering to each status group
    //    based on x position, with threshold based on zoom

    // 6) Enter/Update "groups"
    // -  larger dots for real groups
    // -  smaller dots for "groups of one"

    // 7) Update other stuff

    let priorityHash = {}
    nextProps.priorityFilter.forEach((p) => {
      priorityHash[p.key] = p.isVisible
    })

    let groups = nextProps.statusFilter
      .filter((f) => {
        return f.isVisible
      })
      .map((filter) => {
        let alerts = nextProps.alerts.filter((alert) => {
            return alert.status === filter.key && priorityHash[alert.priority]
          })
          .map((alert) => {
            alert.slaTime = computeSLATime(alert, this.props.now)

            return alert
          })
          .filter((alert) => {
            return alert.slaTime > nextProps.startTime && alert.slaTime < nextProps.endTime
          })
          .sort((a, b) => {
            return a.slaTime - b.slaTime
          })

        let threshold = (nextProps.endTime - nextProps.startTime) / 80

        let clusteredAlerts = this.clusterAlerts(alerts, threshold)

        return {
          label: filter.label,
          key: filter.key,
          alerts: alerts,
          individuals: clusteredAlerts.individuals,
          clusters: clusteredAlerts.clusters
        }
      })

    let priorityCounts = _.countBy(nextProps.alerts, (a) => {
      return a.priority
    })

    this.setState({
      priorityCounts: priorityCounts,
      groups: groups
    })
  }

  tick() {
    this.props.setNow()
    if (this.state.zoomingIn) { this.zoomIn() }
    if (this.state.zoomingOut) { this.zoomOut() }

    if (new Date() - this.props.timestamp > REFRESH_RATE) {
      this.props.fetchAlerts()
    }

    window.requestAnimationFrame(this.tick)
  }

  render() {
    let timeScale = d3.scale.linear()
      .domain([this.props.startTime, this.props.endTime])
      .range([0, this.state.width])

    // Timelines
    let timelines = this.state.groups.map((g, i) => {
      let y = ((i + 1) * 30)

      return (
        <g transform={'translate(0,' + y + ')'} key={g.label}>
          <TimelineByStatus 
            setTooltip={this.setTooltip}
            clearTooltip={this.clearTooltip}
            group={g} 
            timeScale={timeScale} 
            y={y} 
            width={this.state.width} />
        </g>
      )
    })

    let slaColor = '#de2d26'
    let slaBoundaryX = timeScale(0)
    let slaBoundary = (
      <g transform={'translate(' + slaBoundaryX + ',0)'}>
        <line y1="0" y2={HEIGHT - 20} stroke={slaColor} strokeWidth="1" />
        <text textAnchor="middle" className="slaLabel" y={HEIGHT - 4} fill={slaColor}>SLA</text>
      </g>
    )

    // Axis
    let ticks = timeScale.ticks(Math.round(this.state.width / 80)).map((t) => {
      let transform = 'translate(' + timeScale(t) + ',0)'
      let label = function(seconds) {
        let string = ''
        if (seconds === 0) { return string }

        let postiveSeconds = Math.abs(seconds)
        let sign = seconds / postiveSeconds

        let hours = Math.floor(postiveSeconds / 60 / 60)
        string += hours + ':'

        let minutes = (Math.floor(postiveSeconds / 60) % 60)
        string += minutes

        string = (sign < 0) ? ('-' + string) : string

        return string
      }(t)

      return (
        <g transform={transform} key={t}>
          <line y1="-16" y2="-12" stroke="#333" />
          <text textAnchor="middle">{label}</text>
        </g>
      )
    })

    // Interactions
    let onZoomIn = {
      mouseDown: (e) => {
        this.setState({
          zoomingIn: true
        })
      },
      mouseUp: (e) => {
        this.setState({
          zoomingIn: false
        })
      }
    }

    let onZoomOut = {
      mouseDown: (e) => {
        this.setState({
          zoomingOut: true
        })
      },
      mouseUp: (e) => {
        this.setState({
          zoomingOut: false
        })
      }
    }

    let zoomMouseOut = (e) => {
      this.setState({
        zoomingIn: false,
        zoomingOut: false
      })
    }

    let onResetClick = (e) => {
      this.props.setSLATimeWindow(SLA_TIMEWINDOW.startTime, SLA_TIMEWINDOW.endTime)
    }

    let onWheel = (e) => {
      e.preventDefault()

      // Mouse Position
      let box = this.refs.alertsSVG.getBoundingClientRect()
      let mouseX = e.clientX - box.left

      // Current Time
      let timeFrame = this.props.endTime - this.props.startTime
      let timeUnderMouse = timeScale.invert(mouseX)

      // New Time
      let newTimeFrame = Math.pow(timeFrame, 1 + e.deltaY / 1000)
      let newBeforeMouse = (mouseX / this.state.width) * newTimeFrame
      let newAfterMouse = (1 - mouseX / this.state.width) * newTimeFrame

      let newStart = timeUnderMouse - newBeforeMouse
      let newEnd = timeUnderMouse + newAfterMouse

      this.props.setSLATimeWindow(newStart, newEnd)
    }

    let onMouseDown = (e) => {
      let box = this.refs.alertsSVG.getBoundingClientRect()
      let mouseX = e.clientX - box.left

      this.setState({
        dragStartX: mouseX,
        dragStartTime: this.props.startTime,
        dragStartDuration: this.props.endTime - this.props.startTime
      })
    }

    let onMouseMove = (e) => {
      if (typeof this.state.dragStartX === 'number') {
        let box = this.refs.alertsSVG.getBoundingClientRect()
        let mouseX = e.clientX - box.left
        let mouseXDelta = mouseX - this.state.dragStartX

        let secondsPerPixel = this.state.dragStartDuration / this.state.width
        let secondsDelta = secondsPerPixel * -mouseXDelta

        let newStart = this.state.dragStartTime + secondsDelta
        let newEnd = newStart + this.state.dragStartDuration

        this.props.setSLATimeWindow(newStart, newEnd)
      }
    }

    let onMouseUp = (e) => {
      this.setState({
        dragStartX: null,
        dragStartTime: null,
        dragStartDuration: null
      })
    }

    // Tooltip
    let tooltip = null
    if (this.state.tooltipContent) {
      tooltip = (
        <AlertTooltipWrapper x={this.state.tooltipX} y={this.state.tooltipY}>
          {this.state.tooltipContent}
        </AlertTooltipWrapper>
      )
    }

    return (
      <div id="alerts-timeline">
        <div id="alerts-controls">
          <AlertFilters counts={this.state.priorityCounts} />
          <AlertZoomControls onZoomIn={onZoomIn} onZoomOut={onZoomOut} onResetClick={onResetClick} />
        </div>
        <div id="alerts-tooltip-anchor" ref="alertsSVGContainer">
          <svg
            className="alerts-svg"
            ref="alertsSVG" 
            width={this.state.width} 
            height={HEIGHT} 
            onWheel={onWheel}
            onMouseDown={onMouseDown}
            onMouseMove={onMouseMove}
            onMouseUp={onMouseUp}>
            {timelines}
            {slaBoundary}
            <g className="timeTicks" transform={'translate(0,' + (HEIGHT - 4) + ')'}>
              <line x1="0" x2={this.state.width} y1="-16" y2="-16" stroke="#333" />
              {ticks}
            </g>
          </svg>
          {tooltip}
        </div>
      </div>
    )
  }
}

AlertsTimeline.PropTypes = {
  startTime: PropTypes.number.isRequired,
  endTime: PropTypes.number.isRequired,
  alerts: PropTypes.array.isRequired,
  statusFilter: PropTypes.array.isRequired,
  priorityFilter: PropTypes.array.isRequired
}

const mapStateToProps = (state) => {
  return {
    fetchAlerts: fetchAlerts,
    setSLATimeWindow: setSLATimeWindow,
    setNow: setNow,

    now: state.now,
    alerts: state.alerts.alerts,
    timestamp: state.alerts.timestamp,
    statusFilter: state.statusFilter,
    priorityFilter: state.priorityFilter,
    startTime: state.timeWindow.start,
    endTime: state.timeWindow.end
  }
}

export default connect(
  mapStateToProps,
  { fetchAlerts, setSLATimeWindow, setNow }
)(AlertsTimeline)
