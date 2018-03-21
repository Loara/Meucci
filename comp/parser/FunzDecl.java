/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.parser;

import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.Token;
import comp.scanner.VirgToken;

/**
 * Funzioni dichiarate in un protocollo, senza implementazione
 * @author loara
 */
public class FunzDecl {
    public String name;
    public TypeName[] params;
    public TypeName retType;
    public String[] err;
    public boolean shadow;
    public FunzDecl(VScan<Token> t)throws ParserException{
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
            Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
            name = ((IdentToken)t.get()).getString();
        }
        t.nextEx();
        if(!(t.get() instanceof PareToken) || ((PareToken)t.get()).s!='(')
            throw new ParserException(Lingue.getIstance().format("m_par_invfun"), t);
        t.nextEx();
        if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
            params=new TypeName[0];
        }
        else{
            Stack<TypeName> es=new Stack<>(TypeName.class);
            while(true){
                TypeName dic=new TypeName(t);
                es.push(dic);
                if(t.get() instanceof VirgToken){
                    t.nextEx();
                }
                else if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')
                    break;
                else throw new ParserException(Lingue.getIstance().format("m_par_invdic"), t);
            }
            params=es.toArray();
        }
        t.nextEx();
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("errors")){
            Stack<String> sta = new Stack<>(String.class);
            t.nextEx();
            while(t.get() instanceof IdentToken){
                Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
                sta.push(((IdentToken)t.get()).getString());
                t.nextEx();
            }
            err = sta.toArray();
        }
        else
            err = new String[0];        
    }
}
