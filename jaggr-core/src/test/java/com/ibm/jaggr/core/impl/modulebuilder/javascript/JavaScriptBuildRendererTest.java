/*
 * (C) Copyright 2012, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.jaggr.core.impl.modulebuilder.javascript;

import com.ibm.jaggr.core.deps.ModuleDepInfo;
import com.ibm.jaggr.core.deps.ModuleDeps;
import com.ibm.jaggr.core.test.TestUtils;
import com.ibm.jaggr.core.util.ConcurrentAddOnlyList;

import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

public class JavaScriptBuildRendererTest {
	static final String content = "define([],function() {require([\"foo\",\"" +
			String.format(JavaScriptBuildRenderer.REQUIRE_EXPANSION_PLACEHOLDER_FMT, 0) +
			"\"]);require([\"bar\", \"" +
			String.format(JavaScriptBuildRenderer.REQUIRE_EXPANSION_PLACEHOLDER_FMT, 1) +
			"\"])});";

	@Test
	public void testRenderBuild() throws Exception {
		ModuleDeps deps1 = new ModuleDeps();
		ModuleDeps deps2 = new ModuleDeps();
		deps1.add("foodep1", new ModuleDepInfo());
		deps1.add("foodep2", new ModuleDepInfo());
		deps2.add("bardep", new ModuleDepInfo());
		List<ModuleDeps> depsList = Arrays.asList(new ModuleDeps[]{deps1, deps2});
		JavaScriptBuildRenderer compiled = new JavaScriptBuildRenderer("test", content, depsList, false);
		ConcurrentAddOnlyList<String[]> expDeps = new ConcurrentAddOnlyList<String[]>();

		// validate the rendered output (inline expansion)
		Map<String, Object> requestAttributes = new HashMap<String, Object>();
		HttpServletRequest mockRequest = TestUtils.createMockRequest(TestUtils.createMockAggregator(), requestAttributes);
		EasyMock.replay(mockRequest);
		String result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\",\"foodep1\",\"foodep2\"]);require([\"bar\",\"bardep\"])});", result);

		// validate the rendered output (layer expansion)
		requestAttributes.put(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS, expDeps);
		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][0]));require([\"bar\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][1]))});", result);
		System.out.println(expDeps);
		Assert.assertEquals(expDeps.size(), 2);
		Assert.assertEquals(Arrays.asList(new String[]{"foodep1","foodep2"}), Arrays.asList(expDeps.get(0)));
		Assert.assertEquals(Arrays.asList(new String[]{"bardep"}), Arrays.asList(expDeps.get(1)));

		// validate the rendered output (inline expansion - logging)
		requestAttributes.remove(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS);
		ModuleDeps enclosingDeps = new ModuleDeps();
		enclosingDeps.add("foodep2", new ModuleDepInfo());
		enclosingDeps.add("bardep", new ModuleDepInfo());
		mockRequest.setAttribute(JavaScriptModuleBuilder.EXPANDED_DEPENDENCIES, enclosingDeps);
		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\",\"foodep1\"]);require([\"bar\"])});", result);

		// validate the rendered output (layer expansion - logging)
		expDeps = new ConcurrentAddOnlyList<String[]>();
		requestAttributes.put(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS, expDeps);

		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][0]));require([\"bar\"])});", result);
		System.out.println(expDeps);
		Assert.assertEquals(expDeps.size(), 1);
		Assert.assertEquals(Arrays.asList(new String[]{"foodep1"}), Arrays.asList(expDeps.get(0)));
	}

	@Test
	public void testRenderBuild_withDetails() throws Exception {
		String contentWithComments = content +
				"console.log(\"deps1=" +
				String.format(JavaScriptBuildRenderer.REQUIRE_EXPANSION_LOG_PLACEHOLDER_FMT, 0) +
				"\");console.log(\"deps2=" +
				String.format(JavaScriptBuildRenderer.REQUIRE_EXPANSION_LOG_PLACEHOLDER_FMT, 1) +
				"\");";
		ModuleDeps deps1 = new ModuleDeps();
		ModuleDeps deps2 = new ModuleDeps();
		deps1.add("foodep1", new ModuleDepInfo());
		deps1.add("foodep2", new ModuleDepInfo());
		deps2.add("bardep", new ModuleDepInfo());
		List<ModuleDeps> depsList = Arrays.asList(new ModuleDeps[]{deps1, deps2, deps1, deps2});
		JavaScriptBuildRenderer compiled = new JavaScriptBuildRenderer("test", contentWithComments, depsList, true);
		ConcurrentAddOnlyList<String[]> expDeps = new ConcurrentAddOnlyList<String[]>();

		// validate the rendered output
		Map<String, Object> requestAttributes = new HashMap<String, Object>();
		HttpServletRequest mockRequest = TestUtils.createMockRequest(TestUtils.createMockAggregator(), requestAttributes);
		EasyMock.replay(mockRequest);
		String result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals(
				"define([],function() {require([\"foo\",\"foodep1\",\"foodep2\"]);require([\"bar\",\"bardep\"])});console.log(\"deps1=foodep1, foodep2\");console.log(\"deps2=bardep\");",
				result);

		// validate the rendered output (layer expansion)
		requestAttributes.put(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS, expDeps);
		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][0]));require([\"bar\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][1]))});console.log(\"deps1=foodep1, foodep2\");console.log(\"deps2=bardep\");", result);
		Assert.assertEquals(expDeps.size(), 2);
		Assert.assertEquals(Arrays.asList(new String[]{"foodep1","foodep2"}), Arrays.asList(expDeps.get(0)));
		Assert.assertEquals(Arrays.asList(new String[]{"bardep"}), Arrays.asList(expDeps.get(1)));

		// validate the rendered output (inline expansion - logging)
		requestAttributes.remove(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS);
		ModuleDeps enclosingDeps = new ModuleDeps();
		enclosingDeps.add("foodep2", new ModuleDepInfo());
		enclosingDeps.add("bardep", new ModuleDepInfo());
		mockRequest.setAttribute(JavaScriptModuleBuilder.EXPANDED_DEPENDENCIES, enclosingDeps);
		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		Assert.assertEquals(
				"define([],function() {require([\"foo\",\"foodep1\"]);require([\"bar\"])});console.log(\"deps1=foodep1\");console.log(\"deps2=\");",
				result);
		System.out.println(result);

		// validate the rendered output (layer expansion - logging)
		expDeps = new ConcurrentAddOnlyList<String[]>();
		requestAttributes.put(JavaScriptModuleBuilder.MODULE_EXPANDED_DEPS, expDeps);

		result = compiled.renderBuild(mockRequest, Collections.<String>emptySet());
		System.out.println(result);
		Assert.assertEquals("define([],function() {require([\"foo\"].concat(" +
				JavaScriptModuleBuilder.EXPDEPS_VARNAME +
				"[0][0]));require([\"bar\"])});console.log(\"deps1=foodep1\");console.log(\"deps2=\");", result);
		Assert.assertEquals(expDeps.size(), 1);
		Assert.assertEquals(Arrays.asList(new String[]{"foodep1"}), Arrays.asList(expDeps.get(0)));

	}
}
