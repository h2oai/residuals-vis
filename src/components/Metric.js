import * as React from 'react';

export function Metric(props) {
  return (
    <div className='metric'>
      <span style={{
        whiteSpace: 'pre',
        fontWeight: 600
      }}>{props.name}<sup>{props.superscript}</sup> </span>
      <span>{props.value}</span>
    </div>
  )
}