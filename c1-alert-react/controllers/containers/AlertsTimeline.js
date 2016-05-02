import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import { setAlerts } from '../actions/alertsActions'

class AlertsTimeline extends Component {
  render() {
    return (
      <div>Hello world again</div>
    )
  }
}

AlertsTimeline.PropTypes = {
  alerts: PropTypes.array.isRequired,
  priorityFilter: PropTypes.array.isRequired
}

const mapStateToProps = (state) => {
  return {
    setAlerts: setAlerts,
    alerts: state.alerts,
    priorityFilter: state.priorityFilter
  }
}

export default connect(
  mapStateToProps,
  { setAlerts }
)(AlertsTimeline)
