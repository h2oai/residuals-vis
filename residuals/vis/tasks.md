# high level
+ classification problems
+ show @branden et al
+ connect to h2o
+ aggregator

DONE + remove legend
DONE + set points to constant radius
DONE + port xLabel and yLabel back to example
DONE + complete color interaction
DONE + use categorical color scale with max hue distance
DONE + deploy residuals vis on a server for sharing with 
DONE   the science team
DONE + abstract out config

## classification
DONE + identify three good classification datasets
DONE + calculate Pearson residual for categorial variables
CLOSED plot boxplots for all permutations of categories 
CLOSED sort order - idea from @ivy - to see if order
CLOSED influence the appears of the residuals plots

## regression
DONE + add data for Rossman for dl, drf, gbm, glm
DONE + support multiple models for one dataset
CLOSED + set scale domains to max of 4 models
DONE + transition axes on model transition
DONE + on model transition update x-axis label
DONE + transition y-axis label position
DONE + abstract out tooltip so that it is easier 
DONE   to update from setModelTransition.js
DONE + update tooltip text after transition
DONE + abstract out distance limited voronoi drawing
DONE + update voronoi overlay for tooltips
DONE + show large model name text in transparent gray on 
DONE   chart area **@leland**
DONE + model transition should not occur if button's model 
DONE   is already active
DONE + on model transition update model name in subtitle
DONE + show constant scales - no axis transitions **@pasha**
DONE + show transition for points position on model 
DONE   switch at once **@pasha** so that we can
DONE   visually track the points
DONE + on enter, animate points from the 0 baseline 
DONE   to give intuition about error **@ivy**
DONE + on switch between models animate points
DONE   in place **@ivy**
DONE + fix dropdown menu bug that omits first categorical
DONE   variable `open`
DONE + on switch between models, update residual value
DONE   and transition points y-position for continuous
DONE   independent variable scatterplots

## boxplots
DONE + find example boxplot to iterate on
DONE + convert example boxplot to es2015
DONE + convert example boxplot to d3 v4
DONE ? exploding box plots
DONE + draw boxplots for one categorical variable
DONE + drive circle opacity for boxplot from configuration
DONE + ensure that the circle opacity for all plots on the residuals
DONE   vis page is the same - set from common configuration
DONE + drive boxplot y-axis ticks with configuration
DONE + add variable property for axis variable config
DONE   to replace label
DONE + improve boxplot width when there are a small number of classes
DONE   --> make narrower boxes and a narrower plot overall
DONE + translate x-axis line vertically to be at 0 on y-axis
DONE + style y-axis label like scatterplots on residuals vis
DONE + ensure that width of reset area extends
DONE   for entire width of boxplot
CLOSED ? hide x-axis baseline?
DONE + color boxplot points based on category dropdown
DONE + support config for axis label placement
DONE + link category class dropdown to boxplots
DONE + style x-axis label like scatterplots on residuals vis
DONE + scope boxplot selections so that we can plot multiple boxplots
DONE   on one page
DONE + draw boxplots  for all categorical variables
DONE + use flexbox to bring together boxplots when 
DONE   there is  enough space to show more than one boxplot
DONE   on one row
DONE + color boxplot rect fill
DONE   design: set color to color of plurality class.
DONE           set fill-opacity to % of total of plurality class
DONE + use local variables for options in
DONE   d3ExplodingBoxplot.js
DONE + change `options` to `chartOptions` in
DONE   d3ExplodingBoxplot.js for consistency
DONE + on switch between models, update residual value,
DONE   recalculate boxplots, transition boxplots
DONE + on switch between models, transition outlier points
DONE   y-position for categorical independent variables
DONE + add a class `exploded` to g.boxcontent when the 
DONE   box is exploded
DONE + on switch between models, transition normal points
DONE   y-position for categorical independent variables
DONE + sort by the sum of the residuals for each category
DONE   (for each class)
DONE + draw points on top of boxes
DONE + draw points residual vs index value
DONE   after sorting by residual value, descending
DONE + show boxplot with no fill if skeleton: true configured
DONE + show one series of points, sorted

---

+ set consistent ID for first chart 
  'chart'
  'predictBoxplot'


### classfication


### regression
+ improve tooltips for independent variables
+ orient tooltip to point with arrow

### boxplots
+ add tooltips for independent categorical variables
  shown in boxplots

+ preserve state so that when boxplots implode,
  they implode to y-positions from the y-variable
  associated with the currently selected model

+ ensure that boxplot outliers only animate 
  on points-to-box implode if that box 
  is already exploded




### Aggregator features
DONE + add static exemplar dataset from h2o-3 Aggregator for Rossman
DONE + use aggregator to show many points
DONE + add update function to scatterplot
DONE + abstract out scatterplot component
DONE + create exemplar datasets with higher radius_scale, fewer rows
DONE + use original colors for points
DONE + use gray for color of exit selection
DONE + setModelTransitionAggregated for independent variable scatterplots
DONE   so that they transition on model change as well
+ test update pattern transitions with multiple scatterplots 

+ test update pattern transitions with boxplots
+ handle interrupted scatterplot transitions nicely
+ improve scatterplot transitions



### multiple datsets features
+ support multiple datasets
+ add data for all algos for Grupo Bimbo
+ add data for all algos for Prudential


#### eventually
+ add arrows to axis labels
+ resolve inconsistency between xVariable and drfPredict
+ add RMSPE (percentage error) **@branden** defer to table in steam?
+ add RSMLE (log error) **@branden** defer to table in steam?
+ abstract color palettes into config from scatterplot.js
  and d3ExplodingBoxplot.js
+ investigate extra `g class="explodingBoxplot box"`
  that is rendered
+ support absolute px values for boxPadding
  in addition to boxPaddingProportion
+ add optional extra delay to boxplot transition Y so that
  boxplot transitions can happen after scatterplot transitions


#### possibly
+ custom easing function for axis tick label transition?
  (ticks labels appear to fly out now)
+ use a named transition for repeated transition patterns?
+ show residuals vs actual response column value? **@arno**
  prefer not to show residuals vs actual response column value **@leland**
  [source](http://stats.stackexchange.com/questions/155587/residual-plots-why-plot-versus-fitted-values-not-observed-y-values)
+ show stepped transition with an option
+ on exit animate points back to 0 baseline **@ivy**