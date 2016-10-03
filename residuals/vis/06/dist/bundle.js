(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory(require("react"));
	else if(typeof define === 'function' && define.amd)
		define("residualsVis", ["react"], factory);
	else if(typeof exports === 'object')
		exports["residualsVis"] = factory(require("react"));
	else
		root["residualsVis"] = factory(root["React"]);
})(this, function(__WEBPACK_EXTERNAL_MODULE_2__) {
return /******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ function(module, exports, __webpack_require__) {

	eval("module.exports = __webpack_require__(1);\n\n\n/*****************\n ** WEBPACK FOOTER\n ** multi main\n ** module id = 0\n ** module chunks = 0\n **/\n//# sourceURL=webpack:///multi_main?");

/***/ },
/* 1 */
/***/ function(module, exports, __webpack_require__) {

	eval("/* REACT HOT LOADER */ if (false) { (function () { var ReactHotAPI = require(\"/Users/m/workspace/visualizations/residuals/vis/06/node_modules/react-hot-api/modules/index.js\"), RootInstanceProvider = require(\"/Users/m/workspace/visualizations/residuals/vis/06/node_modules/react-hot-loader/RootInstanceProvider.js\"), ReactMount = require(\"react/lib/ReactMount\"), React = require(\"react\"); module.makeHot = module.hot.data ? module.hot.data.makeHot : ReactHotAPI(function () { return RootInstanceProvider.getRootInstances(ReactMount); }, React); })(); } try { (function () {\n\n'use strict';\n\nObject.defineProperty(exports, \"__esModule\", {\n  value: true\n});\nexports.ResidualsVis = undefined;\n\nvar _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if (\"value\" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();\n\nvar _react = __webpack_require__(2);\n\nvar React = _interopRequireWildcard(_react);\n\nfunction _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }\n\nfunction _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError(\"Cannot call a class as a function\"); } }\n\nfunction _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError(\"this hasn't been initialised - super() hasn't been called\"); } return call && (typeof call === \"object\" || typeof call === \"function\") ? call : self; }\n\nfunction _inherits(subClass, superClass) { if (typeof superClass !== \"function\" && superClass !== null) { throw new TypeError(\"Super expression must either be null or a function, not \" + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }\n\nvar ResidualsVis = exports.ResidualsVis = function (_React$Component) {\n  _inherits(ResidualsVis, _React$Component);\n\n  function ResidualsVis() {\n    _classCallCheck(this, ResidualsVis);\n\n    return _possibleConstructorReturn(this, (ResidualsVis.__proto__ || Object.getPrototypeOf(ResidualsVis)).apply(this, arguments));\n  }\n\n  _createClass(ResidualsVis, [{\n    key: 'render',\n\n    // render(): React.ReactElement<HTMLAnchorElement> {\n    value: function render() {\n      return React.createElement(\n        'div',\n        { className: 'flex-container', style: {\n            display: 'flex',\n            flexDirection: 'column'\n          } },\n        React.createElement(\n          'div',\n          { className: 'nav', style: {\n              display: 'flex',\n              flexDirection: 'row',\n              alignItems: 'flex-start',\n              justifyContent: 'space-between',\n              paddingLeft: '120px',\n              paddingRight: '80px',\n              height: '80px',\n              zIndex: 2\n            } },\n          React.createElement(\n            'div',\n            { className: 'title', style: {\n                display: 'flex',\n                flexDirection: 'column',\n                fontSize: '2em',\n                fontWeight: 'bold',\n                paddingRight: '10px'\n              } },\n            'residuals',\n            React.createElement(\n              'div',\n              { className: 'subTitle', style: {\n                  paddingTop: '0px',\n                  paddingBottom: '0px'\n                } },\n              React.createElement('p', { id: 'subTitle', style: {\n                  fontWeight: 'normal',\n                  marginTop: '0px',\n                  marginBottom: '0px',\n                  font: 'Open Sans, sans-serif',\n                  fontSize: '12px'\n                } })\n            )\n          ),\n          React.createElement(\n            'div',\n            { className: 'modelControls', style: {\n                display: 'flex',\n                flexDirection: 'column',\n                alignItems: 'flex-end',\n                justifyContent: 'space-around',\n                marginRight: '10px',\n                zIndex: 2,\n                font: 'Open Sans, sans-serif',\n                fontSize: '12px',\n                fontWeight: 'bold'\n              } },\n            React.createElement(\n              'div',\n              { id: 'dlButton' },\n              'dl'\n            ),\n            React.createElement(\n              'div',\n              { id: 'drfButton' },\n              'drf'\n            ),\n            React.createElement(\n              'div',\n              { id: 'gbmButton' },\n              'gbm'\n            ),\n            React.createElement(\n              'div',\n              { id: 'glmButton' },\n              'glm'\n            )\n          ),\n          React.createElement(\n            'div',\n            { className: 'selectContainer', style: {\n                display: 'flex',\n                flexDirection: 'column'\n              } },\n            React.createElement('select', { id: 'dropdown', style: {\n                marginBottom: '12px'\n              } }),\n            React.createElement('svg', { height: '120px',\n              width: '120px',\n              overflow: 'visible',\n              id: 'categoricalVariableLegend' })\n          )\n        ),\n        React.createElement('div', { className: 'dependent-variable-plot-container', style: {\n            display: 'flex',\n            flexDirection: 'column',\n            flexWrap: 'nowrap'\n          } }),\n        React.createElement(\n          'div',\n          { className: 'sectionNav', style: {\n              display: 'flex',\n              flexDirection: 'row',\n              alignItems: 'flex-start',\n              justifyContent: 'space-between',\n              paddingLeft: '120px',\n              paddingRight: '80px',\n              height: '80px',\n              zIndex: 2\n            } },\n          React.createElement(\n            'div',\n            { className: 'sectionTitle', style: {\n                display: 'flex',\n                flexDirection: 'column',\n                fontSize: '2em',\n                fontWeight: 'bold',\n                paddingRight: '10px'\n              } },\n            'partial residuals'\n          )\n        ),\n        React.createElement('div', { className: 'scatterplot-container', style: {\n            display: 'flex',\n            flexDirection: 'column',\n            flexWrap: 'nowrap',\n            margin: '-5px',\n            paddingLeft: '120px'\n          } }),\n        React.createElement('div', { className: 'boxplot-container', style: {\n            display: 'flex',\n            flexDirection: 'row',\n            flexWrap: 'wrap'\n          } })\n      );\n    }\n  }]);\n\n  return ResidualsVis;\n}(React.Component);\n\n/* REACT HOT LOADER */ }).call(this); } finally { if (false) { (function () { var foundReactClasses = module.hot.data && module.hot.data.foundReactClasses || false; if (module.exports && module.makeHot) { var makeExportsHot = require(\"/Users/m/workspace/visualizations/residuals/vis/06/node_modules/react-hot-loader/makeExportsHot.js\"); if (makeExportsHot(module, require(\"react\"))) { foundReactClasses = true; } var shouldAcceptModule = true && foundReactClasses; if (shouldAcceptModule) { module.hot.accept(function (err) { if (err) { console.error(\"Cannot not apply hot update to \" + \"index.js\" + \": \" + err.message); } }); } } module.hot.dispose(function (data) { data.makeHot = module.makeHot; data.foundReactClasses = foundReactClasses; }); })(); } }\n\n/*****************\n ** WEBPACK FOOTER\n ** ./src/index.js\n ** module id = 1\n ** module chunks = 0\n **/\n//# sourceURL=webpack:///./src/index.js?");

/***/ },
/* 2 */
/***/ function(module, exports) {

	eval("module.exports = __WEBPACK_EXTERNAL_MODULE_2__;\n\n/*****************\n ** WEBPACK FOOTER\n ** external {\"root\":\"React\",\"commonjs2\":\"react\",\"commonjs\":\"react\",\"amd\":\"react\"}\n ** module id = 2\n ** module chunks = 0\n **/\n//# sourceURL=webpack:///external_%7B%22root%22:%22React%22,%22commonjs2%22:%22react%22,%22commonjs%22:%22react%22,%22amd%22:%22react%22%7D?");

/***/ }
/******/ ])
});
;