/*
 * Copyright (C) 2016 loara
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package comp.code.template;

import comp.parser.template.TemplateEle;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author loara
 * @param <T>
 */
public class TList<T> {
    protected final HashSet<T> val;
    protected HashSet<Notifica> nos;
    public TList(){
        val=new HashSet<>();
        nos=null;
    }
    public boolean add(T va){
        return val.add(va);
    }
    public void setHashNotif(HashSet<Notifica> n){
        nos=n;
    }
    protected Notifica isIn(String name, TemplateEle[] param){
        for(Notifica n:nos){
            if(n.nome.equals(name)){
                if(Arrays.equals(param, n.parametri))
                    return n;
            }
        }
        return null;
    }
    public boolean addAll(T[] vas){
        boolean add=true;
        for(T td:vas){
            add=val.add(td)&&add;
        }
        return add;
    }
    public HashSet<T> loaded(){
        return val;
    }
    public HashSet<Notifica> notifiche(){
        return nos;
    }
    public void clearAll(){
        val.clear();
    }
}
