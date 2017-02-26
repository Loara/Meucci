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

import comp.general.CompException;
import comp.general.VScan;
import comp.scanner.Token;

/**
 *
 * @author loara
 */
public class ParserException extends CompException{
    public ParserException(String t, int to){
        super(t, to);
    }
    public ParserException(String t, Token to){
        super(t, to.getRiga());
    }
    public ParserException(String t, VScan<Token> v){
        super(t, v.get().getRiga());
    }
    public ParserException(ParserException b){
        super(b);
    }
    /*
    public ParserException(VScan<Token> t, int i){
        t.setInd(i);
    }
    */
    public ParserException(String e, int r, VScan<Token> t, int i){
        super(e, r);
        t.setInd(i);
    }
    public ParserException(ParserException e, VScan<Token> t, int i){
        super(e);
        t.setInd(i);
    }
}
