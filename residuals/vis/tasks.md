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
+ on model transition update model name in subtitle
+ custom easing function for axis tick label transition?
  (ticks labels appear to fly out now)
+ update tooltip text after transition
DONE + transition y-axis label position
+ use a named transition for repeated transition patterns?
+ abstract out tooltip so that it is easier to update from 
  setModelTransition.js

+ support multiple datasets
+ add static exemplar dataset from h2o-3 Aggregator for Rossman
+ add data for all algos for Grupo Bimbo
+ add data for all algos for Prudential
+ draw box and whisker plots for categorical variables
+ add RMSPE (percentage error) 
+ add RSMLE (log error)

## classification
+ identify three good classification datasets
+ caculate Pearson residual for categorial variables

## eventually
+ add arrows to axis labels
? exploding box plots
