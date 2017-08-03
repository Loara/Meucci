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

import comp.code.template.FunzList;
import comp.code.template.Substitutor;
import comp.general.Lingue;
import comp.parser.Callable;
import comp.parser.TypeName;
import comp.parser.template.TemplateEle;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author loara
 */
public class Funz {
    private final HashSet<FElement> s;
    private final FunzList fl;
    private Substitutor suds;
    public void clearAll(){
        s.clear();
        fl.clearAll();
        glob.clear();
        ext.clear();
        suds=null;
    }
    public void clearGE(){
        glob.clear();
        ext.clear();
    }
    private Funz(){
        s=new HashSet<>();
        fl=new FunzList();
        suds=null;
    }
    private static Funz f;
    public static Funz getIstance(){
        if(f==null)
            f=new Funz();
        return f;
    }
    public void setSubstitutor(Substitutor sub){
        suds=sub;
    }
    /*
    public void load(Callable f, boolean ext)throws CodeException{
        if(f.templates().length!=0){
            fl.add(f);
            return;
        }
        FElement fe=new FElement(f, ext, new TemplateEle[0]);
        s.add(fe);
    }
    */
    public boolean load(FElement f){
        return s.add(f);
    }
    public boolean loadTemplate(Callable c){
        return fl.add(c);
    }
    public boolean loadAll(FElement[] e){
        boolean a=true;
        for(FElement tt:e){
            a=a && s.add(tt);
        }
        return a;
    }
    public boolean loadAllTemplates(Callable[] td){
        return fl.addAll(td);
    }
    /*
    noAdd serve a FunzList per non aggiungere la funzione (e quindi i relativi tipi a
    ClassList)
    i typeelem sono già sostituiti
    */
    public FElement request(String name, TypeElem[] types, boolean noAdd
            , TemplateEle... ete)throws CodeException{
        TemplateEle[] te;
        if(suds!=null && suds.size()>0)
            te=suds.recursiveGet(ete);
        else
            te=ete;
        ArrayList<FElement> e=new ArrayList<>();
        for(FElement i:s){
            if(!i.name.equals(Meth.funzKey(name, te)))//equivalenza sia di nome che template
                continue;
            if(types.length!=i.trequest.length)
                continue;
            boolean jus=true;
            for(int ij=0; ij<types.length; ij++){
                if(!types[ij].ifEstende(i.trequest[ij], noAdd)){
                    jus=false;
                    break;
                }
            }
            if(!jus)
                continue;
            e.add(i);
        }
        if(e.size()==1){
            return e.get(0);
        }
        else {
            if(e.isEmpty() && te.length>0){
                FElement fer=fl.generate(name, te, types, noAdd);
                s.add(fer);
                return fer;
            }
            String err=Lingue.getIstance().format("m_cod_foufunn", e.size(), 
                    Meth.funzKey(name, te))+":\n";
            err = e.stream().map((ex) -> ex.modname+"\n").reduce(err, String::concat);
            throw new CodeException(err);
        }
    }
    public FElement request(String modname)throws CodeException{
        if(modname==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_funnfon"));
        for(FElement e:s){
            if(modname.equals(e.modname))
                return e;
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_funnfon"));
    }
    public FElement requestDestructor(TypeName tn, boolean noAdd)throws CodeException{
        TypeElem te=Types.getIstance().find(tn, noAdd);
        return request(Meth.destructorName(tn), new TypeElem[]{te}, noAdd, tn.templates());
    }
    
    public FElement requestDestructor(String name, TemplateEle[] tel, boolean noAdd)throws CodeException{
        TypeElem te=Types.getIstance().find(new TypeName(name, tel), noAdd);
        return request(Meth.destructorName(name), new TypeElem[]{te}, noAdd, tel);
    }
    /**
     * params NON contiene il tipo del costruttore, deve essere dedotto
     * fname NON inizia con init_
     * 
     * @param fname
     * @param tem
     * @param params
     * @param noAdd
     * @return
     * @throws CodeException 
     */
    public FElement requestCostructor(String fname, TemplateEle[] tem, TypeElem[] params, 
            boolean noAdd)throws CodeException{
        TemplateEle[] te;
        if(suds!=null && suds.size()>0)
            te=suds.recursiveGet(tem);
        else
            te=tem;
        ArrayList<FElement> e=new ArrayList<>();
        for(FElement i:s){
            if(!i.name.equals(Meth.funzKey("init_"+fname, te)))//equivalenza sia di nome che template
                continue;
            if(params.length!= (i.trequest.length-1))
                continue;
            boolean jus=true;
            for(int ij=0; ij<params.length; ij++){
                if(!params[ij].ifEstende(i.trequest[ij+1], noAdd)){
                    jus=false;
                    break;
                }
            }
            if(!jus)
                continue;
            e.add(i);
        }
        if(e.size()==1){
            return e.get(0);
        }
        else {
            if(e.isEmpty() && te.length>0){
                FElement fer=fl.generateCostructor(fname, tem, params, noAdd);
                s.add(fer);
                return fer;
            }
            String err=Lingue.getIstance().format("m_cod_foufunn", e.size(), 
                    Meth.funzKey("init_"+fname, te))+":\n";
            err = e.stream().map((ex) -> ex.modname+"\n").reduce(err, String::concat);
            throw new CodeException(err);
        }
    }
    public void esiste(String name, TypeElem[] types, TemplateEle... ete)throws CodeException{
        TemplateEle[] te;
        if(suds!=null && suds.size()>0)
            te=suds.recursiveGet(ete);
        else
            te=ete;
        ArrayList<FElement> e=new ArrayList<>();
        for(FElement i:s){
            if(!i.name.equals(Meth.funzKey(name, te)))//equivalenza sia di nome che template
                continue;
            if(types.length!=i.trequest.length)
                continue;
            boolean jus=true;
            for(int ij=0; ij<types.length; ij++){
                if(!types[ij].ifEstende(i.trequest[ij], true)){
                    jus=false;
                    break;
                }
            }
            if(!jus)
                continue;
            e.add(i);
        }
        if(e.size()!=1){
            if(e.isEmpty() && te.length>0){
                fl.esiste(name, te, types);
                return;
            }
            String err=Lingue.getIstance().format("m_cod_foufunn", e.size(), 
                    Meth.funzKey(name, te))+":\n";
            err = e.stream().map((ex) -> ex.modname+"\n").reduce(err, String::concat);
            throw new CodeException(err);
        }
    }
    public void esiste(String modname)throws CodeException{
        if(modname==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_funnfon"));
        for(FElement e:s){
            if(modname.equals(e.modname))
                return;
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_funnfon"));
    }
    public void esisteDestructor(TypeName tn)throws CodeException{
        TypeElem te=Types.getIstance().find(tn, true);//E' necessario comunque per via dei template
        esiste(Meth.destructorName(tn), new TypeElem[]{te}, tn.templates());
    }
    
    public void esisteDestructor(String name, TemplateEle[] tel)throws CodeException{
        TypeElem te=Types.getIstance().find(new TypeName(name, tel), true);
        esiste(Meth.destructorName(name), new TypeElem[]{te}, tel);
    }
    public void esisteCostructor(String fname, TemplateEle[] tem, TypeElem[] params)throws CodeException{
        TemplateEle[] te;
        if(suds!=null && suds.size()>0)
            te=suds.recursiveGet(tem);
        else
            te=tem;
        ArrayList<FElement> e=new ArrayList<>();
        for(FElement i:s){
            if(!i.name.equals(Meth.funzKey("init_"+fname, te)))//equivalenza sia di nome che template
                continue;
            if(params.length != (i.trequest.length-1) )
                continue;
            boolean jus=true;
            for(int ij=0; ij<params.length; ij++){
                if(!params[ij].ifEstende(i.trequest[ij+1], true)){
                    jus=false;
                    break;
                }
            }
            if(!jus)
                continue;
            e.add(i);
        }
        if(e.size()!=1){
            if(e.isEmpty() && te.length>0){
                fl.esisteCostructor(fname, tem, params);
                return;
            }
            String err=Lingue.getIstance().format("m_cod_foufunn", e.size(), 
                    Meth.funzKey("init_"+fname, te))+":\n";
            err = e.stream().map((ex) -> ex.modname+"\n").reduce(err, String::concat);
            throw new CodeException(err);
        }
    }
    
    public FunzList getFunzList(){
        return fl;
    }
    public HashSet<String> glob=new HashSet<>();
    public HashSet<String> ext=new HashSet<>();
}
