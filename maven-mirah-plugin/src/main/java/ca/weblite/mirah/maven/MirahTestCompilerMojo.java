package ca.weblite.mirah.maven;


import ca.weblite.asm.WLMirahCompiler;
import com.esotericsoftware.wildcard.Paths;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.TestCompilerMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.codehaus.plexus.util.FileUtils;

import org.codehaus.plexus.util.StringUtils;

import org.mirah.MirahCommand;

/**
 * Compiles Mirah source files
 *
 * @extendsPlugin compiler
 * @goal testCompile
 * @phase process-test-sources
 * @threadSafe
 * @requiresDependencyResolution test
 */
public class MirahTestCompilerMojo extends TestCompilerMojo {
    
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp_test/java_stubs"
     */
    private String javaStubsDirectory;
    
    
    /**
     * Mirah macro sources directory (in case this project also includes macros)
     * @parameter expression="${project.build.directory}/mirah_tmp_test/macro_sources"
     */
    private String macroSourcePath;
    
    
   
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp_test/macro_classes"
     */
    private String macroClassesDirectory;
    
    
    /**
     * @parameter expression="${project.build.directory}/mirah_tmp_test/macro_bootstrap_classes"
     */
    private String macroBootstrapClassesDirectory;
   
    
    /**
     * @parameter expression="lib/mirah-tmp-classes-test"
     */
    private String mirahClassesOutputDirectory;
    
    /**
     * @parameter expression="lib/mirah-tmp-classes-test.jar"
     */
    private String mirahClassesJarFile;
    
    
    /**
     * Project classpath.
     *
     * @parameter default-value="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;
    /**
     * The source directories containing the sources to be compiled.
     *
     * @parameter default-value="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> compileSourceRoots;
    /**
     * Classes destination directory
     * @parameter expression="${project.build.testOutputDirectory}"
     */
    private File outputDirectory;
    /**
     * Classes source directory
     * @parameter expression="src/test/java"
     */
    private File sourceDirectory;
    /**
     * Whether produce bytecode or java source
     * @parameter bytecode, default true
     */
    private boolean bytecode = true;
    /**
     * Show log
     * @parameter verbose, default false
     */
    private boolean verbose;

    /**
     * Mirah macro bootstrap directory (bootstrap files for macros)
     * @parameter expression="${project.build.directory}/mirah_tmp_test/macro_bootstrap_sources"
     */
    private String macroBootstrapPath;
    
    
    
    public void execute() throws MojoExecutionException, CompilationFailureException {
        
       if ( !pathExists(macroSourcePath)){
            FileUtils.mkdir(macroSourcePath);
        }
        Paths paths = new Paths();
        paths.glob(this.sourceDirectory.getPath(), "**/macros/**");
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
        paths.glob(sourceDirectory.getPath(), "**/macros/Bootstrap.mirah");
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
        
        
        if ( pathExists(sourceDirectory.getPath())){
            WLMirahCompiler c = new WLMirahCompiler();
            setupCompiler(c);
            c.setSourcePath(sourceDirectory.getPath());
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
                c.compile(new String[]{sourceDirectory.getPath()});
            } catch (Throwable t){
                throw new MojoExecutionException(t.getMessage(), t);
            }
            
            // Copy to classes directory 
            File classesDir = new File(outputDirectory.getPath());
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

    
    
    
    private void setupCompiler(WLMirahCompiler c){
        
        c.setClassPath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        c.setMacroClassPath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        
        c.setSourcePath(StringUtils.join(classpathElements.iterator(), File.pathSeparator));
        File stubsDir = new File(javaStubsDirectory);
        stubsDir.mkdirs();
        c.setJavaStubDirectory(stubsDir);
        c.setDestinationDirectory(outputDirectory);
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
