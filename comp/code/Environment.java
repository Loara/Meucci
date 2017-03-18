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
package comp.code;

import comp.general.Stack;
import java.util.HashMap;

/**
 * Setta le variabili globali per la compilazione
 * @author loara
 */
public class Environment extends HashMap<String, Integer>{
    //per i break e continue
    private final Stack<String> stackvals;//per i break
    public static TypeElem ret;//tipo di ritorno funzione/operazione
    public static String currentModulo;
    public static boolean template;
    public static String[] errors;
    public Environment(){
        stackvals=new Stack<>(String.class);
    }
    public void increment(String k){
        if(!containsKey(k)){
            put(k, 1);
        }
        else{
            put(k, get(k)+1);
        }
    }
    public void clear(String k){
        put(k, 0);
    }
    public void push(String e){
        stackvals.push(e);
    }
    public void pop(){
        stackvals.pop();
    }
    public String getSt(){
        return stackvals.getLast();
    }
}
