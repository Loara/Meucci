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
package comp.scanner;

import comp.general.CompException;
import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import java.util.ArrayList;

/**
 *
 * @author loara
 */
public class Analyser {
    public void analyze(VScan<Character> scan, ArrayList<Token> tok)throws ScanException{
        int criga=1;
        while(!scan.isEnded()){
            if(scan.get()==' '||scan.get()=='\t'){//tabulazione
                scan.next();
                continue;
            }
            if(scan.get()=='\n'){
                criga++;
                scan.next();
                continue;
            }
            if(scan.get()=='/'&& scan.get(1)=='/'){
                //commento
                do
                    scan.next();
                while(!scan.isEnded() && scan.get()!='\n');
                if(scan.get()=='\n'){
                    criga++;
                    scan.next();
                }
                continue;
            }
            if(scan.get()==';'){
                tok.add(new EolToken(criga));
                scan.next();
                continue;
            }
            if(scan.get()=='#'){
                tok.add(new HashToken(criga));
                scan.next();
                continue;
            }
            if(scan.get()=='.'){
                tok.add(new DotToken(criga));
                scan.next();
                continue;
            }
            if(scan.get()==':' && scan.get(1)=='='){
                scan.next();
                scan.next();
                tok.add(new AssignToken(criga));
                continue;
            }
            if(scan.get()=='='){
                if(scan.next()){
                    char c=scan.get();
                    if(c=='='){
                        tok.add(new SymbToken("==", criga));
                        scan.next();
                    }
                    else if(Info.isSymb(c)){
                        StringBuilder stb=new StringBuilder();
                        while(Info.isSymb(c)){
                            stb.append(c);
                            scan.next();
                            if(scan.isEnded())
                                throw new ScanException(criga);
                            c=scan.get();
                        }
                        tok.add(new UgualToken(criga, stb.toString()));
                    }
                    else
                        tok.add(new UgualToken(criga));
                }
                continue;
            }
            if(scan.get()=='\''){
                scan.next();
                {
                    if(scan.get()=='\'')
                        throw new ScanException(criga);
                    if(scan.get(1)!='\'')
                        throw new ScanException(criga);
                    tok.add(new CharToken(scan.get(), false, criga));
                    scan.next();
                    scan.next();
                }
                continue;
            }
            if(scan.get()=='%'&&scan.get(1)=='a'&&scan.get(2)=='{'){
                if(!analyzeASM(scan,tok,criga))
                    throw new ScanException(criga);
                continue;
            }
            if(scan.get()==','){
                tok.add(new VirgToken(criga));
                scan.next();
                continue;
            }
            if(scan.get()=='\"'){
                if(!analyzeString(scan, tok, criga)){
                    throw new ScanException(criga);
                }
                continue;
            }
            if(Info.isNum(scan.get())){
                analyzeInt(scan, tok, criga, false);
                continue;
            }
            if((scan.get()=='U' || scan.get()=='u') && Info.isNum(scan.get(1))){
                scan.next();
                analyzeInt(scan, tok, criga, true);
                continue;
            }
            if(Info.isLet(scan.get())){
                analyzeIndent(scan, tok, criga);
                continue;
            }
            if(scan.get()==':' || Info.isSymb(scan.get())){
                analyzeSymbol(scan, tok, criga);
                continue;
            }
            if(Info.parentesi(scan.get())!=0){
                tok.add(new PareToken(scan.get(), criga));
                scan.next();
            }
        }
    }
    protected boolean analyzeASM(VScan<Character> scan, ArrayList<Token> tok, int r){
        //%a{
        scan.next();
        scan.next();
        StringBuilder build=new StringBuilder();
        while(scan.next()){
            if(scan.get()=='}'){
                tok.add(new ASMToken(build.toString(),r));
                scan.next();
                return true;
            }
            else{
                build.append(scan.get());
            }
        }
        return false;
    }
    protected boolean analyzeString(VScan<Character> scan, ArrayList<Token> tok, int r){
        if(scan.get()!='\"')
            return false;
        StringBuilder build=new StringBuilder();
        while(scan.next()){
            if(scan.get()=='\"'){
                tok.add(new StrToken(build.toString(), r));
                scan.next();
                return true;
            }
            else{
                build.append(scan.get());
            }
        }
        return false;
    }
    protected boolean analyzeInt(VScan<Character> scan, ArrayList<Token> tok, 
            int r, boolean uns){
        long n=Long.parseLong(String.valueOf(scan.get()));
        char val='I';//intero
        while(scan.next()){
            if(!Info.isNum(scan.get())){
                if(!uns && scan.get()=='.'){
                    scan.next();
                    Stack<Character> ff=new Stack<>(Character.class);
                    while(Info.isNum(scan.get())){
                        ff.push(scan.get());
                        scan.next();
                    }
                    Character[] cp=ff.toArray();
                    tok.add(new RealToken(encodeReal(n, cp), r));
                    return true;
                }
                if(scan.get()=='B' || scan.get()=='L' || scan.get()=='S'){
                    val=scan.get();
                    scan.next();
                }
                if(scan.get()=='b' || scan.get()=='l' || scan.get()=='s'){
                    val=(char)(scan.get()-32);
                    scan.next();
                }
                break;
            }
            else{
                n=10*n+Long.parseLong(String.valueOf(scan.get()));
            }
        }
        tok.add(new IntToken(n, r, val, uns));
        return true;
    }
    protected static long encodeReal(long pint, Character[] pf){
        double pff=(double)pint;
        for(int i=pf.length-1; i>=0; i--)
            pff=pff+(pf[i]-'0')*Math.pow((double)10, i-pf.length);
        return Double.doubleToRawLongBits(pff);
    }
    protected boolean analyzeIndent(VScan<Character> scan, ArrayList<Token> tok, int r){
        StringBuilder build=new StringBuilder();
        build.append(scan.get());
        while(scan.next() && (Info.isLet(scan.get()) || Info.isNum(scan.get()))){
            build.append(scan.get());
        }
        tok.add(new IdentToken(build.toString(), r));
        return true;
    }
    protected boolean analyzeSymbol(VScan<Character> scan, ArrayList<Token> tok, int r){
        StringBuilder build=new StringBuilder();
        build.append(scan.get());
        if(scan.get()==':'){
            while(scan.next() && Info.isLet(scan.get())){
                build.append(scan.get());
            }
        }
        else{
            while(scan.next() && (Info.isSymb(scan.get()) || scan.get()=='=')){
                build.append(scan.get());
            }
        }
        tok.add(new SymbToken(build.toString(), r));
        return true;
    }
    public static class ScanException extends CompException{
        public ScanException(int r){
            super(Lingue.getIstance().format("m_scanerr"), r);
        }
    }
}
