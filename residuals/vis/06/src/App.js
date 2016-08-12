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
            residuals
            <div className='subTitle' style={{
              paddingTop: '0px',
              paddingBottom: '0px',
            }}>
              <p id='subTitle' style={{
                fontWeight: 'normal',
                marginTop: '0px',
                marginBottom: '0px',
                font: 'Open Sans, sans-serif',
                fontSize: '12px'
              }}>
              </p>
            </div>
          </div>
          <div className='modelControls'style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-end',
              justifyContent: 'space-around',
              marginRight: '10px',
              zIndex: 2,
              font: 'Open Sans, sans-serif',
              fontSize: '12px',
              fontWeight: 'bold'
            }}>
              <div id='dlButton'>dl</div>
              <div id='drfButton'>drf</div >
              <div id='gbmButton'>gbm</div>
              <div id='glmButton'>glm</div>
            </div>
          <div className='selectContainer' style={{
            display: 'flex',
            flexDirection: 'column'
          }}>
            <select id='dropdown' style={{
              marginBottom: '12px'
            }}>
              <option value='-1'>color by...</option>
            </select>
            <svg height='120px' 
              width='120px' 
              overflow='visible'
              id='categoricalVariableLegend'>
            </svg>
          </div>
        </div>
      </div>
    );
  }
}

drawResidualsVis(1000);
