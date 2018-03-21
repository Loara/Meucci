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
 * Un protocollo rappresenta un generico insieme di oggetti e metodi senza però 
 * specificarne l'implementazione.
 * 
 * Va dichiarato con la parola chiave protocol
 * @author loara
 */
public class Protocol {
    public String nome;
    public String[] dep;
    public Boolean[] pub;
    public String[] obj;
    public FunzDecl[] fd;
    public Protocol(VScan<Token> t)throws ParserException{
        if(!t.reqSpace(4))
            throw new FineArrayException();
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("protocol")){
            t.nextEx();
            if(t.get() instanceof IdentToken)
                nome=((IdentToken)t.get()).getString();
            else throw new ParserException("Nome prot non valido", t);
            t.nextEx();
            //dipendenze
            if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("depends")){
                Stack<String> de=new Stack<>(String.class);
                Stack<Boolean> pb=new Stack<>(Boolean.class);
                do{
                    t.nextEx();
                    if(!(t.get() instanceof IdentToken)){
                        throw new ParserException("", t);
                    }
                    if(((IdentToken)t.get()).getString().equals("public")){
                        pb.push(true);
                        t.nextEx();
                    }
                    else
                        pb.push(false);
                    if(!(t.get() instanceof IdentToken)){
                        throw new ParserException("", t);
                    }
                    de.push(((IdentToken)t.get()).getString());
                    t.nextEx();
                }
                while(t.get() instanceof VirgToken);
                dep=de.toArray();
                pub=pb.toArray();
            }
            else{
                dep=new String[0];
                pub=new Boolean[0];
            }
            
            if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'))
                throw new ParserException(Lingue.getIstance().format("m_par_invmod", nome), t);
            t.nextEx();
        }
        Stack<String> tn=new Stack<>(String.class);
        Stack<FunzDecl> fud = new Stack<>(FunzDecl.class);
        while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
            if(t.get().isIdent("type")){
                t.nextEx();
                if(t.get() instanceof IdentToken){
                    Info.isForbitten(((IdentToken)t.get()).getString(), t.get().getRiga());
                    tn.push(((IdentToken)t.get()).getString());
                }
                t.nextEx();
            }
            else{
                fud.push(new FunzDecl(t));
            }
        }
    }
}
