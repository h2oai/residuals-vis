import * as React from 'react';

export function IndependentVariableScatterplot(props) {
  const xNoSpaces = props.x.split(' ').join('');
  return (
    <div id={`${xNoSpaces}Card`} className='card' key={props.i} style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'flex-start'
    }}>
    </div>
  );
}
