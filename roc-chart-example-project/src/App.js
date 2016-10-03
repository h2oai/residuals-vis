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
// drawROCChart(150, 150);
// drawROCChart(200, 200);
// drawROCChart(720, 720);