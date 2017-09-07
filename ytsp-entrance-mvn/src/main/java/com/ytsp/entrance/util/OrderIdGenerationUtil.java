package com.ytsp.entrance.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderIdGenerationUtil {
	private static OrderIdGenerationUtil orderIdGeneration = new OrderIdGenerationUtil();
	
	public static OrderIdGenerationUtil getInstance(){
		return orderIdGeneration;
	}
	private static SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");

	private int seed = 100;
	private String s ;
	private int _seed ;
	private Integer master;
	
	public long genOrderId(){
		synchronized (this) {
			String s = format.format(new Date());
			if(!s.equals(this.s)){
				_seed = seed;
			}
			if(_seed==0)
				_seed = seed;
			this.s=s;
			if(master != null){
				s += master;
			}
			s += _seed;
			_seed += 2;//按照不同的机器分配不同的值，目前一共2台机器
			return new Long(s);
		}
	}
	
	
	public void setSeed(int seed) {
		this.seed = seed;
	}

	public Integer getMaster() {
		return master;
	}

	public void setMaster(Integer master) {
		this.master = master;
	}

}
