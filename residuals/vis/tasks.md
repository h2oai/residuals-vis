# tasks
DONE + connect to h2o
DONE + remove `algos` config in favor of `models`
DONE + support multiple datasets
CLOSED show residuals vs actual response column value? **@arno**
CLOSED prefer not to show residuals vs actual response column value 
CLOSED  **@leland** [source](http://stats.stackexchange.com/questions/155587/residual-plots-why-plot-versus-fitted-values-not-observed-y-values)
CLOSED + on exit animate points back to 0 baseline **@ivy**

+ display independent variable scatterplots by variable importance
  from h2o-3, with design from @tonyhschu
+ show deviances from h2o-3 as calculated by @arno's code
  (instead of simple residuals)
+ support classification problems
+ show @branden et al and get feedback
+ support dynamic zoom with aggregator

+ set consistent ID for first chart 
  'chart'
  'predictBoxplot'
+ the 10th plot has an issue with the voronoi overlay
  - one edge is undefined
  - renders fine when only two scatterplots on are the page

+ add data for all algos for Grupo Bimbo
+ add data for all algos for Prudential












 
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
