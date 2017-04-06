package com.ncaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ncaa.service.NCAAService;

@Controller
public class NCAAController {
	
	@Autowired
	NCAAService ncaaService;

	@RequestMapping("/ncaa")
	public ModelAndView welcome() {
 
		return new ModelAndView("ncaa");
	
	}
	
	//@RequestMapping("/getMetadata")
	//public @ResponseBody String getMetadata() {
 
		//System.out.println("In Java Controller");
		
    	//String jsonString = ncaaService.getMetadata().toString();
    	
    	//return jsonString;
	
	//}
	
	@RequestMapping("/getHistorical")
	public @ResponseBody String getHistorical(@RequestParam("year") String year) {
 
    	String jsonArrayString = ncaaService.getHistorical(year).toString();
    	
    	return jsonArrayString;
	
	}
	
	@RequestMapping("/getPredictions")
	public @ResponseBody String getPredictions(@RequestParam("year") String year) {
 
    	String jsonArrayString = ncaaService.getPredictions(year).toString();
    	
    	return jsonArrayString;
	
	}
	

}
