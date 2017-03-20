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
        acc.pushAll(text);//bisogna farlo prima altrimenti inquina lo stack
        //per la chiamata a funzione
        //non sono importanti i registri prenotati dopo, verranno rilasciati
        FElement fe=modname(var);
        if(!TryIstr.checkThrows(fe.errors, env))
            throw new CodeException("Errori di "+fe.name+" non gestiti correttamente");
        perfCall(fe, values, text, var, env, acc);
    }
    public static void perfCall(FElement fe, Espressione[] values, Segmenti text,
            Variabili vars, Environment env, Accumulator acc)throws CodeException
    {
        for(Espressione e:values){
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
        text.addIstruzione("call", fe.modname, null);
        acc.popAll(text);//non modifica rax, xmm0 e rdx
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
     * Nota: non effettua il pushall
     * @param modname
     * @param rettype
     * @param vals
     * @param text
     * @param vars
     * @param env
     * @param acc
     * @throws CodeException 
    public static void perffCall(String modname, TypeElem rettype, Espressione[] vals, Segmenti text,
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
    */
    /**
     * Nota: non effettua il pushall, ma il popall e il libera sì
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
}
