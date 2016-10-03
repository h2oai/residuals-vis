import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import { setPriorityFilter } from '../actions/alertsActions'

class AlertFilters extends Component {
  render() {
    let priorities = this.props.priorityFilter.map((f) => {
      let onChange = () => {
        this.props.setPriorityFilter(f.key, !f.isVisible)
      }

      let count = ''
      if (this.props.counts) {
        count = this.props.counts[f.key]
      }

      return (
        <li key={f.label}>
          <input type="checkbox" id={'alert-filter-' + f.key} checked={f.isVisible} onChange={onChange} />
          <label htmlFor={'alert-filter-' + f.key}>
            {f.label}
            <span className="alerts-color-patch" style={{ backgroundColor: f.color }}>{count}</span>
          </label>
        </li>
      )
    })

    return (
      <div className="alerts-filter">
        <ul>
          {priorities}
        </ul>
      </div>
    )
  }
}

AlertFilters.PropTypes = {
  setPriorityFilter: PropTypes.func.isRequired,
  priorityFilter: PropTypes.array.isRequired,
  counts: PropTypes.array
}

const mapStateToProps = (state) => {
  return {
    setPriorityFilter: setPriorityFilter,
    priorityFilter: state.priorityFilter,
  }
}

export default connect(
  mapStateToProps,
  { setPriorityFilter }
)(AlertFilters)
