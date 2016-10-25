import * as React from 'react';
import { Metric } from './Metric';

export function ModelMetrics(props) {
  const currentModel = props.config.currentAlgo;
  console.log('currentModel from ModelMetrics', currentModel);
  return (
    <div className='modelMetricsContainer' style={{
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        paddingLeft: '0px',
        paddingRight: '0px',
        // height: '80px',
        zIndex: 2,
        flexGrow: 1
    }}>
      <div className='modelMetrics' style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        alignContent: 'center',
        flexWrap: 'wrap',
        paddingLeft: '0px',
        paddingRight: '10px',
        // height: '80px',
        zIndex: 2
      }}>
        <Metric 
          name='MSE'
          value={props.config.modelMetrics[currentModel].mse}
        />
        <Metric 
          name='RMSE'
          value={props.config.modelMetrics[currentModel].rmse}
        />
        <Metric 
          name='RMSLE'
          value={props.config.modelMetrics[currentModel].rmsle}
        />
      </div>
      <div className='modelMetrics' style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        alignContent: 'center',
        flexWrap: 'wrap',
        paddingLeft: '10px',
        paddingRight: '10px',
        // height: '80px',
        zIndex: 2
      }}>

        <Metric 
          name='r'
          superscript='2'
          value={props.config.modelMetrics[currentModel].r2}
        />
       {/*} <Metric 
          name='Mean Residual Deviance'
          value={props.config.modelMetrics[currentModel].mean_residual_deviance}
        />*/}
        <Metric 
          name='MAE'
          value={props.config.modelMetrics[currentModel].mae}
        />
        <Metric 
          name='N'
          value={props.config.modelMetrics[currentModel].nobs}
        />
      </div>
    </div>
  )
}