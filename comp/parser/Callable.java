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
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.general.Info;
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
 * @author loara
 */
public abstract class Callable implements Serializable{
    protected TypeName retType;
    protected Token nome;//può essere o ident o symb
    protected FunzParam[] dichs;
    protected MultiIstr istr;
    protected Template[] temp;//solo per funzioni
    private String mod;
    protected boolean noglobal;
    protected Callable(String m){
        //non fa niente
        mod=m;
    }
    public Callable(Token n, FunzParam[] d, MultiIstr i, String mod){
        nome=n;
        dichs=d;
        istr=i;
        noglobal=false;
        retType=new TypeName("void");
        temp=new Template[0];
    }
    /**
     * Da modificare
     * @param t
     * @return 
     */
    public static boolean canBeCalled(VScan<Token> t){
        int sc=0;
        if(t.get() instanceof IdentToken){
            if(((IdentToken)t.get()).getString().equals("init"))
                return t.get(1) instanceof PareToken;
            if(((IdentToken)t.get()).getString().equals("shadow"))
                sc=1;
            else
                sc=0;
        }
        //+2 per nome e ritorno
        return t.get(sc+2) instanceof PareToken;
    }
    public Callable(VScan<Token> t, String modulo)throws ParserException{
        //Tutte le dichiarazioni sono effettuate dall'utente. Possibile utilizzare
        //i forbittenNames di Info
        if(t.get() instanceof IdentToken && (((IdentToken)t.get()).getString().equals("init")
                || ((IdentToken)t.get()).getString().equals("end"))){
            //costruttore
            noglobal=false;
            retType=new TypeName("void");
            nome=new IdentToken(((IdentToken)t.get()).getString(), t.getInd());
            t.nextEx();
        }
        else{
            if(!(t.get() instanceof IdentToken))
                throw new ParserException("Ritorno funzione invalido", t);
            else
                Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
            retType=new TypeName(t);
            nome=t.get();
            t.nextEx();
        }
        temp=Template.parseTemp(t);
        if(!(t.get() instanceof PareToken)||((PareToken)t.get()).s!='(')
            throw new ParserException("Funzione invalida", t);
        t.next();
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
                else throw new ParserException("Dichiarazioni errate", t);
            }
            dichs=es.toArray();
        }
        t.nextEx();
        Istruzione i=IstrExe.toIstr(t);
        if(i instanceof MultiIstr)
            istr=(MultiIstr)i;
        else throw new ParserException("Istruzione non valida", t);
        mod=modulo;
    }
    public TypeName[] types(){
        TypeName[] t=new TypeName[dichs.length];
        for(int i=0; i<t.length; i++){
            t[i]=dichs[i].dich.getRType();
        }
        return t;
    }
    public Template[] templates(){
        return temp;
    }
    public String[] templateNames(){
        String[] n=new String[temp.length];
        for(int i=0; i<temp.length; i++){
            n[i]=temp[i].getIdent();
        }
        return n;
    }
    public FunzParam[] getElems(){
        return dichs;
    }
    public abstract String getName();//per via di Token
    public TypeName getReturn(){
        return retType;
    }
    public TypeElem getReturnType(boolean v)throws CodeException{
        return Types.getIstance().find(retType, v);
    }
    public void substituteAll(Substitutor s)throws CodeException{
        retType=s.recursiveGet(retType);
        for (FunzParam dich : dichs) {
            dich.dich.type = s.recursiveGet(dich.dich.type);
        }
        istr.substituteAll(s);
    }
    public void toCode(Segmenti text, Dichiarazione[] varSt, Environment env, 
            TemplateEle... temps)throws CodeException{
        if(temp.length!=temps.length)
            throw new CodeException("Lunghezza parametri di template diversa: "+temp.length+
                    " contro "+temps.length);
        Substitutor s=new Substitutor();
        s.clear();
        s.addAll(templateNames(), temps);
        this.substituteAll(s);
        Accumulator acc=new Accumulator();//servirà in VarStack
        Variabili vs=new Variabili(dichs, varSt, false, acc);
        Environment.ret=Types.getIstance().find(retType, false);
        Environment.template=temp.length!=0;
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
        text.text.set(mem-1, "\tenter\t"+(Info.alignConv(i)+i)+",0");//deve essere allineato.
        i=vs.getVarStack().getDimArgs();//gli argomenti passati dalla funzione
        if(!ret){
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
        Template.addTemplateConditions(temp);
        Variabili vs=new Variabili(dichs, varSt, true, null);
        Environment.ret=Types.getIstance().find(retType, true);
        Environment.template=true;
        istr.validate(vs, env);
        Template.removeTemplateConditions(temp);
    }
    public String getModulo(){
        return mod;
    }
}
