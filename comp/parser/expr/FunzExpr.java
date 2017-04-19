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
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.ParserException;
import comp.parser.istruz.TryIstr;
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
    //private String modname; Proviamo ad eliminarlo
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        return request(var, v).Return(v);
    }
    private FElement request(Variabili var, boolean v)throws CodeException{
            TypeElem[] tr=new TypeElem[values.length];
            for(int i=0; i<tr.length; i++){
                tr[i]=values[i].returnType(var, v);
            }
            return Funz.getIstance().request(nome, tr, v, params);
    }
    private FElement modname(Variabili var)throws CodeException{
            FElement fe=request(var, false);
            if(fe.isExternFile())
                Funz.getIstance().ext.add(fe.modname);
            return fe;
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        TypeElem[] esp1=new TypeElem[values.length];
        for(int i=0; i<values.length; i++){
            values[i].validate(var);
            esp1[i]=values[i].returnType(var, true);
        }
        FElement fe=Funz.getIstance().request(nome, esp1, true, params);
        if(!Info.containedIn(fe.errors, Environment.errors))
            throw new CodeException("Errori di "+fe.name+" non gestiti correttamente");
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc
        )throws CodeException{
        var.getVarStack().pushAll(text);//bisogna farlo prima altrimenti inquina lo stack
        //per la chiamata a funzione
        //non sono importanti i registri prenotati dopo, verranno rilasciati
        FElement fe=modname(var);
        if(!TryIstr.checkThrows(fe.errors, env))
            throw new CodeException("Errori di "+fe.name+" non gestiti correttamente");
        perfCall(fe, values, text, var, env, acc);
    }
    public static void perfCall(FElement fe, Espressione[] values, Segmenti text,
            Variabili vars, Environment env, Accumulator acc)throws CodeException{
        text.addIstruzione("sub", "rsp", String.valueOf(8*values.length));
        vars.getVarStack().doPush(values.length);
        for(int i=0; i<values.length; i++){
            values[i].toCode(text, vars, env, acc);
            TypeElem t=values[i].returnType(vars, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "qword [rsp+"+(8*i)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "qword [rsp+"+(8*i)+"]", acc.getAccReg().getReg());
            }
        }
        text.addIstruzione("call", fe.modname, null);
        vars.getVarStack().remPush(values.length);
        vars.getVarStack().popAll(text);//non modifica rax, xmm0 e rdx
        //C'è un problema: dopo aver effettuato il pushall e viene generata un eccezione
        //durante l'esecuzione dei parametri (prima della chiamata alla funzione
        //vera e propria), chi pulisce lo stack? Non ci sarebbero problemi di esecuzione
        //ma solamente uno spreco di memoria
        //Una soluzione: far gestire il pushall da VarStack e non Accumulator, la
        //risoluzione verrà effettuata durante l'ottimizzazione delle letture in memora
        if(fe.errors.length>0){
            String trb;//Il catch su cui saltare
            int yy;
            env.increment("ERC");
            yy=env.get("ERC");
            text.addIstruzione("test", Register.DX.getReg(4), Register.DX.getReg(4));
            text.addIstruzione("jz", "ERC"+yy, null);//rdx=0, più velose senza errori
            for(int i=0; i<fe.errors.length; i++){
                text.addIstruzione("cmp", Register.DX.getReg(4), ""+(i+1));
                trb=env.getErrorHandler(fe.errors[i]);
                if(trb != null){
                    env.increment("ERRY");
                    int y=env.get("ERRY");
                    text.addIstruzione("jne", "ERRY"+y, null);
                    vars.getVarStack().destroyTry(text, env.getTry(fe.errors[i]).tryName);
                    text.addIstruzione("jmp", trb, null);
                    text.addLabel("ERRY"+y);
                }
                else{
                    int ind =Info.indexOf(fe.errors[i], fe.errors);
                    vars.getVarStack().destroyAll(text);
                    text.addIstruzione("mov", Register.DX.getReg(4), String.valueOf(ind+1));
                    text.addIstruzione("leave", null, null);
                    int p=vars.getVarStack().getDimArgs();
                    if(p>0)
                        text.addIstruzione("ret",""+p,null);
                    else
                        text.addIstruzione("ret", null, null);
                }
            }
            text.addLabel("ERC"+yy);
        }
        TypeElem rettype=fe.Return(false);
        if(rettype.name.equals("void"))
            return;
        if(rettype.xmmReg())
            text.addIstruzione("movsd", acc.getXAccReg().getReg(), XReg.XMM0.getReg());
        else
            text.addIstruzione("mov", acc.getAccReg().getReg(rettype.realDim()
                ), Register.AX.getReg(rettype.realDim()));
    }
    /*
    robj punta alla vtable
    l'oggetto è nell'accumulatore
    offst è l'offset della funzione da chiamare all'interno della vtable
    
    Ricordare che il primo parametro è sempre l'oggetto
    
     non fà il pushall
    */
    public static void obtElem(int robj, int offst, Espressione[] exp, TypeElem type
            , Segmenti text, Variabili var, Environment env, Accumulator acc)
            throws CodeException{
        text.addIstruzione("sub", "rsp", String.valueOf(8*exp.length+8));
        var.getVarStack().doPush(exp.length+1);
        text.addIstruzione("mov", "[rsp]", acc.getAccReg().getReg());
        for(int i=0; i<exp.length; i++){
            exp[i].toCode(text, var, env, acc);
            TypeElem t=exp[i].returnType(var, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "[rsp+"+(8*i+8)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "[rsp+"+(8*i+8)+"]", acc.getAccReg().getReg());
            }
        }
        text.addIstruzione("call", "["+acc.getReg(robj).getReg()+"+"+offst+"]", null);
        var.getVarStack().remPush(exp.length+1);
        acc.libera(robj);
        acc.popAll(text);//Niente eccezioni (per ora)
        if(type.xmmReg())
            text.addIstruzione("movsd", acc.getXAccReg().getReg(), XReg.XMM0.getReg());
        else
            text.addIstruzione("mov", acc.getAccReg().getReg(type.realDim()
                ), Register.AX.getReg(type.realDim()));        
    }
    /*
    robj punta alla vtable
    l'oggetto è nell'accumulatore
    offst è l'offset della funzione da chiamare all'interno della vtable
    input (GPR) contiene il valore da settare
    
    Ricordare che il primo parametro è sempre l'oggetto, il secondo è il valore da settare
    
     non fà il pushall
    */
    public static void setElem(int robj, int offst, int input, Espressione[] exp
            , Segmenti text, Variabili var, Environment env, Accumulator acc)
            throws CodeException{
        text.addIstruzione("sub", "rsp", String.valueOf(8*exp.length+16));
        var.getVarStack().doPush(exp.length+2);
        text.addIstruzione("mov", "[rsp]", acc.getAccReg().getReg());
        text.addIstruzione("mov", "[rsp+8]", acc.getReg(input).getReg());
        for(int i=0; i<exp.length; i++){
            exp[i].toCode(text, var, env, acc);
            TypeElem t=exp[i].returnType(var, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "[rsp+"+(8*i+16)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "[rsp+"+(8*i+16)+"]", acc.getAccReg().getReg());
            }
        }
        text.addIstruzione("call", "["+acc.getReg(robj).getReg()+"+"+offst+"]", null);
        var.getVarStack().remPush(exp.length+2);
        acc.libera(robj);
        acc.libera(input);
        acc.popAll(text);//Niente eccezioni (per ora)     
    }
    /*
    robj punta alla vtable
    l'oggetto è nell'accumulatore
    offst è l'offset della funzione da chiamare all'interno della vtable
    input (XMM) contiene il valore da settare
    
    Ricordare che il primo parametro è sempre l'oggetto, il secondo è il valore da settare
    
     non fà il pushall
    */
    public static void setXElem(int robj, int offst, int input, Espressione[] exp
            , Segmenti text, Variabili var, Environment env, Accumulator acc)
            throws CodeException{
        text.addIstruzione("sub", "rsp", String.valueOf(8*exp.length+16));
        var.getVarStack().doPush(exp.length+2);
        text.addIstruzione("mov", "[rsp]", acc.getAccReg().getReg());
        text.addIstruzione("movq", "[rsp+8]", acc.getXReg(input).getReg());
        for(int i=0; i<exp.length; i++){
            exp[i].toCode(text, var, env, acc);
            TypeElem t=exp[i].returnType(var, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "[rsp+"+(8*i+16)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "[rsp+"+(8*i+16)+"]", acc.getAccReg().getReg());
            }
        }
        text.addIstruzione("call", "["+acc.getReg(robj).getReg()+"+"+offst+"]", null);
        var.getVarStack().remPush(exp.length+2);
        acc.libera(robj);
        acc.libera(input);
        acc.popAll(text);//Niente eccezioni (per ora)     
    }
    /*
    public static void perfCall(int fd, int offset, TypeElem rettype, Espressione[] vals, Segmenti text,
            Variabili vars, Environment env, Accumulator acc)throws CodeException{
        text.addIstruzione("sub", "rsp", String.valueOf(8*vals.length));
        vars.getVarStack().doPush(vals.length);
        for(int i=0; i<vals.length; i++){
            vals[i].toCode(text, vars, env, acc);
            TypeElem t=vals[i].returnType(vars, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "[rsp+"+(8*i)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "[rsp+"+(8*i)+"]", acc.getAccReg().getReg());
            }
        }
        text.addIstruzione("call", "["+acc.getReg(fd).getReg()+"+"+offset+"]", null);
        vars.getVarStack().remPush(vals.length);
        acc.libera(fd);
        acc.popAll(text);//Niente eccezioni (per ora)
        if(rettype.name.equals("void"))
            return;
        if(rettype.xmmReg())
            text.addIstruzione("movsd", acc.getXAccReg().getReg(), XReg.XMM0.getReg());
        else
            text.addIstruzione("mov", acc.getAccReg().getReg(rettype.realDim()
                ), Register.AX.getReg(rettype.realDim()));
    }
    */
    /*
    Queste funzioni devono essere utilizzate per l'allocazione di oggetti
    */
    public static void allc1(Segmenti text, Variabili vars, Environment env, Accumulator acc,
            Espressione[] params)throws CodeException{
        //Preliminari
        vars.getVarStack().pushAll(text);
        //memoria per parametri + valore di ritorno
        int nump=params.length+1;
        text.addIstruzione("sub", "rsp", String.valueOf(8*nump+8));
        vars.getVarStack().doPush(nump+1);
        //parametri
        for(int i=0; i<params.length; i++){
            params[i].toCode(text, vars, env, acc);
            TypeElem t=params[i].returnType(vars, false);
            if(t.xmmReg()){
                text.addIstruzione("movq", "[rsp+"+(8*i+8)+"]", acc.getXAccReg().getReg());
            }
            else{
                text.addIstruzione("mov", "[rsp+"+(8*i+8)+"]", acc.getAccReg().getReg());
            }
        }
    }
    /*
    Nell'accumulatore vi è il puntatore alla memoria allocata
    */
    public static void allc2(Segmenti text, Variabili vars, Environment env, Accumulator acc,
            Espressione[] params, FElement cos, TypeElem tp)throws CodeException{
        if(cos.isExternFile())
            Funz.getIstance().ext.add(cos.modname);
        text.addIstruzione("mov", "[rsp]", acc.getAccReg().getReg());
        text.addIstruzione("mov", "[rsp+"+(8*params.length+8)+"]", acc.getAccReg().getReg());
        if(!tp.explicit){
            //c'è l'_INIT_
            if(Environment.template || tp.external || tp.isTemplate())
                Funz.getIstance().ext.add("_INIT_"+tp.name);
            text.addIstruzione("push", acc.getAccReg().getReg(), null);
            text.addIstruzione("call", "_INIT_"+tp.name, null);
        }
        text.addIstruzione("call", cos.modname, null);
        text.addIstruzione("pop", acc.getAccReg().getReg(), null);
        vars.getVarStack().remPush(params.length+2);
        vars.getVarStack().popAll(text);
    }
}
