import * as React from 'react';
import { Metric } from './Metric';

export function ModelMetrics(props) {
  const currentModel = 'drf';
  return (
    <div className='modelMetricsContainer' style={{
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        paddingLeft: '0px',
        paddingRight: '0px',
        height: '80px',
        zIndex: 2
    }}>
      <div className='modelMetrics' style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        alignContent: 'center',
        flexWrap: 'wrap',
        paddingLeft: '20px',
        paddingRight: '20px',
        height: '80px',
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
          name='nobs'
          value={props.config.modelMetrics[currentModel].nobs}
        />
      </div>
      <div className='modelMetrics' style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        alignContent: 'center',
        flexWrap: 'wrap',
        paddingLeft: '20px',
        paddingRight: '20px',
        height: '80px',
        zIndex: 2
      }}>
        <Metric 
          name='r2'
          value={props.config.modelMetrics[currentModel].r2}
        />
        <Metric 
          name='Mean Residual Deviance'
          value={props.config.modelMetrics[currentModel].mean_residual_deviance}
        />
        <Metric 
          name='MAE'
          value={props.config.modelMetrics[currentModel].mae}
        />
      </div>
      <div className='modelMetrics' style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        alignContent: 'center',
        flexWrap: 'wrap',
        paddingLeft: '20px',
        paddingRight: '20px',
        height: '80px',
        zIndex: 2
      }}>
        <Metric 
          name='RMSLE'
          value={props.config.modelMetrics[currentModel].rmsle}
        />
      </div>
    </div>
  )
}