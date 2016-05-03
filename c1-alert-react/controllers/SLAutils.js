import moment from 'moment'

const sla_times = {
  "0": {
    "1": moment.duration(10, 'm').asSeconds(),
    "2": moment.duration(30, 'm').asSeconds(),
    "3": moment.duration(8, 'h').asSeconds(),
    "4": moment.duration(8, 'h').asSeconds()
  },
  "1":{
    "1": moment.duration(20, 'm').asSeconds(),
    "2": moment.duration(45, 'm').asSeconds(),
    "3": moment.duration(12, 'h').asSeconds(),
    "4": moment.duration(12, 'h').asSeconds()
  },
  "2":{
    "1": moment.duration(1, 'h').asSeconds(),
    "2": moment.duration(1, 'h').asSeconds(),
    "3": moment.duration(24, 'h').asSeconds(),
    "4": moment.duration(24, 'h').asSeconds()
  },
  "3":{
    "1": moment.duration(2, 'h').asSeconds(),
    "2": moment.duration(2, 'h').asSeconds(),
    "3": moment.duration(36, 'h').asSeconds(),
    "4": moment.duration(36, 'h').asSeconds()
  },
  "4":{
    "1": moment.duration(4, 'h').asSeconds(),
    "2": moment.duration(4, 'h').asSeconds(),
    "3": moment.duration(48, 'h').asSeconds(),
    "4": moment.duration(48, 'h').asSeconds()
  }
}

export const computeSLATime = function(alert, now) {
  let lastUpdate = moment(alert.updatedTimestamp)
  let secondsSinceLastUpdate = moment(now).diff(lastUpdate, 's')
  let sla = sla_times[alert.priority][alert.status]

  return sla - secondsSinceLastUpdate
}