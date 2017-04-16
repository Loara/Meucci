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

import comp.general.Lingue;
import comp.general.VScan;
import comp.scanner.IdentToken;
import comp.scanner.Token;

/**
 *
 * @author loara
 */
public class Funzione extends Callable{
    @Override
    public String getName(){
        return ((IdentToken)nome).getString();
    }
    public Funzione(VScan<Token> t, String modulo)throws ParserException{
        super(t, modulo);
        if(!(nome instanceof IdentToken))
            throw new ParserException(Lingue.getIstance().format("m_par_invnam"), nome);
    }
}
