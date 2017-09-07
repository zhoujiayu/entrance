<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<s:set name="wid1" value="widthClientInt+150"/>
<s:set name="wid2" value="widthClientInt+10"/>
<html>
<head>     
<meta name="viewport" content="target-densitydpi=device-dpi,width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>苹果pad</title>
<style type="text/css">
*{margin:0;padding:0;}
.sp_pad_wrap{width:<s:property value="#wid1"/>px;margin:0 auto;}
.sp_pad{width:<s:property value="#wid2"/>px; overflow:auto;margin:0 auto;}
.sp_pad img{width:${widthClientInt}px;margin:0 auto;}
.sp_pad_text{width:${widthClientInt}px;float:left;color:#777;margin-top:10px;font-size:16px;line-height:24px;text-indent:32px;}
</style>
<script src="http://entrance.ikan.cn/js/jquery-2.0.2.min.js"></script>
<script type='text/javascript'>
	(function ($) {
	    // alert($.fn.scrollLoading);
	    $.fn.scrollLoading = function (options) {
	        var defaults = {
	            attr: "data-url"
	        };
	        var params = $.extend({}, defaults, options || {});
	        params.cache = [];
	        $(this).each(function () {
	            var node = this.nodeName.toLowerCase(), url = $(this).attr(params["attr"]);
	            if (!url) { return; }
	            var data = {
	                obj: $(this),
	                tag: node,
	                url: url
	            };
	            params.cache.push(data);
	        });
	
	        var loading = function () {
	            var st = $(window).scrollTop(), sth = st + $(window).height();
	            $.each(params.cache, function (i, data) {
	                var o = data.obj, tag = data.tag, url = data.url;
	                if (o) {
	                    //post = o.position().top; posb = post + o.height();
	                    //if ((post > st && post < sth) || (posb > st && posb < sth)) {
	                        
	                    //}
	                    if (tag === "img") {
                            o.attr("src", url);
                        } else {
                            o.load(url);
                        }
	                }
	            });
	            return false;
	        };
	        loading();
	        //$(window).bind("touchstart", loading);
	    };
	})(jQuery);
	
	$(document).ready(function () {
	    //实现图片慢慢浮现出来的效果
	    $("img").load(function () {
	        //图片默认隐藏  
	        $(this).hide();
	        //使用fadeIn特效  
	        $(this).fadeIn("500");
	    });
	    // 异步加载图片，实现逐屏加载图片
	    $(".scrollLoading").scrollLoading(); 
	});
</script>
</head>
<body>
	<div class="sp_pad_wrap">
		<s:iterator value="details" id="detail">
			<div class="sp_pad">
				<s:if test="detail.above==0">
					 <p class="sp_pad_text">
						${detail.description}
					</p>
				</s:if>
				<img class="scrollLoading" style="height:${detail.imgHeight}px" data-url="${detail.imageSrc}" src=""  alt="" />
				<s:if test="detail.above==1">
					 <p class="sp_pad_text">
						${detail.description}
					</p>
				</s:if>
			</div>
		</s:iterator>
	</div>
</body>
</html>
