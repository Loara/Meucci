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
 * valori numerici, anche long
 * @author loara
 */
public class IntToken extends Token{
    public final long s;
    public final char val;
    public final boolean unsigned;
    public IntToken(long t, int r, char va, boolean uns){
        super(r);
        s=t;
        val=va;
        unsigned=uns;
    }
    @Override
    public String toString(){
        return "Int: "+String.valueOf(s);
    }
}
