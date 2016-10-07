import * as React from 'react';
import { SubTitle } from './SubTitle';

export function Title(props) {
  return (
    <div className='title' style={{
      display: 'flex',
      flexDirection: 'column',
      fontSize: '2em',
      fontWeight: 'bold',
      paddingRight: '10px'
    }}>
      {props.title}
      <SubTitle/>
    </div>
  )
}