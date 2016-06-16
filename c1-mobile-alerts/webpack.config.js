var path = require('path');

var node_dir = __dirname + '/node_modules';

module.exports = {
    entry: './controllers/main.js',
    devtool: 'eval',
    cache: true,
    debug: true,
    resolve: {
        alias: {
            'stompjs': node_dir + '/stompjs/lib/stomp.js',
            'when': node_dir + '/when/when.js'
        }
    },
    output: {
        path: __dirname,
        filename: './public/dist/bundle.js'
    },
    module: {
        loaders: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                loader: 'babel-loader'
            }
        ]
    }
};
