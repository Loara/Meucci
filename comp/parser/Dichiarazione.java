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
import comp.code.CodeException;
import comp.code.template.Substitutor;
import comp.general.Info;
import comp.general.VScan;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;
import comp.scanner.Token;
import java.io.Serializable;

/**
 * Da usare solo nelle istruzioni
 * @author loara
 */
public class Dichiarazione implements Serializable{
    protected String lvalue;
    protected TypeName type;
    public Dichiarazione(VScan<Token> s)throws ParserException{
        if(!s.reqSpace(2))
            throw new FineArrayException();
        type=new TypeName(s);
        if(s.get() instanceof IdentToken){
            lvalue=((IdentToken)s.get()).getString();
            /*
            Non effettuare qui il controllo (per via di this), ma in ClassisIstr
            Info.isForbitten(lvalue, s.get().getRiga());
            */
            s.next();
        }
        else throw new ParserException("Dichiarazione errata", s);
    }
    public Dichiarazione(String typet, String ident, TemplateEle... tn){
        type=new TypeName(typet, tn);
        lvalue=ident;
    }
    public Dichiarazione(TypeName typet, String ident){
        type=typet;
        lvalue=ident;
    }
    public String getType(){
        return type.getName();
    }
    public TypeName getRType(){
        return type;
    }
    public String getIdent(){
        return lvalue;
    }
    public IdentToken getTokIdent(){
        return new IdentToken(lvalue, 0);
    }
    @Override
    public String toString(){
        return type.getName()+" "+lvalue;
    }
    public void substitute(Substitutor sub)throws CodeException{
        type=sub.recursiveGet(type);
    }
}
