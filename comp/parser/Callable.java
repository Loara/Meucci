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
package comp.parser;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Meth;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.istruz.IstrExe;
import comp.parser.istruz.MultiIstr;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.Token;
import comp.scanner.VirgToken;
import java.io.Serializable;

/**
 * Tutto ciò che si può chiamare (funzioni, operazioni, ...)
 * 
 * @author loara
 */
public abstract class Callable implements Serializable{
    protected TypeName retType;
    protected Token nome;//può essere o ident o symb
    protected FunzParam[] dichs;
    protected MultiIstr istr;
    private String mod;
    protected boolean noglobal;
    protected String[] errors;//Errori che può generare. CONTA l'ordine
    protected boolean shadow;
    protected Callable(String m){
        //non fa niente
        mod=m;
    }
    public Callable(Token n, boolean shadow, FunzParam[] d, MultiIstr i, String mod, String[] errors){
        nome=n;
        dichs=d;
        istr=i;
        noglobal=false;
        retType=new TypeName("void");
        this.mod=mod;
        this.errors=errors;
        this.shadow=shadow;
    }
    /*
     * Da modificare
     * @param t
     * @return 
     
    public static boolean canBeCalled(VScan<Token> t){
        if(t.get() instanceof IdentToken){
            if(((IdentToken)t.get()).getString().equals("init"))
                return t.get(1) instanceof PareToken;
        }
        return t.get(2) instanceof PareToken;
    
    Se f è true allora cerca se il nome è vietato o no
    }
    */
    public Callable(VScan<Token> t, String modulo, boolean f)throws ParserException{
        //Tutte le dichiarazioni sono effettuate dall'utente. Possibile utilizzare
        //i forbittenNames di Info
        if(t.get().isIdent("shadow")){
            shadow=true;
            t.nextEx();
        }
        else
            shadow=false;
        retType=new TypeName(t);
        if(!(t.get() instanceof IdentToken))
            throw new ParserException(Lingue.getIstance().format("m_par_invret"), t);
        else{
            if(f){
                Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
            }
        }
        nome=t.get();
        t.nextEx();
        
        initRest(t, modulo);
    }
    /*
    costr -> scartare il primo token (constructor), altrimenti è un distruttore
    */
    protected Callable(VScan<Token> t, String modulo, boolean costr, boolean f)throws ParserException{
        if(costr)
            t.nextEx();//constructor
        if(t.get().isIdent("shadow")){
            if(!costr){
                throw new ParserException(Lingue.getIstance().format("shderr"), t);
            }
            shadow=true;
            t.nextEx();
        }
        else
            shadow=false;
        if(costr)
            retType=new TypeName(t);
        else
            retType=new TypeName("void");
        if(!(t.get() instanceof IdentToken))
            throw new ParserException(Lingue.getIstance().format("m_par_invret"), t);
        else{
            if(f)
                Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
        }
        nome=t.get();
        t.nextEx();
        
        initRest(t, modulo);
    }
    protected final void initRest(VScan<Token> t, String modulo)throws ParserException{
        //t deve puntare ai parametri template
        if(!(t.get() instanceof PareToken) || ((PareToken)t.get()).s!='(')
            throw new ParserException(Lingue.getIstance().format("m_par_invfun"), t);
        t.nextEx();
        if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
            dichs=new FunzParam[0];
        }
        else{
            Stack<FunzParam> es=new Stack<>(FunzParam.class);
            while(true){
                FunzParam dic=new FunzParam(t);
                Info.isForbitten(dic.getIdent(), nome.getRiga());
                es.push(dic);
                if(t.get() instanceof VirgToken){
                    t.nextEx();
                }
                else if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')
                    break;
                else throw new ParserException(Lingue.getIstance().format("m_par_invdic"), t);
            }
            dichs=es.toArray();
        }
        t.nextEx();
        if(t.get().isIdent("errors")){
            Stack<String> sta = new Stack<>(String.class);
            t.nextEx();
            while(t.get() instanceof IdentToken){
                Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
                sta.push(((IdentToken)t.get()).getString());
                t.nextEx();
            }
            errors = sta.toArray();
        }
        else
            errors = new String[0];
        Istruzione i=IstrExe.toIstr(t);
        if(i instanceof MultiIstr)
            istr=(MultiIstr)i;
        else throw new ParserException(Lingue.getIstance().format("m_par_invist"), t);
        mod=modulo;
    }
    public TypeName[] types(){
        TypeName[] t=new TypeName[dichs.length];
        for(int i=0; i<t.length; i++){
            t[i]=dichs[i].dich.getRType();
        }
        return t;
    }
    public boolean isShadow(){
        return shadow;
    }
    public String[] errors(){
        return errors;
    }
    public FunzParam[] getElems(){
        return dichs;
    }
    /**
     * Il nome con cui è stata dichiarata la funzione.
     * <b>NOTA</b>: per le funzioni di accesso vale get o set, mentre per i distruttori
     * vale end sempre, si consiglia di utilizzare memName per quanto possibile
     * @return 
     */
    public abstract String getName();//per via di Token
    /**
     * Nome codificato da utilizzare per FElement, si consiglia di effettuarne l'override
     * 
     * @return 
     */
    public String memName(){
        return getName();
    }
    public TypeName getReturn(){
        return retType;
    }
    public TypeElem getReturnType(boolean v)throws CodeException{
        return Types.getIstance().find(retType, v);
    }
    public void toCode(Segmenti text, Dichiarazione[] varSt, Environment env, 
            TemplateEle... temps)throws CodeException{
        Accumulator acc=new Accumulator();//servirà in VarStack
        Variabili vs=new Variabili(dichs, varSt, false, acc);
        Environment.ret=Types.getIstance().find(retType, false);
        Environment.template=false;
        Environment.errors=errors;
        String mname=Meth.modName(this, temps);
        if(!noglobal)//Per le FunzMem
            Funz.getIstance().glob.add(mname+":function");
        text.addLabel(mname);
        text.add("");//in sequito
        int mem=text.text.size();
        this.preCode(text, vs, env, acc);
        istr.toCode(text, vs, env, acc);
        this.postCode(text, vs, env, acc);
        boolean ret="ret".equals(text.text.prevIstr()[0]);
        int i=vs.getVarStack().internalVarsMaxDim();
        text.text.toArrayList().set(mem-1, "\tenter\t"+(Info.alignConv(i)+i)+",0");//deve essere allineato.
        i=vs.getVarStack().getDimArgs();//gli argomenti passati dalla funzione
        if(!ret){
            text.addIstruzione("xor", Register.DX.getReg(), Register.DX.getReg());
            text.addIstruzione("leave", null, null);
            if(i>0)
                text.addIstruzione("ret",String.valueOf(i), null);
            else
                text.addIstruzione("ret", null, null);
        }
    }
    protected void preCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        
    }
    protected void postCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        
    }
    public void validate(Environment env, Dichiarazione[] varSt)throws CodeException{
        Variabili vs=new Variabili(dichs, varSt, true, null);
        Environment.ret=Types.getIstance().find(retType, true);
        Environment.template=true;
        Environment.errors=errors;
        istr.validate(vs, env);
    }
    public String getModulo(){
        return mod;
    }
}
