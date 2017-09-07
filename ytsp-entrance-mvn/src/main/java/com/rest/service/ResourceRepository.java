package com.rest.service;

import java.util.HashMap;
import java.util.Map;

import com.rest.bean.Resource;

public class ResourceRepository {
	// 模拟数据库储存数据
	private static Map<String,Resource> repository;
	// 初始化数据
	static{
		repository = new HashMap<String,Resource>();
		
		repository.put("id_111", new Resource("id_111", "maven", 11));
		repository.put("id_112", new Resource("id_112", "git", 12));
		repository.put("id_113", new Resource("id_113", "svn", 13));
		repository.put("id_114", new Resource("id_114", "cvs", 14));
		}
	
	public static Resource getResourceById(String id){
		return repository.get(id);
	}
	
	public static void insertResource(Resource resource){
		repository.put(resource.getId(), resource);
	}
	
	public static void updateResource(Resource resource){
		insertResource(resource);
	}
	
	public static void deleteResource(String id){
		repository.remove(id);
	}
	
}

