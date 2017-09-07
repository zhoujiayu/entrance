package com.statistics.entity;

import java.lang.reflect.Field;

import com.ytsp.common.util.StringUtil;

public class Location {
	private static final String RECOMMEND_IMAG = "推荐首页轮播图";
	
	private static final String RECOMMEND_NAVIGATION_1 = "推荐首页导航1";
	
	private static final String RECOMMEND_NAVIGATION_2= "推荐首页导航2";
	
	private static final String RECOMMEND_NAVIGATION_3 = "推荐首页导航3";
	
	private static final String RECOMMEND_NAVIGATION_4 = "推荐首页导航4";
	
	private static final String RECOMMEND_LARGE_POSTER = "推荐首页一级海报";
	
	private static final String RECOMMEND_POSTER_SECOND_1 = "推荐页第1个二级海报";
	
	private static final String RECOMMEND_POSTER_SECOND_2 = "推荐页第2个二级海报";
	
	private static final String RECOMMEND_POSTER_THIRD_1 = "推荐页第1个3级海报";
	
	private static final String RECOMMEND_POSTER_THIRD_2 = "推荐页第2个3级海报";
	
	private static final String RECOMMEND_POSTER_THIRD_3 = "推荐页第3个3级海报";
	
	private static final String RECOMMEND_BANNER_1 = "推荐页横幅1";
	
	private static final String RECOMMEND_BANNER_2 = "推荐页横幅2";
	
	private static final String ANIME_IMAGE = "动漫首页轮播图";
	
	private static final String ANIME_POSTER_FIRST = "动漫首页一级海报";
	
	private static final String ANIME_POSTER_SECOND_1 = "动漫第1个二级海报";
	
	private static final String ANIME_POSTER_THIRD_1 = "动漫第1个3级海报";
	
	private static final String ANIME_INFO = "动漫首页资讯";
	
	private static final String TOY_IMAGE = "玩具首页轮播图";
	
	private static final String TOY_NAVIGATION_1 = "玩具首页导航1";
	
	private static final String TOY_NAVIGATION_2 = "玩具首页导航2";
	
	private static final String TOY_NAVIGATION_3 = "玩具首页导航3";
	
	private static final String TOY_LARGE_POSTER = "玩具首页一级海报";

	private static final String TOY_POSTER_SECOND_1 = "玩具首页第1个二级海报";

	private static final String TOY_POSTER_SECOND_2 = "玩具首页第2个二级海报";

	private static final String TOY_POSTER_THIRD_1 = "玩具首页第1个3级海报";

	private static final String TOY_POSTER_THIRD_2 = "玩具首页第2个3级海报";

	private static final String TOY_POSTER_THIRD_3 = "玩具首页第3个3级海报";
	
	private static final String TOY_POSTER_THIRD_4 = "玩具首页第4个3级海报";

	private static final String TOY_BANNER_1 = "玩具首页横幅1";

	private static final String TOY_BANNER_2 = "玩具首页横幅2";

	private static final String KNOWNLEDGE_IMAGE = "知识首页轮播图";

	private static final String KNOWNLEDGE_LARGE_POSTER = "知识首页一级海报";

	private static final String KNOWNLEDGE_POSTER_FIRST = "知识1级海报";

	private static final String KNOWNLEDGE_POSTER_SECOND_1 = "知识第1个二级海报";

	private static final String KNOWNLEDGE_POSTER_THIRD_1 = "知识第1个3级海报";

	private static final String KNOWNLEDGE_INFO = "知识首页资讯";
	
	/**
	* <p>功能描述:通过属性名称获取相应的值</p>
	* <p>参数：@param sign
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getLocation(String sign){
		if(StringUtil.isNullOrEmpty(sign)){
			return "";
		}
		Location l = new Location();
		try {
			Class clazz = l.getClass();
			Field[] fileds = clazz.getDeclaredFields();
			if(sign.startsWith("RECOMMEND_IMAGE")){
				String index = sign.substring("RECOMMEND_IMAGE".length());
				return Location.RECOMMEND_IMAG+index;
			}else if(sign.startsWith("ANIME_IMAGE")){
				String index = sign.substring("ANIME_IMAGE".length());
				return Location.ANIME_IMAGE+index;
			}else if(sign.startsWith("ANIME_INFO")){
				return Location.ANIME_INFO;
			}else if(sign.startsWith("TOY_IMAGE")){
				String index = sign.substring("TOY_IMAGE".length());
				return Location.TOY_IMAGE+index;
			}else if(sign.startsWith("KNOWNLEDGE_IMAGE")){
				String index = sign.substring("KNOWNLEDGE_IMAGE".length());
				return Location.KNOWNLEDGE_IMAGE+index;
			}else if(sign.startsWith("KNOWNLEDGE_INFO")){
				return Location.KNOWNLEDGE_INFO;
			}
			for (int i = 0; i < fileds.length; i++) {
				Field f = fileds[i];
				// 设置属性是可以访问的
				f.setAccessible(true);
				if(f.getName().equals(sign)){
					// 得到此属性的值
					Object val = f.get(sign);
					return ((String)val).toString();
				}
			}
		} catch (Exception e) {
			System.out.println("统计获取当前位置值出错："+sign);
		}
		return "";
	}
	
	public static void main(String[] args) {
		System.out.println(getLocation("TOY_BANNER_1"));
	}

	public static String getRecommendImag() {
		return RECOMMEND_IMAG;
	}

	public static String getRecommendNavigation1() {
		return RECOMMEND_NAVIGATION_1;
	}

	public static String getRecommendNavigation2() {
		return RECOMMEND_NAVIGATION_2;
	}

	public static String getRecommendNavigation3() {
		return RECOMMEND_NAVIGATION_3;
	}

	public static String getRecommendNavigation4() {
		return RECOMMEND_NAVIGATION_4;
	}

	public static String getRecommendLargePoster() {
		return RECOMMEND_LARGE_POSTER;
	}

	public static String getRecommendPosterSecond1() {
		return RECOMMEND_POSTER_SECOND_1;
	}

	public static String getRecommendPosterSecond2() {
		return RECOMMEND_POSTER_SECOND_2;
	}

	public static String getRecommendPosterThird1() {
		return RECOMMEND_POSTER_THIRD_1;
	}

	public static String getRecommendPosterThird2() {
		return RECOMMEND_POSTER_THIRD_2;
	}

	public static String getRecommendPosterThird3() {
		return RECOMMEND_POSTER_THIRD_3;
	}

	public static String getRecommendBanner1() {
		return RECOMMEND_BANNER_1;
	}

	public static String getRecommendBanner2() {
		return RECOMMEND_BANNER_2;
	}

	public static String getAnimeImage() {
		return ANIME_IMAGE;
	}

	public static String getAnimePosterFirst() {
		return ANIME_POSTER_FIRST;
	}

	public static String getAnimePosterSecond1() {
		return ANIME_POSTER_SECOND_1;
	}

	public static String getAnimePosterThird1() {
		return ANIME_POSTER_THIRD_1;
	}

	public static String getAnimeInfo() {
		return ANIME_INFO;
	}

	public static String getToyImage() {
		return TOY_IMAGE;
	}

	public static String getToyNavigation1() {
		return TOY_NAVIGATION_1;
	}

	public static String getToyNavigation2() {
		return TOY_NAVIGATION_2;
	}

	public static String getToyNavigation3() {
		return TOY_NAVIGATION_3;
	}

	public static String getToyLargePoster() {
		return TOY_LARGE_POSTER;
	}

	public static String getToyPosterSecond1() {
		return TOY_POSTER_SECOND_1;
	}

	public static String getToyPosterSecond2() {
		return TOY_POSTER_SECOND_2;
	}

	public static String getToyPosterThird1() {
		return TOY_POSTER_THIRD_1;
	}

	public static String getToyPosterThird2() {
		return TOY_POSTER_THIRD_2;
	}

	public static String getToyPosterThird3() {
		return TOY_POSTER_THIRD_3;
	}

	public static String getToyBanner1() {
		return TOY_BANNER_1;
	}

	public static String getToyBanner2() {
		return TOY_BANNER_2;
	}

	public static String getKnownledgeImage() {
		return KNOWNLEDGE_IMAGE;
	}

	public static String getKnownledgeLargePoster() {
		return KNOWNLEDGE_LARGE_POSTER;
	}

	public static String getKnownledgePosterFirst() {
		return KNOWNLEDGE_POSTER_FIRST;
	}

	public static String getKnownledgePosterSecond1() {
		return KNOWNLEDGE_POSTER_SECOND_1;
	}

	public static String getKnownledgePosterThird1() {
		return KNOWNLEDGE_POSTER_THIRD_1;
	}

	public static String getKnownledgeInfo() {
		return KNOWNLEDGE_INFO;
	}
	
}
