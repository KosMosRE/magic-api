package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.modules.SQLModule;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.ScriptClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicConfigController extends MagicController {

	public MagicConfigController(MagicConfiguration configuration) {
		super(configuration);
		// 给前端添加代码提示
		MagicScriptEngine.addScriptClass(SQLModule.class);
		MagicScriptEngine.addScriptClass(MagicAPIService.class);
	}

	/**
	 * 获取所有class
	 */
	@RequestMapping("/classes")
	@ResponseBody
	public JsonBean<Map<String, Map<String, ScriptClass>>> classes() {
		Map<String, ScriptClass> classMap = MagicScriptEngine.getScriptClassMap();
		classMap.putAll(MagicResourceLoader.getModules());
		ScriptClass db = classMap.get(SQLModule.class.getName());
		if (db != null) {
			List<ScriptClass.ScriptAttribute> attributes = new ArrayList<>();
			// 给与前台动态数据源提示
			configuration.getMagicDynamicDataSource().datasources().stream().filter(StringUtils::isNotBlank)
					.forEach(item -> attributes.add(new ScriptClass.ScriptAttribute("db", item)));
			db.setAttributes(attributes);
		}
		Map<String, Map<String, ScriptClass>> values = new HashMap<>();
		values.put("classes", classMap);
		values.put("extensions", MagicScriptEngine.getExtensionScriptClass());
		return new JsonBean<>(values);
	}

	/**
	 * 获取单个class
	 *
	 * @param className 类名
	 */
	@RequestMapping("/class")
	@ResponseBody
	public JsonBean<List<ScriptClass>> clazz(String className) {
		return new JsonBean<>(MagicScriptEngine.getScriptClass(className));
	}
}
