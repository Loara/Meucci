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
package comp.code.template;

import comp.parser.template.TemplateEle;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author loara
 */
public class Notifica {
    public String nome, modulo;
    public TemplateEle[] parametri;
    public boolean segnato;//Per dire se è stato già generato il relativo file
    public Notifica(String n, String modul, TemplateEle... p){
        nome=n;
        parametri=p;
        segnato=false;
        modulo=modul;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof Notifica){
            Notifica ed=(Notifica)o;
            if(nome.equals(ed.nome) && modulo.equals(ed.modulo))
                return Arrays.equals(parametri, ed.parametri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.nome);
        hash = 59 * hash + Arrays.deepHashCode(this.parametri);
        hash = 59 * hash + Objects.hashCode(this.modulo);
        return hash;
    }
}
