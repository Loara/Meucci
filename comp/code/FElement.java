/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.code;

import comp.parser.Callable;
import comp.parser.OpDef;
import comp.parser.TypeName;
import comp.parser.template.TemplateEle;
import java.util.Objects;

/**
 *
 * @author loara
 */
public class FElement {
    public final String name, modname, modulo;
        public final TypeName ret;
        public final TypeName[] trequest;
        public final boolean oper, templ, shadow;
        public final String[] errors;
        public FElement(String nome, String mnome, TypeName[] tr, TypeName rit,
                boolean operatore, boolean sdw, String mod, 
                boolean template, String[] err){
            name=nome;
            modname=mnome;
            trequest=tr;
            ret=rit;
            oper=operatore;
            modulo=mod;
            templ=template;
            errors = err;
            shadow=sdw;
        }
        public FElement(Callable ca, TemplateEle... params)throws CodeException{
            //Non deve effettuare la sostituzione, in quanto è stata fatta da FunzList
            //oppure non ha parametri template
            name=Meth.funzKey(ca.memName(), params);
            modname=Meth.modName(ca, params);
            ret=ca.getReturn();
            oper=ca instanceof OpDef;
            trequest=ca.types();
            modulo=ca.getModulo();
            templ=params.length!=0;
            errors = ca.errors();
            shadow=ca.isShadow();
        }
        public TypeElem Return(boolean validate)throws CodeException{
            return Types.getIstance().find(ret, validate);
        }
        public boolean external(){
            return !modulo.equals(Environment.currentModulo);
        }
        public boolean isExternFile(){
            return (!modulo.equals(Environment.currentModulo)) || templ || Environment.template;
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.modname);
            return hash;
        }
        @Override
        public boolean equals(Object o){
            if(!(o instanceof FElement))
                return false;
            return modname.equals(((FElement)o).modname);
        }
}
