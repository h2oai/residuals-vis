import React, { Component, PropTypes } from 'react'

class AlertTooltipWrapper extends Component {
  render() {
    let style = {
      top: this.props.y + 'px',
      left: this.props.x + 'px'
    }

    return (
      <div style={style} className="alerts-tooltip-wrapper">
        <div className="alerts-tooltip">
          {this.props.children}
        </div>
      </div>
    )
  }
}

AlertTooltipWrapper.PropTypes = {
  x: PropTypes.number.isRequired,
  y: PropTypes.number.isRequired
}

export default AlertTooltipWrapper