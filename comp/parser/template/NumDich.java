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
package comp.parser.template;

import java.io.Serializable;

/**
 *
 * @author loara
 */
public class NumDich implements Serializable, TemplateEle{
    private final long num;
    private final int exdim;
    public NumDich(long n, int dim){
        num=n;
        exdim=dim;
    }
    public long getNum(){
        return num;
    }
    public int numBytes(){
        return 1 << exdim;
    }
    public int expDim(){
        return exdim;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof NumDich){
            return num==((NumDich)o).num;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) (this.num ^ (this.num >>> 32));
        return hash;
    }
    @Override
    public String toString(){
        return String.valueOf(num);
    }
}
