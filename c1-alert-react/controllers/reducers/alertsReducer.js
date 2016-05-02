import { SET_ALERTS, SET_PRIORITY_FILTER } from '../actions/alertsActions'
import { combineReducers } from 'redux'

let initialFilters = [
  {
    key: 'CRITICAL',
    isVisible: true
  },
  {
    key: 'HIGH',
    isVisible: true
  },
  {
    key: 'MEDIUM',
    isVisible: true
  },
  {
    key: 'LOW',
    isVisible: true
  },
  {
    key: 'INFO',
    isVisible: true
  },
]

export function priorityFilterReducer(state = initialFilters, action) {
  switch (action.type) {
    case SET_PRIORITY_FILTER:
      return state.map((f) => {
        if (f.key === action.key) {
          return {
            key: f.key,
            visible: action.isVisible
          }
        } else {
          return f
        }
      })

    default:
      return state
  }
}

let initialAlerts = []

export function alertsReducer(state = initialAlerts, action) {
  switch (action.type) {
    case SET_ALERTS:
      return action.alerts

    default:
      return state
  }
}