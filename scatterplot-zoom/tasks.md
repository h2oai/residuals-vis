DONE 03 + comment out xAxis button
DONE 04 + convert data to json
DONE 04 + update chart to use json data
DONE 04 + increase chart size
DONE 04 + revert impossible zoom
DONE 04 + identify the  threshold zoom level
DONE 04 + add nested data for one point
DONE 04 + above zoom level threshold show nested data
DONE 04 + below zoom level threshold hide nested data
DONE 05 + transition detail points from parent aggregate point position
  to actual position
DONE + load exemplar dataset with an API call

DONE + figure out how to customize the position of the dat.gui controls
+ scale text dx, dy with radius
+ update color of exemplar node when its members are shown
  - orange fill with gray stroke?



Roadmap

+ add custom easing function to gradually slow dots as they
  near their destination point
+ add third zoom level with different marks symbol
+ add fourth zoom level with different marks symbol
+ fix bug where exemplarMember points jump on pan (dragStart)
+ handle simulateneous pan and zoom. 
  ensure that examplarMember points still emanate from 
  the position of the exemplar point
+ find out if it is faster to
  - add a mouseover event to the voronoi path always that 
  has a conditional statement that checks zoom level
  before drawing detail points
  - add a conditional statement to the zoom function that adds a
  mouseover event to all voronoi paths beyond a certain zoom level
  - approach C???
+ update opacity with zoom level?
  - zoomed out, low opacity so points are not occluded
  - zoomed in, high opacity so points are clearly visible
+ add lensing for the exemplar (fisheye lensing, Furnival)
+ add lensing for the axes as well
