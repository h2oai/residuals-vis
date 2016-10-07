import * as React from 'react';

export function PointDensityLegend(props) {
  return (
    <div className='pointDensityLegend'>
      <svg width='100' height='50'>
        <g className='legendPoints' transform='translate(0,0)'>
          <circle style={{
            cx: 10,
            cy: 10,
            r: props.config.marks.r,
            fill: props.config.marks.colors[0],
            fillOpacity: props.config.marks.fillOpacity
          }}></circle>
          <circle style={{
            cx: 10,
            cy: 20,
            r: props.config.marks.r,
            fill: props.config.marks.colors[0],
            fillOpacity: (props.config.marks.fillOpacity * 2)
          }}></circle>
          <circle style={{
            cx: 10,
            cy: 30,
            r: props.config.marks.r,
            fill: props.config.marks.colors[0],
            fillOpacity: (props.config.marks.fillOpacity * 3)
          }}></circle>
          <circle style={{
            cx: 10,
            cy: 40,
            r: props.config.marks.r,
            fill: props.config.marks.colors[0],
            fillOpacity: (props.config.marks.fillOpacity * 4)
          }}></circle>
        </g>
        <g className='legendText' transform={`translate(${(3 * props.config.marks.r)},${props.config.marks.r})`}>
          <text x='10' y='10' style={{
            dy: '0.35em',
            font: 'Open Sans, sans-serif',
            fontSize: '8px',
            whiteSpace: 'pre',
            fill: props.config.marks.colors[0],
            stroke: 'none',
            opacity: props.config.marks.fillOpacity
          }}>
            1   point
          </text>
          <text x='10' y='20' style={{
            dy: '0.35em',
            font: 'Open Sans, sans-serif',
            fontSize: '8px',
            whiteSpace: 'pre',
            fill: props.config.marks.colors[0],
            stroke: 'none',
            opacity: (props.config.marks.fillOpacity * 2)
          }}>
            2   points overlaid
          </text>
          <text x='10' y='30' style={{
            dy: '0.35em',
            font: 'Open Sans, sans-serif',
            fontSize: '8px',
            whiteSpace: 'pre',
            fill: props.config.marks.colors[0],
            stroke: 'none',
            opacity: (props.config.marks.fillOpacity * 3)
          }}>
            3   points overlaid
          </text>
          <text x='10' y='40' style={{
            dy: '0.35em',
            font: 'Open Sans, sans-serif',
            fontSize: '8px',
            whiteSpace: 'pre',
            fill: props.config.marks.colors[0],
            stroke: 'none',
            opacity: (props.config.marks.fillOpacity * 4)
          }}>
            4+ points overlaid
          </text>
        </g>
      </svg>
    </div>
  )
}