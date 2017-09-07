package com.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.rest.service.RestOrderService;

@Controller
@RequestMapping("/order")
public class OrderController {
	

	@Autowired
	private RestOrderService orderService;
	
	@RequestMapping(value="/get/{id}" , method=RequestMethod.GET)
	public String get(@PathVariable("id") String id , ModelMap model){
		model.put("order", orderService.getOrderResource(id));
		return "order";
	}
	
}
