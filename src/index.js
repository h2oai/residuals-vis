import * as React from 'react';
import { drawResidualsVis } from './drawResidualsVis';
import { IndependentVariableScatterplot } from './components/IndependentVariableScatterplot';
import { PointDensityLegend } from './components/PointDensityLegend';
import { CategoricalVariableLegend } from './components/CategoricalVariableLegend';
import { Dropdown } from './components/Dropdown';
import { SectionNav } from './components/SectionNav';
import { ModelControls } from './components/ModelControls';

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
          <ModelControls config={this.props.config}/>
          <PointDensityLegend config={this.props.config}/>
          <div className='selectContainer' style={{
            display: 'flex',
            flexDirection: 'column'
          }}>
            <Dropdown/>
            <CategoricalVariableLegend/>
          </div>
        </div>
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
