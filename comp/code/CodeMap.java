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
 * Meglio non estenderlo da ArrayList per facilitare il debugging
 * @author loara
 */
public class CodeMap{
    private final ArrayList<String> cname;
    private String opc, op1, op2;
    public CodeMap(){
        cname=new ArrayList<>();
    }
    public ArrayList<String> toArrayList(){
        return cname;
    }
    public boolean add(String t){
        flush();
        return cname.add(t);
    }
    public void flush(){
        if(opc!=null){
            if(op1==null)
                cname.add("\t"+opc);
            else if(op2==null)
                cname.add("\t"+opc+"\t"+op1);
            else
                cname.add("\t"+opc+"\t"+op1+","+op2);
            opc=null;
        }
    }
    public void addLabel(String t){
        if(opc != null && opc.startsWith("j") && t.equals(op1))
            opc = null;//salto inutile
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
                if( (opc.startsWith("movsx") || opc.startsWith("movzx") ) && o1.contains("[")){
                    //non è possibile combinarli
                    flush();
                    opc="mov";
                    op1=o1;
                    op2=o2;
                    return;
                }
                if(!op1.contains("[")){
                    if(!op2.contains("[") || !o1.contains("[")){
                        if(o1.contains("[") && Info.isNum(op2.charAt(0))){
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
        if("sub".equals(opc) && "sub".equals(oc) && op1.equals(o1)){
            try{
                int v=Integer.parseInt(op2);
                int w=Integer.parseInt(o2);
                op2=String.valueOf(v+w);
                return;
            }
            catch(NumberFormatException ex){
                //Standard
            }
        }
        if("leave".equals(oc) && "mov".equals(opc)){
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
    public int size(){
        return cname.size();
    }
}
