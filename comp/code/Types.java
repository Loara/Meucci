/*
 * Copyright (C) 2015 loara
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
package comp.code;

import comp.code.template.ClassList;
import comp.code.template.Substitutor;
import comp.general.Info;
import comp.parser.Membro;
import comp.parser.TypeDef;
import comp.parser.TypeName;
import comp.parser.template.TypTemplate;
import java.util.HashSet;

/**
 * Immagazzina tutte le informazioni sui tipi
 * @author loara
 */
public class Types {
    private static Types t;
    public static Types getIstance(){
        if(t==null)
            t=new Types();
        return t;
    }
    private final HashSet<TypeElem> elems, Telems;//Tipi template e tipi con parametri template
    private final ClassList cl;
    private Substitutor suds;
    public void clearAll(){
        elems.clear();
        Telems.clear();
        cl.clearAll();
        suds=null;
        addPrim();
    }
    private Types(){
        elems=new HashSet<>();
        addPrim();
        cl=new ClassList();
        Telems=new HashSet<>();
        suds=null;
    }
    private void addPrim(){
        for(String tt:Info.primitive()){
            elems.add(new TypeElem(tt));
        }
        elems.add(new TypeElem("void"));
        elems.add(new TypeElem(":null"));
    }
    public void setSubstitutor(Substitutor sub){
        suds=sub;
    }
    public Substitutor getSubstitutor(){
        return suds;
    }
    /**
     * Carica un tipo. Decide automaticamente dove inserirlo
     * @param e
     * @param ext
     * @return
     * @throws CodeException 
     */
    public boolean load(TypeDef e, boolean ext)throws CodeException{
        if(e.templates().length!=0){
            if(!ext)
                return cl.add(e);
        }
        return elems.add(new TypeElem(e, ext));
    }
    public boolean load(TypeElem e){
        return elems.add(e);
    }
    public boolean loadTemplate(TypTemplate td)throws CodeException{
        td.validate();
        boolean b=Telems.add(new TypeElem(td.getIdent(), td.ext(), new Membro[0],
                false, td.isRef(), td.isNum()));//non è mai esterno
        return b;
    }
    public boolean removeTemplate(TypTemplate td){
        String n=td.getIdent();
        for(TypeElem te:Telems){
            if(te.name.equals(n)){
                Telems.remove(te);
                return true;
            }
        }
        return false;
    }
    public void removeAllTemplate(){
        Telems.clear();
    }
    public TypeElem find(String t)throws CodeException{
        for(TypeElem i:elems){
            if(t.equals(i.name))
                return i;
        }
        //Serve per i tipi template all'interno dei validate
        for(TypeElem i:Telems){
            if(t.equals(i.name))
                return i;
        }
        throw new CodeException("Tipo sconosciuto: "+t);
    }
    public TypeName translate(TypeName tn)throws CodeException{
        if(suds != null)
            return suds.recursiveGet(tn);
        else
            return tn;
    }
    /*
    il validate serve a sapere se è richiamato all'interno di un validate,
    nel qual caso non deve essere aggiunta la notifica in ClassList
    */
    public TypeElem find(TypeName t, boolean validate)throws CodeException{
        if(suds!=null){
            t=suds.recursiveGet(t);
        }
        if(t.templates().length==0){
            return find(t.getName());
        }
        String mname=Meth.className(t);
        for(TypeElem i:Telems){
            if(i.name.equals(mname))
                return i;
        }
        if(t.templates().length==0){
            throw new CodeException("Impossibile trovare il tipo "+t.getName());
        }
        TypeElem tt=cl.generate(t.getName(), t.templates(), validate);
        if(tt==null)
            throw new CodeException("Impossibile trovate classe template di nome "
            +t.getName());
        Telems.add(tt);
        return tt;
    }
    /*
    Controlla solo l'esistenza del tipo, non lo definisce. Utile nei validate
    per ridurre i tempi
    */
    public void esiste(String t)throws CodeException{
        for(TypeElem i:elems){
            if(t.equals(i.name))
                return;
        }
        for(TypeElem i:Telems){
            if(t.equals(i.name))
                return;
        }
        throw new CodeException("Tipo sconosciuto: "+t);
    }
    public void esiste(TypeName t)throws CodeException{
        if(suds!=null){
            t=suds.recursiveGet(t);
        }
        if(t.templates().length==0){
            esiste(t.getName());
            return;
        }
        String mname=Meth.className(t);
        for(TypeElem i:Telems){
            if(i.name.equals(mname))
                return;
        }
        cl.esiste(t.getName(), t.templates());
    }
    public ClassList getClassList(){
        return cl;
    }
}
