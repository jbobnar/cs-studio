package org.csstudio.utility.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.csstudio.ui.util.NoResourceEditorInput;
import org.csstudio.ui.util.ResourceUtil;
import org.csstudio.utility.product.IWorkbenchWindowAdvisorExtPoint;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IFileUtilStartup implements IWorkbenchWindowAdvisorExtPoint{

	@Override
	public void preWindowOpen() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean preWindowShellClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void postWindowRestore() throws WorkbenchException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("External Files");
		if(project.exists()){
			File directory = project.getLocation().toFile();
			List<File> partFiles = new ArrayList<>();
			if (directory.isDirectory()){
				List<File> files = Arrays.asList(directory.listFiles());
				for(IWorkbenchWindow workbenchWindow:PlatformUI.getWorkbench().getWorkbenchWindows()){
					IWorkbenchPage[] workbenchPages = workbenchWindow.getPages();

					for(IWorkbenchPage workbenchPage : workbenchPages){
						for(File file: files){
							IFile iFile = IFileUtil.getInstance().createFileResource(file);
							IEditorPart editPart = org.eclipse.ui.ide.ResourceUtil.findEditor(workbenchPage,iFile);
							if(editPart!=null){
								IFileUtil.getInstance().registerPart(editPart, iFile);
								partFiles.add(file);
							} 
								// can have the same file open in view
								// BOY has a input attached to a view
								// Look in the memento for the ids or inspect ... hmmm (both bad)

									for(IViewReference partRef: workbenchPage.getViewReferences()){
										IWorkbenchPart viewPart = partRef.getPart(false);
										if (viewPart!=null){
										try {
											Field f = viewPart.getClass().getDeclaredField("input");
											f.setAccessible(true);
											IEditorInput element = (IEditorInput)f.get(viewPart);
											Field f2 = element.getClass().getDeclaredField("path");
											f2.setAccessible(true);
											Path path = (Path)f2.get(element);
											if(file.getPath().endsWith(path.toOSString())){
												IFileUtil.getInstance().registerPart(viewPart, iFile);
												partFiles.add(file);
											}
										} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
											// I don't care if these fail, best attempt to read boy view files
											
											// e1.printStackTrace();
										}
										}
									
								
							}
						}
					}
				}
				// cleanup stray files in project
				for(File file:files){
					if(!partFiles.contains(file) && !file.getName().equals(".project"))
						file.delete();
				}
			}
		}
	}

	@Override
	public void postWindowCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postWindowOpen() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postWindowClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IStatus saveState(IMemento memento) {

		return Status.OK_STATUS;
	}

	@Override
	public IStatus restoreState(IMemento memento) {
		
//		Maybe use memento? register windows, remove them from memento if there is no file
//		Field f;
//		Element element = null;
//		try {
//			f = memento.getClass().getDeclaredField("element");
//			f.setAccessible(true); 
//			element = (Element)f.get(memento);
//		} catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException e) {
//			
//			e.printStackTrace();
//		} 
//		NodeList list = element.getElementsByTagName("editor"); 
//
//		for(int i = 0; i > list.getLength(); i++) { 
//			if(((Element)list.item(i)).getAttribute("id").equals("org.csstudio.utility.file"))
//				element.removeChild(list.item(i)); 
//		} 
	
		return Status.OK_STATUS;
	}



}
