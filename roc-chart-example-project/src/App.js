import React, { Component } from 'react';
import { drawROCChart } from './drawROCChart';

export default class App extends Component {
  render() {
    return (
      <h1>roc-chart-example-project</h1>
    );
  }
}

drawROCChart();