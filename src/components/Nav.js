import * as React from 'react';
import { Title } from './Title';
import { ModelControls } from './ModelControls';
import { PointDensityLegend } from './PointDensityLegend';
import { Dropdown } from './Dropdown';
import { CategoricalVariableLegend } from './CategoricalVariableLegend';

export function Nav(props) {
  return (
    <div className='nav' style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      paddingLeft: '120px',
      paddingRight: '80px',
      height: '80px',
      zIndex: 2
    }}>
      <Title title='residuals' config={props.config}/>
      <ModelControls config={props.config}/>
      <PointDensityLegend config={props.config}/>
      <div className='selectContainer' style={{
        display: 'flex',
        flexDirection: 'column'
      }}>
        <Dropdown/>
        <CategoricalVariableLegend/>
      </div>
    </div>
  )
}