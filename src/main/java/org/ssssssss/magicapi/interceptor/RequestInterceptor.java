package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.script.MagicScriptContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求拦截器
 */
public interface RequestInterceptor {

	enum Authorization {
		SAVE, DETAIL, RUN, DELETE,
	}

	/**
	 * 是否拥有页面按钮的权限
	 */
	default boolean allowVisit(HttpServletRequest request, Authorization authorization) {
		return true;
	}

	/**
	 * 请求之前执行
	 *
	 * @return 当返回对象时，直接将此对象返回到页面，返回null时，继续执行后续操作
	 */
	default Object preHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null;
	}


	/**
	 * 执行完毕之后执行
	 *
	 * @param value 即将要返回到页面的值
	 * @return 返回到页面的对象, 当返回null时执行后续拦截器，否则直接返回该值，不执行后续拦截器
	 */
	default Object postHandle(ApiInfo info, MagicScriptContext context, Object value,HttpServletRequest request,HttpServletResponse response) throws Exception {
		return null;
	}

}
