# where df is the original "covtype_20k_data.hex" h2o-3 frame
df = read.csv("https://s3.amazonaws.com/h2o-public-test-data/smalldata/covtype/covtype.20k.data")
cols <- c(1, 10)
# and subset is the desired "covtype_20_data_C1_C10.hex" h2o-3 frame
subset <- df[,cols]
head(subset)

setwd('~/workspace/visualizations/scatterplot-zoom/')
write.table(subset, file = "covtype_20_data_C1_C10.data",row.names=FALSE, na="",col.names=FALSE, sep=",")