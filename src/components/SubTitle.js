import * as React from 'react';

export function SubTitle(props) {
  const algoLabel = props.config.currentAlgoLabel.toLowerCase();
  const projectLink = props.config.projectLink;
  const projectTitle = props.config.projectTitle;

  let dataText;
  if (typeof props.config.dataText !== 'undefined') {
    dataText = props.config.dataText;
  } else { // default text
    dataText = 'data';
  };

  let projectTitleNote;
  if (typeof props.config.projectTitleNote !== 'undefined') {
    projectTitleNote = props.config.projectTitleNote;
  } else {
    projectTitleNote = '';
  };

  const subtitleHTML = {__html: `calcuated from a ${algoLabel} model 
    trained with <a href='https://github.com/h2oai/h2o-3'>h2o-3</a> 
    on ${dataText} from the <a href='${projectLink}'>${projectTitle}</a> 
    project. ${projectTitleNote}`};

  return (
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
      }} 
      dangerouslySetInnerHTML={subtitleHTML}>   
      </p>
    </div>
  )
}