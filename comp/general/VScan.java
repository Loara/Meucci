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
package comp.general;

import comp.parser.FineArrayException;
import java.util.List;

/**
 *
 * @author loara
 * @param <T>
 */
public class VScan<T> {
    private final List<T> list;
    private int ind;
    private boolean ended;
    public VScan(List<T> lis){
        list=lis;
        ind=0;
        ended=false;
    }
    public T get(){
        return list.get(ind);
    }
    /**
     * Ritorna l'i-esimo elemento dopo la posizione corrente
     * @param i
     * @return 
     */
    public T get(int i){
        if(ind+i<list.size())
            return list.get(ind+i);
        return null;
    }
    /*
    vede se ci sono almeno i elementi da analizzare (incluso quello corrente) o false se è finito
    */
    public boolean reqSpace(int i){
        if(ended)
            return false;
        return list.size()>=ind+i;
    }
    public boolean next(){
        if(ended)
            return false;
        if(ind==list.size()-1){
            ended=true;
            return false;
        }
        else{
            ind++;
            return true;
        }
    }
    public void nextEx()throws FineArrayException{
        if(ended)
            throw new FineArrayException();
        if(ind==list.size()-1){
            ended=true;
            throw new FineArrayException();
        }
        else{
            ind++;
        }
    }
    public void previous(){
        ended=false;
        if(ind!=0)
            ind--;
    }
    public int getInd(){
        return ind;
    }
    public void setInd(int ii){
        ind=ii;
        ended=false;
    }
    public boolean isEnded(){
        return ended;
    }
}
