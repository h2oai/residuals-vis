import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import d3 from 'd3'
import moment from 'moment'

import { computeSLATime } from '../SLAutils.js'
import { fetchAlerts, setSLATimeWindow, setNow } from '../actions/alertsActions'
import { TimelineByPriority } from '../components/TimelineByPriority'

const WIDTH = 900
const HEIGHT = 200

const priorityColor = d3.scale.ordinal()
  .domain(["0", "1", "2", "3", "4"])
  .range(["#F44336", "#FFAB40", "#FFD740", "#FFF59D", "#ddd"])

class AlertsTimeline extends Component {
  constructor() {
    super()

    this.tick = this.tick.bind(this)

    this.state = {
      groups: []
    }
  }

  componentWillMount() {
    this.props.fetchAlerts()
    this.tick()
  }

  componentWillReceiveProps(nextProps) {
    let groups = nextProps.priorityFilter
      .filter((f) => {
        return f.isVisible
      })
      .map((filter) => {
        let alerts = nextProps.alerts.filter((alert) => {
            return alert.priority === filter.key
          })
          .map((alert) => {
            alert.slaTime = computeSLATime(alert, this.props.now)

            return alert
          })
          .filter((alert) => {
            return alert.slaTime > nextProps.startTime && alert.slaTime < nextProps.endTime
          })

        return {
          label: filter.label,
          key: filter.key,
          alerts: alerts
        }
      })

    this.setState({
      groups: groups
    })
  }

  tick() {
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

    this.props.setNow()

    window.requestAnimationFrame(this.tick)
  }

  render() {
    let timeScale = d3.scale.linear()
      .domain([this.props.startTime, this.props.endTime])
      .range([0, WIDTH])

    // Timelines
    let timelines = this.state.groups.map((g, i) => {
      let y = ((i + 1) * 30)

      return (
        <g transform={'translate(0,' + y + ')'} key={g.label}>
          <TimelineByPriority group={g} timeScale={timeScale} y={y} width={WIDTH} priorityColor={priorityColor} />
        </g>
      )
    })

    let slaColor = '#de2d26'
    let slaBoundaryX = timeScale(0)
    let slaBoundary = (
      <g transform={'translate(' + slaBoundaryX + ',0)'}>
        <line y1="0" y2={HEIGHT} stroke={slaColor} strokeWidth="1" />
        <text x="10" y={HEIGHT - 30} fill={slaColor}>SLA</text>
      </g>
    )

    // Axis
    let ticks = timeScale.ticks(Math.round(WIDTH / 80)).map((t) => {
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
          <text textAnchor="middle">{label}</text>
        </g>
      )
    })

    // Interactions
    let onWheel = (e) => {
      e.preventDefault()

      //console.log(e.deltaY, e.clientX)

      // Mouse Position
      let box = this.refs.alertsSVG.getBoundingClientRect()
      let mouseX = e.clientX - box.left

      // Current Time
      let timeFrame = this.props.endTime - this.props.startTime
      let timeUnderMouse = timeScale.invert(mouseX)

      // New Time
      let newTimeFrame = Math.pow(timeFrame, 1 + e.deltaY / 1000)
      let newBeforeMouse = (mouseX / WIDTH) * newTimeFrame
      let newAfterMouse = (1 - mouseX / WIDTH) * newTimeFrame

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

        let secondsPerPixel = this.state.dragStartDuration / WIDTH
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

    return (
      <div>
        <svg
          className="alerts-svg"
          ref="alertsSVG" 
          width={WIDTH} 
          height={HEIGHT} 
          onWheel={onWheel}
          onMouseDown={onMouseDown}
          onMouseMove={onMouseMove}
          onMouseUp={onMouseUp}>
          {slaBoundary}
          {timelines}
          <g className="timeTicks" transform={'translate(0,' + (HEIGHT - 4) + ')'}>
            {ticks}
          </g>
        </svg>
      </div>
    )
  }
}

AlertsTimeline.PropTypes = {
  startTime: PropTypes.number.isRequired,
  endTime: PropTypes.number.isRequired,
  alerts: PropTypes.array.isRequired,
  priorityFilter: PropTypes.array.isRequired
}

const mapStateToProps = (state) => {
  return {
    fetchAlerts: fetchAlerts,
    setSLATimeWindow: setSLATimeWindow,
    setNow: setNow,

    now: state.now,
    alerts: state.alerts,
    priorityFilter: state.priorityFilter,
    startTime: state.timeWindow.start,
    endTime: state.timeWindow.end
  }
}

export default connect(
  mapStateToProps,
  { fetchAlerts, setSLATimeWindow, setNow }
)(AlertsTimeline)
