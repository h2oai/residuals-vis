export const FETCH_ALERTS = 'FETCH_ALERTS'
export const REQUEST_ALERTS = 'REQUEST_ALERTS'
export const SET_ALERTS = 'SET_ALERTS'
export const SET_STATUS_FILTER = 'SET_STATUS_FILTER'
export const SET_SLA_TIME_WINDOW = 'SET_SLA_TIME_WINDOW'
export const SET_NOW = 'SET_NOW'

export function fetchAlerts() {
  return function (dispatch) {
    // Async stuff should go here?
    dispatch(setAlerts([
      {"alertId":"70nv1nljg0b7q6foskhnc20v3r","alertName":"Email Alert from 228.26.225.146","owner":"aks993","priority":2,"status":4,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-01 17:18:59.429"},
      {"alertId":"mc695ifq5gg7ea8jegi7jrvc0c","alertName":"Bluecoat Alert from 42.159.157.99","owner":"ldj892","priority":3,"status":4,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-04-30 16:39:09.496"},
      {"alertId":"dodf8n3632l9lhusme8gorn931","alertName":"Fireye Alert from 117.34.103.95","owner":"aks993","priority":4,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-04-30 20:20:08.869"},
      {"alertId":"34hbbk31641mbo4d6so11a5vtf","alertName":"Bluecoat Alert from 34.138.242.92","owner":"sld088","priority":2,"status":1,"comments":"This Alert has comments","updatedUserId":"aks993","updatedTimestamp":"2016-04-30 17:44:58.405"},
      {"alertId":"md69hdst5vgklpd6om422cjeg8","alertName":"Email Alert from 203.140.128.217","owner":"zhh228","priority":4,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 04:54:15.376"},
      {"alertId":"8ju1ujl8t5vkpe1p5ctjcure7d","alertName":"Fireye Alert from 96.181.240.83","owner":"aks993","priority":5,"status":4,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-04-30 20:27:19.279"},
      {"alertId":"st8hjbhm8cr1i18f9bn3gqt066","alertName":"Email Alert from 67.212.210.55","owner":"ldj892","priority":2,"status":3,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-04-30 19:45:28.08"},
      {"alertId":"8hdut1caeikhp4ghg4ko00l47p","alertName":"Bluecoat Alert from 71.170.128.145","owner":"ldj892","priority":1,"status":4,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-05-02 03:07:51.935"},
      {"alertId":"jf62cebu8ovgl69m8jei2ngfpi","alertName":"Email Alert from 45.203.61.57","owner":"zhh228","priority":1,"status":4,"comments":"This Alert has comments","updatedUserId":"aks993","updatedTimestamp":"2016-05-01 04:34:00.136"},
      {"alertId":"dg5j3f36kjcu34daptkpn3c9ts","alertName":"Ironport Alert from 110.180.35.64","owner":"abc123","priority":3,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 08:46:04.473"},
      {"alertId":"15fmmpal8hsh8j5ovepltrma10","alertName":"Bluecoat Alert from 78.100.16.20","owner":"sld088","priority":1,"status":2,"comments":"This Alert has comments","updatedUserId":"aks993","updatedTimestamp":"2016-05-02 01:45:15.498"},
      {"alertId":"acq4ecdem4vsgc2epe9cdn2srk","alertName":"Syslog Alert from 65.142.102.208","owner":"aks993","priority":1,"status":1,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 01:09:45.864"},
      {"alertId":"74di469f2ctaet3uofl8nhbsu","alertName":"Bluecoat Alert from 118.131.203.82","owner":"ldj892","priority":1,"status":3,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 04:11:59.177"},
      {"alertId":"kq72utbn0o3oogr3oot84lb20o","alertName":"Ironport Alert from 42.73.106.90","owner":"abc123","priority":3,"status":3,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 06:05:51.18"},
      {"alertId":"psartb2lnaaoef38bg9c7a7m94","alertName":"Fireye Alert from 99.150.11.204","owner":"zhh228","priority":3,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-04-30 13:26:32.196"},
      {"alertId":"a6ok7njckg0vbc97ub42cos3on","alertName":"Fireye Alert from 15.27.150.130","owner":"zhh228","priority":2,"status":4,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-04-30 20:19:35.471"},
      {"alertId":"h7epas39gmjdijvsk83nf0qvvr","alertName":"Bluecoat Alert from 241.76.114.245","owner":"ldj892","priority":2,"status":4,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-04-30 12:44:14.693"},
      {"alertId":"oksa9bhjanlqrtsh8pa2uf9c0n","alertName":"Email Alert from 219.184.179.66","owner":"sld088","priority":1,"status":4,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-05-01 00:34:31.686"},
      {"alertId":"4tqfn9ppomg0hama2qgnvb9nb4","alertName":"Bluecoat Alert from 62.0.185.115","owner":"abc123","priority":2,"status":1,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-04-30 21:18:16.112"},
      {"alertId":"vuf0i9a268oaq0m3941cdsi8j7","alertName":"Ironport Alert from 97.238.124.35","owner":"abc123","priority":1,"status":2,"comments":"This Alert has comments","updatedUserId":"aks993","updatedTimestamp":"2016-04-30 13:21:14.64"},
      {"alertId":"t3mn44p0tf3lmjkdiku7rcj614","alertName":"Fireye Alert from 221.193.14.128","owner":"sld088","priority":2,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-04-30 20:21:09.365"},
      {"alertId":"uvur3nnitil27unrq1l641miet","alertName":"Email Alert from 60.246.3.138","owner":"ldj892","priority":2,"status":2,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-05-01 09:15:26.388"},
      {"alertId":"1qth3acdmmln8mj14u6va6iko9","alertName":"Ironport Alert from 190.206.111.228","owner":"sld088","priority":1,"status":2,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-05-01 22:57:50.152"},
      {"alertId":"mpkg64b7h4008ncucmlcmku6bj","alertName":"Email Alert from 61.145.215.51","owner":"abc123","priority":2,"status":1,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-04-30 21:33:10.392"},
      {"alertId":"rjmhitsju932vf6ctlmpgfibg1","alertName":"Bluecoat Alert from 223.14.196.90","owner":"sld088","priority":1,"status":4,"comments":"This Alert has comments","updatedUserId":"aks993","updatedTimestamp":"2016-04-30 11:38:59.419"},
      {"alertId":"bjtbhbf45igqvtl5dt9uljrtad","alertName":"Email Alert from 16.73.141.60","owner":"zhh228","priority":1,"status":3,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-05-02 02:12:23.738"},
      {"alertId":"cr3ddtafub01up2hnuto867c9j","alertName":"Email Alert from 33.239.173.228","owner":"abc123","priority":1,"status":4,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 06:23:56.896"},
      {"alertId":"8gomf9305afbi8tvm64undgrrj","alertName":"Ironport Alert from 236.124.7.169","owner":"ldj892","priority":4,"status":3,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-05-02 02:07:40.131"},
      {"alertId":"1e8rfc843t1il60ak0ugk6rf4","alertName":"Syslog Alert from 249.9.39.76","owner":"abc123","priority":2,"status":3,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-05-01 11:15:43.846"},
      {"alertId":"hiate7n3qaamduppltnnbfrgqk","alertName":"Ironport Alert from 114.252.221.33","owner":"abc123","priority":5,"status":3,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-05-01 09:16:32.117"},
      {"alertId":"e069l638stha9sq9ea2i29mimu","alertName":"Syslog Alert from 62.181.112.94","owner":"abc123","priority":1,"status":1,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-05-02 04:01:30.029"},
      {"alertId":"ps199g5nb5l1meqok36fveolv5","alertName":"Ironport Alert from 234.114.247.222","owner":"sld088","priority":3,"status":3,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-05-02 04:23:23.502"},
      {"alertId":"hepfb8ipkd5mmk64vofcgn9n0n","alertName":"Bluecoat Alert from 159.167.58.232","owner":"zhh228","priority":5,"status":4,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-04-30 17:15:29.554"},
      {"alertId":"clrif00rcjr13jsg7ovs35dtmv","alertName":"Email Alert from 154.121.247.95","owner":"zhh228","priority":1,"status":3,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-04-30 21:28:24.91"},
      {"alertId":"vvl2pme4rr35prbkpk44d3ge71","alertName":"Syslog Alert from 178.144.93.56","owner":"aks993","priority":1,"status":1,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-05-01 23:01:29.235"},
      {"alertId":"s2tdpnsonp4i0cbec6dlk5i9jn","alertName":"Email Alert from 188.39.129.191","owner":"aks993","priority":5,"status":2,"comments":"This Alert has comments","updatedUserId":"sld088","updatedTimestamp":"2016-05-02 04:46:10.096"},
      {"alertId":"l37lruk6bhr3imhrt26b7f35br","alertName":"Email Alert from 208.179.138.75","owner":"abc123","priority":3,"status":4,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-04-30 22:45:38.249"},
      {"alertId":"tg6ltgjb6d9hrtg19fvktb2efi","alertName":"Fireye Alert from 164.64.227.58","owner":"ldj892","priority":1,"status":3,"comments":"This Alert has comments","updatedUserId":"abc123","updatedTimestamp":"2016-05-01 04:50:54.362"},
      {"alertId":"kt4j9vv18egdqi06lru69gjb3k","alertName":"Bluecoat Alert from 216.63.68.154","owner":"abc123","priority":4,"status":1,"comments":"This Alert has comments","updatedUserId":"zhh228","updatedTimestamp":"2016-04-30 12:21:22.326"},
      {"alertId":"4gb6ncla2e00c942rftmaot0ve","alertName":"Bluecoat Alert from 139.40.187.131","owner":"sld088","priority":3,"status":3,"comments":"This Alert has comments","updatedUserId":"ldj892","updatedTimestamp":"2016-05-02 00:32:06.02"}
    ]))
  }
}

export function requestAlerts() {
  return {
    type: REQUEST_ALERTS
  }
}

export function setAlerts(alerts) {
  return {
    type: SET_ALERTS,
    alerts
  }
}

export function setPriorityFilter(key, isVisible) {
  return {
    type: SET_STATUS_FILTER,
    key,
    isVisible
  }
}

export function setSLATimeWindow(start, end) {
  return {
    type: SET_SLA_TIME_WINDOW,
    start,
    end
  }
}

export function setNow(now) {
  if (!now) {
    now = new Date()
  }

  return {
    type: SET_NOW,
    now
  }
}