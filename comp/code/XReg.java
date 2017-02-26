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

/**
 *
 * @author loara
 */
public enum XReg {
    XMM0{
        @Override
        public int getIndex(){
            return 0;
        }
    },
    XMM1{
        @Override
        public int getIndex(){
            return 1;
        }
    },
    XMM2{
        @Override
        public int getIndex(){
            return 2;
        }
    },
    XMM3{
        @Override
        public int getIndex(){
            return 3;
        }
    },
    XMM4{
        @Override
        public int getIndex(){
            return 4;
        }
    },
    XMM5{
        @Override
        public int getIndex(){
            return 5;
        }
    },
    XMM6{
        @Override
        public int getIndex(){
            return 6;
        }
    },
    XMM7{
        @Override
        public int getIndex(){
            return 7;
        }
    },
    XMM8{
        @Override
        public int getIndex(){
            return 8;
        }
    },
    XMM9{
        @Override
        public int getIndex(){
            return 9;
        }
    },
    XMM10{
        @Override
        public int getIndex(){
            return 10;
        }
    },
    XMM11{
        @Override
        public int getIndex(){
            return 11;
        }
    },
    XMM12{
        @Override
        public int getIndex(){
            return 12;
        }
    },
    XMM13{
        @Override
        public int getIndex(){
            return 13;
        }
    },
    XMM14{
        @Override
        public int getIndex(){
            return 14;
        }
    },
    XMM15{
        @Override
        public int getIndex(){
            return 15;
        }
    };
    public abstract int getIndex();
    public String getReg(){
        return "xmm"+getIndex();
    }
}
