package org.ofbiz.plugin.view;


import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.ofbiz.plugin.dnd.DropViewAdapter;
import org.ofbiz.plugin.dnd.OfbizTransfer;
import org.ofbiz.plugin.dnd.WorkbenchGraphNode;
import org.ofbiz.plugin.parser.GoToFile;

public class WorkbenchView extends ViewPart {
	private Composite parent;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		init();
		// TODO Auto-generated method stub
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	private void init() {
		final GraphViewer viewer = new GraphViewer(parent, SWT.NONE);
		final Graph graphControl = viewer.getGraphControl();
		viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { OfbizTransfer.getInstance() }, new DropViewAdapter(viewer, graphControl));
		//		viewer.addDoubleClickListener(new IDoubleClickListener() {
		//			
		//			@Override
		//			public void doubleClick(DoubleClickEvent event) {
		//				ISelection selection = viewer.getSelection();
		//				System.err.println();
		//			}
		//		});
		graphControl.addMouseListener(new MouseAdapter() {
			private GraphViewer pViewer = viewer;
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IFigure figureAt = graphControl.getFigureAt(e.x, e.y);
				List<?> mSelection = pViewer.getGraphControl().getSelection();
				List<?> selection = ((StructuredSelection)pViewer.getSelection()).toList();
				for (Object selected : mSelection) {
					if (selected instanceof GraphNode
							&& ((GraphNode) selected).getNodeFigure()
							.equals(figureAt)) {
						if (selected != null && selected instanceof WorkbenchGraphNode) {
							WorkbenchGraphNode graphNode = (WorkbenchGraphNode) selected;
							GoToFile.gotoFile(graphNode.getXmlDef());
						}
					}

				}
				super.mouseDoubleClick(e);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mouseDown(e);
			}

		});
		graphControl.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		graphControl.applyLayout();

	}

}
