
/*
  Lexer simplificado para la gramatica del ejercicio de CFG.

  Gramatica soportada:
    Program    -> integer id ( ) { Statement }
    Statement  -> id = Expression ;
               | return Expression ;
               | if ( Expression ) { Statement } else { Statement }
               | while ( Expression ) { Statement }
               | Statement Statement
    Expression -> Value + Value | Value
    Value      -> id | number
*/

/* --------------------------Usercode Section------------------------ */
import com.ejemplo.parser.sym;
import java_cup.runtime.*;

%%

/* -----------------Options and Declarations Section----------------- */

%class Lexer

%line
%column

%cup

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/* Macro Declarations */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
dec_int_lit    = 0 | [1-9][0-9]*
identifier     = [A-Za-z_][A-Za-z_0-9]*

%%
/* ------------------------Lexical Rules Section---------------------- */

<YYINITIAL> {

    /* Keywords */
    "integer"          { return symbol(sym.INTEGER); }
    "return"           { return symbol(sym.RETURN); }
    "if"               { return symbol(sym.IF); }
    "else"             { return symbol(sym.ELSE); }
    "while"            { return symbol(sym.WHILE); }

    /* Operators and punctuation */
    ";"                { return symbol(sym.SEMI); }
    "="                { return symbol(sym.ASSIGN); }
    "+"                { return symbol(sym.PLUS); }
    "-"                { return symbol(sym.MINUS); }
    "<"                { return symbol(sym.LT); }
    ">"                { return symbol(sym.GT); }
    "("                { return symbol(sym.LPAREN); }
    ")"                { return symbol(sym.RPAREN); }
    "{"                { return symbol(sym.LBRACE); }
    "}"                { return symbol(sym.RBRACE); }

    /* Literals */
    {dec_int_lit}      { return symbol(sym.NUMBER, Integer.valueOf(yytext())); }

    /* Identifiers */
    {identifier}       { return symbol(sym.ID, yytext()); }

    /* Whitespace - skip */
    {WhiteSpace}       { /* just skip */ }
}

/* Illegal character */
[^]  {
    System.err.println("Error lexico: Caracter ilegal '" + yytext() +
                       "' en linea " + (yyline + 1) + ", columna " + (yycolumn + 1));
}
