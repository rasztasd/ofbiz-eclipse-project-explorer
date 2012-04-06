/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Author: atotic
 * Created on Mar 23, 2004
 */
package org.ofbiz.plugin.debugger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
/**
 * Debugger class that represents a single python process.
 * 
 * It deals with events from RemoteDebugger.
 * Breakpoint updating.
 */
public class BeanshellDebugTarget implements IDebugTarget {
	//private ILaunch launch;
	public final IProject project;
	private ILaunch launch;
	private Thread clientThread;
	private Client client;
	private boolean isTerminated = false;
	private IThread iThread;
	volatile StackFrame stackFrame;
	

	public static class Client {
		private Socket socket;
		InputStream inputStream;
		OutputStream outputStream;
//		OutputStreamWriter outputStreamWriter;
		ObjectOutputStream printWriter;
		ObjectInputStream inputReader;
		volatile boolean isDebugging;
		boolean connected = false;
		boolean terminate = false;
		BeanshellDebugTarget target;
		
		public Client(BeanshellDebugTarget target) {
			this.target = target;
		}
		
		public void run() {
			try {
				socket = new Socket("localhost", 12226);
				connected = true;
				inputStream = socket.getInputStream();
				inputReader = new ObjectInputStream(inputStream);
				outputStream = socket.getOutputStream();
//				outputStreamWriter = new OutputStreamWriter(outputStream);
				printWriter = new ObjectOutputStream(outputStream);
				if (printWriter == null) {
					target.isTerminated = true;
					target.launch.terminate();
					return;
				}
				Thread threadIn = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while (true && !terminate) {
							try {
								if (inputStream.available() > 0) {
									String readLine = inputReader.readUTF();
									if (readLine.equals("stopped")) {
										isDebugging = false;
										DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(target.iThread, DebugEvent.TERMINATE)});
									} else if (readLine.startsWith("started")) {
										isDebugging = true;
										String[] split = readLine.split(" ");
										String line = split[1];
										String file = split[2];
										target.stackFrame.setFileName(file);
										target.stackFrame.setLineNumber(Integer.valueOf(line));
										DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(target.iThread, DebugEvent.CREATE), new DebugEvent(target.stackFrame, DebugEvent.CREATE), new DebugEvent(target.iThread, DebugEvent.SUSPEND), new DebugEvent(target.stackFrame, DebugEvent.SUSPEND, DebugEvent.BREAKPOINT)});
									}
								} else {
									Thread.sleep(500);
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				threadIn.start();
			} catch (UnknownHostException e) {
				connected = false;
				target.isTerminated = true;
			} catch (IOException e) {
				connected = false;
				target.isTerminated = true;
			} catch (DebugException e) {
				connected = false;
				target.isTerminated = true;
			}
		}
		public void setBreakpoint(String filePath, int lineNum) {
			StringBuilder sb = new StringBuilder("set");
			sb.append(" ").append(lineNum).append(" ").append(filePath);
			try {
				printWriter.writeUTF(sb.toString());
				printWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void clearBreakpoint(String filePath, int lineNum) {
			StringBuilder sb = new StringBuilder("clear");
			sb.append(" ").append(lineNum).append(" ").append(filePath);
			try {
				printWriter.writeUTF(sb.toString());
				printWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void step() {
			String command = "step";
			try {
				printWriter.writeUTF(command);
				printWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void resume() {
			String command = "resume";
			try {
				printWriter.writeUTF(command);
				printWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void exit() {
			String command = "close";
			try {
				printWriter.writeUTF(command);
				printWriter.flush();
				target.isTerminated = true;
				terminate = true;
				finalize();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void finalize() {
			if (socket != null) {
				try {
					socket.close();		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public List<String> getVariables() {
			List<String> retValue = new ArrayList<String>();
			try {
				printWriter.writeUTF("vars");
				printWriter.flush();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (inputReader.available() > 0) {
					String readUTF = inputReader.readUTF();
					if (!readUTF.startsWith("var")) {
						retValue.add(readUTF);
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return retValue;
		}

	}

	public BeanshellDebugTarget(final ILaunch launch, IProject project) {
		this.launch = launch;
		this.project = project;
		client = new Client(this);
		client.run();
		iThread = new IThread() {

			@Override
			public void terminate() throws DebugException {
				BeanshellDebugTarget.this.terminate();
				launch.terminate();
			}

			@Override
			public boolean isTerminated() {
				return BeanshellDebugTarget.this.isTerminated();
			}

			@Override
			public boolean canTerminate() {
				return BeanshellDebugTarget.this.canTerminate();
			}

			@Override
			public void stepReturn() throws DebugException {
				// TODO Auto-generated method stub
				client.step();

			}

			@Override
			public void stepOver() throws DebugException {
				// TODO Auto-generated method stub
				client.step();

			}

			@Override
			public void stepInto() throws DebugException {
				client.step();
			}

			@Override
			public boolean isStepping() {
				// TODO Auto-generated method stub
				return client.isDebugging;
			}

			@Override
			public boolean canStepReturn() {
				// TODO Auto-generated method stub
				return client.isDebugging;
			}

			@Override
			public boolean canStepOver() {
				// TODO Auto-generated method stub
				return client.isDebugging;
			}

			@Override
			public boolean canStepInto() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void suspend() throws DebugException {
				// TODO Auto-generated method stub

			}

			@Override
			public void resume() throws DebugException {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isSuspended() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.isSuspended();
			}

			@Override
			public boolean canSuspend() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.canSuspend();
			}

			@Override
			public boolean canResume() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.canResume();
			}

			@Override
			public Object getAdapter(Class adapter) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getModelIdentifier() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.getModelIdentifier();
			}

			@Override
			public ILaunch getLaunch() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.getLaunch();
			}

			@Override
			public IDebugTarget getDebugTarget() {
				// TODO Auto-generated method stub
				return BeanshellDebugTarget.this.getDebugTarget();
			}

			@Override
			public boolean hasStackFrames() throws DebugException {
				// TODO Auto-generated method stub
				return client.isDebugging;
			}

			@Override
			public IStackFrame getTopStackFrame() throws DebugException {
				// TODO Auto-generated method stub
				return getStackFrames().length > 0?getStackFrames()[0]:null;
			}

			@Override
			public IStackFrame[] getStackFrames() throws DebugException {
				// TODO Auto-generated method stub
				if (!client.isDebugging) {
					return new IStackFrame[0];
				} else {
					return new IStackFrame[] {stackFrame};
				}
			}

			@Override
			public int getPriority() throws DebugException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getName() throws DebugException {
				// TODO Auto-generated method stub
				return "beanshell thread";
			}

			@Override
			public IBreakpoint[] getBreakpoints() {
				// TODO Auto-generated method stub
				return new IBreakpoint[0];
			}
		};
		stackFrame = new StackFrame(iThread, client);
		// we have to know when we get removed, so that we can shut off the debugger        
	}

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return "org.ofbiz.plugin";
	}

	@Override
	public IDebugTarget getDebugTarget() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		// TODO Auto-generated method stub
		return launch;
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canTerminate() {
		// TODO Auto-generated method stub
		return !isTerminated;
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return isTerminated;
	}

	@Override
	public void terminate() throws DebugException {
		client.exit();
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, DebugEvent.TERMINATE)});
		isTerminated = true;
	}

	@Override
	public boolean canResume() {
		// TODO Auto-generated method stub
		return client.isDebugging;
	}

	@Override
	public boolean canSuspend() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return client.isDebugging;
	}

	@Override
	public void resume() throws DebugException {
		client.resume();
	}

	@Override
	public void suspend() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		try {
			IMarker marker = breakpoint.getMarker();
			client.setBreakpoint(marker.getResource().getFullPath().toString(), (Integer)marker.getAttribute(IMarker.LINE_NUMBER));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
		try {
			IMarker marker = breakpoint.getMarker();
			client.clearBreakpoint(marker.getResource().getFullPath().toString(), (Integer)marker.getAttribute(IMarker.LINE_NUMBER));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcess getProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return new IThread[] {
				iThread
		};
	}	

	@Override
	public boolean hasThreads() throws DebugException {
		// TODO Auto-generated method stub
		return client.isDebugging;
	}

	@Override
	public String getName() throws DebugException {
		// TODO Auto-generated method stub
		return "Beanshell debug TODO";
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
		return true;
	}        


}
