package com.ncaa.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;
import java.util.concurrent.ThreadLocalRandom;

public class DataDaoImpl implements DataDao {
	
	public DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		System.out.println("HEEEE " + dataSource.toString());
		this.dataSource = dataSource;
	}
	
	public JSONObject getDates(String year) {
		Connection conn = null;
		JSONObject datesJSONObj = new JSONObject();
		String lastRound = "";
		
		try {
			
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement("select distinct date, round from ncaa where substr(date, -2) like ? and round <> 'Opening Round' order by round, date");
			ps.setString(1, year);
			ResultSet rs = ps.executeQuery();
				
			while (rs.next()) {
					
				if (!lastRound.equals(rs.getString("round"))) {
					String dateText = (rs.getString("date").substring(0, 1).equals("3")) ? "March " + rs.getString("date").substring(rs.getString("date").indexOf("/")+1, rs.getString("date").indexOf("/", 2)) : "April " + rs.getString("date").substring(rs.getString("date").indexOf("/")+1, rs.getString("date").indexOf("/", 2));
					datesJSONObj.put(rs.getString("round"), dateText);
				}
				else /*if (lastRound.equals(rs.getString("round")))*/ {
					String dateText = datesJSONObj.getString(rs.getString("round")) + "-" + rs.getString("date").substring(2, 4);
					datesJSONObj.put(rs.getString("round"), dateText);
				}
				
				lastRound = rs.getString("round");
			
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		//System.out.println(datesJSONObj);
		
		return datesJSONObj;
	}
	
	public JSONArray getHistorical(String year) {
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement("select * from ncaa where substr(date, -2) like ? order by date");
			ps.setString(1, year);
			ResultSet rs = ps.executeQuery();
			
			HashMap<String, String> regionalWinners = new HashMap<String, String>();
			String[] regions = new String[4];
			int c=0;
			JSONObject regionsJSONObj = new JSONObject();
			
			while (rs.next()) {
				
				JSONObject projJSONObj = new JSONObject();
				
				projJSONObj.put("date", rs.getString("date"));
				projJSONObj.put("round", rs.getString("round"));
				projJSONObj.put("region", rs.getString("region"));
				projJSONObj.put("winning_seed", rs.getInt("winning_seed"));
				projJSONObj.put("winner", rs.getString("winner"));
				projJSONObj.put("winning_score", rs.getInt("winning_score"));
				projJSONObj.put("losing_seed", rs.getInt("losing_seed"));
				projJSONObj.put("loser", rs.getString("loser"));
				projJSONObj.put("losing_score", rs.getInt("losing_score"));
				projJSONObj.put("overtime", rs.getString("overtime"));
				
				arrayJSON.put(projJSONObj);
				
				if (rs.getString("round").equals("Elite Eight")) {
					regionalWinners.put(rs.getString("winner"), rs.getString("region"));
				}
				else if (rs.getString("round").equals("National Semifinals")) {
					regions[c] = regionalWinners.get(rs.getString("winner")); c++;
					regions[c] = regionalWinners.get(rs.getString("loser")); c++;
					if (c == 4) {
						regionsJSONObj.put("topLeft", regions[0]);
						regionsJSONObj.put("bottomLeft", regions[1]);
						regionsJSONObj.put("topRight", regions[2]);
						regionsJSONObj.put("bottomRight", regions[3]);
					}
				}
			}
			
			arrayJSON.put(regionsJSONObj);
			arrayJSON.put(getDates(year));
			arrayJSON.put(getMaxDate());
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		return arrayJSON;
	}
	
	public JSONArray getPredictions(String year) {
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();

		try {
			conn = dataSource.getConnection();
			// DO NOT USE ORDER BY
			// NEEDS CHRONOLOGICAL ORDER BECAUE OF ORDERING OF ROUNDS AND DETERMINING REGIONS
			PreparedStatement ps = conn.prepareStatement("select * from ncaa where substr(date, -2) like ? order by date");
			ps.setString(1, year);
			ResultSet rs = ps.executeQuery();
			
			HashMap<String, String> regionalWinners = new HashMap<String, String>();
			String[] regions = new String[4];
			int c=0;
			JSONObject regionsJSONObj = new JSONObject();
			
			// create HashMaps from rank to team name
			// this should help down the line with using sets and ranks 
			HashMap<String, HashMap<Integer, String>> regionToTeamRankMap = new HashMap<String, HashMap<Integer, String>>();
			
			while (rs.next()) {
				
				if (rs.getString("round").equals("Round of 64")) {
					
					HashMap<Integer, String> teamToRankMap = regionToTeamRankMap.containsKey(rs.getString("region")) ? regionToTeamRankMap.get(rs.getString("region")) : new HashMap<Integer, String>();
					teamToRankMap.put(rs.getInt("winning_seed"), rs.getString("winner"));
					teamToRankMap.put(rs.getInt("losing_seed"), rs.getString("loser"));
					regionToTeamRankMap.put(rs.getString("region"), teamToRankMap);
					
					int[] winning_info = getWinner(rs.getInt("winning_seed"), rs.getInt("losing_seed"), rs.getString("round"));
					
					JSONObject projJSONObj = new JSONObject();

					if (winning_info[2] == rs.getInt("winning_seed")) {
						projJSONObj.put("date", rs.getString("date"));
						projJSONObj.put("round", rs.getString("round"));
						projJSONObj.put("region", rs.getString("region"));
						projJSONObj.put("winning_seed", rs.getInt("winning_seed"));
						projJSONObj.put("winner", rs.getString("winner"));
						projJSONObj.put("winning_score", winning_info[0] + " wins");
						projJSONObj.put("losing_seed", rs.getInt("losing_seed"));
						projJSONObj.put("loser", rs.getString("loser"));
						projJSONObj.put("losing_score", winning_info[1] + " wins");
						projJSONObj.put("overtime", rs.getString("overtime"));
					}
					else {
						//System.out.println("Upset!");
						//System.out.println("new winner: " + rs.getString("loser") + ", new loser: " + rs.getString("winner"));
						
						projJSONObj.put("date", rs.getString("date"));
						projJSONObj.put("round", rs.getString("round"));
						projJSONObj.put("region", rs.getString("region"));
						projJSONObj.put("winning_seed", rs.getInt("losing_seed"));
						projJSONObj.put("winner", rs.getString("loser"));
						projJSONObj.put("winning_score", winning_info[1] + " wins");
						projJSONObj.put("losing_seed", rs.getInt("winning_seed"));
						projJSONObj.put("loser", rs.getString("winner"));
						projJSONObj.put("losing_score", winning_info[0] + " wins");
						projJSONObj.put("overtime", rs.getString("overtime"));
					}
					
					arrayJSON.put(projJSONObj);
					
				}
				else if (rs.getString("round").equals("Elite Eight")) {
					regionalWinners.put(rs.getString("winner"), rs.getString("region"));
					//System.out.println("Adding regional winner: " + rs.getString("winner") + " region: " + rs.getString("region"));
				}
				else if (rs.getString("round").equals("National Semifinals")) {
					regions[c] = regionalWinners.get(rs.getString("winner")); c++;
					regions[c] = regionalWinners.get(rs.getString("loser")); c++;
					if (c == 4) {
						regionsJSONObj.put("topLeft", regions[0]);
						regionsJSONObj.put("bottomLeft", regions[1]);
						regionsJSONObj.put("topRight", regions[2]);
						regionsJSONObj.put("bottomRight", regions[3]);
					}
				}
			}
			
			Set<Integer> set1 = new HashSet<Integer>(Arrays.asList(1,  8, 9, 16));
			Set<Integer> set2 = new HashSet<Integer>(Arrays.asList(2, 15, 7, 10));
			Set<Integer> set3 = new HashSet<Integer>(Arrays.asList(5, 12, 4, 13));
			Set<Integer> set4 = new HashSet<Integer>(Arrays.asList(6, 11, 3, 14));
			
			int t = arrayJSON.length();
			
			// list of all second round games already played
			// format - "East12" (region + seed)
			HashSet<String> alreadyPlayed = new HashSet<String>();
			
			// First Round Winners have been determined
			// Need to determine Second Round winners
			for (int i = 0; i < t; i++) {
				JSONObject firstRoundWinnerJSONObj = arrayJSON.getJSONObject(i);
				String region = firstRoundWinnerJSONObj.getString("region");
				int seed = firstRoundWinnerJSONObj.getInt("winning_seed");
				String alreadyPlayedString = firstRoundWinnerJSONObj.getString("region") + seed;
				
				//System.out.println("trying to match: " + seed + " and region: " + region);
				
				int j = 0;
				// can be optimized to ignore a team for future loops once it has been matched
				// attempted with alreadyPlayed HashSet
				while (j < t) {
					JSONObject firstRoundMatchWinnerJSONObj = arrayJSON.getJSONObject(j);
					String alreadyPlayedMatchString = firstRoundMatchWinnerJSONObj.getString("region") + firstRoundMatchWinnerJSONObj.getInt("winning_seed");
					if (region.equals(firstRoundMatchWinnerJSONObj.getString("region")) && seed != firstRoundMatchWinnerJSONObj.getInt("winning_seed") && !alreadyPlayed.contains(alreadyPlayedString) && !alreadyPlayed.contains(alreadyPlayedMatchString)) {
						
						//System.out.println("comparing " + firstRoundWinnerJSONObj.getString("winner") + " with " + firstRoundMatchWinnerJSONObj.getString("winner") + " alreadyPlayedString is " + alreadyPlayedString);
						
						JSONObject secondRoundWinner = new JSONObject();
						int[] winningSeed = null;
						boolean found = false;
						
						if (set1.contains(firstRoundMatchWinnerJSONObj.getInt("winning_seed")) && set1.contains(seed)) {
							//System.out.println("Second round matchup: " + seed + " " + firstRoundWinnerJSONObj.getString("winner") + " vs " + firstRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + firstRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, firstRoundMatchWinnerJSONObj.getInt("winning_seed"), "Round of 32");
							//System.out.println("Second round winner: " + winningSeed);
							alreadyPlayed.add(firstRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(firstRoundMatchWinnerJSONObj.getString("region") + firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						if (set2.contains(firstRoundMatchWinnerJSONObj.getInt("winning_seed")) && set2.contains(seed)) {
							//System.out.println("Second round matchup: " + seed + " " + firstRoundWinnerJSONObj.getString("winner") + " vs " + firstRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + firstRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, firstRoundMatchWinnerJSONObj.getInt("winning_seed"), "Round of 32");
							//System.out.println("Second round winner: " + winningSeed);
							alreadyPlayed.add(firstRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(firstRoundMatchWinnerJSONObj.getString("region") + firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						if (set3.contains(firstRoundMatchWinnerJSONObj.getInt("winning_seed")) && set3.contains(seed)) {
							//System.out.println("Second round matchup: " + seed + " " + firstRoundWinnerJSONObj.getString("winner") + " vs " + firstRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + firstRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, firstRoundMatchWinnerJSONObj.getInt("winning_seed"), "Round of 32");
							//System.out.println("Second round winner: " + winningSeed);
							alreadyPlayed.add(firstRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(firstRoundMatchWinnerJSONObj.getString("region") + firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						if (set4.contains(firstRoundMatchWinnerJSONObj.getInt("winning_seed")) && set4.contains(seed)) {
							//System.out.println("Second round matchup: " + seed + " " + firstRoundWinnerJSONObj.getString("winner") + " vs " + firstRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + firstRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, firstRoundMatchWinnerJSONObj.getInt("winning_seed"), "Round of 32");
							//System.out.println("Second round winner: " + winningSeed);
							alreadyPlayed.add(firstRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(firstRoundMatchWinnerJSONObj.getString("region") + firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						
						if (found) {
							if (winningSeed[2] == seed) {
								secondRoundWinner.put("winning_seed", seed);
								secondRoundWinner.put("winner", firstRoundWinnerJSONObj.getString("winner"));
								secondRoundWinner.put("losing_seed", firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
								secondRoundWinner.put("loser", firstRoundMatchWinnerJSONObj.getString("winner"));
								secondRoundWinner.put("winning_score", winningSeed[0] + " wins");
								secondRoundWinner.put("losing_score", winningSeed[1] + " wins");
							}
							else {
								secondRoundWinner.put("winning_seed", firstRoundMatchWinnerJSONObj.getInt("winning_seed"));
								secondRoundWinner.put("winner", firstRoundMatchWinnerJSONObj.getString("winner"));
								secondRoundWinner.put("losing_seed", seed);
								secondRoundWinner.put("loser", firstRoundWinnerJSONObj.getString("winner"));
								secondRoundWinner.put("winning_score", winningSeed[1] + " wins");
								secondRoundWinner.put("losing_score", winningSeed[0] + " wins");
								//System.out.println("Changed history: " + firstRoundMatchWinnerJSONObj.getString("winner"));
							}
							
							secondRoundWinner.put("date", firstRoundWinnerJSONObj.getString("date"));
							secondRoundWinner.put("round", "Round of 32");
							secondRoundWinner.put("region", firstRoundWinnerJSONObj.getString("region"));
							secondRoundWinner.put("overtime", "");
							
							//System.out.println("Adding to jsonArray: " + firstRoundWinnerJSONObj.getString("winner") + ", " + firstRoundMatchWinnerJSONObj.getString("winner"));
							arrayJSON.put(secondRoundWinner);
						}
						
					}
					j++;
				}
			}
			
			set1 = new HashSet<Integer>(Arrays.asList(1, 4, 5, 8,  9, 12, 13, 16));
			set2 = new HashSet<Integer>(Arrays.asList(2, 3, 6, 7, 10, 11, 14, 15));
			
			alreadyPlayed.clear();
			
			int u = arrayJSON.length();
			// loop starting from array index of second round entries 
			for (int i = t; i < u; i++) {
				JSONObject secondRoundWinnerJSONObj = arrayJSON.getJSONObject(i);
				String region = secondRoundWinnerJSONObj.getString("region");
				int seed = secondRoundWinnerJSONObj.getInt("winning_seed");
				String alreadyPlayedString = secondRoundWinnerJSONObj.getString("region") + seed;
				
				//System.out.println("trying to match: " + seed + " and region: " + region);
				
				int j = t;
				// can be optimized to ignore a team for future loops once it has been matched
				// attempted with alreadyPlayed HashSet
				while (j < u) {
					JSONObject secondRoundMatchWinnerJSONObj = arrayJSON.getJSONObject(j);
					String alreadyPlayedMatchString = secondRoundMatchWinnerJSONObj.getString("region") + secondRoundMatchWinnerJSONObj.getInt("winning_seed");
					if (region.equals(secondRoundMatchWinnerJSONObj.getString("region")) && seed != secondRoundMatchWinnerJSONObj.getInt("winning_seed") && !alreadyPlayed.contains(alreadyPlayedString) && !alreadyPlayed.contains(alreadyPlayedMatchString)) {
						
						//System.out.println("comparing " + secondRoundWinnerJSONObj.getString("winner") + " with " + secondRoundMatchWinnerJSONObj.getString("winner") + " alreadyPlayedString is " + alreadyPlayedString);
						
						JSONObject thirdRoundWinner = new JSONObject();
						int[] winningSeed=null;
						boolean found = false;
						
						if (set1.contains(secondRoundMatchWinnerJSONObj.getInt("winning_seed")) && set1.contains(seed)) {
							//System.out.println("Third round matchup: " + seed + " " + secondRoundWinnerJSONObj.getString("winner") + " vs " + secondRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + secondRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, secondRoundMatchWinnerJSONObj.getInt("winning_seed"), "Sweet Sixteen");
							//System.out.println("Third round winner: " + winningSeed);
							alreadyPlayed.add(secondRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(secondRoundMatchWinnerJSONObj.getString("region") + secondRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						if (set2.contains(secondRoundMatchWinnerJSONObj.getInt("winning_seed")) && set2.contains(seed)) {
							//System.out.println("Third round matchup: " + seed + " " + secondRoundWinnerJSONObj.getString("winner") + " vs " + secondRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + secondRoundMatchWinnerJSONObj.getString("winner"));
							winningSeed = getWinner(seed, secondRoundMatchWinnerJSONObj.getInt("winning_seed"), "Sweet Sixteen");
							//System.out.println("Third round winner: " + winningSeed);
							alreadyPlayed.add(secondRoundWinnerJSONObj.getString("region") + seed);
							alreadyPlayed.add(secondRoundMatchWinnerJSONObj.getString("region") + secondRoundMatchWinnerJSONObj.getInt("winning_seed"));
							found = true;
						}
						
						if (found) {
							if (winningSeed[2] == seed) {
								thirdRoundWinner.put("winning_seed", seed);
								thirdRoundWinner.put("winner", secondRoundWinnerJSONObj.getString("winner"));
								thirdRoundWinner.put("losing_seed", secondRoundMatchWinnerJSONObj.getInt("winning_seed"));
								thirdRoundWinner.put("loser", secondRoundMatchWinnerJSONObj.getString("winner"));
								thirdRoundWinner.put("winning_score", winningSeed[0] + " wins");
								thirdRoundWinner.put("losing_score", winningSeed[1] + " wins");
							}
							else {
								thirdRoundWinner.put("winning_seed", secondRoundMatchWinnerJSONObj.getInt("winning_seed"));
								thirdRoundWinner.put("winner", secondRoundMatchWinnerJSONObj.getString("winner"));
								thirdRoundWinner.put("losing_seed", seed);
								thirdRoundWinner.put("loser", secondRoundWinnerJSONObj.getString("winner"));
								thirdRoundWinner.put("winning_score", winningSeed[1] + " wins");
								thirdRoundWinner.put("losing_score", winningSeed[0] + " wins");
							}
							
							thirdRoundWinner.put("date", secondRoundWinnerJSONObj.getString("date"));
							thirdRoundWinner.put("round", "Sweet Sixteen");
							thirdRoundWinner.put("region", secondRoundWinnerJSONObj.getString("region"));
							thirdRoundWinner.put("overtime", "");
							
							//System.out.println("Adding to jsonArray: " + secondRoundWinnerJSONObj.getString("winner") + ", " + secondRoundMatchWinnerJSONObj.getString("winner"));
							arrayJSON.put(thirdRoundWinner);
						}
						
					}
					j++;
				}
			}
			
			// Region -> arrayJSON index
			HashMap<String, Integer> finalFour = new HashMap<String, Integer>();
			
			alreadyPlayed.clear();
			
			int v = arrayJSON.length();
			// loop starting from array index of third round entries 
			for (int i = u; i < v; i++) {
				JSONObject thirdRoundWinnerJSONObj = arrayJSON.getJSONObject(i);
				String region = thirdRoundWinnerJSONObj.getString("region");
				int seed = thirdRoundWinnerJSONObj.getInt("winning_seed");
				String alreadyPlayedString = thirdRoundWinnerJSONObj.getString("region") + seed;
				
				//System.out.println("ELITE EIGHT trying to match: " + seed + " and region: " + region);
				
				int j = u;
				// can be optimized to ignore a team for future loops once it has been matched
				// attempted with alreadyPlayed HashSet
				while (j < v) {
					JSONObject thirdRoundMatchWinnerJSONObj = arrayJSON.getJSONObject(j);
					String alreadyPlayedMatchString = thirdRoundMatchWinnerJSONObj.getString("region") + thirdRoundMatchWinnerJSONObj.getInt("winning_seed");
					if (region.equals(thirdRoundMatchWinnerJSONObj.getString("region")) && seed != thirdRoundMatchWinnerJSONObj.getInt("winning_seed") && !alreadyPlayed.contains(alreadyPlayedString) && !alreadyPlayed.contains(alreadyPlayedMatchString)) {
						
						//System.out.println("comparing " + thirdRoundWinnerJSONObj.getString("winner") + " with " + thirdRoundMatchWinnerJSONObj.getString("winner") + " alreadyPlayedString is " + alreadyPlayedString);
						
						JSONObject fourthRoundWinner = new JSONObject();
						int[] winningSeed=null;
						boolean found = false;
						
						//System.out.println("Fourth round matchup: " + seed + " " + thirdRoundWinnerJSONObj.getString("winner") + " vs " + thirdRoundMatchWinnerJSONObj.getInt("winning_seed") + " " + thirdRoundMatchWinnerJSONObj.getString("winner"));
						winningSeed = getWinner(seed, thirdRoundMatchWinnerJSONObj.getInt("winning_seed"), "Elite Eight");
						//System.out.println("Fourth round winner: " + winningSeed);
						alreadyPlayed.add(thirdRoundWinnerJSONObj.getString("region") + seed);
						alreadyPlayed.add(thirdRoundMatchWinnerJSONObj.getString("region") + thirdRoundMatchWinnerJSONObj.getInt("winning_seed"));
						found = true;
						
						if (found) {
							if (winningSeed[2] == seed) {
								fourthRoundWinner.put("winning_seed", seed);
								fourthRoundWinner.put("winner", thirdRoundWinnerJSONObj.getString("winner"));
								fourthRoundWinner.put("losing_seed", thirdRoundMatchWinnerJSONObj.getInt("winning_seed"));
								fourthRoundWinner.put("loser", thirdRoundMatchWinnerJSONObj.getString("winner"));
								fourthRoundWinner.put("winning_score", winningSeed[0] + " wins");
								fourthRoundWinner.put("losing_score", winningSeed[1] + " wins");
							}
							else {
								fourthRoundWinner.put("winning_seed", thirdRoundMatchWinnerJSONObj.getInt("winning_seed"));
								fourthRoundWinner.put("winner", thirdRoundMatchWinnerJSONObj.getString("winner"));
								fourthRoundWinner.put("losing_seed", seed);
								fourthRoundWinner.put("loser", thirdRoundWinnerJSONObj.getString("winner"));
								fourthRoundWinner.put("winning_score", winningSeed[1] + " wins");
								fourthRoundWinner.put("losing_score", winningSeed[0] + " wins");
							}
							
							fourthRoundWinner.put("date", thirdRoundWinnerJSONObj.getString("date"));
							fourthRoundWinner.put("round", "Elite Eight");
							fourthRoundWinner.put("region", thirdRoundWinnerJSONObj.getString("region"));
							fourthRoundWinner.put("overtime", "");
							
							if (regionsJSONObj.get("topLeft").equals(thirdRoundWinnerJSONObj.getString("region")))
								finalFour.put("topLeft", arrayJSON.length());
							else if (regionsJSONObj.get("bottomLeft").equals(thirdRoundWinnerJSONObj.getString("region")))
								finalFour.put("bottomLeft", arrayJSON.length());
							else if (regionsJSONObj.get("topRight").equals(thirdRoundWinnerJSONObj.getString("region")))
								finalFour.put("topRight", arrayJSON.length());
							else if (regionsJSONObj.get("bottomRight").equals(thirdRoundWinnerJSONObj.getString("region")))
								finalFour.put("bottomRight", arrayJSON.length());
							
							//System.out.println("Adding to jsonArray: " + thirdRoundWinnerJSONObj.getString("winner") + ", " + thirdRoundMatchWinnerJSONObj.getString("winner"));
							arrayJSON.put(fourthRoundWinner);
						}
						
					}
					j++;
				}
			}
			
			//System.out.println("Final Four");
			//System.out.println("Final Four Map: " + finalFour);
			//System.out.println("Final Four Map topLeft winner: " + arrayJSON.get(finalFour.get("topLeft")));
			//System.out.println("Final Four Map bottomLeft winner: " + arrayJSON.get(finalFour.get("bottomLeft")));
			//System.out.println("Final Four Map topRight winner: " + arrayJSON.get(finalFour.get("topRight")));
			//System.out.println("Final Four Map bottomRight winner: " + arrayJSON.get(finalFour.get("bottomRight")));
			
			//JSONObject topLeftObj = new JSONObject(arrayJSON.getJSONObject(finalFour.get("topLeft")), JSONObject.getNames(arrayJSON.getJSONObject(finalFour.get("topLeft"))));
			//JSONObject bottomLeftObj = new JSONObject(arrayJSON.getJSONObject(finalFour.get("bottomLeft")), JSONObject.getNames(arrayJSON.getJSONObject(finalFour.get("bottomLeft"))));
			
			//System.out.println("topLeftObj: " + topLeftObj);
			
			JSONObject ffObj1 = new JSONObject();
			
			int[] winningSeed = getWinner(arrayJSON.getJSONObject(finalFour.get("topLeft")).getInt("winning_seed"), arrayJSON.getJSONObject(finalFour.get("bottomLeft")).getInt("winning_seed"), "National Semifinals");
			ffObj1.put("region", "National");
			ffObj1.put("round", "National Semifinals");
			if (winningSeed[2] == arrayJSON.getJSONObject(finalFour.get("topLeft")).getInt("winning_seed")) {
				ffObj1.put("winning_seed", arrayJSON.getJSONObject(finalFour.get("topLeft")).get("winning_seed"));
				ffObj1.put("winner", arrayJSON.getJSONObject(finalFour.get("topLeft")).get("winner"));
				ffObj1.put("losing_seed", arrayJSON.getJSONObject(finalFour.get("bottomLeft")).get("winning_seed"));
				ffObj1.put("loser", arrayJSON.getJSONObject(finalFour.get("bottomLeft")).get("winner"));
				ffObj1.put("winning_score", winningSeed[0] + " wins");
				ffObj1.put("losing_score", winningSeed[1] + " wins");
			}
			else {
				ffObj1.put("winning_seed", arrayJSON.getJSONObject(finalFour.get("bottomLeft")).get("winning_seed"));
				ffObj1.put("winner", arrayJSON.getJSONObject(finalFour.get("bottomLeft")).get("winner"));
				ffObj1.put("losing_seed", arrayJSON.getJSONObject(finalFour.get("topLeft")).get("winning_seed"));
				ffObj1.put("loser", arrayJSON.getJSONObject(finalFour.get("topLeft")).get("winner"));
				ffObj1.put("winning_score", winningSeed[1] + " wins");
				ffObj1.put("losing_score", winningSeed[0] + " wins");
			}
			arrayJSON.put(ffObj1);

			//JSONObject topRightObj = new JSONObject(arrayJSON.getJSONObject(finalFour.get("topRight")), JSONObject.getNames(arrayJSON.getJSONObject(finalFour.get("topRight"))));
			//JSONObject bottomRightObj = new JSONObject(arrayJSON.getJSONObject(finalFour.get("bottomRight")), JSONObject.getNames(arrayJSON.getJSONObject(finalFour.get("bottomRight"))));

			JSONObject ffObj2 = new JSONObject();
			
			winningSeed = getWinner(arrayJSON.getJSONObject(finalFour.get("topRight")).getInt("winning_seed"), arrayJSON.getJSONObject(finalFour.get("bottomRight")).getInt("winning_seed"), "National Semifinals");
			ffObj2.put("region", "National");
			ffObj2.put("round", "National Semifinals");
			if (winningSeed[2] == arrayJSON.getJSONObject(finalFour.get("topRight")).getInt("winning_seed")) {
				ffObj2.put("winning_seed", arrayJSON.getJSONObject(finalFour.get("topRight")).get("winning_seed"));
				ffObj2.put("winner", arrayJSON.getJSONObject(finalFour.get("topRight")).get("winner"));
				ffObj2.put("losing_seed", arrayJSON.getJSONObject(finalFour.get("bottomRight")).get("winning_seed"));
				ffObj2.put("loser", arrayJSON.getJSONObject(finalFour.get("bottomRight")).get("winner"));
				ffObj2.put("winning_score", winningSeed[0] + " wins");
				ffObj2.put("losing_score", winningSeed[1] + " wins");
				arrayJSON.put(ffObj2);
			}
			else {
				ffObj2.put("winning_seed", arrayJSON.getJSONObject(finalFour.get("bottomRight")).get("winning_seed"));
				ffObj2.put("winner", arrayJSON.getJSONObject(finalFour.get("bottomRight")).get("winner"));
				ffObj2.put("losing_seed", arrayJSON.getJSONObject(finalFour.get("topRight")).get("winning_seed"));
				ffObj2.put("loser", arrayJSON.getJSONObject(finalFour.get("topRight")).get("winner"));
				ffObj2.put("winning_score", winningSeed[1] + " wins");
				ffObj2.put("losing_score", winningSeed[0] + " wins");
				arrayJSON.put(ffObj2);
			}
			
			JSONObject leftObj =  new JSONObject(arrayJSON.getJSONObject(arrayJSON.length()-1), JSONObject.getNames(arrayJSON.getJSONObject(arrayJSON.length()-1)));
			JSONObject rightObj = new JSONObject(arrayJSON.getJSONObject(arrayJSON.length()-2), JSONObject.getNames(arrayJSON.getJSONObject(arrayJSON.length()-2)));
			winningSeed = getWinner(leftObj.getInt("winning_seed"), rightObj.getInt("winning_seed"), "National Championship");
			if (winningSeed[2] == leftObj.getInt("winning_seed")) {
				leftObj.put("region", "National Championship");
				leftObj.put("round", "National Championship");
				leftObj.put("winning_seed", leftObj.get("winning_seed"));
				leftObj.put("winner", leftObj.get("winner"));
				leftObj.put("losing_seed", rightObj.get("winning_seed"));
				leftObj.put("loser", rightObj.get("winner"));
				leftObj.put("winning_score", winningSeed[0] + " wins");
				leftObj.put("losing_score", winningSeed[1] + " wins");
				arrayJSON.put(leftObj);
			}
			else {
				rightObj.put("region", "National Championship");
				rightObj.put("round", "National Championship");
				rightObj.put("winning_seed", rightObj.get("winning_seed"));
				rightObj.put("winner", rightObj.get("winner"));
				rightObj.put("losing_seed", leftObj.get("winning_seed"));
				rightObj.put("loser", leftObj.get("winner"));
				rightObj.put("winning_score", winningSeed[1] + " wins");
				rightObj.put("losing_score", winningSeed[0] + " wins");
				arrayJSON.put(rightObj);
			}
			
			arrayJSON.put(regionsJSONObj);
			arrayJSON.put(getDates(year));
			arrayJSON.put(getMaxDate());
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		return arrayJSON;
	}
	
	// this function determines who actually wins by querying the historical data 
	// and randomizing based off those percentages 
	public int[] getWinner(int seed1, int seed2, String round) {
		int[] wins_losses = new int[3];
		Connection conn = null;
		int d=0;
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps1 = conn.prepareStatement("select count(winning_seed) as w_or_l from ncaa where winning_seed = ? and losing_seed = ? and round = ? and winning_score > 0 union all select count(winning_seed) as w_or_l from ncaa where winning_seed = ? and losing_seed = ? and round = ? and winning_score > 0");
			//System.out.println("select count(winning_seed) as w_or_l from ncaa where winning_seed = " + seed1 + " and losing_seed = " + seed2 + " and round = '" + round + "' union all select count(winning_seed) as w_or_l from ncaa where winning_seed = " + seed1 + " and losing_seed = " + seed2 + " and round = '" + round + "' ");
			ps1.setInt(1, seed1);
			ps1.setInt(2, seed2);
			ps1.setString(3, round);
			ps1.setInt(4, seed2);
			ps1.setInt(5, seed1);
			ps1.setString(6, round);
			ResultSet rs1 = ps1.executeQuery();
			
			while (rs1.next()) {
				wins_losses[d] = rs1.getInt("w_or_l");
				d++;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		// between the given least value (inclusive) and bound (exclusive).
		// >= 0 and < number_of_games played + 1 
		int randomNum = ThreadLocalRandom.current().nextInt(0, wins_losses[0] + wins_losses[1] + 1);
		//System.out.println("seed1: " + seed1 + ", seed2: " + seed2 + ", wins_losses[0] : " + wins_losses[0] + ", wins_losses[1] : " + wins_losses[1] + ", randomNum: " + randomNum);
		//System.out.println("randomNum: " + randomNum);
		
		//JSONObject projJSONObj = new JSONObject();

		if (randomNum <= wins_losses[0]) {
			wins_losses[2] = seed1;
			return wins_losses;
			//return seed1;
		}
		else {
			wins_losses[2] = seed2;
			return wins_losses;
			//return seed2;
		}
	}
	
	public JSONObject getMaxDate() {
		Connection conn = null;
		JSONObject metaJSONObj = new JSONObject();
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement("select distinct substr(date, -2), case when cast(substr(date, -2) as unsigned) > 84 then concat('19',substr(date,-2)) else concat('20',substr(date,-2)) end as year_date from ncaa order by 2");
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				metaJSONObj.put("max_date", rs.getString("year_date"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		return metaJSONObj;
	}
	
}