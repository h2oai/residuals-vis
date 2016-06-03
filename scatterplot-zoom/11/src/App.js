import React, { Component } from 'react';
import { drawScatterplot } from './scatter';

export default class App extends Component {
  render() {
    return (
      // Add your component markup and other subcomponent references here.
      <div id='scatter'></div>
    );
  }
}

drawScatterplot();
