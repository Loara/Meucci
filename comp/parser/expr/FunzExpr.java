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
package comp.parser.expr;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Funz.FElement;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.XReg;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.ParserException;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;

/**
 *
 * @author loara
 */
public class FunzExpr extends Espressione{
    protected String nome;
    protected Espressione[] values;
    protected TemplateEle[] params;
    public FunzExpr(IdentToken n, TemplateEle[] p, Espressione[] val)throws ParserException{
        nome=n.getString();
        values=val;
        params=p;
    }
    public String getName(){
        return nome;
    }
    public Espressione[] getValues(){
        return values;
    }
    public TemplateEle[] template(){
        return params;
    }
    @Override
    public void println(int inter){
        String i="";
        for(int e=0; e<inter; e++){
            i+=" ";
        }
        for(Espressione es:values){
            System.out.println(i+"Elemento:");
            es.println(inter+2);
        }
    }
    private String modname;
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        setModname(var, v);
        return Funz.getIstance().request(modname).Return(v);
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        params=sub.recursiveGet(params);
        for(Espressione e:values)
            e.substituteAll(sub);
    }
    /*
    Non solo setta il modname, ma effettua altri controlli, tra cui il cost
    */
    private void setModname(Variabili var, boolean v)throws CodeException{
            TypeElem[] tr=new TypeElem[values.length];
            for(int i=0; i<tr.length; i++){
                tr[i]=values[i].returnType(var, v);
            }
            FElement fe=Funz.getIstance().request(nome, tr, v, params);
            modname=fe.modname;
            if(!v && (fe.isExternFile()))
                Funz.getIstance().ext.add(modname);
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        TypeElem[] esp1=new TypeElem[values.length];
        for(int i=0; i<values.length; i++){
            values[i].validate(var);
            esp1[i]=values[i].returnType(var, true);
        }
        Funz.getIstance().request(nome, esp1, true, params);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc
        )throws CodeException{
        setModname(var, false);
        acc.pushAll(text);//bisogna farlo prima altrimenti inquina lo stack
        //per la chiamata a funzione
        //non sono importanti i registri prenotati dopo, verranno rilasciati
        perfCall(modname, returnType(var, false), values, text, var, env, acc);
        acc.popAll(text);
    }
    /**
     * Nota: non effettua il pushall
     * @param modname
     * @param rettype
     * @param vals
     * @param text
     * @param vars
     * @param env
     * @param acc
     * @throws CodeException 
     */
    public static void perfCall(String modname, TypeElem rettype, Espressione[] vals, Segmenti text,
            Variabili vars, Environment env, Accumulator acc)throws CodeException{
        for(Espressione e:vals){
            e.toCode(text, vars, env, acc);
            TypeElem t=e.returnType(vars, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", acc.getAccReg().getReg(), acc.getXAccReg().getReg());
                text.addIstruzione("push", acc.getAccReg().getReg(), null);
            }
            else{
                text.addIstruzione("push", acc.getAccReg().getReg(), null);
            }
        }
        text.addIstruzione("call", modname, null);
        if(rettype.name.equals("void"))
            return;
        if(rettype.xmmReg())
            text.addIstruzione("movsd", acc.getXAccReg().getReg(), XReg.XMM0.getReg());
        else
            text.addIstruzione("mov", acc.getAccReg().getReg(rettype.name
                ), Register.AX.getReg(rettype.name));
    }
    /**
     * Nota: non effettua il pushall
     * Come il precedente, solo non chiama da label ma da puntatore memorizzato 
     * in registro salvato.
     * @param fd    il registro che punta alla vtable
     * @param offset    l'offset della funzione da chiamare
     * @param rettype
     * @param vals
     * @param text
     * @param vars
     * @param env
     * @param acc
     * @throws CodeException 
     */
    public static void perfCall(int fd, int offset, TypeElem rettype, Espressione[] vals, Segmenti text,
            Variabili vars, Environment env, Accumulator acc)throws CodeException{
        for(Espressione e:vals){
            e.toCode(text, vars, env, acc);
            TypeElem t=e.returnType(vars, false);
            if(t.xmmReg()){
                text.addIstruzione("push", acc.getAccReg().getReg(), null);
                text.addIstruzione("movsd", "[rsp]", acc.getXAccReg().getReg());
                //non ci sono istruzioni
            }
            else{
                text.addIstruzione("push", acc.getAccReg().getReg()
                        , null);
            }
        }
        text.addIstruzione("call", "["+acc.getReg(fd).getReg()+"+"+offset+"]", null);
        if(rettype.name.equals("void"))
            return;
        if(rettype.xmmReg())
            text.addIstruzione("movsd", acc.getXAccReg().getReg(), XReg.XMM0.getReg());
        else
            text.addIstruzione("mov", acc.getAccReg().getReg(rettype.realDim()
                ), Register.AX.getReg(rettype.realDim()));
    }
}
