package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.context.CookieContext;
import org.ssssssss.magicapi.context.HeaderContext;
import org.ssssssss.magicapi.context.SessionContext;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestHandler {

	private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	/**
	 * 请求拦截器
	 */
	private List<RequestInterceptor> requestInterceptors = new ArrayList<>();

	/**
	 * 请求出错时，是否抛出异常
	 */
	private boolean throwException = false;

	/**
	 * 结果处理器
	 */
	private ResultProvider resultProvider;

	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		requestInterceptors.add(requestInterceptor);
	}

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	@ResponseBody
	public Object invoke(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable(required = false) Map<String, Object> pathVariables,
						 @RequestParam(required = false) Map<String, Object> parameters,
						 @RequestBody(required = false) Map<String, Object> requestBody) throws Throwable {
		ApiInfo info;
		try {
			//	找到对应的接口信息
			info = MappingHandlerMapping.getMappingApiInfo(request);
			// 构建脚本上下文
			MagicScriptContext context = new MagicScriptContext();
			context.putMapIntoContext(parameters);
			context.putMapIntoContext(pathVariables);
			context.set("cookie", new CookieContext(request));
			context.set("header", new HeaderContext(request));
			context.set("session", new SessionContext(request.getSession()));
			context.set("path", pathVariables);
			if (requestBody != null) {
				context.set("body", requestBody);
			}
			// 执行前置拦截器
			for (RequestInterceptor requestInterceptor : requestInterceptors) {
				Object value = requestInterceptor.preHandle(info, context);
				if (value != null) {
					return value;
				}
			}
			// 执行脚本
			Object value = MagicScriptEngine.execute(MagicScriptCompiler.compile(info.getScript()), context);
			// 执行后置拦截器
			for (RequestInterceptor requestInterceptor : requestInterceptors) {
				Object target = requestInterceptor.postHandle(info, context, value);
				if (target != null) {
					return target;
				}
			}
			return resultProvider.buildResult(value);
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			logger.error("接口请求出错", root);
			return resultProvider.buildResult(root);
		}

	}
}
