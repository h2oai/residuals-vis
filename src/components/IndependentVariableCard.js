import * as React from 'react';

export function IndependentVariableCard(props) {
  return (
    <div id={`${props.x}Card`} className='card' key={props.i} style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'flex-start',
      flexBasis: 'auto',
      flexGrow: 1,
      flexShrink: 1
    }}>
    </div>
  );
}
