import React, { Component, PropTypes } from 'react'

class SingleAlertTooltipDetail extends Component {
  render() {
    return (
      <table className="alert-detail-table">
        <tbody>
          <tr><td className="alert-detail-title" colSpan="2">{this.props.alert.alertName}</td></tr>
          <tr>
            <th>Last Updated</th>
            <td>{this.props.alert.updatedTimestamp}</td>
          </tr>
          <tr>
            <th>Owner</th>
            <td>{this.props.alert.owner}</td>
          </tr>
          <tr>
            <th>Comments</th>
            <td>{this.props.alert.comments}</td>
          </tr>
        </tbody>
      </table>
    )
  }
}

export default SingleAlertTooltipDetail