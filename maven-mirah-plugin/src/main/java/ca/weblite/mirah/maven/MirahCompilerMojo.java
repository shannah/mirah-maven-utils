/**
 * Copyright 2014 Steve Hannah
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ca.weblite.mirah.maven;

import ca.weblite.asm.WLMirahCompiler;
import com.esotericsoftware.wildcard.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.mirah.maven.*;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Compiles Mirah source files
 *
 * @goal compile
 * @phase process-sources
 * @threadSafe
 * @requiresDependencyResolution compile
 */
public class MirahCompilerMojo extends AbstractMirahMojo {

    /**
     * Java sources directory (in case this project also includes java sources)
     * @parameter expression="src/main/java""
     */
    private String javaSourcePath;
    
    /**
     * Mirah macro sources directory (in case this project also includes macros)
     * @parameter expression="${project.build.directory}/mirah_tmp/macro_sources"
     */
    private String macroSourcePath;
    
    
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp/java_stubs"
     */
    private String javaStubsDirectory;
    
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp/macro_classes"
     */
    private String macroClassesDirectory;
    
    
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp/macro_bootstrap_classes"
     */
    private String macroBootstrapClassesDirectory;
   
    
    /**
     * @parameter expression="lib/mirah-tmp-classes"
     */
    private String mirahClassesOutputDirectory;
    
    /**
     * @parameter expression="lib/mirah-tmp-classes.jar"
     */
    private String mirahClassesJarFile;
    
    
    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;
    
    /**
     * Mirah macro bootstrap directory (bootstrap files for macros)
     * @parameter expression="${project.build.directory}/mirah_tmp/macro_bootstrap_sources"
     */
    private String macroBootstrapPath;
    
    
    private void setupCompiler(WLMirahCompiler c){
        
        c.setClassPath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        c.setMacroClassPath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        
        c.setSourcePath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        File stubsDir = new File(javaStubsDirectory);
        stubsDir.mkdirs();
        c.setJavaStubDirectory(stubsDir);
        c.setDestinationDirectory(new File(outputDirectory));
    }
    
    public void execute() throws MojoExecutionException, CompilationFailureException {
        if ( !pathExists(macroSourcePath)){
            FileUtils.mkdir(macroSourcePath);
        }
        Paths paths = new Paths();
        paths.glob(this.sourceDirectory, "**/macros/**");
        if ( !paths.isEmpty()){
            try {
                paths.copyTo(macroSourcePath);
            } catch ( Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }

            paths = new Paths();
            paths.glob(macroSourcePath, "**/macros/Bootstrap.mirah");
            paths.delete();

            WLMirahCompiler c = new WLMirahCompiler();
            setupCompiler(c);
            c.setSourcePath(macroSourcePath);
            File macroClassesDir = new File(macroClassesDirectory);
            macroClassesDir.mkdirs();
            c.setDestinationDirectory(macroClassesDir);
            try {
                c.compile(new String[]{macroSourcePath});
            } catch (Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }

        }
        
        if ( !pathExists(macroBootstrapPath)){
            FileUtils.mkdir(macroBootstrapPath);
        }
        
        paths = new Paths();
        paths.glob(sourceDirectory, "**/macros/Bootstrap.mirah");
        if ( !paths.isEmpty() ){
            try {
                paths.copyTo(macroBootstrapPath);
            } catch ( Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }
            WLMirahCompiler c = new WLMirahCompiler();
            setupCompiler(c);
            c.setSourcePath(macroBootstrapPath);
            File macroBootstrapClassesDir = new File(macroBootstrapClassesDirectory);
            macroBootstrapClassesDir.mkdirs();
            c.setDestinationDirectory(macroBootstrapClassesDir);
            c.setMacroClassPath(
                    StringUtils.join(classpathElements.iterator(), File.pathSeparator) +
                            File.pathSeparator + macroClassesDirectory
            );
            c.setClassPath(
                    StringUtils.join(classpathElements.iterator(), File.pathSeparator) +
                            File.pathSeparator + macroClassesDirectory
            );
            try {
                c.compile(new String[]{macroBootstrapPath});
            } catch (Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }
            
        }
        
        
        if ( pathExists(sourceDirectory)){
            WLMirahCompiler c = new WLMirahCompiler();
            setupCompiler(c);
            c.setSourcePath(sourceDirectory);
            if ( pathExists(macroSourcePath) && pathExists(macroBootstrapPath)){
                c.setMacroClassPath(
                        StringUtils.join(classpathElements.iterator(), File.pathSeparator) +
                            File.pathSeparator + macroClassesDirectory +
                            File.pathSeparator + macroBootstrapClassesDirectory
                        
                );
                c.setClassPath(
                        StringUtils.join(classpathElements.iterator(), File.pathSeparator) +
                            File.pathSeparator + macroClassesDirectory +
                            File.pathSeparator + macroBootstrapClassesDirectory
                        
                );
            }
            File outputDir = new File(mirahClassesOutputDirectory);
            outputDir.mkdirs();
            c.setDestinationDirectory(outputDir);
            try {
                c.compile(new String[]{sourceDirectory});
            } catch (Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }
            
            // Copy to classes directory 
            File classesDir = new File(outputDirectory);
            classesDir.mkdirs();
            try {
                FileUtils.copyDirectoryStructure(outputDir, classesDir);
            } catch (IOException ex) {
                throw new MojoExecutionException(ex.getMessage(), ex);
            }
            
            // Generate jar file with mirah classes only
            File mirahJar = new File(mirahClassesJarFile);
            mirahJar.getParentFile().mkdirs();
            try {            
                createJar(outputDir, outputDir.getPath(), mirahJar );
            } catch (IOException ex) {
                throw new MojoExecutionException(ex.getMessage(), ex);
            }
            
        }
    }
    
    private boolean pathExists(String path){
        String[] parts = path.split(Pattern.quote(File.pathSeparator));
        for ( String part : parts ){
            File f = new File(part);
            if (f.exists()){
                return true;
            }
        }
        return false;
    }
    
    private void createJar(File source, String sourceRoot, File jarFile) throws IOException {
        FileOutputStream fos = null;
        JarOutputStream jos = null;
        try {
            fos = new FileOutputStream(jarFile);
            jos = new JarOutputStream(fos);
            jos.setLevel(0);
            
            addToJar(source, sourceRoot, jos);
        } finally {
            try {
                if ( jos != null ) jos.close();
            } catch ( Throwable t ){}
            try {
                if ( fos != null ) fos.close();
            } catch ( Throwable t){}
        }
              
        
        
    }
    
    private void addToJar(File source, String sourceRoot, JarOutputStream jos) throws IOException {
        if ( source.getName().endsWith(".class")){
            String fileName = formatEntry(source, sourceRoot, false);
            System.out.println("Adding file "+fileName+" to jar ("+source+")");
            ZipEntry entry = new ZipEntry(fileName);
            jos.putNextEntry(entry);
            InputStream fis = null;
            try {
                fis = new FileInputStream(source);
                byte[] buf = new byte[4096];
                int len;
                while ( (len = fis.read(buf)) != -1 ){
                    System.out.println("Writing "+len+" bytes");
                    jos.write(buf, 0, len);
                }
                jos.closeEntry();
            } finally {
                try {
                    if ( fis != null ){
                        fis.close();
                    }
                } catch ( Exception ex){}
            }
        } else if ( source.isDirectory() ){
            String dirName = formatEntry(source, sourceRoot, true);
            System.out.println("Adding "+dirName+" to jar");
            //ZipEntry entry = new ZipEntry(dirName);
            //jos.putNextEntry(entry);
            for ( File child : source.listFiles()){
                addToJar(child, sourceRoot, jos);
            }
            //jos.closeEntry();
        }
    }
    
    private String formatEntry(File f, String sourceRoot, boolean directory){
        if ( directory ){
            String name = f.getPath().substring(sourceRoot.length());
            name = name.replace("\\", "/");
            if ( !name.endsWith("/")){
                name += "/";
            }
            if ( name.startsWith("/")){
                name = name.substring(1);
            }
            return name;
        } else {
            String name = f.getPath().substring(sourceRoot.length());
            name = name.replace("\\", "/");
            
            if ( name.startsWith("/")){
                name = name.substring(1);
            }
            return name;
        }
    }
    
    
}
