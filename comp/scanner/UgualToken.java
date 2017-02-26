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
package comp.scanner;

/**
 *
 * @author loara
 */
public class UgualToken extends Token{
    private String symb;
    public UgualToken(int r, String s){
        super(r);
        symb=s;
    }
    public UgualToken(int r){
        this(r, null);
    }
    public String getSymb(){
        return symb;
    }
    @Override
    public String toString(){
        if(symb==null)
            return "Uguale: =";
        else
            return "Uguale: "+symb+"=";
    }
}
