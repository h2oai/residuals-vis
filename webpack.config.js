var path = require('path');
var webpack = require('webpack');
var BrowserSyncPlugin = require('browser-sync-webpack-plugin');

module.exports = {
  devtool: 'eval',
  entry: [
    './src/index'
  ],
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'bundle.js',
    library: 'residualsVis',
    libraryTarget: 'umd',
    umdNamedDefine: true
  },
  externals: [
    {
      "react": {
        "root": "React",
        "commonjs2": "react",
        "commonjs": "react",
        "amd": "react"
      },
      "react-dom": {
        "root": "ReactDOM",
        "commonjs2": "react-dom",
        "commonjs": "react-dom",
        "amd": "react-dom"
      }
    }
  ],
  resolve: {
    extensions: ['', '.js', '.css']
  },
  plugins: [
    new BrowserSyncPlugin({
      host: 'localhost',
      port: 3000,
      server: { baseDir: ['dist'] }
    })
  ],
  module: {
    loaders: [{
      test: /\.js$/,
      loaders: ['babel'],
      include: path.join(__dirname, 'src')
    }]
  }
};
