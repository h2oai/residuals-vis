import * as React from 'react';
import { SubTitle } from './SubTitle';

export function Title(props) {
  return (
    <div className='titleBar' style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      paddingLeft: '120px',
      paddingRight: '80px',
      height: '80px',
      zIndex: 2
    }}>
      <div className='title' style={{
        display: 'flex',
        flexDirection: 'column',
        fontSize: '2em',
        fontWeight: 'bold',
        paddingRight: '10px'
      }}>
        {props.title}
        <SubTitle config={props.config}/>
      </div>
    </div>
  )
}