an effort at a minimal example of the `Cannot read property '0' of null in clipCells()` error message described in [d3-voronoi github issue 16](https://github.com/d3/d3-voronoi/issues/16)

this example renders the Voronoi example as expected, and does not reproduce the error.

strangely, when I pass the same data to d3.voronoi() inside my larger app elsewhere, I experience the error.

the code for the `d3-voronoi-scatterplot` chart plugin is at [https://github.com/micahstubbs/d3-voronoi-scatterplot](https://github.com/micahstubbs/d3-voronoi-scatterplot)