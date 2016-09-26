# residuals

a data visualization of row-wise residuals from models trained with h2o.

in this description, the terms `residual`, `error`, and `deviance` will be used interchangeably.  we acknowledge that these terms do have more specific meanings in formal contexts.  we're open to tightening up the semantics in the future.

#### what is it good for?

+ _finding non-random patterns in residuals_ (errors, in some cases deviances).  these non-random patterns can tell you that you have an opportunity to improve model accuracy by adding a new feature that accounts for the non-random pattern.  our hypothesis is that this residuals vis can help you the machine learning practitioner iteratively discover new features, which helps you then build better fitting models.

+ _comparing machine learning models._  this visualization allows you to compare the ability of multiple models to generate accurate predictions on specific subsets of the validation sets.  

for example, for a retail dataset where we want to predict sales by store, a random forest model may have the lowest overall error.  That said, a deep learning model may have lower error on the subset of validation set rows where the `open` field's value is `0` (the store whose sales being measured was not open).  if a model that handles stores that are not open accurately is very important to you, the deep learning model might be optimal for your situation.


