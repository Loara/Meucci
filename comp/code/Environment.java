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

import comp.general.Info;
import comp.general.Stack;
import comp.general.Stack.SEl;
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
    public static class TryBlock{
        public String tryName;
        public String[] exc;
        public boolean hasDef;
        public TryBlock(String t, String[] ex, boolean def){
            tryName=t;
            exc=ex;
            hasDef=def;
        }
    }
    private final Stack<TryBlock> trb;
    public Environment(){
        stackvals=new Stack<>(String.class);
        trb=new Stack<>(TryBlock.class);
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
    public String getErrorHandler(String err){
        SEl<TryBlock> y = trb.getEx();
        while(y != null && !y.ele().hasDef && !Info.isIn(err, y.ele().exc)){
            y = y.next();
        }
        if(y == null)
            return null;
        if(y.ele().hasDef){
            if(!Info.isIn(err, y.ele().exc))
                return encode(y.ele().tryName);//Mandato in default
        }
        return encode(y.ele().tryName, err);
    }
    public TryBlock getTry(String err){
        SEl<TryBlock> y = trb.getEx();
        while(y != null && !y.ele().hasDef && !Info.isIn(err, y.ele().exc)){
            y = y.next();
        }
        if(y == null)
            return null;
        return y.ele();
    }
    public void addTry(String name, String[] err, boolean def){
        trb.push(new TryBlock(name, err, def));
    }
    public void removeTry(){
        trb.pop();
    }
    public static String encode(String tryName, String error){
        return tryName+"_"+error;
    }
    public static String encode(String tryName){
        return tryName+"_@default";
    }
}
