import * as React from 'react';
import * as classNames from 'classnames';

interface Props {
  className: any
}

export class ResidualsVis extends React.Component<Props, any> {
  render(): React.ReactElement<HTMLAnchorElement> {
    let className: string = classNames('residuals-vis', this.props.className);
    return (
      <div>
        ResidualsVis
      </div>
    );
  }
}