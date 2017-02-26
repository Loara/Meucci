/*
 * Copyright (C) 2016 loara
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

import comp.general.VScan;
import comp.scanner.Token;
import java.io.Serializable;

/**
 *
 * @author loara
 */
public class FunzParam implements Serializable{
    public final Dichiarazione dich;
    public FunzParam(Dichiarazione d){
        dich=d;
    }
    public FunzParam(TypeName type, String ident){
        dich=new Dichiarazione(type, ident);
    }
    public FunzParam(VScan<Token> t)throws ParserException{
        if(t.isEnded())
            throw new FineArrayException();
        if(t.reqSpace(2))
            dich=new Dichiarazione(t);
        else
            throw new FineArrayException();
    }
    public String getIdent(){
        return dich.getIdent();
    }
    public String getType(){
        return dich.getType();
    }
}
