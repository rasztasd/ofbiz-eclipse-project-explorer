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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.model.ComponentHelper;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.EntityRef;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.ofbiz.ScreenFile;
import org.xmlpull.v1.XmlPullParser;


public class ScreenParser extends Parser {

	private static final String ACTIONS_TAG = "actions";
	private static final String WIDGETS_TAG = "widgets";
	private static final String SCREEN_TAG = "screen";
	private static final String SCRIPT_TAG = "script";
	private static final String TEMPLATE_TAG = "html-template";

	private Screen curScreen;
	private boolean widgets=false;
	private boolean actions=false;
	private Set<String> screens = new HashSet<String>();
	private Set<String> forms = new HashSet<String>();
	private ScreenFile currentScreenFile;
	private String screenUrl;

	public ScreenParser(IFile file, String screenUrl, Project project) {
		this.file = file;
		this.screenUrl = screenUrl;
		currentScreenFile = OfbizFactory.eINSTANCE.createScreenFile();
		Component componentByUrl = ComponentHelper.getComponentByUrl(project, screenUrl);
		currentScreenFile.setComponent(componentByUrl);
		currentScreenFile.setFile(file);
		String markerKey = "screenfile";
		currentScreenFile.setMarkerKey(markerKey);
		currentScreenFile.setNameToShow(screenUrl);
		createMarker(1, markerKey);
		currentScreenFile.setName(screenUrl);
	}

	@Override protected void processStartElement(XmlPullParser xpp) {

		if (xpp.getName().equals(SCREEN_TAG)) {
			handleScreen(xpp);

		} else if (xpp.getName().equals(ACTIONS_TAG)){
			actions=true;

		} else if (xpp.getName().equals(WIDGETS_TAG)) {
			widgets=true;

		} else if (xpp.getName().equals(SCRIPT_TAG)) {
			handleScript(xpp);
		} else if (xpp.getName().equals(TEMPLATE_TAG)) {
			handleTemplate(xpp);

		} else if (xpp.getName().equals("include-screen")) {
			screens.add(xpp.getAttributeValue(null, "location"));
		} else if (xpp.getName().equals("include-form")) {
			forms.add(xpp.getAttributeValue(null, "location"));
		}
	}

	@Override protected void processEndElement(XmlPullParser xpp) {

		if (xpp.getName().equals("screen")) {
			curScreen = null;
		}
	}

	// -- utility methods

	private void handleScreen(XmlPullParser xpp) {
		curScreen = OfbizFactory.eINSTANCE.createScreen();
		curScreen.setScreenFile(currentScreenFile);
		curScreen.setName(xpp.getAttributeValue(null, "name"));
		curScreen.setFile(file);
		String markerKey = curScreen.getName();
		curScreen.setMarkerKey(markerKey);
		curScreen.setNameToShow(curScreen.getName());
		/*curScreen.setEngine(xpp.getAttributeValue(null, "engine"));
		String location = xpp.getAttributeValue(null, "location");
		if(location!=null)
			curScreen.setLocation(location);
		String invoke = xpp.getAttributeValue(null, "invoke");
		if(invoke!=null)
			curScreen.setInvoke(invoke);
		curScreen.setEntity(xpp.getAttributeValue(null, "default-entity-name"));*/

		//TODO: actions



		//widgets



		createMarker(xpp.getLineNumber(), markerKey);
	}
	private void handleScript(XmlPullParser xpp) {
		//TODO:
	}

	private void handleTemplate(XmlPullParser xpp) {
		//TODO: HTMLTemplate = OfbizFactory.eINSTANCE.createScreen();


	}
	public Set<String> getScreens() {
		return screens;
	}
	public Set<String> getForms() {
		return forms;
	}

	@Override
	protected String getMarkerType() {
		return "org.ofbiz.plugin.screenMarker";
	}
}
