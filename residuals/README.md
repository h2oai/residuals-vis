# residuals

a data visualization of row-wise residuals from models trained with h2o.

in this description, the terms `residual`, `error`, and `deviance` will be used interchangeably.  we acknowledge that these terms do have more specific meanings in formal contexts.  we're open to tightening up the semantics in the future.

#### what is it good for?

+ _finding non-random patterns in residuals_ (errors, in some cases deviances).  these non-random patterns can tell you that you have an opportunity to improve model accuracy by adding a new feature that accounts for the non-random pattern.  our hypothesis is that this residuals vis can help you the machine learning practitioner iteratively discover new features, which helps you then build better fitting models.

+ _comparing machine learning models._  this visualization allows you to compare the ability of multiple models to generate accurate predictions on specific subsets of the validation sets.  

for example, for a retail dataset where we want to predict sales by store, a random forest model may have the lowest overall error.  That said, a deep learning model may have lower error on the subset of validation set rows where the `open` field's value is `0` (the store whose sales being measured was not open).  if a model that handles stores that are not open accurately is very important to you, the deep learning model might be optimal for your situation.

#### structure

the residuals vis is a single page React app layed out in `App.js`  with additional elements that are drawn and modified with Javascript.  configuration is specified in a `.json` file.  a good example configuration to refer to is [santanderAggregated.js](vis/06/src/config/santanderAggregated.js)   the residuals vis in it's current form requires a running instance of `h2o-3` that has trained models in memory.


the residuals vis expects data and configuration from one or more machine learning models trained with `h2o-3`.

#### data 

for each model, the residuals vis takes the ID of the models and issues a series of API calls to `h2o-3` to:

1. generate row-wise predictions and deviances. parameters for this call are:
  - model ID
  - validation frame ID

2. combine the validation frame with the prediction frame. parameters:
  - string to use as ID for new combined frame
  - validation frame ID
  - prediction frame ID

3. combine the result of 2 with the deviances frame. parameters:
  - string to use as ID for this second new combined frame
  - ID of frame resulting from 2
  - deviances frame ID

4. retrieve & serialize to a javascript array of objects the combined frame with 
  - validation set data
  - row-wise predictions for response variable
  - row-wise deviances

this process is repeated for all models that you would like to compare.

#### API reference

