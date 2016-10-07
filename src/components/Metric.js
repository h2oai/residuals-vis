import * as React from 'react';

export function Metric(props) {
  return (
    <div className='metric'>
      <span style={{
        whiteSpace: 'pre',
        fontWeight: 600
      }}>{props.name} </span>
      <span>{props.value}</span>
    </div>
  )
}