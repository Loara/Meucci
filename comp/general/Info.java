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

import comp.parser.ParserException;
import comp.scanner.PareToken;
import comp.scanner.Token;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * @author loara
 */
public class Info {
    public static final char[] symb={'!', '$', '%', '&', '*', '+', '-', '<', 
    '>', '?', '@', '/', '^', '|', '~', '\\'};
    public static final int inde=4;
    /**
     * Dimensione dei puntatori
     */
    public static final int pointerdim=8;
    /**
     * Tipi primitivi
     */
    public static final String[] primitive={"char", "boolean", "pt"
        , "real"};
    public static final String[] numbers={"int", "long", "byte", "short",
        "uint", "ulong", "ubyte", "ushort"};
    public static final int[] realDim={1, 1, pointerdim, 8};//dimensione del dato
    public static final int[] numDim={4, 8, 1, 2, 4, 8, 1, 2};
    public static final int maxDimExp=3;//2^3=8
    
    //Ordine crescente per ricerca binaria
    //Nomi vietati per nomi funzioni normali, variabili, nome tipi
    public static final String[] forbittenNames=new String[]{"break", "catch", "continue", 
        "default", "depends", "destroy", "else", "end", "errors", "explicit", "extends", "for", "ghost", 
        "if", "init", "modulo", "new", "num", "number", "override", "packed", "public", "read", "reference", 
        "return", "shadow", "static", "super", "this", "throw", "try", "typ", "type", "while"};
    
    public static String[] primitive(){
        String[] ret=new String[primitive.length+numbers.length];
        System.arraycopy(primitive, 0, ret, 0, primitive.length);
        System.arraycopy(numbers, 0, ret, primitive.length, numbers.length);
        return ret;
    }
    public static int realDim(String name){
        int ind=indexOf(name, primitive);
        if(ind!=-1)
            return realDim[ind];
        ind=indexOf(name, numbers);
        if(ind!=-1)
            return numDim[ind];
        return -1;
    }
    public static boolean varNum(String ex2){
        return isIn(ex2, numbers);
    }
    public static boolean unsignNum(String i){
        return indexOf(i, numbers)>=4;
    }
    public static boolean isPrimitive(String i){
        return isIn(i, primitive) || isIn(i, numbers);
    }
    public static boolean xmmReg(String type){
        return "real".equals(type);
    }
    public static boolean isReference(String i){
        if(isIn(i, numbers))
            return false;
        if(isIn(i, primitive))
            return "pt".equals(i);
        return true;
    }
    public static boolean isNum(char c){
        return c>='0' && c<='9';
    }
    public static boolean isLet(char c){
        return c=='_'||(c>='a' && c<='z')||(c>='A' && c<='Z');
    }
    public static boolean isSymb(char c){
        for(char s : symb){
            if(s==c)
                return true;
        }
        return false;
    }
    public static void isForbitten(String name, int r)throws ParserException{
        boolean b=binarySearch(forbittenNames, name, 0, forbittenNames.length);
        if(b)
            throw new ParserException(Lingue.getIstance().format("m_par_forbnm", name), r);
    }
    /**
     * 
     * @param c
     * @return <ul><li>0 - no parentesi</li><li>1 - aperta</li><li>-1 - chiusa</li></ul>
     */
    public static int parentesi(char c){
        if(c=='(' || c=='[' || c=='{')
            return 1;
        if(c==')' || c==']' || c=='}')
            return -1;
        return 0;
    }
    @SuppressWarnings("unchecked")
    public static <T> T[] toAr(Class<?> istance, ArrayList<T> e){
        T[] arr=(T[])Array.newInstance(istance, e.size());
        for(int i=0; i<e.size(); i++){
            arr[i]=e.get(i);
        }
        return arr;
    }
    public static String cSpace(int i){
        String e="";
        for(int j=0; j<i; j++){
            e+=" ";
        }
        return e;
    }
    /**
     * Ritorna il numero da aggiungere per allinearlo alla memoria
     * @param i
     * @return 
     */
    public static int alignConv(int i){
        return (-i)&7;
    }
    /**
     * Ritorna il numero da sottrarre per allinearlo alla memoria
     * @param i
     * @return 
     */
    public static int decConv(int i){
        return (i)&7;
    }
    public static boolean[] conversion(Boolean[] bb){
        boolean[] ret=new boolean[bb.length];
        for(int i=0; i<bb.length; i++)
            ret[i]=bb[i];
        return ret;
    }
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> boolean isIn(T el, T[] ar){
        if(ar.length==0)
            return false;
        if(el==null)
            return false;
        for(T ev:ar){
            if(el.compareTo(ev)==0)
                return true;
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> int indexOf(T el, T[] ar){
        if(ar.length==0)
            return -1;
        if(el==null)
            return -1;
        for(int i=0; i<ar.length; i++){
            if(el.compareTo(ar[i])==0)
                return i;
        }
        return -1;
    }
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> boolean containedIn(T[] el, T[] ar){
        if(el.length > ar.length)
            return false;
        if(el.length == 0)
            return true;
        for (T el1 : el) {
            if (!isIn(el1, ar)) {
                return false;
            }
        }
        return true;
    }
    public static boolean isTemplatePare(VScan<Token> t, boolean open){
        char p=open ? '[' : ']';
        return (t.get() instanceof PareToken
                && ((PareToken)t.get()).s==p);
    }
    public static boolean binarySearch(String[] ar, String val, int a, int b){
        if(val==null)
            return false;
        if(a>=b)
            return false;
        if((b-a)==1)
            return val.equals(ar[a]);
        int c=(a+b)/2;
        /*
        The result is a negative integer if this String object lexicographically 
        precedes the argument string. The result is a positive integer if this 
        String object lexicographically follows the argument string. The result 
        is zero if the strings are equal; compareTo returns 0 exactly when the 
        String.equals(Object) method would return true.
        */
        int ret=val.compareTo(ar[c]);
        if(ret==0)
            return true;
        if(ret<0)
            return binarySearch(ar, val, a, c);
        else
            return binarySearch(ar, val, c+1, b);
    }
}
