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

import comp.parser.expr.IdentEle;
import comp.code.CodeException;
import comp.code.template.TNumbers;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.template.FunzDich;
import comp.parser.template.NumDich;
import comp.parser.template.ParamDich;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.Token;
import comp.scanner.VirgToken;
import java.io.Serializable;

/**
 * Sono i dati membro dei tipi. A differenza delle
 * dichiarazioni, permettono di aggiungere modificatori (shadow, read, ghost, ...)
 * 
 * [override | explicit | ghost] [read | shadow] tipo nome [parametri];
 * 
 * @author loara
 */
public class Membro implements Serializable{
    public final Dichiarazione dich;
    public boolean shadow, read;
    /*
    Con la versione 3 sarà abolito definitivamente il modificatore explicit
    e ogni membro senza modificatore di accesso sarà explicit
    
    In tal modo il doppio punto .. sarà superfluo e l'override sarà possibile
    solo su membri con modificatore di memorizzazione
    */
    public boolean explicit, override, ghost, gpacked;
    public final TypeName[] params;
    public TemplateEle packed;
    public Membro(TypeName type, String name, TypeName[] p, boolean shadow, 
            boolean read, boolean exp, boolean gh, TemplateEle pack){
        dich=new Dichiarazione(type, name);
        this.shadow=shadow;
        this.read=read;
        explicit=exp;
        override=false;//caricato da file
        params=p;
        ghost=gh;
        packed=pack;
        gpacked=false;
    }
    public Membro(TypeName type, String name, boolean shadow, boolean read){
        this(type, name, new TypeName[0], shadow, read, false, false, null);
    }
    public Membro(Membro sup, TypeName conv, TemplateEle pac){
        dich=new Dichiarazione(conv, sup.dich.getIdent());
        shadow=sup.shadow;
        read=sup.read;
        explicit=sup.explicit;
        override=sup.override;
        params=sup.params;
        ghost=sup.ghost;
        packed=pac;
        gpacked=sup.gpacked;
    }
    public Membro(VScan<Token> t, boolean typeExpl)throws ParserException{
        if(t.get() instanceof IdentToken){
            detectAttr(t, typeExpl);
            detectAcc(t);
            dich=new Dichiarazione(t);
            Info.isForbitten(dich.getIdent(), t.get().getRiga());
            if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='['){
                if(explicit)
                    throw new ParserException(Lingue.getIstance().format("m_par_expacc", dich.lvalue), t);
                t.nextEx();
                Stack<TypeName> ss=new Stack<>(TypeName.class);
                if(t.get() instanceof IdentToken){
                    ss.push(new TypeName(t));
                    while(!(t.get() instanceof PareToken && ((PareToken)t.get())
                            .s==']')){
                        if(!(t.get() instanceof VirgToken)){
                            throw new ParserException(Lingue.getIstance().format("m_par_comma"),t);
                        }
                        t.nextEx();
                        ss.push(new TypeName(t));
                    }
                    t.nextEx();
                    params=ss.toArray();
                    packed=null;
                }
                else throw new ParserException("Tipo errato", t);
            }
            else{
                if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("packed")){
                    t.nextEx();
                    packed=Template.detect(t);
                    if(!explicit)
                        throw new ParserException(Lingue.getIstance().format("m_par_notexp"), t);
                    params=new TypeName[]{new TypeName("uint")};
                }
                else{
                    params=new TypeName[0];
                    packed=null;
                }
            }
            //il ; deve essere controllato altrove
        }
        else{
            throw new ParserException(Lingue.getIstance().format("m_par_invmem"), t);
        }
        //non è necessario il controllo sintattico
    }
    //Se il tipo è esplicito, tutti i membri sono espliciti
    private void detectAttr(VScan<Token> t, boolean expl)throws ParserException{
        if(expl){
            override=false;
            explicit=true;
            ghost=false;
            gpacked=false;
            if(((IdentToken)t.get()).getString().equals("explicit"))
                t.nextEx();
            return;
        }
        override=((IdentToken)t.get()).getString().equals("override");
        if(override){
            t.nextEx();
            explicit=false;
            ghost=false;
            return;
        }
        explicit = ((IdentToken)t.get()).getString().equals("explicit");
        if(explicit){
            t.nextEx();
            ghost=false;
            return;
        }
        ghost = ((IdentToken)t.get()).getString().equals("ghost");
        if(ghost){
            t.nextEx();
            return;
        }
        gpacked = ((IdentToken)t.get()).getString().equals("gpacked");
        if(gpacked){
            t.nextEx();
        }
    }
    private void detectAcc(VScan<Token> t)throws ParserException{
        shadow=((IdentToken)t.get()).getString().equals("shadow");
        if(shadow){
            t.nextEx();
            read=false;
            return;
        }
        read=((IdentToken)t.get()).getString().equals("read");
        if(read){
            t.nextEx();
        }
    }
    public void chechPack(boolean validate)throws CodeException{
        if(packed==null)
            return;
        if(validate){
            if(packed instanceof NumDich){
                if(((NumDich)packed).expDim()>2)
                    throw new CodeException(Lingue.getIstance().format("m_cod_bignume"));
                return;
            }
            if(packed instanceof FunzDich){
                if(((FunzDich)packed).dimension()>2)
                    throw new CodeException(Lingue.getIstance().format("m_cod_bignume"));
                return;
            }
            if(packed instanceof ParamDich){
                if(TNumbers.getIstance().expDim(((ParamDich)packed).getName())>2){
                    throw new CodeException(Lingue.getIstance().format("m_cod_bignume"));
                }
            }
            else
                throw new CodeException(Lingue.getIstance().format("m_cod_costnis"));
        }
        else{
            if(!(packed instanceof NumDich))
                throw new CodeException(Lingue.getIstance().format("m_cod_costnis"));
            else{
                if(((NumDich)packed).expDim()>2)
                    throw new CodeException(Lingue.getIstance().format("m_cod_bignume"));
            }
        }
    }
    public String getIdent(){
        return dich.getIdent();
    }
    public TypeName getType(){
        return dich.getRType();
    }
    @Override
    public String toString(){
        return dich.toString();
    }
    /*
    determina se i valori passati da ie siano compatibili con le dichiarazioni del membro.
    Utilizzato dalla VTable come controllo
    0 - OK
    -1 - Nome diverso
    -2 - Lunghezza parametri diversa
    i>0 - i - esimo parametro errato
    */
    public int compatible(IdentEle ie, Variabili var, boolean v)throws CodeException{
        if(!ie.identEq(dich.getIdent()))
            return -1;
        if(ie.getVals().length!=params.length)
            return -2;
        for(int i=0; i<params.length; i++){
            if(!ie.getVals()[i].returnType(var, v).ifEstende(params[i], v)){
                return i+1;
            }
        }
        return 0;
    }
    public boolean compatible(TypeName[] pparams)throws CodeException{
        if(pparams.length!=params.length)
            return false;
        for(int i=0; i<params.length; i++){
            if(!pparams[i].equals(params[i]))
                return false;
        }
        return true;
    }
}
