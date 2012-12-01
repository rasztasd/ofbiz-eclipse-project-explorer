package org.ofbiz.plugin.handlers.showdocumentation;

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
import org.ofbiz.plugin.ofbiz.HasDocumentation;

public class OpenOfbizDocumentationDialog extends FilteredItemsSelectionDialog {
	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	private List<HasDocumentation> currentlySelectedElements = new ArrayList<HasDocumentation>();

	public OpenOfbizDocumentationDialog(Shell shell, boolean multi,
			IContainer container, int typesMask) {
		super(shell, multi);
		setTitle("Open Ofbiz documentation");
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
				HasDocumentation definition = (HasDocumentation) arg0;
				return definition.getLookupName();
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
				HasDocumentation hasDocumentation = (HasDocumentation) arg0;
				return hasDocumentation.getDocumentation(); 
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
					HasDocumentation documentation = (HasDocumentation) arg0;
					String markerKey = documentation.getLookupName();
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
//		for (HasXmlDefinition hasXmlDefinition : currentlySelectedElements) {
//			GoToFile.gotoFile(hasXmlDefinition);
//		}
		super.okPressed();
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
					throws CoreException {
		Set<HasDocumentation> allFilesWithDocumentation = OfbizModelSingleton.get().getAllFilesWithDocumentation();
		for (HasDocumentation hasDocumentation : allFilesWithDocumentation) {
			contentProvider.add(hasDocumentation, itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.beginTask("Searching for Ofbiz components", allFilesWithDocumentation.size());
		progressMonitor.done();
	}
	public String getElementName(Object item) {
		HasDocumentation hasDocumentation = (HasDocumentation) item;
		return hasDocumentation.getLookupName();
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
