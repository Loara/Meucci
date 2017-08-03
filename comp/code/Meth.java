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

import comp.code.template.WeakSubstitutor;
import comp.general.Info;
import comp.parser.Callable;
import comp.parser.Costructor;
import comp.parser.Funzione;
import comp.parser.Membro;
import comp.parser.TypeName;
import comp.parser.template.FunzDich;
import comp.parser.template.NumDich;
import comp.parser.template.ParamDich;
import comp.parser.template.TemplateEle;
import comp.parser.template.TypeDich;

/**
 *
 * @author loara
 */
public class Meth {
    /**
     * Il nome che viene memorizzato all'interno di Funz su cui effettuare la ricerca
     * Non vengono considerati FunzMem, Costructor, ... in quanto la modifica del loro nome
     * è fatta già durante il parsing
     * @param d
     * @param params
     * @return
     * @throws CodeException 
     */
    public static String funzKey(String d, TemplateEle[] params)throws CodeException{
        String a;
        if(isOp(d)){
            if(d.startsWith(":"))
                a="o"+d.substring(1);
            else
                a="o"+encode(d);
        }
        else 
            a=d;
        if(params.length!=0)
            a += "#A"+paramsName(params)+"#C";
        return a;
    }
    public static String funzKey(String d)throws CodeException{
        String a;
        if(isOp(d)){
            if(d.startsWith(":"))
                a="o"+d.substring(1);
            else
                a="o"+encode(d);
        }
        else 
            a=d;
        return a;
    }
    public static boolean isOp(String n){
        return n.startsWith(":") || Info.isSymb(n.charAt(0));
    }
    public static String modName(Callable d, TemplateEle... params)throws CodeException{
        String suf=d.getModulo()+"~";
        if(d instanceof Funzione)
            suf+="@";
        else
            suf+="";
        String nn=suf+funzKey(d.memName(), params);
        WeakSubstitutor sub=new WeakSubstitutor();
        sub.addAll(d.templateNames(), params);
        int s=d instanceof Costructor ? 1 : 0;
        //Se è un costruttore il primo parametro non conta 
        for(int i=s; i<d.getElems().length; i++)
            nn+="."+className(sub.recursiveGet(d.getElems()[i].dich.getRType()));
        return nn;
    }
    public static String paramsName(TemplateEle[] vv){
        String cn="";
        for(TemplateEle nt:vv){
            if(nt instanceof NumDich){
                cn+=String.valueOf(((NumDich)nt).getNum());
            }
            else if(nt instanceof ParamDich){
                cn+="@"+((ParamDich)nt).getName();
            }
            else if(nt instanceof TypeDich){
                cn+="@"+className((TypeDich)nt);
            }
            else if(nt instanceof FunzDich){
                FunzDich fd=(FunzDich)nt;
                cn+="#"+fd.toString()+paramsName(fd.getParams());
            }
        }
        return cn;
    }
    /**
     * Non utilizzare sui tipi primitivi. Da vedere
     * @param vv
     * @return 
     */
    public static String className(TypeName vv){
        if(vv.templates().length==0)
            return vv.getName();
        else
            return vv.getName()+"#A"+paramsName(vv.templates())+"#C";
    }
    public static String className(String vv, TemplateEle... te){
        if(te.length==0)
            return vv;
        else
            return vv+"#A"+paramsName(te)+"#C";
    }
    /**
     * classname è la classe che contiene il membro
     * @param membro
     * @param classname
     * @param get
     * @return 
     */
    public static String accessFunzName(String membro, String classname, boolean get){
        return FMemName(membro, get)+"_"+classname;
    }
    public static String destructorName(TypeName classname){
        return "end_"+classname.getName();//NON vanno considerati anche i parametri
        //per vari motivi:
        //1)Sono già considerati come parametri template della funzione
        //2)Il costruttore è associato a tutta la famiglia di tipi e non ad un tipo in particolare
    }
    public static String destructorName(String classname){
        return "end_"+classname;
    }
    private static String className(TypeDich vv){
        if(vv.templates().length==0)
            return vv.getName();
        else
            return vv.getName()+"#A"+paramsName(vv.templates())+"#C";
    }
    //Genera il modName da un dato membro
    //Deve essere naturalmente modificato insieme a modName
    public static String generate(Membro mem, String clas, boolean get, String modulo,
            TemplateEle[] tparams, String[] ttemp)throws CodeException{
        String u=modulo+"~"+accessFunzName(mem.getIdent(), clas, get);
        if(tparams.length!=0)
            u+="#A"+paramsName(tparams)+"#C";
        WeakSubstitutor sub=new WeakSubstitutor();
        sub.addAll(ttemp, tparams);
        for(TypeName tn:mem.params)
            u+="."+className(sub.recursiveGet(tn));
        return u;
    }
    /**
     * Preferire {@link comp.code.Meth#accessFunzName(java.lang.String, java.lang.String, boolean) }
     * @param name Nome parametro
     * @param get Accesso al parametro
     * @return 
     */
    public static String FMemName(String name, boolean get){
        return (get ? "G" : "S")+name;
    }
    /*
    nasm non supporta i simboli, verranno dunque codificati nella forma
    #xx , dove xx è il codice ASCII del simbolo
    */
    public static String encodeSymbol(char c){
        if(!Info.isSymb(c))//nota: i due punti sono già eliminati
            return String.valueOf(c);
        return "#"+Integer.toHexString((int)c);
    }
    public static String encode(String s){
        StringBuilder bui=new StringBuilder();
        for(char c:s.toCharArray())
            bui.append(encodeSymbol(c));
        return bui.toString();
    }
}
