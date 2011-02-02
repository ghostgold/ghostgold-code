package Parse;
import java_cup.runtime.*;
import java.util.*;
import ErrorMsg.*;
%%

%class Lexer
%unicode
%cup
%line
%column
%char

%{
	StringBuffer string = new StringBuffer();
	int stringBegin = 0;
	int commentDepth = 0;
	ErrorMsg errorSender;

	private Symbol symbol(int type)
	{
		return new Symbol(type, yychar, yychar + yylength());
	}
	private Symbol symbol(int type, Object value)
	{
		return new Symbol(type, yychar, yychar + yylength(),value);
	}
	private Symbol symbol(int type, Object value, int pos)
	{
		return new Symbol(type, pos, yychar + yylength(),value);
	}
	public Lexer(java.io.InputStream in, ErrorMsg aErrorMsg)
	{
		this(in);
		errorSender = aErrorMsg;
	}
%}

%eofval{
	if(yystate() == COMMENT){
				 errorSender.error(yychar,"comment is not closed");
				 return symbol(sym.error);
	}
	if(yystate() == STRING){
				 errorSender.error(yychar,"string is not closed");
				 return symbol(sym.error);
	}
	return symbol(sym.EOF);
%eofval}

LineTerminator = \r|\n|\r\n|\n\r
WhiteSpace = {LineTerminator}|[ \t\f]

Identifier = [A-Za-z][A-Za-z0-9_]*

DecInteger = [0-9][0-9]*

%state STRING
%state COMMENT

%%
<YYINITIAL> {
	"array"	    {return symbol(sym.ARRAY);}
	"break"	    {return symbol(sym.BREAK);}  
	"do"	    {return symbol(sym.DO);}
	"else"	    {return symbol(sym.ELSE);}
	"end"	    {return symbol(sym.END);}
	"for"	    {return symbol(sym.FOR);}
	"function"  {return symbol(sym.FUNCTION);}
	"if"	    {return symbol(sym.IF);}
	"in"	    {return symbol(sym.IN);}
	"let"	    {return symbol(sym.LET);}
	"nil"	    {return symbol(sym.NIL);}
	"of"	    {return symbol(sym.OF);}
	"then"	    {return symbol(sym.THEN);}
	"to"	    {return symbol(sym.TO);}
	"type"	    {return symbol(sym.TYPE);}
	"var"	    {return symbol(sym.VAR);}
	"while"	    {return symbol(sym.WHILE);}

	","	    {return symbol(sym.COMMA);}
	":"	    {return symbol(sym.COLON);}
	";"	    {return symbol(sym.SEMICOLON);}
	"("	    {return symbol(sym.LPAREN);}
	")"	    {return symbol(sym.RPAREN);}
	"["	    {return symbol(sym.LBRACK);}
	"]"	    {return symbol(sym.RBRACK);}
	"{"	    {return symbol(sym.LBRACE);}
	"}"	    {return symbol(sym.RBRACE);}
	"."	    {return symbol(sym.DOT);}
	"+"	    {return symbol(sym.PLUS);}
	"-"	    {return symbol(sym.MINUS);}
	"*"	    {return symbol(sym.TIMES);}
	"/"	    {return symbol(sym.DIVIDE);}
	"="	    {return symbol(sym.EQ);}
	"<>"	{return symbol(sym.NEQ);}
	"<"	    {return symbol(sym.LT);}
	"<="	{return symbol(sym.LE);}
	">"	    {return symbol(sym.GT);}
	">="	{return symbol(sym.GE);}
	"&"	    {return symbol(sym.AND);}
	"|"	    {return symbol(sym.OR);}
	":="	{return symbol(sym.ASSIGN);}
	
	{Identifier}	    {return symbol(sym.ID,yytext());}
	{DecInteger}	    {return symbol(sym.INT,new Integer(yytext()));}	
		
	\"          {string.setLength(0);yybegin(STRING);stringBegin = yychar;}

	"/*"	    {commentDepth++; yybegin(COMMENT);}

	{WhiteSpace} {}
}

<COMMENT>
{
	.|\n	{/* do nothing*/}
	"/*"	{commentDepth++;}
	"*/"	{commentDepth--; if(commentDepth == 0)yybegin(YYINITIAL);}
}

<STRING>
{
	[^\n\r\"\\]	{string.append(yytext());}
	{LineTerminator} {errorSender.error(yychar,"string not closed");return symbol(sym.error);}
	\\n			{string.append("\n");}
	\\t			{string.append("\t");}
	\\\"		{string.append("\"");}
	\\\\		{string.append("\\");}
	\\\^[A-Z@\[\]\\\^_]	{string.append((char)((int)yytext().charAt(2)-64));}
	\\[0-9]{3}	{string.append(yytext());}
	\\{WhiteSpace}*\\	{}
	\"			{yybegin(YYINITIAL);return symbol(sym.STRING,string.toString(),stringBegin);}	
}

.|\n	{errorSender.error(yychar,"illegal character");return symbol(sym.error);}
