import * as React from 'react';

export function ModelControls(props) {
  const modelButtonComponents = props.config.modelIDs.map((model, i) => {
    return <div id={`${model}Button`} key={i}>{model}</div>
  });
  return (
    <div className='modelControls'style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-end',
        justifyContent: 'space-around',
        marginRight: '10px',
        zIndex: 2,
        font: 'Open Sans, sans-serif',
        fontSize: '12px',
        fontWeight: 'bold',
        flexBasis: 'auto',
        flexGrow: 1,
        flexShrink: 1
      }}>
      <div>{modelButtonComponents}</div>
    </div>
  )
}