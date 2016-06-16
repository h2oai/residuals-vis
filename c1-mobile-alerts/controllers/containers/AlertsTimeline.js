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
const HEIGHT = 700

const REFRESH_RATE = 15 * 1000 // Every 15 seconds

const pad = d3.format("02d")

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
      width: WIDTH,
      height: HEIGHT
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
    let length = (this.props.endTime - this.props.startTime) * 0.95

    let newStart = middle - length / 2
    let newEnd = middle + length / 2

    this.props.setSLATimeWindow(newStart, newEnd)
  }

  zoomOut() {
    let middle = (this.props.startTime + this.props.endTime) / 2
    let length = (this.props.endTime - this.props.startTime) * 1.05

    let newStart = middle - length / 2
    let newEnd = middle + length / 2

    this.props.setSLATimeWindow(newStart, newEnd)
  }

  setSVGWidth() {
    let box = this.refs.alertsSVGContainer.getBoundingClientRect()



    this.setState({
      width: Math.floor(box.width),
      height: Math.floor(box.height)
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

        let threshold = (nextProps.endTime - nextProps.startTime) / 20

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

    if (this.state.zoomingIn || this.state.zoomingOut) {
      window.requestAnimationFrame(this.tick)
    }
  }

  render() {
    let timeScale = d3.scale.linear()
      .domain([this.props.startTime, this.props.endTime])
      .range([this.state.height, 0])

    // Timelines
    let timelines = this.state.groups.map((g, i) => {
      let spacing = (this.state.width / (this.state.groups.length + 3))

      let x = ((i + 2) * spacing)

      return (
        <g transform={'translate(' + x + ', 0)'} key={g.label}>
          <TimelineByStatus 
            setTooltip={this.setTooltip}
            clearTooltip={this.clearTooltip}
            group={g} 
            timeScale={timeScale} 
            x={x} 
            height={this.state.height} />
        </g>
      )
    })

    let slaColor = '#de2d26'
    let slaBoundaryY = timeScale(0)
    let slaBoundary = (
      <g transform={'translate(0, ' + slaBoundaryY + ')'}>
        <line x1="0" x2={this.state.width} stroke={slaColor} strokeWidth="1" />
        <text textAnchor="middle" className="slaLabel" x={WIDTH - 4} fill={slaColor}>SLA</text>
      </g>
    )

    // Axis
    let ticks = timeScale.ticks(Math.round(this.state.height / 80)).map((t) => {
      let transform = 'translate(0, ' + timeScale(t) + ')'
      let label = function(seconds) {
        let string = ''
        if (seconds === 0) { return string }

        let postiveSeconds = Math.abs(seconds)
        let sign = seconds / postiveSeconds

        let hours = Math.floor(postiveSeconds / 60 / 60)
        string += hours + ':'

        let minutes = pad(Math.floor(postiveSeconds / 60) % 60)
        string += minutes

        string = (sign < 0) ? ('-' + string) : string

        return string
      }(t)

      let color = (t < 0) ? slaColor : '#999'

      return (
        <g transform={transform} key={t}>
          <line x1="0" x2="40" stroke={color} />
          <text x="4" y="-2" textAnchor="start" fill={color}>{label}</text>
        </g>
      )
    })

    // Interactions
    let onZoomIn = {
      mouseDown: (e) => {
        e.preventDefault()

        this.setState({
          zoomingIn: true
        }, () => {
          window.requestAnimationFrame(this.tick)
        })
      },
      mouseUp: (e) => {
        e.preventDefault()

        this.setState({
          zoomingIn: false
        })
      }
    }

    let onZoomOut = {
      mouseDown: (e) => {
        e.preventDefault()

        this.setState({
          zoomingOut: true
        }, () => {
          window.requestAnimationFrame(this.tick)
        })
      },
      mouseUp: (e) => {
        e.preventDefault()

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

    let onMouseDown = (e) => {
      let box = this.refs.alertsSVG.getBoundingClientRect()
      let mouseY = e.clientY - box.top

      this.setState({
        dragStartY: mouseY,
        dragStartTime: this.props.startTime,
        dragStartDuration: this.props.endTime - this.props.startTime
      })
    }

    let onMouseMove = (e) => {
      if (typeof this.state.dragStartY === 'number') {
        let box = this.refs.alertsSVG.getBoundingClientRect()
        let mouseY = e.clientY - box.top
        let mouseYDelta = mouseY - this.state.dragStartY

        let secondsPerPixel = this.state.dragStartDuration / this.state.height
        let secondsDelta = secondsPerPixel * mouseYDelta

        let newStart = this.state.dragStartTime + secondsDelta
        let newEnd = newStart + this.state.dragStartDuration

        this.props.setSLATimeWindow(newStart, newEnd)
      }
    }

    let onMouseUp = (e) => {
      this.setState({
        dragStartY: null,
        dragStartTime: null,
        dragStartDuration: null
      })
    }

    // Let Touch Interactions
    let onTouchStart = (e) => {
      let touchCoordinates = d3.range(e.touches.length).map((key) => {
        let t = e.touches[key]

        return {
          x: t.clientX,
          y: t.clientY,
          time: timeScale.invert(t.clientY)
        }
      })

      this.setState({
        dragStartTime: this.props.startTime,
        dragStartDuration: this.props.endTime - this.props.startTime,
        touchStartCoordinates: touchCoordinates,
        touchMoveCoordinates: touchCoordinates
      })
    }

    let onTouchMove = (e) => {
      e.preventDefault()

      let touchCoordinates = d3.range(e.touches.length).map((key) => {
        let t = e.touches[key]

        return {
          x: t.clientX,
          y: t.clientY,
          time: timeScale.invert(t.clientY)
        }
      })

      // If number of touches change
      if (touchCoordinates.length !== this.state.touchStartCoordinates.length) {
        this.setState({
          dragStartTime: this.props.startTime,
          dragStartDuration: this.props.endTime - this.props.startTime,
          touchStartCoordinates: touchCoordinates,
          touchMoveCoordinates: touchCoordinates
        })

        return 
      }

      // Touch Drag
      if (e.touches.length <= 1) {
        let secondsPerPixel = this.state.dragStartDuration / this.state.height

        let newStart = this.state.touchStartCoordinates[0].time - (this.state.height - touchCoordinates[0].y) * secondsPerPixel 
        let newEnd = newStart + this.state.dragStartDuration

        this.props.setSLATimeWindow(newStart, newEnd)  

      } else {
        let firstStartTouch = this.state.touchStartCoordinates[0]
        let secondStartTouch = this.state.touchStartCoordinates[1] || touchCoordinates[1]

        let secondsPerPixel = Math.abs(firstStartTouch.time - secondStartTouch.time) / Math.abs(touchCoordinates[0].y - touchCoordinates[1].y)
        let newTimeFrame = secondsPerPixel * this.state.height

        let newStart = Math.max(firstStartTouch.time, secondStartTouch.time) - (this.state.height - Math.min(touchCoordinates[0].y, touchCoordinates[1].y)) * secondsPerPixel
        let newEnd = newStart + newTimeFrame

        this.props.setSLATimeWindow(newStart, newEnd)

        if (!this.state.touchStartCoordinates[1]) {
          this.setState({
            touchStartCoordinates: [firstStartTouch, secondStartTouch]
          })
        }
      }
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
        <div id="alerts-tooltip-anchor" ref="alertsSVGContainer">
          <svg
            className="alerts-svg"
            ref="alertsSVG" 
            width={this.state.width} 
            height={this.state.height} 
            onMouseDown={onMouseDown}
            onMouseMove={onMouseMove}
            onMouseUp={onMouseUp}
            onTouchStart={onTouchStart}
            onTouchMove={onTouchMove}>

            <g className="timeTicks">
              <line x1="0" x2={this.state.width} stroke="#333" />
              {ticks}
            </g>

            {slaBoundary}
            {timelines}
            
          </svg>
          <div id="alerts-controls">
            <AlertFilters counts={this.state.priorityCounts} />
            <AlertZoomControls onZoomIn={onZoomIn} onZoomOut={onZoomOut} onResetClick={onResetClick} />
          </div>
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
