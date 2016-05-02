export const SET_ALERTS = 'SET_ALERTS'
export const SET_PRIORITY_FILTER = 'SET_PRIORITY_FILTER'

export function setAlerts(alerts) {
  return {
    type: SET_ALERTS,
    alerts
  }
}

export function setPriorityFilter(key, isVisible) {
  return {
    type: SET_PRIORITY_FILTER,
    key,
    isVisible
  }
}