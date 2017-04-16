/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.general;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author loara
 */
public class Lingue {
    private static Lingue lin;
    public static Lingue getIstance(){
        if(lin==null)
            lin=new Lingue();
        return lin;
    }
    public Lingue(){
        /*
        Locale def = Locale.getDefault();
        if(def.equals(Locale.ITALY)){
        */
            rb = ResourceBundle.getBundle("comp.languages.Mess", Locale.ITALIAN);
            rbP = ResourceBundle.getBundle("comp.languages.Mess-parser", Locale.ITALIAN);
            rbC = ResourceBundle.getBundle("comp.languages.Mess-code", Locale.ITALIAN);
        /*
        }
        else{
            rb = ResourceBundle.getBundle("comp.languages.Mess", Locale.ENGLISH);
            rbP = ResourceBundle.getBundle("comp.languages.Mess-parser", Locale.ENGLISH);
            rbC = ResourceBundle.getBundle("comp.languages.Mess-code", Locale.ENGLISH);
        }
        */
    }
    private final ResourceBundle rb, rbP, rbC;
    public String format(String s, Object... obj){
        ResourceBundle ch;
        if(s.startsWith("m_par_"))
            ch=rbP;
        else if(s.startsWith("m_cod_"))
            ch=rbC;
        else
            ch=rb;
        if(obj.length==0)
            return ch.getString(s);
        else
            return MessageFormat.format(ch.getString(s), obj);
    }
}
