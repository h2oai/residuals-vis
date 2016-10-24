import * as React from 'react';

export function Metric(props) {
  return (
    <div className={`${props.name}`}>
      <span className='name' style={{
        whiteSpace: 'pre',
        fontWeight: 600
      }}>{props.name}<sup>{props.superscript}</sup> </span>
      <span className='value'>{props.value}</span>
    </div>
  )
}