import React, { Component, PropTypes } from 'react'
import { PRIORITIES_MAP } from '../constants'

class MultipleAlertsTooltipDetail extends Component {
  render() {
    let alerts = this.props.cluster.values
    .sort((a, b) => {
      return parseInt(a.priority) - parseInt(b.priority)
    })
    .map((a) => {
      return (
        <tr key={a.alertId}>
          <th>{PRIORITIES_MAP[a.priority].label}</th>
          <td>{a.alertName}</td>
        </tr>
      )
    })

    return (
      <table className="alert-detail-table">
        <tbody>
          <tr><td className="alert-detail-title" colSpan="2">{this.props.cluster.values.length} alerts</td></tr>
          {alerts}
        </tbody>
      </table>
    )
  }
}

export default MultipleAlertsTooltipDetail