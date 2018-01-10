/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.parser.expr.IdentArray;

/**
 *
 * @author loara
 */
public class AssignIstr extends Istruzione{
    private final Espressione sin, des;
    public AssignIstr(Espressione l, Espressione r){
        sin=l;
        des=r;
    }

    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc) throws CodeException {
        
    }

    @Override
    public void validate(Variabili var, Environment env) throws CodeException {
        sin.validate(var);
        des.validate(var);
    }
    private boolean isGP(IdentArray exp){
        return false;
    }
}
