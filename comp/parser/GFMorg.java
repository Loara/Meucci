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
package comp.parser;

import comp.general.Lingue;
import comp.general.VScan;
import comp.parser.template.Template;
import comp.scanner.PareToken;
import comp.scanner.Token;
import java.io.Serializable;

/**
 *
 * @author loara
 */
public class GFMorg implements Serializable{
    public GFunzMem get, set;
    public TypeName type;
    public GFMorg(TypeName type){
        get=null;
        set=null;
        this.type=type;
    }
    public GFMorg(VScan<Token> t, TypeName type, String ctype, 
            Template[] ctemplate, String name, String modulo)throws ParserException{
            get=null;
            set=null;
            this.type=type;
            if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'){
                t.nextEx();
                while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
                    GFunzMem fm=new GFunzMem(t, type, ctype, ctemplate, name, modulo);
                    if(fm.getAccess()){
                        if(get!=null)
                            throw new ParserException(Lingue.getIstance().format("m_par_getovw"), t);
                        get=fm;
                    }
                    else{
                        if(set!=null)
                            throw new ParserException(Lingue.getIstance().format("m_par_setovw"), t);
                        set=fm;
                    }
                }
                t.nextEx();
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_errblk"), t);
    }
}
