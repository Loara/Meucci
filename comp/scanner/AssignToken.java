/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.scanner;

/**
 *
 * @author loara
 */
public class AssignToken extends Token{
    public AssignToken(int r){
        super(r);
    }
    @Override
    public String toString(){
        return ":=";
    }
}
