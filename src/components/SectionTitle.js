import * as React from 'react';

export function SectionTitle(props) {
  return (
    <div className='sectionTitle' style={{
        display: 'flex',
        flexDirection: 'column',
        fontSize: '2em',
        fontWeight: 'bold',
        paddingRight: '10px'
      }}>
      {props.text}
    </div>
  )
}