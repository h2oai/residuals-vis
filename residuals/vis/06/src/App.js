import React, { Component } from 'react';
import { drawResidualsVis } from './drawResidualsVis';

export default class App extends Component {
  render() {
    return (
      <div className='flex-container' style={{
        display: 'flex',
        flexDirection: 'column'
      }}>
        <div className='nav' style={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          paddingLeft: '120px',
          paddingRight: '80px',
          height: '40px'
        }}>
          <div className='title' style={{
            fontSize: '2em',
            fontWeight: 'bold'
          }}>
            residuals
          </div>
          <div className='selectContainer' style={{
            display: 'flex',
            flexDirection: 'column'
          }}>
            <select id='dropdown' style={{
              marginBottom: '12px'
            }}>
            </select>
            <svg id='categoricalVariableLegend' 
              height={120}
              width={120}
              overflow='visible'>
            </svg>
          </div>
        </div>
      </div>
    );
  }
}

drawResidualsVis(1000);
