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

+ add tooltips for independent categorical variables
  shown in boxplots

+ preserve state so that when boxplots implode,
  they implode to y-positions from the y-variable
  associated with the currently selected model

+ ensure that boxplot outliers only animate 
  on points-to-box implode if that box 
  is already exploded