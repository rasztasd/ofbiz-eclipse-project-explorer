package org.ofbiz.plugin.handlers.opencomponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.RequestMap;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ViewEntity;
import org.ofbiz.plugin.parser.GoToFile;

public class OpenComponentDialog extends FilteredItemsSelectionDialog {
	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	private List<HasXmlDefinition> currentlySelectedElements = new ArrayList<HasXmlDefinition>();

	public OpenComponentDialog(Shell shell, boolean multi,
			IContainer container, int typesMask) {
		super(shell, multi);
		setTitle("Open Ofbiz resource");
		setListLabelProvider(new ILabelProvider() {

			@Override
			public void removeListener(ILabelProviderListener arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object arg0, String arg1) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(ILabelProviderListener arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getText(Object arg0) {
				if (arg0 == null) {
					return "sdf";
				}
				HasXmlDefinition definition = (HasXmlDefinition) arg0;
				return definition.getNameToShow();
			}

			@Override
			public Image getImage(Object arg0) {
				if (arg0 == null) {
					return null;
				}
				String className = arg0.getClass().getSimpleName();
				return Plugin.create("icons/full/obj16/" + (className.substring(0, className.length()-"Impl".length())) + ".gif").createImage();
			}
		});
		setDetailsLabelProvider(new ILabelProvider() {

			@Override
			public void removeListener(ILabelProviderListener arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object arg0, String arg1) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(ILabelProviderListener arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getText(Object arg0) {
				StringBuilder sb = new StringBuilder();
				HasXmlDefinition hasXmlDefinition = (HasXmlDefinition) arg0;
				if (arg0 instanceof Service) {
					sb.append("Service");
				} else if (arg0 instanceof RequestMap) {
					RequestMap requestMap = (RequestMap) arg0;
					sb.append("RequestMap");
					sb.append(" ").append(requestMap.getController().getComponent().getName());
				} else if (arg0 instanceof Screen) {
					sb.append("Screen");
				} else if (arg0 instanceof ViewEntity) {
					sb.append("View Entity");
				} else if (arg0 instanceof Screen) {
					sb.append("Screen");
				} else if (arg0 instanceof Entity) {
					sb.append("Entity");
				} else {
					sb.append("TODO");
				}
				sb.append(" ").append(hasXmlDefinition.getFile().getName());
				return sb.toString();
			}

			@Override
			public Image getImage(Object arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		try {
			IWorkbenchWindow activeWorkbenchWindow = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
			IEditorPart activeEditor2 = activeWorkbenchWindow.getActivePage().getActiveEditor();
			if (activeEditor2 == null) {
				return;
			}
			IEditorPart activeEditor = activeEditor2.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
			IEditorSite editorSite = activeEditor.getEditorSite();
			ISelectionProvider selectionProvider = editorSite.getSelectionProvider();
			ISelection selection = selectionProvider.getSelection();
			if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				String string = textSelection.getText();
				setInitialPattern(string);
			}
		} catch (Exception x) {
		}
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter(){

			@Override
			public boolean isConsistentItem(Object arg0) {
				return true;
			}

			@Override
			public boolean matchItem(Object arg0) {
				if (arg0 != null) {
					HasXmlDefinition hasXmlDefinition = (HasXmlDefinition) arg0;
					String markerKey = hasXmlDefinition.getNameToShow();
					if (markerKey == null) {
						return false;
					}
					return matches(markerKey);
				} else {
					return false;
				}
			}

		};
	}



	@Override
	protected StructuredSelection getSelectedItems() {
		// TODO Auto-generated method stub
		StructuredSelection selectedItems = super.getSelectedItems();
		currentlySelectedElements = selectedItems.toList();
		return selectedItems;
	}

	@Override
	protected void okPressed() {
		for (HasXmlDefinition hasXmlDefinition : currentlySelectedElements) {
			GoToFile.gotoFile(hasXmlDefinition);
		}
		super.okPressed();
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
					throws CoreException {
		Set<HasXmlDefinition> allFilesWithXmlDefinitions = OfbizModelSingleton.get().getAllFilesWithXmlDefinitions();
		for (HasXmlDefinition hasXmlDefinition : allFilesWithXmlDefinitions) {
			contentProvider.add(hasXmlDefinition, itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.beginTask("Searching for Ofbiz components", allFilesWithXmlDefinitions.size());
		progressMonitor.done();
	}
	public String getElementName(Object item) {
		HasXmlDefinition hasXmlDefinition = (HasXmlDefinition) item;
		return hasXmlDefinition.getNameToShow();
	}

	protected Comparator getItemsComparator() {
		return new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		};
	}
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Plugin.getDefault().getDialogSettings()
				.getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = Plugin.getDefault().getDialogSettings()
					.addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}
}
