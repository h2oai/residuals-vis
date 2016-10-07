import * as React from 'react';
import { drawResidualsVis } from './drawResidualsVis';
import { Nav } from './components/Nav';
import { IndependentVariableScatterplot } from './components/IndependentVariableScatterplot';
import { SectionNav } from './components/SectionNav';

export class ResidualsVis extends React.Component<any, any> {
  componentDidMount() {
    drawResidualsVis(this.props);
  }

  render() {
    console.log('this.props from ResidualsVis', this.props);

    const independentVariableScatterplotComponents = this.props.config.xColumns.map((x, i) => {
      return (
        <IndependentVariableScatterplot x={x} i={i} key={i}/>
      );
    });

    return (
      <div className='flex-container' style={{
        display: 'flex',
        flexDirection: 'column'
      }}>
        <Nav config={this.props.config}/>
        <div className='dependent-variable-plot-container' style={{
          display: 'flex',
          flexDirection: 'column',
          flexWrap:'nowrap'
        }}>
        </div>
        <SectionNav title='partial residuals'/>
          <div className='scatterplot-container' style={{
            display: 'flex',
            flexDirection: 'column',
            flexWrap:'nowrap',
            margin: '-5px',
            paddingLeft: '120px'
          }}>
            {independentVariableScatterplotComponents}
          </div>
          <div className='boxplot-container' style={{
            display: 'flex',
            flexDirection: 'row',
            flexWrap:'wrap'
          }}>
          </div>
      </div>
    );
  }
}
