/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Istruzione;

/**
 *
 * @author loara
 */
public class ThrowIstr extends Istruzione{
    public final String exc;
    public ThrowIstr(String s){
        exc=s;
    }
    @Override
    public void validate(Variabili var, Environment env){
        
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
    throws CodeException{
        vars.getVarStack().destroyAll(text);
        int ind=Info.indexOf(exc, Environment.errors);
        if(ind != -1){
            text.addIstruzione("mov", Register.DX.getReg(4), ""+(ind+1));
            text.addIstruzione("leave", null, null);
            int p=vars.getVarStack().getDimArgs();
            if(p>0)
                text.addIstruzione("ret",""+p,null);
            else
                text.addIstruzione("ret", null, null);
        }
        else{
            //termina programma
        }
    }
}
