import React, { Component } from 'react';
import { drawResidualsVis } from './drawResidualsVis';

export default class App extends Component {
  render() {
    return (
      <h1>residuals</h1>
    );
  }
}

drawResidualsVis(1000);
