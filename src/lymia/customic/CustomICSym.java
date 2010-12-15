/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lymia.customic;

class CustomICSym {
    static final int SYM_TYPE = 0;
    static final int SYM_NAME = 1;
    static final int SYM_PROG = 2;
    static final int SYM_FILE = 3;
    
    static class Type {
        final String type;
        final String language;
        public Type(String type) {
            String[] s = type.split("/");
            this.type = s[0];
            this.language = s[1];
        }
    }
    static class Name {
        final String icName;
        final String title;
        public Name(String name) {
            String[] s = name.split(":");
            this.icName = s[0];
            this.title = s.length==2?s[1].replace('-',' '):"UNTITLED IC";
        }
    }
}
