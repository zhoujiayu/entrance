package com.rest.service;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.rest.bean.Resource;

@Repository
public class ResourceService {

	/**
	 * 
	 * 根据id查询 resource
	 * @param id
	 * @return 
	 * @return Resource    
	 */
	public Resource getResource(String id){
		return ResourceRepository.getResourceById(id)==null?new Resource():ResourceRepository.getResourceById(id);
	}

	/**
	 * 
	 * <p>resource存储
	 * @param resource 
	 * @return void    
	 */
	public void insertResource(Resource resource){
		ResourceRepository.insertResource(resource);
	}
	
	/**
	 * 
	 * <p>更新 resource
	 * @param resource 
	 * @return void    
	 */
	public void updateResource(Resource resource){
		Resource tem = new Resource();
		if(StringUtils.isEmpty(resource.getId())){
			return;
		}
		tem.setId(resource.getId());
		
		if(!StringUtils.isEmpty(resource.getName())){
			tem.setName(resource.getName());
		}
		
		if(!StringUtils.isEmpty(resource.getNumber())){
			tem.setNumber(resource.getNumber());
		}
			
		ResourceRepository.updateResource(tem);
	}
	
	/**
	 * 
	 * <p>删除id对应的resource
	 * @param id 
	 * @return void    
	 */
	public void deleteResource(String id ){
		ResourceRepository.deleteResource(id);
	}
	
}

