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
        Locale def = Locale.getDefault();
        if(def.equals(Locale.ITALY))
            rb = ResourceBundle.getBundle("comp.general.Mess", Locale.ITALIAN);
        else
            rb = ResourceBundle.getBundle("comp.general.Mess", Locale.ENGLISH);
    }
    private final ResourceBundle rb;
    public String format(String s, Object... obj){
        if(obj.length==0)
            return rb.getString(s);
        else
            return MessageFormat.format(rb.getString(s), obj);
    }
}
