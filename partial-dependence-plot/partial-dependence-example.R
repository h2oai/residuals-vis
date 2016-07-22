library(h2o)
h2o.init(nthreads=-1,strict_version_check = F)
df <- h2o.importFile("http://s3.amazonaws.com/h2o-public-test-data/smalldata/gbm_test/titanic.csv")
response = "age"
predictors <- setdiff(names(df),c(response,"name"))
model <- h2o.gbm(x=predictors,y=response,training_frame=df)

predictor <- "boat"
predictor
minVal <- min(df[,predictor], na.rm=TRUE)
maxVal <- max(df[,predictor], na.rm=TRUE)
N <- 20
frame <- df ## could be a different frame
minVal
maxVal

origpreds <- h2o.predict(model, frame)

values <- seq(minVal,maxVal,(maxVal-minVal)/N)
values
deltas <- c()
for (val in values) {
  tempframe <- frame
  tempframe[,predictor] <- val
  newpreds <- h2o.predict(model, tempframe)
  deltas <- c(deltas,mean(newpreds-origpreds))
}

deltas
plot(values,deltas,type='l',main=paste0("Partial dependence plot for ", response, " as a function of ", predictor), xlab=predictor, ylab=paste0("delta.",response))