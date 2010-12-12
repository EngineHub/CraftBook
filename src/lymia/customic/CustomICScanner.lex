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

import java.io.File;
import java.io.IOException;

import lymia.util.Symbol;

import static lymia.customic.CustomICSym.*;

//Yeah, JFlex might be a bit heavy for this...
//But, still. It'll be useful for later features (i.e. higher level languages).
@SuppressWarnings("unused")
%%

%{
    private StringBuilder s = new StringBuilder();
    
    private Symbol sym(int type) {
        return sym(type, yytext());
    }

    private Symbol sym(int type, Object value) {
        return new Symbol(type, value, yyline, yycolumn);
    }
    
    public Symbol next() throws IOException, UnknownTokenException {
        return yylex();
    }
%}

%final
%apiprivate
%type Symbol
%class CustomICScanner
%throws UnknownTokenException

%unicode
%line
%column

%state PROGRAM,FILE

LT = \r|\n|\r\n
SP = [ \t\f]
IC = [^\r\n]
WS = {LT} | {SP}

IDENTIFIER = [a-zA-Z0-9\-_.]+
TYPE = {IDENTIFIER} "/" {IDENTIFIER}

FULL_NAME = {IDENTIFIER} ":" {IDENTIFIER}
PART_NAME = {IDENTIFIER}
NAME = {FULL_NAME} | {PART_NAME}

DECLARATION_START = "=" {WS}*
INLINE_DECLARATION = {DECLARATION_START} "begin"
FILE_DECLARATION = {DECLARATION_START} "file:"

INLINE_DECLARATION_END = {LT}{SP}* "end"

COMMENT = "//" {IC}* {LT}

%%

<YYINITIAL> {
    {COMMENT}    {}
    {TYPE}       {return sym(SYM_TYPE,new Type(yytext()));}
    {NAME}       {return sym(SYM_NAME,new Name(yytext()));}
    
    {INLINE_DECLARATION} {s.setLength(0); yybegin(PROGRAM);}
    {FILE_DECLARATION}   {s.setLength(0); yybegin(FILE);}
	{WS} {}
}

<PROGRAM> {
    {INLINE_DECLARATION_END} {yybegin(YYINITIAL); return sym(SYM_PROG,s.toString());}
    .|\n                     {s.append(yytext());}
}

<FILE> {
    {LT} {yybegin(YYINITIAL); return sym(SYM_FILE,new File(s.toString()));}
    {IC} {s.append(yytext());}
}

.|\n {throw new UnknownTokenException(yyline,yycolumn);}