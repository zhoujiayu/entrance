package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.FavoritesDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.Favorites;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.FavoritesService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 专辑收藏
 *
 */
public class FavoritesCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_FAVORITES_SAVE == code || CommandList.CMD_FAVORITES_LIST == code || 
			CommandList.CMD_FAVORITES_DELETE == code || CommandList.CMD_FAVORITES_DELETE_ALL == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_FAVORITES_SAVE == code) {
				return saveFavorites();
				
			} else if (CommandList.CMD_FAVORITES_LIST == code) {
				return listFavorites();
				
			} else if (CommandList.CMD_FAVORITES_DELETE == code) {
				return deleteFavorites();
				
			} else if (CommandList.CMD_FAVORITES_DELETE_ALL == code) {
				return deleteAllFavorites();
				
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
		return null;
	}
	
	private ExecuteResult saveFavorites() throws Exception{
		JSONObject jsonObj = getContext().getBody().getBodyObject();
//		String name = jsonObj.getString("name");
//		String summary = jsonObj.getString("summary");
		int albumid = jsonObj.getInt("aid");
		
		SessionCustomer sc = getSessionCustomer();
		if(sc == null || sc.getCustomer() == null){
			return getNoPermissionExecuteResult();
		}
		
		Customer customer = sc.getCustomer();
		AlbumDao ad = SystemInitialization.getApplicationContext().getBean(AlbumDao.class);
		FavoritesDao fd = SystemInitialization.getApplicationContext().getBean(FavoritesDao.class);
		
//		int count = fd.getRecordCount(" WHERE customer.id = ?", new Object[]{customer.getId()});
//		if(count >= 30){
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "收藏数量已达到上限！", null, this);
//		}
		
		Album album = ad.findById(albumid);
		if(album == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "专辑不存在！", null, this);
		}
		
		Favorites favorites = new Favorites();
//		favorites.setName(name);
//		favorites.setSummary(summary);
		favorites.setAlbum(album);
		favorites.setCustomer(customer);
		fd.save(favorites);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "添加收藏成功！", null, this);
	}
	
	private ExecuteResult listFavorites() throws Exception{
		SessionCustomer sc = getSessionCustomer();
		if(sc == null || sc.getCustomer() == null){
			return getNoPermissionExecuteResult();
		}
		
		Customer customer = sc.getCustomer();
		FavoritesService fs = SystemInitialization.getApplicationContext().getBean(FavoritesService.class);
		JSONArray array = fs.getFavoritesArray(customer.getId());
		
		JSONObject obj = new JSONObject();
		obj.put("favoritesList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取收藏列表成功！", obj, this);
	}
	
	private ExecuteResult deleteAllFavorites() throws Exception{
		SessionCustomer sc = getSessionCustomer();
		if(sc == null || sc.getCustomer() == null){
			return getNoPermissionExecuteResult();
		}
		
		FavoritesDao fd = SystemInitialization.getApplicationContext().getBean(FavoritesDao.class);
//		fd.deleteById(favoritesid);
		fd.deleteByHql(" WHERE customer.id = ?", new Object[]{sc.getCustomer().getId()});
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "刪除收藏成功！", null, this);
	}
	
	private ExecuteResult deleteFavorites() throws Exception{
		JSONObject jsonObj = getContext().getBody().getBodyObject();
//		int favoritesid = jsonObj.getInt("fid");
		int albumid = jsonObj.getInt("aid");
		
		SessionCustomer sc = getSessionCustomer();
		if(sc == null || sc.getCustomer() == null){
			return getNoPermissionExecuteResult();
		}
		
		FavoritesDao fd = SystemInitialization.getApplicationContext().getBean(FavoritesDao.class);
//		fd.deleteById(favoritesid);
		fd.deleteByHql(" WHERE customer.id = ? AND album.id = ?", new Object[]{sc.getCustomer().getId(), albumid});
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "刪除收藏成功！", null, this);
	}

}
