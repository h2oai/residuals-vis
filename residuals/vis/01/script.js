////////////////////////////////////////////////////////////
//////////////////////// Set-up ////////////////////////////
////////////////////////////////////////////////////////////

//Quick fix for resizing some things for mobile-ish viewers
var mobileScreen = ($( window ).innerWidth() < 500 ? true : false);

//Scatterplot
var margin = {left: 30, top: 20, right: 20, bottom: 20},
	width = Math.min($("#chart").width(), 800) - margin.left - margin.right,
	height = width*2/3
	maxDistanceFromPoint = 50;
			
var svg = d3.select("svg")
			.attr("width", (width + margin.left + margin.right))
			.attr("height", (height + margin.top + margin.bottom));
			
var wrapper = svg.append("g").attr("class", "chordWrapper")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

//////////////////////////////////////////////////////
///////////// Initialize Axes & Scales ///////////////
//////////////////////////////////////////////////////

var opacityCircles = 0.7; 

//Set the color for each region
var color = d3.scale.ordinal()
					.range(["#EFB605", "#E58903", "#E01A25", "#C20049", "#991C71", "#66489F", "#2074A0", "#10A66E", "#7EB852"])
					.domain(["Africa | North & East", "Africa | South & West", "America | North & Central", "America | South", 
							 "Asia | East & Central", "Asia | South & West", "Europe | North & West", "Europe | South & East", "Oceania"]);
							 
//Set the new x axis range
var xScale = d3.scale.log()
	.range([0, width])
	.domain([100,2e5]); //I prefer this exact scale over the true range and then using "nice"
	//.domain(d3.extent(countries, function(d) { return d.GDP_perCapita; }))
	//.nice();
//Set new x-axis
var xAxis = d3.svg.axis()
	.orient("bottom")
	.ticks(2)
	.tickFormat(function (d) { //Difficult function to create better ticks
		return xScale.tickFormat((mobileScreen ? 4 : 8),function(d) { 
			var prefix = d3.formatPrefix(d); 
			return "$" + prefix.scale(d) + prefix.symbol;
		})(d);
	})	
	.scale(xScale);	
//Append the x-axis
wrapper.append("g")
	.attr("class", "x axis")
	.attr("transform", "translate(" + 0 + "," + height + ")")
	.call(xAxis);
		
//Set the new y axis range
var yScale = d3.scale.linear()
	.range([height,0])
	.domain(d3.extent(countries, function(d) { return d.lifeExpectancy; }))
	.nice();	
var yAxis = d3.svg.axis()
	.orient("left")
	.ticks(6)  //Set rough # of ticks
	.scale(yScale);	
//Append the y-axis
wrapper.append("g")
		.attr("class", "y axis")
		.attr("transform", "translate(" + 0 + "," + 0 + ")")
		.call(yAxis);
		
//Scale for the bubble size
var rScale = d3.scale.sqrt()
			.range([mobileScreen ? 1 : 2, mobileScreen ? 10 : 16])
			.domain(d3.extent(countries, function(d) { return d.GDP; }));
	
////////////////////////////////////////////////////////////	
/////////////////// Scatterplot Circles ////////////////////
////////////////////////////////////////////////////////////	

//Initiate a group element for the circles
var circleGroup = wrapper.append("g")
	.attr("class", "circleWrapper"); 
	
//Place the country circles
circleGroup.selectAll("countries")
	.data(countries.sort(function(a,b) { return b.GDP > a.GDP; })) //Sort so the biggest circles are below
	.enter().append("circle")
		.attr("class", function(d,i) { return "countries " + d.CountryCode; })
		.style("opacity", opacityCircles)
		.style("fill", function(d) {return color(d.Region);})
		.attr("cx", function(d) {return xScale(d.GDP_perCapita);})
		.attr("cy", function(d) {return yScale(d.lifeExpectancy);})
		.attr("r", function(d) {return rScale(d.GDP);});

////////////////////////////////////////////////////////////	
///////////////// distance-limited Voronoï /////////////////
////////////////////////////////////////////////////////////

//Initiate the voronoi function
//Use the same variables of the data in the .x and .y as used in the cx and cy of the circle call
//The clip extent will make the boundaries end nicely along the chart area instead of splitting up the entire SVG
//(if you do not do this it would mean that you already see a tooltip when your mouse is still in the axis area, which is confusing)

var xAccessor = function(d) { return xScale(d.GDP_perCapita); };
var yAccessor = function(d) { return yScale(d.lifeExpectancy); };

var limitedVoronoi = d3.distanceLimitedVoronoi()
	.x(xAccessor)
	.y(yAccessor)
	.limit(50)
	.clipExtent([[0, 0], [width, height]]);
var limitedVoronoiCells = limitedVoronoi(countries);

//Initiate a group element to place the voronoi diagram in
var limitedVoronoiGroup = wrapper.append("g")
	.attr("class", "voronoiWrapper");
	
//Create the distance-limited Voronoi diagram
limitedVoronoiGroup.selectAll("path")
	.data(limitedVoronoiCells) //Use vononoi() with your dataset inside
	.enter().append("path")
    //.attr("d", function(d, i) { return "M" + d.join("L") + "Z"; })
		.attr("d", function(d, i) { return d.path; })
    .datum(function(d, i) { return d.point; })
    //Give each cell a unique class where the unique part corresponds to the circle classes
    .attr("class", function(d,i) { return "voronoi " + d.CountryCode; })
    .style("stroke", "lightblue") //I use this to look at how the cells are dispersed as a check
    .style("fill", "none")
    .style("pointer-events", "all")
    .on("mouseenter", showTooltip)
    .on("mouseout",  removeTooltip);

//////////////////////////////////////////////////////
///////////////// Initialize Labels //////////////////
//////////////////////////////////////////////////////

//Set up X axis label
wrapper.append("g")
	.append("text")
	.attr("class", "x title")
	.attr("text-anchor", "end")
	.style("font-size", (mobileScreen ? 8 : 12) + "px")
	.attr("transform", "translate(" + width + "," + (height - 10) + ")")
	.text("GDP per capita [US $] - Note the logarithmic scale");

//Set up y axis label
wrapper.append("g")
	.append("text")
	.attr("class", "y title")
	.attr("text-anchor", "end")
	.style("font-size", (mobileScreen ? 8 : 12) + "px")
	.attr("transform", "translate(18, 0) rotate(-90)")
	.text("Life expectancy");
	
///////////////////////////////////////////////////////////////////////////
///////////////////////// Create the Legend////////////////////////////////
///////////////////////////////////////////////////////////////////////////

if (!mobileScreen) {
	//Legend			
	var	legendMargin = {left: 5, top: 10, right: 5, bottom: 10},
		legendWidth = 160,
		legendHeight = 270;
		
	var svgLegend = d3.select("#legend").append("svg")
				.attr("width", (legendWidth + legendMargin.left + legendMargin.right))
				.attr("height", (legendHeight + legendMargin.top + legendMargin.bottom));			

	var legendWrapper = svgLegend.append("g").attr("class", "legendWrapper")
					.attr("transform", "translate(" + legendMargin.left + "," + legendMargin.top +")");
		
	var rectSize = 16, //dimensions of the colored square
		rowHeight = 22, //height of a row in the legend
		maxWidth = 125; //widht of each row
		  
	//Create container per rect/text pair  
	var legend = legendWrapper.selectAll('.legendSquare')  	
			  .data(color.range())                              
			  .enter().append('g')   
			  .attr('class', 'legendSquare') 
			  .attr("transform", function(d,i) { return "translate(" + 0 + "," + (i * rowHeight) + ")"; });
	 
	//Append small squares to Legend
	legend.append('rect')                                     
		  .attr('width', rectSize) 
		  .attr('height', rectSize) 			  		  
		  .style('fill', function(d) {return d;});                                 
	//Append text to Legend
	legend.append('text')                                     
		  .attr('transform', 'translate(' + 25 + ',' + (rectSize/2) + ')')
		  .attr("class", "legendText")
		  .style("font-size", "11px")
		  .attr("dy", ".35em")		  
		  .text(function(d,i) { return color.domain()[i]; });  
}//if !mobileScreen
else {
	d3.select("#legend").style("display","none");
}

//Show the tooltip on the hovered over circle
function showTooltip(d) {
	
	//Save the circle element (so not the voronoi which is triggering the hover event)
	//in a variable by using the unique class of the voronoi (CountryCode)
	var element = d3.selectAll(".countries."+d.CountryCode);
	
	//skip tooltip creation if already defined
  existingTooltip = $(".popover");
  if (existingTooltip !== null 
      && existingTooltip.length >0
      && existingTooltip.text()===d.Country) {
    return;
  }
  
  //Define and show the tooltip using bootstrap popover
	//But you can use whatever you prefer
	$(element).popover({
		placement: 'auto top', //place the tooltip above the item
		container: '#chart', //the name (class or id) of the container
		trigger: 'manual',
		html : true,
		content: function() { //the html content to show inside the tooltip
			return "<span style='font-size: 11px; text-align: center;'>" + d.Country + "</span>"; }
	});
	$(element).popover('show');

	//Make chosen circle more visible
	element.style("opacity", 1);
					
}//function showTooltip

//Hide the tooltip when the mouse moves away
function removeTooltip(d) {

	//Save the circle element (so not the voronoi which is triggering the hover event)
	//in a variable by using the unique class of the voronoi (CountryCode)
	var element = d3.selectAll(".countries."+d.CountryCode);
	
	//Hide the tooltip
	$('.popover').each(function() {
		$(this).remove();
	}); 
	
	//Fade out the bright circle again
	element.style("opacity", opacityCircles);
	
}//function removeTooltip

//iFrame handler
//iFrame handler
var pymChild = new pym.Child();
pymChild.sendHeight()
setTimeout(function() { pymChild.sendHeight(); },5000);