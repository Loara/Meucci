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

package comp.code.vars;

import comp.code.CodeException;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.XReg;

/**
 *
 * @author loara
 */
public abstract class Var {
    public abstract void getVar(Segmenti te, String t, Register reg)throws CodeException;
    public abstract void setVar(Segmenti te, String t, Register reg)throws CodeException;
    public abstract void xgetVar(Segmenti te, String t, XReg reg)throws CodeException;
    public abstract void xsetVar(Segmenti te, String t, XReg reg)throws CodeException;
    public abstract boolean isIn(String ident);
    public abstract TypeElem type(String ident)throws CodeException;
    protected void getGVar(Segmenti text, String ident, Register reg, int dim)throws CodeException{
        text.addIstruzione("mov", reg.getReg(dim), ident);
    }
    protected void setGVar(Segmenti text, String ident, Register reg, int dim)throws CodeException{
        text.addIstruzione("mov", ident, reg.getReg(dim));
    }
    protected void getXVar(Segmenti text, String id, XReg r)throws CodeException{
        text.addIstruzione("movsd", r.getReg(), id);
    }
    protected void setXVar(Segmenti text, String id, XReg r)throws CodeException{
        text.addIstruzione("movsd", id, r.getReg());
    }
}
