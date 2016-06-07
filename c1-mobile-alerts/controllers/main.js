import React from 'react'
import { render } from 'react-dom'
import { createStore, combineReducers, applyMiddleware } from 'redux'
import thunkMiddleware from 'redux-thunk';
import { Provider } from 'react-redux'

import AlertsTimeline from './containers/AlertsTimeline'
import { alertsReducer, statusFilterReducer, priorityFilterReducer, timeWindowReducer, timeNowReducer } from './reducers/alertsReducer'
import { setAlerts, setStatusFilter } from './actions/alertsActions'

const store = createStore(
  combineReducers({
    alerts: alertsReducer,
    statusFilter: statusFilterReducer,
    priorityFilter: priorityFilterReducer,
    timeWindow: timeWindowReducer,
    now: timeNowReducer
  }),
  applyMiddleware(thunkMiddleware)
)

render(
  <Provider store={store}>
    <AlertsTimeline />
  </Provider>,
  document.getElementById('app')
)