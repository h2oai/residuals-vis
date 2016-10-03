# tasks
DONE + connect to h2o
DONE + remove `algos` config in favor of `models`
DONE + support multiple datasets
CLOSED show residuals vs actual response column value? **@arno**
CLOSED prefer not to show residuals vs actual response column value 
CLOSED  **@leland** [source](http://stats.stackexchange.com/questions/155587/residual-plots-why-plot-versus-fitted-values-not-observed-y-values)
CLOSED + on exit animate points back to 0 baseline **@ivy**
DONE + display independent variable scatterplots by variable importance
DONE from h2o-3, with design from @tonyhschu
DONE + set consistent ID for first chart 
DONE   'chart'
DONE   'predictBoxplot'
DONE + add outlines to independent variable plots, 
DONE   so that they look like cards


+ support classification problems
+ show @branden et al and get feedback
+ support dynamic zoom with aggregator


+ the 10th plot has an issue with the voronoi overlay
  - one edge is undefined
  - renders fine when only two scatterplots on are the page

+ add data for all algos for Grupo Bimbo
+ add data for all algos for Prudential

+ implement sorting for plots
+ implement pagination for plots
+ add density plots (rug plots)
+ add toggle to switch between linear and log scales











 
#### eventually
+ add arrows to axis labels
+ resolve inconsistency between xVariable and drfPredict
+ add RMSPE (percentage error) **@branden** defer to table in steam?
+ add RSMLE (log error) **@branden** defer to table in steam?
+ abstract color palettes into config from scatterplot.js
  and d3ExplodingBoxplot.js



#### possibly
+ custom easing function for axis tick label transition?
  (ticks labels appear to fly out now)
+ use a named transition for repeated transition patterns?

+ show stepped transition with an option
