<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html lang="en">
<head>
<!-- Taken from here: https://github.com/jasonmelgoza/NCAA-Bracket -->
  <meta name="NCAA Bracket" content="content">
  <link rel="shortcut icon" href="<%=request.getContextPath() %>/images/basketball.ico" />
  <link rel="stylesheet" href="<%=request.getContextPath() %>/CSS/bracket.css" type="text/css">
  <title> NCAA Men's Basketball Tourney History </title>
  
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/jquery-3.1.1.min.js"></script>
  <script type="text/javascript">
  
	var ncaaData;
	var regionalWinners = [];
	var defaultYear = '17';
		  
	// Shorthand for $( document ).ready()
	$( function() {
		  
		fillBracket(defaultYear, 'getHistorical');
		
		var changeFunc = function() {
							if ($( "#hist_or_predict_dropdown" ).val() == "hist") {
								fillBracket( $( "select#year_dropdown option:checked" ).val(), 'getHistorical' );
							}
							else {
								fillBracket( $( "select#year_dropdown option:checked" ).val(), 'getPredictions' );
							}
						 };
		
		$( "#year_dropdown, #hist_or_predict_dropdown" ).change(changeFunc);
		
		$( "#refreshButton" ).click(changeFunc);
		
	});
  	  
	function fillBracket(year, url) {
		
		$.ajax({
			url: url,  //'getHistorical',
			
					type: 'POST',
					cache: false,
					data:{  year: year
					},
					beforeSend: function( xhr ) {
						
					},
					success: function (data) {
						
						ncaaData = jQuery.parseJSON(data);
						console.log(ncaaData.length);
						maxDate = ncaaData.splice(-1, 1);
						datesData = ncaaData.splice(-1, 1);
						
						if (ncaaData.length < 66) {
							clearOutRounds();
						}
						
						var $select = $('#year_dropdown');
						if ($('#year_dropdown').children('option').length == 0) {
							for (i=maxDate[0].max_date; i > 1984; i--) {
								var selected = (i.toString().substring(2, 4) == defaultYear) ? 'selected' : '';
								$select.append('<option value="' + i.toString().substring(2, 4) + '" ' + selected + '>' + i + '</option>');
							}
						}
						
						//if (year == defaultYear) { clearOutRounds(); }
						
						$(".round_1_date").html(datesData[0]["Round of 64"]);
						$(".round_2_date").html(datesData[0]["Round of 32"]);
						$(".round_3_date").html(datesData[0]["Sweet Sixteen"]);
						$(".round_4_date").html(datesData[0]["Elite Eight"]);
						$(".round_5_date").html(datesData[0]["National Semifinals"]);
						$(".round_6_date").html(datesData[0]["National Championship"]);
						
						//console.log(datesData);
						//console.log(ncaaData);
						regionLocation = ncaaData.splice(-1, 1);
						//console.log(regionLocation);
						
						// flag so that bracket isn't filled with incomplete data.
						// if sweet sixteen data is blank, bracket will ignore 
						// elite eight, final four, championship.
						// also, keep in mind, this works because the JSON data is in round order.
						var sweetSixteen = false;
						
						$.each( ncaaData, function( i, game ) { 
							
							//var lowSeed = Math.min(game.winning_seed, game.losing_seed);
							
							$( 'h4.region1.first_region' ).text(regionLocation[0].topLeft);
					    	$( 'h4.region2.first_region' ).text(regionLocation[0].bottomLeft);
					    	$( 'h4.region3.first_region' ).text(regionLocation[0].topRight);
					    	$( 'h4.region4.first_region' ).text(regionLocation[0].bottomRight);
							
					    	var region;
					    	
							// find the region for the game
							switch(game.region) {
						    case regionLocation[0].topLeft:
						    	//console.log("setting topLeft to " + game.region);
						   		region = '1';
						    	//console.log("checking topLeft " + game.winning_seed);
						        break;
						    case regionLocation[0].bottomLeft:
						    	//console.log("setting bottomLeft to " + game.region);
						   		region = '2';
						        break;
						    case regionLocation[0].topRight:
						    	//console.log("setting topRight to " + game.region);
						   		region = '3';
						        break;
						    case regionLocation[0].bottomRight:
						    	//console.log("setting bottomRight to " + game.region);
						   		region = '4';
						        break;
							}
							
					    	var gameNum = findMatchNumber(game);
					    	//console.log("gameNum: " + gameNum);
					    	if (game.round == "Round of 64") {
					    		// winner was favored - top half of bracket
					    		if (parseInt(game.winning_seed, 10) < parseInt(game.losing_seed, 10)) {
					    			htmlString1 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    			htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    		}
					    		else {
					    			htmlString1 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    			htmlString2 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    		}
					    		var round = '1';
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot1").html( htmlString1 );
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot2").html( htmlString2 );
					    	}
					    	else if (game.round == "Round of 32") {
					    		var topSeeds = [1, 16, 5, 12, 6, 11, 7, 10];
					    		if (topSeeds.includes(game.winning_seed)) {
					    			//console.log("putting " + game.winner + " on top. seed: " + game.winning_seed + ".");
					    			htmlString1 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    			htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    		}
					    		else {
					    			htmlString1 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    			htmlString2 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    		}
					    		var round = '2';
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot1").html( htmlString1 );
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot2").html( htmlString2 );
					    	}
					    	else if (game.round == "Sweet Sixteen") {
					    		sweetSixteen = true;
					    		var topSeeds = [1, 16, 8, 9, 6, 11, 3, 14];
					    		if (topSeeds.includes(game.winning_seed)) {
					    			//console.log("putting " + game.winner + " on top. seed: " + game.winning_seed + ".");
					    			htmlString1 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    			htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    		}
					    		else {
					    			htmlString1 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    			htmlString2 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    		}
					    		var round = '3';
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot1").html( htmlString1 );
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot2").html( htmlString2 );
					    	}
					    	else if (game.round == "Elite Eight" && sweetSixteen) {
					    		var topSeeds = [1, 16, 8, 9, 5, 12, 4, 13];
					    		if (topSeeds.includes(game.winning_seed)) {
					    			//console.log("putting " + game.winner + " on top. seed: " + game.winning_seed + ".");
					    			htmlString1 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    			htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    		}
					    		else {
					    			htmlString1 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    			htmlString2 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    		}
					    		var round = '4';
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot1").html( htmlString1 );
					    		$('#round' + round).children("div.region.region" + region).children("div").filter(".match." + gameNum).children("p").filter(".slot.slot2").html( htmlString2 );
					    		
					    		// set the Final Four Teams
					    		regionalWinners[parseInt(region)-1] = game.winner;
					    	}
					    	else if (game.round == "National Semifinals" && sweetSixteen) {
					    		var round = '5';
					    		//console.log('Trying to fill National Semifinals.');
					    		//console.log( $('#round' + round).children("div.region").children("div").filter(".match." + gameNum) );
					    		if (regionalWinners[0] == game.winner || regionalWinners[2] == game.winner) {
					    			htmlString1 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    			htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    		}
					    		else if (regionalWinners[1] == game.winner || regionalWinners[3] == game.winner) {
					    			htmlString1 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
					    			htmlString2 = "<span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em>";
					    		}
					    		$('#round' + round).children("div.region").children("div").filter(".match." + gameNum).children("p").filter(".slot.slot1").html( htmlString1 );
					    		$('#round' + round).children("div.region").children("div").filter(".match." + gameNum).children("p").filter(".slot.slot2").html( htmlString2 );
					    	}
							
						});
		
						//console.log( $('.slot.slot1') );
						
					}
		});

	}
	
	function clearOutRounds() {
		console.log( $( ".slot.slot1" ) );
		$( ".slot.slot1" ).each(function (i, v) {
			$(this).text('');
		});		
		$( ".slot.slot2" ).each(function (i, v) {
			$(this).text('');
		});		
	}
	
	function findMatchNumber(game) {
		switch(game.round) {
	    case "Round of 64":
	    	if (game.winning_seed == '1' || game.winning_seed == '16') return 'm1';
	    	if (game.winning_seed == '8' || game.winning_seed ==  '9') return 'm2';
	    	if (game.winning_seed == '5' || game.winning_seed == '12') return 'm3';
	    	if (game.winning_seed == '4' || game.winning_seed == '13') return 'm4';
	    	if (game.winning_seed == '6' || game.winning_seed == '11') return 'm5';
	    	if (game.winning_seed == '3' || game.winning_seed == '14') return 'm6';
	    	if (game.winning_seed == '7' || game.winning_seed == '10') return 'm7';
	    	if (game.winning_seed == '2' || game.winning_seed == '15') return 'm8';
	        break;
	    case "Round of 32":
	    	if (game.winning_seed == '1' || game.winning_seed == '16' || game.winning_seed == '8' || game.winning_seed ==  '9') return 'm1';
	    	if (game.winning_seed == '5' || game.winning_seed == '12' || game.winning_seed == '4' || game.winning_seed == '13') return 'm2';
	    	if (game.winning_seed == '6' || game.winning_seed == '11' || game.winning_seed == '3' || game.winning_seed == '14') return 'm3';
	    	if (game.winning_seed == '7' || game.winning_seed == '10' || game.winning_seed == '2' || game.winning_seed == '15') return 'm4';
	    	//console.log("setting bottomLeft to " + game.region);
	        break;
	    case "Sweet Sixteen":
	    	if (game.winning_seed == '1' || game.winning_seed == '16' || game.winning_seed == '8' || game.winning_seed ==  '9') return 'm1';
	    	if (game.winning_seed == '5' || game.winning_seed == '12' || game.winning_seed == '4' || game.winning_seed == '13') return 'm1';
	    	if (game.winning_seed == '6' || game.winning_seed == '11' || game.winning_seed == '3' || game.winning_seed == '14') return 'm2';
	    	if (game.winning_seed == '7' || game.winning_seed == '10' || game.winning_seed == '2' || game.winning_seed == '15') return 'm2';
	    	//console.log("setting bottomLeft to " + game.region);
	        break;
	    case "Elite Eight":
	    	return 'm1';
	    	break;
	    case "National Semifinals":
	    	//console.log("National Semifinals. game.winner: " + game.winner + ", regionalWinners: " + regionalWinners[0] + ", " + regionalWinners[1] + ", " + regionalWinners[2] + ", " + regionalWinners[3]);
	    	if (game.winner == regionalWinners[0] || game.winner == regionalWinners[1]) return 'm1';
	    	else return 'm2';
	    	break;
	    case "National Championship":
	    	if (game.winning_seed > 0) {
				htmlString1 = "<strong><span class='seed'>" + game.winning_seed + "</span> " + game.winner + " <em class='score'>" + game.winning_score + "</em></strong>";
				htmlString2 = "<strike><span class='seed'>" + game.losing_seed + "</span> " + game.loser + "</strike> <em class='score'>" + game.losing_score + "</em>";
	    		$('#slot127').html( htmlString1 );
	    		$('#slot128').html( htmlString2 );
	    	}
	    	break;
		}
	}
	
	</script>
  	  
</head>
<body class="bracket standings light-blue">
<div id="content-wrapper">
  <div id="table">

<!-- Table Dates -->
    <table style="margin:auto;">
      <tr>
        <td style="margin:auto;text-align:center;">
        <Select style="vertical-align:text-bottom;" id="hist_or_predict_dropdown"><option value="hist">Historical</option><option value="pred">Predictor</option></Select>
        &nbsp;&nbsp;
        <Select style="vertical-align:text-bottom;" id="year_dropdown"></Select>
        &nbsp;
        <img id="refreshButton" src="<%=request.getContextPath() %>/images/refresh.png" align="bottom" style="cursor:pointer;border-radius:1px;vertical-align:text-bottom;height:17px;border-color:rgba(170,170,170,1);border-style:solid;border-width:1px;border-collapse:collapse;"/>
        <br/><br/>
        <li style="font-size:12px;width:50%;margin:auto;">In predictor mode, only historical seed records are used and "wins" represents performance of one seed over the other.</li> 
        <br/>
        </td>
      </tr>
	</table>
    <table class="gridtable">
      <tr>
        <th class="round_1 current"> 1st ROUND </th>
        <th class="round_2 "> 2nd ROUND </th>
        <th class="round_3"> SWEET 16 </th>
        <th class="round_4"> ELITE EIGHT </th>
        <th class="round5"> FINAL FOUR </th>
        <th class="round_6"> CHAMPION </th>
        <th class="round_5"> FINAL FOUR </th>
        <th class="round_4"> ELITE EIGHT </th>
        <th class="round_3"> SWEET 16 </th>
        <th class="round_2"> 2nd ROUND </th>
        <th class="round_1 current"> 1st ROUND </th>
      </tr>
      <tr>
        <td class="round_1_date"> March 18-19 </td>
        <td class="round_2_date"> March 20-21 </td>
        <td class="round_3_date"> March 25-26 </td>
        <td class="round_4_date"> March 27-28 </td>
        <td class="round_5_date"> April 3 </td>
        <td class="round_6_date"> April 5 </td>
        <td class="round_5_date"> April 3 </td>
        <td class="round_4_date"> March 27-28 </td>
        <td class="round_3_date"> March 25-26 </td>
        <td class="round_2_date"> March 20-21 </td>
        <td class="round_1_date"> March 18-19 </td>
      </tr>
    </table>
  </div>

<!-- Bracket -->
  <div id="bracket">
    <div id="round1" class="round">
      <h3>
        Round One (NCAA Men's Basketball Tournament) 
      </h3>

<!-- start region1 -->
      <div class="region region1">
        <h4 class="region1 first_region">
          MIDWEST 
        </h4>
        <!-- id="match18" --> <div class="match m1">
          <p class="slot slot1">
            <!-- strike--><span class="seed">1</span>  <em class="score">0</em><!-- /strike--> 
          </p>
          <p class="slot slot2">
            <!-- strong --><span class="seed">16</span>  <em class="score">0</em><!-- /strong--> 
          </p>
        </div>

<!-- /#match18 -->
        <div id="match19" class="match m2">
          <p class="slot slot1">
            <span class="seed">8</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">9</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match19 -->
        <div id="match20" class="match m3">
          <p class="slot slot1">
            <span class="seed">5</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">12</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match20 -->
        <div id="match21" class="match m4">
          <p class="slot slot1">
            <span class="seed">4</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">13</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match21 -->
        <div id="match22" class="match m5">
          <p class="slot slot1">
            <span class="seed">6</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">11</span>  <em class="score">0</em> 
          </p>
        </div>
        <div id="match23" class="match m6">
          <p class="slot slot1">
            <span class="seed">3</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">14</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match23 -->
        <div id="match24" class="match m7">
          <p class="slot slot1">
            <span class="seed">7</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">10</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match24 -->
        <div id="match25" class="match m8">
          <p class="slot slot1">
            <span class="seed">2</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">15</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match25 -->
      </div>
<!-- /.region1 -->
<!-- start region2 -->
      <div class="region region2">
        <h4 class="region2 first_region">
          West 
        </h4>
        <div id="match26" class="match m1">
          <p class="slot slot1">
            <span class="seed">1</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">16</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match26 -->
        <div id="match27" class="match m2">
          <p class="slot slot1">
            <span class="seed">8</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">9</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- #/match27 -->
        <div id="match28" class="match m3">
          <p class="slot slot1">
            <span class="seed">5</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">12</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- #/match28 -->
        <div id="match29" class="match m4">
          <p class="slot slot1">
            <span class="seed">4</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">13</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- #/match29 -->
        <div id="match30" class="match m5">
          <p class="slot slot1">
            <span class="seed">6</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">11</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match30 -->
        <div id="match31" class="match m6">
          <p class="slot slot1">
            <span class="seed">3</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">14</span>  <em class="score">0</em> 
          </p>
        </div>

<!--/#match31-->
        <div id="match32" class="match m7">
          <p class="slot slot1">
            <span class="seed">7</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">10</span>  <em class="score">0</em> 
          </p>
        </div>

<!--/#match32-->
        <div id="match33" class="match m8">
          <p class="slot slot1">
            <span class="seed">2</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">15</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match33 -->
      </div>
<!-- /.region2 -->
<!-- start region3 -->
      <div class="region region3">
        <h4 class="region3 first_region">
          EAST 
        </h4>
        <div id="match65" class="match m1">
          <p class="slot slot1">
            <span class="seed">1</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">16</span> <em class="score">0</em> 
          </p>
        </div>

<!--#/match65-->
        <div id="match66" class="match m2">
          <p class="slot slot1">
            <span class="seed">8</span><em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">9</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- #/match66 -->
        <div id="match4" class="match m3">
          <p class="slot slot1">
            <span class="seed">5</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">12</span>  <em class="score">0</em> 
          </p>
        </div>

<!--/#match4 -->
        <div id="match5" class="match m4">
          <p class="slot slot1">
            <span class="seed">4</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">13</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match5 -->
        <div id="match6" class="match m5">
          <p class="slot slot1">
            <span class="seed">6</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">11</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match6 -->
        <div id="match7" class="match m6">
          <p class="slot slot1">
            <span class="seed">3</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">14</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match7 -->
        <div id="match8" class="match m7">
          <p class="slot slot1">
            <span class="seed">7</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">10</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match8 -->
        <div id="match9" class="match m8">
          <p class="slot slot1">
            <span class="seed">2</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">15</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match9 -->
      </div>
<!-- /.region3 -->
<!-- start region4 -->
      <div class="region region4">
        <h4 class="region4 first_region">
          South 
        </h4>
        <div id="match10" class="match m1">
          <p class="slot slot1">
            <span class="seed">1</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">16</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match10 -->
        <div id="match11" class="match m2">
          <p class="slot slot1">
            <span class="seed">8</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">9</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match11 -->
        <div id="match12" class="match m3">
          <p class="slot slot1">
            <span class="seed">5</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">12</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match12 -->
        <div id="match13" class="match m4">
          <p class="slot slot1">
            <span class="seed">4</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">13</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match13 -->
        <div id="match14" class="match m5">
          <p class="slot slot1">
            <span class="seed">6</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">11</span>  <em class="score">0</em> 
          </p>
        </div>

<!-- /#match14 -->
        <div id="match15" class="match m6">
          <p class="slot slot1">
            <span class="seed">3</span>  <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">14</span> <em class="score">0</em> 
          </p>
        </div>
        <div id="match16" class="match m7">
          <p class="slot slot1">
            <span class="seed">7</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">10</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match16 -->
        <div id="match17" class="match m8">
          <p class="slot slot1">
            <span class="seed">2</span> <em class="score">0</em> 
          </p>
          <p class="slot slot2">
            <span class="seed">15</span> <em class="score">0</em> 
          </p>
        </div>

<!-- /#match17 -->
      </div>
<!-- /.region4 -->
    </div>
<!-- /#round1 -->
    <div id="round2" class="round">
      <h3>
        Round Two (NCAA Men's Basketball Tournament) 
      </h3>
      <div class="region region1">
        <h4 class="region1">
          MIDWEST 
        </h4>
        <div id="match41" class="match m1">
          <p rel="match18" class="slot slot1">
            <span class="seed"></span> <em class="score"></em> 
          </p>
          <p rel="match19" class="slot slot2">
            <span class="seed"></span> <em class="score"></em> 
          </p>
        </div>

<!-- /#match41 -->
        <div id="match42" class="match m2">
          <p rel="match20" class="slot slot1">
          </p>
          <p rel="match21" class="slot slot2">
          </p>
        </div>
<!-- /#match42 -->
        <div id="match43" class="match m3">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- /#match43 -->
        <div id="match44" class="match m4">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- /#match44 -->
      </div>
<!-- /.region1 -->
      <div class="region region2">
        <h4 class="region2">
          WEST 
        </h4>
        <div id="match45" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
        <div id="match46" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!-- /#match46 -->
        <div id="match47" class="match m3">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
        <div id="match48" class="match m4">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- /#match48 -->
      </div>
      <div class="region region3">
        <h4 class="region3">
          East 
        </h4>
        <div id="match3" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/match3-->
        <div id="match34" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- #/match34 -->
        <div id="match35" class="match m3">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- #/match35 -->
        <div id="match36" class="match m4">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!-- #/match36 -->
      </div>
<!--/.region3-->
      <div class="region region4">
        <h4 class="region4">
          South 
        </h4>
        <div id="match37" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/#match37-->
        <div id="match38" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!--/#match38-->
        <div id="match39" class="match m3">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!--/#match39-->
        <div id="match40" class="match m4">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!--/#match40-->
      </div>
<!--/.region4-->
    </div>
    <div id="round3" class="round">
      <h3>
        Round Three (NCAA Men's Basketball Tournament) 
      </h3>
      <div class="region region1">
        <h4 class="region1">
          Midwest 
        </h4>
        <div id="match53" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
        <div id="match54" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
      </div>

<!-- /.region1 -->
      <div class="region region2">
        <h4 class="region2">
          West 
        </h4>
        <div id="match55" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
        <div id="match56" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/#match56-->
      </div>
      <div class="region region3">
        <h4 class="region3">
          East 
        </h4>
        <div id="match49" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/#match49-->
        <div id="match50" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!--/#match50-->
      </div>
<!-- /.region3 -->
      <div class="region region4">
        <h4 class="region4">
          South 
        </h4>
        <div id="match51" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/#match51-->
        <div id="match52" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
<!--/#match52-->
      </div>
<!-- /.region4 -->
    </div>
    <div id="round4" class="round">
      <h3>
        Round Four (NCAA Men's Basketball Tournament) 
      </h3>
      <div class="region region1">
        <h4 class="region1">
          Midwest 
        </h4>
        <div id="match60" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!-- /#match60 -->
      </div>
<!-- /.region1 -->
      <div class="region region2">
        <h4 class="region2">
          West 
        </h4>
        <div id="match61" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!-- /#match61 -->
      </div>
<!-- /.region2 -->
      <div class="region region3">
        <h4 class="region3">
          East 
        </h4>
        <div id="match58" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!-- /#match58 -->
      </div>
<!--/.region3-->
      <div class="region region4">
        <h4 class="region4">
          South 
        </h4>
        <div id="match59" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>

<!--/#match59-->
      </div>
<!--/#match59-->
    </div>
    <div id="round5" class="round">
      <h3>
        Round Five (NCAA Men's Basketball Tournament) 
      </h3>
      <div class="region">
        <div id="match63" class="match m1">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
        <div id="match62" class="match m2">
          <p class="slot slot1">
          </p>
          <p class="slot slot2">
          </p>
        </div>
      </div>
    </div>
    <div id="round6" class="round">
      <h3>
        Round Six (NCAA Men's Basketball Tournament) 
      </h3>
      <div class="region">
        <div id="match64" class="match m1">
          <p class="slot slot1" id="slot127">
            <strong><span class="seed"></span> <em class="score"></em></strong>
<!-- winner -->
          </p>
          <p class="slot slot2" id="slot128">
            <strike><span class="seed"></span> <em class="score"></em></strike>
<!-- loser -->
          </p>
        </div>
      </div>
    </div>
  </div>
</div>
<div style="margin:auto;width:100%;text-align:center;font-size:11px;">
	Most of the HTML and CSS taken from here:<br/> 
	<a href="https://github.com/jasonmelgoza/NCAA-Bracket" target="_blank">https://github.com/jasonmelgoza/NCAA-Bracket</a><br/><br/>
	Most of the data was taken from here:<br/>
	<a href="https://data.world/sports/ncaa-mens-march-madness" target="_blank">https://data.world/sports/ncaa-mens-march-madness</a><br/><br/>
	Refresh icon taken from here:<br/>
	<a href="https://www.iconfinder.com/nastu_bol" target="_blank">https://www.iconfinder.com/nastu_bol</a><br/><br/>
	Basketball favicon taken from here:<br/>
	<a href="https://www.iconfinder.com/inipagi" target="_blank">https://www.iconfinder.com/inipagi</a><br/><br/>
	</div>
</body>
</html>