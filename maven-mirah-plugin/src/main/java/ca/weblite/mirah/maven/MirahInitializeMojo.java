/*
 * The MIT License
 *
 * Copyright 2014 shannah.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ca.weblite.mirah.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Compiles Mirah source files
 *
 * @goal initialize
 * @phase validate
 * @threadSafe
 */
public class MirahInitializeMojo extends AbstractMojo {

    /**
     * @parameter expression="${project.build.directory}/mirah_tmp/classes.jar"
     */
    private String mirahClassesJarFile;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("Executeing Initialize mojo");
        File f = new File(mirahClassesJarFile);
        if ( f.exists()){
            return;
            
        }
        f.getParentFile().mkdirs();
        FileOutputStream fos = null;
        JarOutputStream jos = null;
        try {
            fos = new FileOutputStream(f);
            jos = new JarOutputStream(fos);
            jos.setComment("Empty jar");
            
            
        } catch (IOException ex) {
            throw new MojoFailureException("Failed to create tmp classes", ex);
        } finally {
            try {
                if ( jos != null ){
                    jos.close();
                } 
            } catch ( Exception ex){}
            
            try {
                if ( fos != null ){
                    fos.close();
                }
            } catch ( Exception ex){}
        }
        
    }
    
}
