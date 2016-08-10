import React, { Component } from 'react';
import { drawResidualsVis } from './drawResidualsVis';

export default class App extends Component {
  render() {
    return (
      <div className='flex-container' style={{
        flexDirection: 'column',
        display: 'flex'
      }}>
        <div className='nav' style={{
          flexDirection: 'row',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <h1>residuals</h1>
        </div>
      </div>
    );
  }
}

drawResidualsVis(1000);
