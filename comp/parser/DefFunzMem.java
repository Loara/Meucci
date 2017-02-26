/*
 * Copyright (C) 2017 loara
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

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Meth;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.XReg;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.expr.IdentEle;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;

/**
 *
 * @author loara
 */
public class DefFunzMem extends FunzMem{
    public DefFunzMem(TypeName type, String ctype, Template[] ctemplates, String name, String modulo, boolean acc){
        super(type, ctype, ctemplates, name, modulo, acc);
    }
    @Override
    public void validate(Environment env, Dichiarazione[] varSt)throws CodeException{
        
    }
    @Override
    public void substituteAll(Substitutor s)throws CodeException{
        retType=s.recursiveGet(retType);
        for (FunzParam dich : dichs) {
            dich.dich.type = s.recursiveGet(dich.dich.type);
        }
    }
    @Override
    public void toCode(Segmenti text, Dichiarazione[] varSt, Environment env, 
            TemplateEle... temps)throws CodeException{
        Substitutor s=new Substitutor();
        s.clear();
        s.addAll(templateNames(), temps);
        this.substituteAll(s);
        Accumulator acc=new Accumulator();//servirà in VarStack
        Variabili vs=new Variabili(dichs, varSt, false, acc);
        Environment.ret=Types.getIstance().find(retType, false);
        Environment.template=temp.length!=0;
        String mname=Meth.modName(this, temps);
        text.addLabel(mname);
        text.addIstruzione("enter", "0", "0");
        TypeElem te=Types.getIstance().find(new TypeName(className(), temps), false);
        Membro m=te.information(varName(), false);
        TypeElem ty=Types.getIstance().find(m.getType(), false);
        if(getAccess()){
            if(ty.xmmReg()){
                acc.setXAccReg(XReg.XMM0);
            }
            else{
                acc.setAccReg(Register.AX);
            }
            vs.getVar(text, acc, "this");
            te.getTypeElement(text, vs, env, new IdentEle(varName()), acc, false);
        }
        else{
            vs.getVar(text, acc, "aaa");
            int in;
            if(ty.xmmReg()){
                in=acc.xsaveAccumulator();
                vs.getVar(text, acc, "this");
                te.setXValueElement(text, vs, env, new IdentEle(varName()), in, acc, false);
            }
            else{
                in=acc.saveAccumulator();
                vs.getVar(text, acc, "this");
                te.setValueElement(text, vs, env, new IdentEle(varName()), in, acc, false);
            }
        }
        text.addIstruzione("leave", null, null);
        text.addIstruzione("ret", null, null);
    }
}
