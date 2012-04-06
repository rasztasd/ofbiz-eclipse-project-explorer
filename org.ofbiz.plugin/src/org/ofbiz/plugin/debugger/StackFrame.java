package org.ofbiz.plugin.debugger;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.ofbiz.plugin.debugger.BeanshellDebugTarget.Client;

public class StackFrame implements IStackFrame {
	private IThread iThread;
	private Client client;
	private String fileName;
	private int lineNumber;
	
	public StackFrame(IThread iThread, Client client) {
		this.iThread = iThread;
		this.client = client;
	}
	
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return iThread.getModelIdentifier();
	}

	@Override
	public IDebugTarget getDebugTarget() {
		// TODO Auto-generated method stub
		return iThread.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		// TODO Auto-generated method stub
		return iThread.getLaunch();
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canStepInto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepOver() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStepping() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void stepInto() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canResume() {
		// TODO Auto-generated method stub
		return iThread.canResume();
	}

	@Override
	public boolean canSuspend() {
		// TODO Auto-generated method stub
		return iThread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return iThread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		iThread.getDebugTarget().resume();
	}

	@Override
	public void suspend() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canTerminate() {
		// TODO Auto-generated method stub
		return iThread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return iThread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		iThread.terminate();
	}

	@Override
	public IThread getThread() {
		// TODO Auto-generated method stub
		return iThread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		// TODO Auto-generated method stub
		List<String> variables2 = client.getVariables();
		IVariable[] variables = new IVariable[variables2.size()];
		for (int i = 0; i < variables2.size(); i++) {
			String string = variables2.get(i);
			final String[] split = string.split(" ");
			variables[i] = new IVariable() {
				
				@Override
				public boolean verifyValue(IValue value) throws DebugException {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean verifyValue(String expression) throws DebugException {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean supportsValueModification() {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public void setValue(IValue value) throws DebugException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void setValue(String expression) throws DebugException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public Object getAdapter(Class adapter) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public String getModelIdentifier() {
					// TODO Auto-generated method stub
					return iThread.getModelIdentifier();
				}
				
				@Override
				public ILaunch getLaunch() {
					// TODO Auto-generated method stub
					return iThread.getLaunch();
				}
				
				@Override
				public IDebugTarget getDebugTarget() {
					// TODO Auto-generated method stub
					return iThread.getDebugTarget();
				}
				
				@Override
				public boolean hasValueChanged() throws DebugException {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public IValue getValue() throws DebugException {
					// TODO Auto-generated method stub
					return new IValue() {
						
						@Override
						public Object getAdapter(Class adapter) {
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public String getModelIdentifier() {
							// TODO Auto-generated method stub
							return iThread.getModelIdentifier();
						}
						
						@Override
						public ILaunch getLaunch() {
							// TODO Auto-generated method stub
							return iThread.getLaunch();
						}
						
						@Override
						public IDebugTarget getDebugTarget() {
							// TODO Auto-generated method stub
							return iThread.getDebugTarget();
						}
						
						@Override
						public boolean isAllocated() throws DebugException {
							// TODO Auto-generated method stub
							return false;
						}
						
						@Override
						public boolean hasVariables() throws DebugException {
							// TODO Auto-generated method stub
							return false;
						}
						
						@Override
						public IVariable[] getVariables() throws DebugException {
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public String getValueString() throws DebugException {
							// TODO Auto-generated method stub
							return split[2];
						}
						
						@Override
						public String getReferenceTypeName() throws DebugException {
							// TODO Auto-generated method stub
							return "Object";
						}
					};
				}
				
				@Override
				public String getReferenceTypeName() throws DebugException {
					// TODO Auto-generated method stub
					return "Object";
				}
				
				@Override
				public String getName() throws DebugException {
					// TODO Auto-generated method stub
					return split[1];
				}
			};
		}
		return variables;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLineNumber() throws DebugException {
		// TODO Auto-generated method stub
		return lineNumber;
	}

	@Override
	public int getCharStart() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCharEnd() throws DebugException {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public String getName() throws DebugException {
		// TODO Auto-generated method stub
		return "Beanshell";
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

}
