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
package comp.code.vars;

import comp.code.CodeException;
import comp.code.TypeElem;
import comp.code.Types;
import comp.general.Lingue;
import comp.parser.TypeName;
import java.util.ArrayList;

/**
 * Permette di tener conto delle variabili aggiunte in maniera astratta. Utilizzare solo
 * in validating
 * @author loara
 */
public class GhostVar {
    public static class GVarEle{
        public GVarEle(boolean a, String b, TypeName t)
                throws CodeException{
            var=a;//se è una variabile o un blocco
            name=b;
            if(t==null){
                type=null;
            }
            else{
                type=Types.getIstance().find(t, true);//è GhostVar
            }
        }
        public final boolean var;
        public final String name;
        public final TypeElem type;
    }
    private final ArrayList<GVarEle> al;
    public GhostVar(){
        al=new ArrayList<>();
    }
    public GVarEle get(String ident)throws CodeException{
        GVarEle f=null;
        for(int i=al.size()-1; i>=0; i--){//meglio
            if(al.get(i).var&&al.get(i).name.equals(ident)){
                f=al.get(i);
                break;
            }
        }
        if(f==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
        return f;
    }
    public boolean isIn(String ident){
        GVarEle f=null;
        for(int i=al.size()-1; i>=0; i--){//meglio
            if(al.get(i).var&&al.get(i).name.equals(ident)){
                f=al.get(i);
                break;
            }
        }
        return f != null;
    }
    public void addVar(TypeName t, String n)throws CodeException{
        al.add(new GVarEle(true, n, t));
    }
    public TypeElem getType(String n)throws CodeException{
        GVarEle ge=get(n);
        return ge.type;
    }
    /**
     * aggiunge un blocco di variabili. tutte le variabili dichiarate in seguito rimarranno nel blocco
     * fino alla sua eliminazione
     */
    public void addBlock(){
        try{
            al.add(new GVarEle(false, "block", null));
        }
        catch(CodeException e){}//inutilizzato
    }
    /**
     * rimuove un blocco e le variabili al suo interno. Per salvaguardare il loro scope
     */
    public void removeBlock(){
        GVarEle e=al.remove(al.size()-1);
        while(!(e.name.equals("block") && !e.var)){
            e=al.remove(al.size()-1);
        }
    }
}
