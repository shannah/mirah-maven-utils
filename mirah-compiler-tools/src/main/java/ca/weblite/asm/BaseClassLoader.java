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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class BaseClassLoader implements ClassLoader {

    ClassLoader parent;
    Context context;
    
    public BaseClassLoader(Context context, ClassLoader parent){
        this.parent = parent;
        this.context = context;
        context.set(ClassLoader.class, this);
    }
    
    @Override
    public ClassNode findClass(Type type) {
        if ( parent != null ){
            return parent.findClass(type);
        }
        return null;
    }

    @Override
    public ClassNode findStub(Type type) {
         if ( parent != null ){
             return parent.findStub(type);
         }
        return null;
    }
    
    public ClassLoader getParent(){
        return parent;
    }
    
    public Context getContext(){
        return context;
    }
    
}
