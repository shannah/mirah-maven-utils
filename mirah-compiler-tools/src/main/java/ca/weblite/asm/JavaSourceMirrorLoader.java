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
package ca.weblite.asm;

import org.mirah.jvm.mirrors.ArrayType;
import org.mirah.jvm.mirrors.BytecodeMirror;
import org.mirah.jvm.mirrors.MirrorType;
import org.mirah.jvm.mirrors.OrErrorLoader;
import org.mirah.jvm.mirrors.SimpleMirrorLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaSourceMirrorLoader extends SimpleMirrorLoader {
    private final org.mirah.util.Context context;
    private final JavaSourceClassLoader loader;
    private final OrErrorLoader ancestorLoader;
    
    public JavaSourceMirrorLoader(
            org.mirah.util.Context context,
            JavaSourceClassLoader resourceLoader,
            org.mirah.jvm.mirrors.MirrorLoader parent){
        super(parent);
        this.context = context;
        this.loader = resourceLoader;
        this.ancestorLoader = new OrErrorLoader(this);
    }
    
    @Override
    public MirrorType findMirror(Type type){
        MirrorType out = super.findMirror(type);
        if ( out != null ){
            return out;
        }
        
        
        
        if ( type.getSort() == Type.ARRAY ){
            return findArrayMirror(
                    Type.getType(type.getDescriptor().substring(1))
            );
        }
        
        ClassNode node = loader.findClass(type);
        if ( node != null ){
            BytecodeMirror mirror = new BytecodeMirror(context,
                    node,
                    ancestorLoader
            );
            return mirror;
        }
        return null;
        
        
    }
    
    public MirrorType findArrayMirror(Type type){
        MirrorType component = loadMirror(type);
        if ( component != null ){
            return new ArrayType(context, component);
        }
        return null;
    }
}
