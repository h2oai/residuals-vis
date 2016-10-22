import * as React from 'react';
import { ModelControls } from './ModelControls';
import { PointDensityLegend } from './PointDensityLegend';
import { Dropdown } from './Dropdown';
import { CategoricalVariableLegend } from './CategoricalVariableLegend';
import { ModelMetrics } from './ModelMetrics';

export function Nav(props) {
  return (
    <div className='nav' style={{
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'flex-start',
      flexBasis: 'auto',
      flexGrow: 1,
      flexShrink: 1,
      paddingTop: '20px',
      paddingLeft: '60px',
      paddingBottom: '20px',
      paddingRight: '80px',
      marginLeft: '0px',
      height: '100px',
      zIndex: 2
    }}>
      <ModelMetrics config={props.config}/>
      <ModelControls config={props.config}/>
      <PointDensityLegend config={props.config}/>
      <div className='selectContainer' style={{
        display: 'flex',
        flexDirection: 'column',
        flexGrow: 0
      }}>
        <Dropdown/>
        <CategoricalVariableLegend/>
      </div>
    </div>
  )
}