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

import java.lang.reflect.Array;

/**
 *
 * @author loara
 * @param <T>
 */
public class Stack<T> {
    //è il primo ad uscire, ovvero è l'ultimo inserito
    private SEl<T> frist;
    private int len;
    private final Class<?> istance;
    public Stack(Class<?> i){
        frist=null;
        len=0;
        istance=i;
    }
    public void push(T el){
        if(frist==null)
            frist=new SEl<>(el);
        else{
            SEl<T> e=new SEl<>(el);
            e.setNext(frist);
            frist=e;
        }
        len++;
    }
    @SuppressWarnings("unchecked")
    public T[] toArray(){//Gli elementi vanno messi IN ORDINE DI INSERIMENTO, non di ESTRAZIONE
        if(len==0)
            return (T[])Array.newInstance(istance, 0);
        T[] ar=(T[])Array.newInstance(istance, len);
        SEl<T> ve=frist;
        for(int i=0; i<len; i++){
            ar[len-1-i]=ve.ele();
            ve=ve.next();
        }
        return ar;
    }
    public T pop(){
        if(frist==null)
            throw new ArrayIndexOutOfBoundsException();
        T el=frist.ele();
        frist=frist.next();
        len--;
        return el;
    }
    //come pop, ma non lo rimuove
    public T getLast(){
        if(frist==null)
            return null;
        return frist.ele();
    }
    /*
    Utilizzare con cautela
    */
    public SEl<T> getEx(){
        return frist;
    }
    public static class SEl<T>{
        private final T cl;
        private SEl<T> next;
        public SEl(T el){
            cl=el;
            next=null;
        }
        public void setNext(SEl<T> ne){
            next=ne;
        }
        public SEl<T> next(){
            return next;
        }
        public T ele(){
            return cl;
        }
    }
    public int size(){
        return len;
    }
}
