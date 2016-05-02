import React from 'react'
import { render } from 'react-dom'
import { createStore, combineReducers, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'

import AlertsTimeline from './containers/AlertsTimeline'
import { alertsReducer, priorityFilterReducer } from './reducers/alertsReducer'
import { setAlerts, setPriorityFilter } from './actions/alertsActions'

const store = createStore(
  combineReducers({
    alerts: alertsReducer,
    priorityFilter: priorityFilterReducer
  })
)

// Log the initial state
console.log(store.getState())

// Every time the state changes, log it
// Note that subscribe() returns a function for unregistering the listener
let unsubscribe = store.subscribe(() =>
  console.log(store.getState())
)

// Dispatch some actions
store.dispatch(setPriorityFilter('CRITICAL', false))
store.dispatch(setAlerts([1, 2, 3]))

// Stop listening to state updates
unsubscribe()


render(
  <Provider store={store}>
    <AlertsTimeline />
  </Provider>,
  document.getElementById('app')
)