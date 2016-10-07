import * as React from 'react';
import { SectionTitle } from './SectionTitle';

export function SectionNav(props) {
  return (
    <div className='sectionNav' style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      paddingLeft: '120px',
      paddingRight: '80px',
      height: '80px',
      zIndex: 2
    }}>
      <SectionTitle text={props.title}/>
    </div>
  )
}