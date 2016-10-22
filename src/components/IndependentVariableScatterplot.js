import * as React from 'react';

export function IndependentVariableScatterplot(props) {
  return (
    <div id={`${props.x}Card`} className='card' key={props.i} style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'flex-start'
    }}>
    </div>
  );
}
