/*
 * Copyright (C) 2016 loara
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
import java.util.ArrayList;

/**
 *
 * @author loara
 */
public class CodeMap extends ArrayList<String>{
    private String opc, op1, op2;
    @Override
    public boolean add(String t){
        flush();
        return super.add(t);
    }
    public void flush(){
        if(opc!=null){
            if(op1==null)
                super.add("\t"+opc);
            else if(op2==null)
                super.add("\t"+opc+"\t"+op1);
            else
                super.add("\t"+opc+"\t"+op1+","+op2);
            opc=null;
        }
    }
    public void addLabel(String t){
        //non è necessario il flush, lo esegue in automatico
        //la funzione add
        add(t+":");
    }
    public void addIstruzione(String oc, String o1, String o2)throws CodeException{
        if(opc==null){
            opc=oc;
            op1=o1;
            op2=o2;
            return;
        }
        if(o1!=null && oc.startsWith("mov") && o1.equals(o2))
            return;
        if(opc.startsWith("mov") && "mov".equals(oc)){
            if(o1==null)
                throw new CodeException("");
            if(op1.equals(o2)){
                if(op2.equals(o1))
                    return;//inutile aggiungere
                if(!op1.startsWith("[")){
                    if(!op2.startsWith("[") || !o1.startsWith("[")){
                        if(o1.startsWith("[") && Info.isNum(op2.charAt(0))){
                            //settare numero a memoria
                            if(op1.endsWith("b")){
                                op2 = "byte "+op2;
                            }
                            else if(op1.endsWith("w")){
                                op2 = "word "+op2;
                            }
                            else if(op1.endsWith("d")){
                                op2 = "dword "+op2;
                            }
                            else{
                                op2 = "qword "+op2;
                            }
                        }
                        op1=o1;
                        return;
                    }
                }
                else{
                    //legge dal registro invece che dalla memoria
                    //sia op1 che o2 puntano memorie
                    String temp=op2;
                    flush();
                    opc="mov";
                    op1=o1;
                    op2=temp;
                    return;
                }
            }
        }
        if(opc!=null && "leave".equals(oc) && "mov".equals(opc)){
            if(!op2.contains("[") && op2.contains("a")){//ax
                //mov rbx,rax   inutile
                //leave
                opc=oc;
                op1=null;
                op2=null;
                return;
            }
        }
        flush();
        opc=oc;
        op1=o1;
        op2=o2;
    }
    public String[] prevIstr(){
        return new String[]{opc, op1, op2};
    }
    public void substitute(String a, String b, String c){
        opc=a;
        op1=b;
        op2=c;
    }
}
