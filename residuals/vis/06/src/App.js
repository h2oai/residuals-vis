import React, { Component } from 'react';
import { drawResidualsVis } from './drawResidualsVis';

export default class App extends Component {
  render() {
    return (
      <div className="flex-container">
        <h1>residuals</h1>
      </div>
    );
  }
}

drawResidualsVis(1000);
