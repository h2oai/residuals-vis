library(h2o)
h2o.init(strict_version_check=FALSE)
m <- h2o.getModel("aggregatormodel")
df <- h2o.getFrame(m@parameters$training_frame)
agg <- h2o.getFrame(m@model$output_frame$name)

nrow(df)
nrow(agg)

## find two features that are most present in first eigenvector
pca <- h2o.prcomp(df, names(df), k=1)
ev <- abs(pca@model$eigenvectors$pc1)
sev <- sort(ev, decreasing = TRUE)
x=names(df)[which(ev==sev[1])]
y=names(df)[which(ev==sev[2])]
print(x)
print(y)

## scatter plots
plot(h2o.tabulate(df,x,y))
plot(h2o.tabulate(agg,x,y))
plot(h2o.tabulate(h2o.getFrame("members_exemplar0"),x,y))
plot(h2o.tabulate(h2o.getFrame("members_exemplar1"),x,y))
plot(h2o.tabulate(h2o.getFrame("members_exemplar2"),x,y))