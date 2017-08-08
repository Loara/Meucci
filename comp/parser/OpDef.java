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

import comp.code.Meth;
import comp.general.Lingue;
import comp.general.VScan;
import comp.scanner.SymbToken;
import comp.scanner.Token;

/**
 *
 * @author loara
 */
public class OpDef extends Callable{
    public OpDef(VScan<Token> t, String modulo)throws ParserException{
        super(t, modulo);
        if(super.temp.length!=0)
            throw new ParserException(Lingue.getIstance().format("m_par_temopr"), super.nome);
        if(!(nome instanceof SymbToken))
            throw new ParserException(Lingue.getIstance().format("m_par_invnap"), nome);
        if(((SymbToken)nome).getString().equals("=="))
            throw new ParserException(Lingue.getIstance().format("m_par_uguopr"), nome);
        if(dichs==null || dichs.length==0 || dichs.length>2)
            throw new ParserException(Lingue.getIstance().format("m_par_nerpar"), nome);//solo per la riga
    }
    @Override
    public String getName(){
        return ((SymbToken)nome).getString();
    }
    @Override
    public String memName(){
        String n=getName();
        if(n.startsWith(":"))
            return "o"+n.substring(1);
        else
            return "o"+Meth.encode(n);
    }
    public static String opName(String n){
        if(n.startsWith(":"))
            return "o"+n.substring(1);
        else
            return "o"+Meth.encode(n);
    }
}
