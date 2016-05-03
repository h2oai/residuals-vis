import { SET_ALERTS, SET_PRIORITY_FILTER, SET_SLA_TIME_WINDOW, SET_NOW } from '../actions/alertsActions'
import { combineReducers } from 'redux'

let initialFilters = [
  {
    label: 'CRITICAL',
    key: 0,
    isVisible: true
  },
  {
    label: 'HIGH',
    key: 1,
    isVisible: true
  },
  {
    label: 'MEDIUM',
    key: 2,
    isVisible: true
  },
  {
    label: 'LOW',
    key: 3,
    isVisible: true
  },
  {
    label: 'INFO',
    key: 4,
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

let initialTimeWindow = {
  start: - 60 * 60 * 10,
  end: 60 * 60 * 10
}

export function timeWindowReducer(state = initialTimeWindow, action) {
  switch (action.type) {
    case SET_SLA_TIME_WINDOW:
      return {
        start: action.start,
        end: action.end
      }

    default:
      return state
  }
}

let initialNow = new Date()

export function timeNowReducer(state = initialNow, action) {
  switch (action.type) {
    case SET_NOW:
      return action.now

    default:
      return state
  }
}