package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.ofbiz.DataSource;
import org.ofbiz.plugin.ofbiz.EntityEngine;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.xmlpull.v1.XmlPullParser;

public class EntityEngineParser extends Parser {
	
	private EntityEngine entityEngine;
	
	public EntityEngineParser(Project project, IFile file) {
		this.file = file;		
		entityEngine = OfbizFactory.eINSTANCE.createEntityEngine();
		entityEngine.setProject(project);
		entityEngine.setName("entityengine.xml");
		entityEngine.setFile(file);
		entityEngine.setMarkerKey("entityengine.xml");
		entityEngine.setParser(this);
		createMarker(1, "entityengine.xml");
	}
	

	@Override
	protected void processStartElement(XmlPullParser xpp) {
		if (xpp.getName().equals("datasource")) {
			DataSource dataSource = OfbizFactory.eINSTANCE.createDataSource();
			String dataSourceName = xpp.getAttributeValue(null, "name");
			dataSource.setName(dataSourceName);
			dataSource.setMarkerKey(dataSourceName);
			createMarker(xpp.getLineNumber(), dataSourceName);
			dataSource.setFile(file);
			dataSource.setJdbcDriver(xpp.getAttributeValue(null, "jdbc-driver"));
			dataSource.setJdbcUri(xpp.getAttributeValue(null, "jdbc-uri"));
			dataSource.setJdbcUserName(xpp.getAttributeValue(null, "jdbc-username"));
			dataSource.setJdbcPassword(xpp.getAttributeValue(null, "jdbc-password"));
			dataSource.setEntityEngine(entityEngine);
		}
	}

	@Override
	protected String getMarkerType() {
		// TODO Auto-generated method stub
		return "org.ofbiz.plugin.text";
	}

}
