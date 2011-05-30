/**
 * Copyright 2008 Anders Hessellund
 * Copyright 2011 Peter Pasztor
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
 *
 * $Id: ScreenParser.java,v 1.0 2011/04/29 18:48:15 ppeterka Exp $
 */
package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.model.ComponentHelper;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.EntityRef;
import org.ofbiz.plugin.ofbiz.Form;
import org.ofbiz.plugin.ofbiz.FormFile;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Screen;
import org.xmlpull.v1.XmlPullParser;


public class FormParser extends Parser {

	private static final String ACTIONS_TAG = "actions";
	private static final String WIDGETS_TAG = "widgets";
	private static final String SCREEN_TAG = "screen";
	private static final String SCRIPT_TAG = "script";
	private static final String TEMPLATE_TAG = "html-template";

	private Screen curScreen;
	private EntityRef curAutoAttr;
	private boolean widgets=false;
	private boolean actions=false;
	private FormFile currentForm;

	public FormParser(IFile file, String screenUrl, Project project) {
		this.file = file;
		currentForm = OfbizFactory.eINSTANCE.createFormFile();
		Component componentByUrl = ComponentHelper.getComponentByUrl(project, screenUrl);
		currentForm.setComponent(componentByUrl);
		currentForm.setFile(file);
		String markerKey = "screenfile";
		currentForm.setMarkerKey(markerKey);
		createMarker(1, markerKey);
		currentForm.setName(screenUrl);
	}

	@Override protected void processStartElement(XmlPullParser xpp) {

		if (xpp.getName().equals(ACTIONS_TAG)){
			actions=true;

		} else if (xpp.getName().equals(WIDGETS_TAG)) {
			widgets=true;

		} else if (xpp.getName().equals(SCRIPT_TAG)) {
			handleScript(xpp);
		} else if (xpp.getName().equals(TEMPLATE_TAG)) {
			handleTemplate(xpp);

		} else if (xpp.getName().equals("include-screen")) {
		} else if (xpp.getName().equals("include-form")) {
		} else if (xpp.getName().equals("form")) {
			Form form = OfbizFactory.eINSTANCE.createForm();
			form.setFile(file);
			String name = xpp.getAttributeValue(null, "name");
			form.setName(name);
			String markerKey = "form_" + name;
			form.setMarkerKey(markerKey);
			form.setFormFile(currentForm);
			createMarker(xpp.getLineNumber(), markerKey);
		}
	}


	@Override protected void processEndElement(XmlPullParser xpp) {

		if (xpp.getName().equals("screen")) {
			curScreen = null;

		} else if (xpp.getName().equals("actions")) {
			curAutoAttr = null;

		} else if (xpp.getName().equals("auto-attributes")) {
			curAutoAttr = null;

		} else if (xpp.getName().equals("auto-attributes")) {
			assert curAutoAttr != null;
			curAutoAttr = null;

		}
	}

	// -- utility methods
	private void handleScript(XmlPullParser xpp) {
		//TODO:
	}

	private void handleTemplate(XmlPullParser xpp) {
		//TODO: HTMLTemplate = OfbizFactory.eINSTANCE.createScreen();


	}
}
