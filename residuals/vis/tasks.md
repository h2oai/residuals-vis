DONE + remove legend
DONE + set points to constant radius
DONE + port xLabel and yLabel back to example
DONE + complete color interaction
DONE + use categorical color scale with max hue distance
DONE + deploy residuals vis on a server for sharing with the science team

## regression
DONE + add data for Rossman for dl, drf, gbm, glm
DONE + support multiple models for one dataset
CLOSED + set scale domains to max of 4 models
DONE + transition axes on model transition
DONE + on model transition update x-axis label
DONE + transition y-axis label position
DONE + abstract out tooltip so that it is easier to update from 
       setModelTransition.js
DONE + update tooltip text after transition
DONE + abstract out distance limited voronoi drawing
DONE + update voronoi overlay for tooltips
DONE + show large model name text in transparent gray on chart area **@leland**
DONE + model transition should not occur if button's model is already active
DONE + on model transition update model name in subtitle
DONE + show constant scales - no axis transitions **@pasha**

+ show transition at once **@pasha**
  so that we can visually track the points
+ show stepped transition with an option


+ on enter, animate points from the 0 baseline to give intuition about error **@ivy**
+ on switch between models animate points in place **@ivy**
+ on exit animate points back to 0 baseline **@ivy**

+ prefer not to show residuals vs actual response column value **@leland**
  [source](http://stats.stackexchange.com/questions/155587/residual-plots-why-plot-versus-fitted-values-not-observed-y-values)



#### possibly
+ custom easing function for axis tick label transition?
  (ticks labels appear to fly out now)
+ use a named transition for repeated transition patterns?
+ show residuals vs actual response column value **@arno**


+ support multiple datasets
+ add static exemplar dataset from h2o-3 Aggregator for Rossman
+ use aggregator to show many points
+ add data for all algos for Grupo Bimbo
+ add data for all algos for Prudential
+ draw box and whisker plots for categorical variables
+ add RMSPE (percentage error) **@branden**
+ add RSMLE (log error) **@branden**

## classification
+ identify three good classification datasets
+ caculate Pearson residual for categorial variables

## eventually
+ add arrows to axis labels
? exploding box plots
+ resolve inconsistency between xVariable and drfPredict