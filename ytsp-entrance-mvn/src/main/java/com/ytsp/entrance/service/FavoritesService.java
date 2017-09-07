package com.ytsp.entrance.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.dao.FavoritesDao;
import com.ytsp.db.domain.Favorites;

/**
 * @author GENE
 * @description 收藏服务
 */
public class FavoritesService {
	private static final Logger logger = Logger.getLogger(FavoritesService.class);

	private FavoritesDao favoritesDao;
	
	public JSONArray getFavoritesArray(int customerid) throws Exception{
		List<Favorites> favorites = favoritesDao.findAllByHql(" WHERE customer.id = ?",  new Object[]{customerid});
		JSONArray array = new JSONArray();
		for (Favorites fav : favorites) {
			JSONObject obj = new JSONObject();
//			obj.put("name", fav.getName() == null ? "" : fav.getName());
//			obj.put("summary", fav.getSummary() == null ? "" : fav.getSummary());
//			obj.put("aid", fav.getAlbum().getId());
//			obj.put("sname", fav.getAlbum().getName() == null ? "" : fav.getAlbum().getName());
//			obj.put("fid", fav.getId());
			
			
			obj.put("aid", fav.getAlbum().getId());
			obj.put("fid", fav.getId());
			obj.put("name", fav.getAlbum().getName() == null ? "" : fav.getAlbum().getName());
			obj.put("totalCount", fav.getAlbum().getTotalCount() == null ? 0 : fav.getAlbum().getTotalCount());
			array.put(obj);
		}
		return array;
	}
	
	public Set<Integer> getFavoritesAlbumIds(int customerid) throws Exception{
		List<Favorites> favorites = favoritesDao.findAllByHql(" WHERE customer.id = ?", new Object[]{customerid});
		Set<Integer> albums = new HashSet<Integer>(favorites.size());
		for (Favorites fav : favorites) {
			albums.add(fav.getAlbum().getId());
		}
		return albums;
	}

	public void saveFavorites(Favorites favorites) throws Exception {
		favoritesDao.save(favorites);
	}
	
	public void saveOrUpdate(Favorites favorites) throws Exception {
		favoritesDao.saveOrUpdate(favorites);
	}

	public void updateFavorites(Favorites favorites) throws Exception {
		favoritesDao.update(favorites);
	}

	public void deleteFavorites(Favorites favorites) throws Exception {
		favoritesDao.delete(favorites);
	}

	public Favorites findFavoritesById(int favoritesid) throws Exception {
		return favoritesDao.findById(favoritesid);
	}
	
	public List<Favorites> getAllFavoritess() throws Exception {
		return favoritesDao.getAll();
	}

	public void deleteFavoritesById(int favoritesid) throws Exception {
		favoritesDao.deleteById(favoritesid);
	}
	
	public FavoritesDao getFavoritesDao() {
		return favoritesDao;
	}

	public void setFavoritesDao(FavoritesDao favoritesDao) {
		this.favoritesDao = favoritesDao;
	}

}
