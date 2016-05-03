import { SET_ALERTS, SET_STATUS_FILTER, SET_PRIORITY_FILTER, SET_SLA_TIME_WINDOW, SET_NOW } from '../actions/alertsActions'
import { combineReducers } from 'redux'

let initialFilters = [
  {
    label: 'Unassigned',
    key: 1,
    isVisible: true
  },
  {
    label: 'Assigned',
    key: 2,
    isVisible: true
  },
  {
    label: 'In-progress',
    key: 3,
    isVisible: true
  },
  {
    label: 'Closed',
    key: 4,
    isVisible: false
  },
]

export function statusFilterReducer(state = initialFilters, action) {
  switch (action.type) {
    case SET_STATUS_FILTER:
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

let initialPriorities = [
  {
    color: '#F44336',
    label: 'Critical',
    key: 1,
    isVisible: true
  },
  {
    color: '#FFAB40',
    label: 'High',
    key: 2,
    isVisible: true
  },
  {
    color: '#FFD740',
    label: 'Medium',
    key: 3,
    isVisible: true
  },
  {
    color: '#FFF59D',
    label: 'Low',
    key: 4,
    isVisible: true
  },
  {
    color: '#DDDDDD',
    label: 'Info',
    key: 5,
    isVisible: true
  }
]

export function priorityFilterReducer(state = initialPriorities, action) {
  switch (action.type) {
    case SET_PRIORITY_FILTER:
      return state.map((f) => {
        if (f.key === action.key) {
          return Object.assign({}, f, {
            isVisible: action.isVisible
          })
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