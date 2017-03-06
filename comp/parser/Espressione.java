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

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import java.io.Serializable;

/**
 *  Nelle espressioni le operazioni unarie hanno sempre la priorità rispetto a quelle binarie.
 * <b>L'associatività è sempre da sinistra a destra e le operazioni non hanno differenti priorità</b>
 * , si possono usare le parentesi per modificare la priorità.
 * @author loara
 */
public abstract class Espressione implements Serializable{
    public Espressione(){
        
    }
    public abstract void println(int inter);
    /*
    Il validate serve per sapere se è chiamata all'interno di validate o di toCode
    */
    public abstract TypeElem returnType(Variabili var, boolean validate)throws CodeException;
    /**
     * il tipo di ritorno và messo nell'accumulatore
     * @param text
     * @param var
     * @param env 
     * @param acc 
     * @throws comp.code.CodeException 
     */
    public abstract void toCode(Segmenti text, Variabili var, Environment env, 
            Accumulator acc)throws CodeException;
    /**
     * 
     * @param var
     * @throws CodeException 
     */
    public abstract void validate(Variabili var)throws CodeException;
}
