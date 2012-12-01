package org.ofbiz.plugin.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.FreemarkerMacroDeclaration;
import org.ofbiz.plugin.ofbiz.FreemarkerTemplateFile;
import org.ofbiz.plugin.ofbiz.FtlFilesContainer;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;

import freemarker.core.Macro;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerParser {
	public FreemarkerParser(FtlFilesContainer filesContainer, IFile file) throws CoreException, IOException {
		FreemarkerTemplateFile freemarkerTemplateFile = OfbizFactory.eINSTANCE.createFreemarkerTemplateFile();
		freemarkerTemplateFile.setFile(file);
		freemarkerTemplateFile.setFtlFilesContainer(filesContainer);
		freemarkerTemplateFile.setName(file.getName());
		String freemarkerFileMarkerKey = file.getName() + "_markerkey";
		createMarker(file, 0, freemarkerFileMarkerKey);
		freemarkerTemplateFile.setMarkerKey(freemarkerFileMarkerKey);
		freemarkerTemplateFile.setNameToShow(file.getName());
		
		Reader locationReader = new InputStreamReader(file.getContents());
		Template template = new Template(file.getName(), locationReader, new Configuration());
		Map macros = template.getMacros();
		for (Object key : macros.keySet()) {
			FreemarkerMacroDeclaration freemarkerMacroDeclaration = OfbizFactory.eINSTANCE.createFreemarkerMacroDeclaration();
			freemarkerMacroDeclaration.setFreemarkerTemplateFile(freemarkerTemplateFile);
			freemarkerMacroDeclaration.setName((String) key);
			freemarkerMacroDeclaration.setNameToShow((String) key);
			freemarkerMacroDeclaration.setMarkerKey((String) key + "_macrodeclaration");
			freemarkerMacroDeclaration.setFile(file);
			Macro macro = (Macro) macros.get(key);
			createMarker(file, macro.getBeginLine(), (String) key + "_macrodeclaration");
			StringBuilder sb = new StringBuilder();
			for (String argument : macro.getArgumentNames()) {
				sb.append(argument).append(" ");
			}
			freemarkerMacroDeclaration.setDocumentation(sb.toString());
			freemarkerMacroDeclaration.setLookupName((String) key);
		}
	}
	
	private IMarker createMarker(IFile file, int lineno, String name) {
		if (!Plugin.USE_MARKERS) return null;
		try {
			IMarker marker = file.createMarker("org.ofbiz.plugin.ftlFileMarker");
			marker.setAttribute(IMarker.LINE_NUMBER, lineno);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			marker.setAttribute("name", name);
			//Plugin.debug("marker created for "+name+" in file "+file.getFullPath());
			return marker;
		} catch (CoreException e) {
			Plugin.logError("Unable to create marker for "+file.getName(), e);
			return null;
		}
	}
}
