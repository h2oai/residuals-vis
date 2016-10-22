import * as React from 'react';
import { SectionTitle } from './SectionTitle';

export function SectionNav(props) {
  return (
    <div className='sectionNav' style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      paddingTop: '10px',
      paddingLeft: '60px',
      paddingRight: '80px',
      height: '60px',
      zIndex: 2
    }}>
      <SectionTitle text={props.title}/>
    </div>
  )
}