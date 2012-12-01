package org.ofbiz.plugin.handlers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.CharBuffer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ofbiz.plugin.ofbiz.IEntity;

public class QuaryEntityHandler implements IObjectActionDelegate{

	private IWorkbenchPart targetPart;
	private ISelection selection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void run(final IAction action) {
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		assert treeSelect instanceof EObject;
		IEntity component = (IEntity) treeSelect;
		Socket socket = null;
		try {
			socket = new Socket("localhost", 9990);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
			outputStreamWriter.write("import java.util.*;" +
					"import org.ofbiz.entity.GenericDelegator;" +
					"import org.ofbiz.service.GenericDispatcher;" +
					"import org.ofbiz.base.util.*;" +
					"import org.ofbiz.entity.util.*;" +
					"delegator = GenericDelegator.getGenericDelegator(\"default\");" +
					"dispatcher = GenericDispatcher.getLocalDispatcher(\"ecommerce\", delegator);" +
					"dctx =  dispatcher.getDispatchContext();" +
					"admin = delegator.findByPrimaryKey(\"UserLogin\", UtilMisc.toMap(\"userLoginId\", \"admin\"));");
			InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
			outputStreamWriter.flush();
			Thread.sleep(500);
			while (inputStreamReader.ready()) {
				int responseByte = inputStreamReader.read();
			}
			
			outputStreamWriter.write("entity = delegator.findAll(\""+ component.getName() + "\"); for (int i=0; i<entity.size();i++) {print(entity.get(i).get(1));}");
			outputStreamWriter.flush();
			final StringBuffer inputBuff = new StringBuffer();
			Thread.sleep(1000);
			while (inputStreamReader.ready()) {
				int responseByte = inputStreamReader.read();
				inputBuff.append((char) responseByte);
			}
			System.out.println(inputBuff);
			Dialog dialog = new Dialog(new Shell()) {

				@Override
				protected Control createDialogArea(Composite parent) {
					// TODO Auto-generated method stub
					new Label(parent, 0).setText(inputBuff.toString().substring(0, inputBuff.toString().lastIndexOf("\n") + 1));
					return super.createDialogArea(parent);
				}
								
			};
			dialog.open();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
