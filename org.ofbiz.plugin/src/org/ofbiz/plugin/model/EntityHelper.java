package org.ofbiz.plugin.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;
import org.ofbiz.plugin.parser.GoToFile;

public class EntityHelper {
	public static List<HyperlinkMarker> getHyperlinksForEntity(final String searchString) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		TreeIterator<EObject> eAllContents = project.eAllContents();
		while (eAllContents.hasNext()) {
			EObject eObject = eAllContents.next();
			OfbizSwitch<IEntity> ofbizSwitch = new OfbizSwitch<IEntity>() {

				@Override
				public IEntity caseIEntity(IEntity object) {
					String objectUrl = object.getHyperlinkKey();
					if (searchString.equals(objectUrl)) {
						return object;
					}
					return null;
				}

			};
			final IEntity doSwitch = ofbizSwitch.doSwitch(eObject);
			if (doSwitch != null) {
				retValue.add(new HyperlinkMarker(GoToFile.getMarker(doSwitch)) {
					
					@Override
					public String getTypeLabel() {
						return "";
					}
					
					@Override
					public String getHyperlinkText() {
						return doSwitch.getHyperlinkText();
					}
				});
			}
		}
		return retValue;
	}
	public static List<IEntity> getIEntities() {
		List<IEntity> retValue = new ArrayList<IEntity>();
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		TreeIterator<EObject> eAllContents = project.eAllContents();
		OfbizSwitch<IEntity> ofbizSwitch = new OfbizSwitch<IEntity>(){

			@Override
			public IEntity caseIEntity(IEntity object) {
				// TODO Auto-generated method stub
				return object;
			}
			
		};
		while (eAllContents.hasNext()) {
			retValue.add(ofbizSwitch.doSwitch(eAllContents.next()));
		}
		return retValue;
	}
}
