package org.holodeckb2b.commons.testing;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains utility method to easily access resources when running tests. 
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 */
public class TestUtils {

    /**
     * Returns path to the specified file first looking in the <code>test/resources/<i>«class name of caller»</i></code> 
     * directory of the project or when this directory does not exist in the <code>test/resources</code> directory. The
     * <i>«class name of the caller»</i> here is the lower case simple class name.
     * 
     * @param fileName	name of the file to get path of
     * @return	path to the file
     */
    public static Path getTestResource(String fileName) {
    	return getTestClassBasePath().resolve(fileName);
    }
    
    /**
     * Returns the path of the resource directory for the current test. This directory is defined as 
     * <code>test/resources/<i>«class name of caller»</i></code> of the project or when this directory does not exist 
     * the <code>test/resources</code> directory. The <i>«class name of the caller»</i> here is the lower case simple 
     * class name.
     * 
     * @return  path to the resource directory for the current test
     */
    public static Path getTestClassBasePath() {
    	final StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
    	String clsName = trace[1].getClassName();
    	if (TestUtils.class.getName().equals(clsName))
    		clsName = trace[2].getClassName();
    			
    	Path clsPath = getResourcePath(clsName.substring(clsName.lastIndexOf('.') + 1).toLowerCase());
    	return clsPath != null ? clsPath : getResourcePath("."); 
    }     

    /**
     * Returns a multi platform path to the specified file in the <code>test/resources</code> directory of the project.
     * <p>
     * Is needed mainly in Windows OS to bypass the problem discribed here: 
     * http://stackoverflow.com/questions/6164448/convert-url-to-normal-windows-filename-java
     * 
     * @param file 	path to the file we want to get
     * @return path to the file, or <code>null</code> if the file does not exist
     */
    private static Path getResourcePath(String file) {
        try {
            URL url = TestUtils.class.getClassLoader().getResource(file);
            return url == null ? null : Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }    
}

