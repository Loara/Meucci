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

import java.util.ArrayList;

/**
 * I segmenti in memoria. Non rappresentano segmenti reali, in quanto possono 
 * essere "fusi" in base alle implementazioni.
 * @author loara
 */
public class Segmenti {
    public final ArrayList<String> data, bss, stat;//Deve essere creata una sorta 
    //di DataMap che tenga conto dell'allineamento, ma molto più avanti, in un
    //rilascio futuro magari
    public final CodeMap text, vtors;
    public Segmenti(){
        text=new CodeMap();
        data=new ArrayList<>();
        bss=new ArrayList<>();
        vtors=new CodeMap();
        stat=new ArrayList<>();//inizializzazione modulo
    }
    public void add(String t){
        text.add(t);
    }
    public void addLabel(String l){
        text.addLabel(l);
    }
    public void addIstruzione(String oc, String o1, String o2)throws CodeException{
        text.addIstruzione(oc, o1, o2);
    }
    public void closeAll(){
        text.flush();
        vtors.flush();
    }
}
