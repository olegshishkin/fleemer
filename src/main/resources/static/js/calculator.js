/******************************************************
Copyright (c/c++) 2013-doomsday by Alexey Slovesnov 
homepage http://slovesnov.users.sourceforge.net/
email slovesnov@yandex.ru
All rights reserved.
******************************************************/

//http://sccalculator.sourceforge.net/calculator.html?language=russian&useMemory=false&useBuffer=false&useLanguageSelector=false
//parameters parsing is not active

var replaceA=[
  ['a','b','c','d']//text fields
  ,['exp()','log()','pow(,)','sqrt()','abs()','random()','min(,)','max(,)']
  ,['pi','e','sqrt2','sqrt1_2','ln2','ln10','log2e','log10e']
  ,['sin()','asin()','cos()','acos()','tan()','atan()','atan2(,)']
  ,['ceil()','floor()','round()']
];
var language;
var LANGUAGES=['english','russian'];

var languageString=[
  [
  'clear','copy to memory','add to buffer',
  'expression','result',
  'common functions','constants','trigonometric functions','rounding functions',
  'memory','buffer',
  'error','<a href="?parser#list">fullsupported functions list</a>'
  ],[
  'очистить','копировать в память','добавить в буфер',
  'выражение','результат',
  'общие функции','константы','тригонометрические функции','функции округления',
  'память','буфер',
  'ошибка','<a href="?parser#list">полный список поддерживаемых функций</a>'
  ],
];

function getLanguageString(n){
  return languageString[language][STRING_ENUM[n]];
}

function Enum(constantsList) {
    for (var i in constantsList) {
        this[constantsList[i]] = i;
    }
    this['length']=constantsList.length
}

var STRING_ENUM=new Enum([
  'CLEAR','COPY_TO_MEMORY','ADD_TO_BUFFER','EXPRESSION','RESULT',
  'COMMON_FUNCTIONS','CONSTANTS','TRIGONOMETRIC_FUNCTIONS','ROUNDING_FUNCTIONS',
  'MEMORY','BUFFER','ERROR','LIST_REFERENCE'
  ]
);

function calculate(expression){
  //todo calculate('1,2') = error
  //todo calculate('sin(1,2)') = error
	//test '1e2' ok '1.e-2'
	
  var i,j,k,s,e,from,to,r;
  e=expression.toLowerCase().replace(/\s+/g,"");
  if(e.length==0){
    throw "empty expression";
  }
  
	/*random()-> randomvalue
	several occurences 'random()' in string should be changed with different values, so use shile cycle
	should do it before unbalanced parentheses checking
	*/
	while( (r=e.replace(/random\(\)/,Math.random() ))!=e ){//replace is not global
		e=r
	}
	
  //check unbalanced parentheses
  parentheses=['(',')','[',']','{','}']
  for(s=0;s<parentheses.length-1;s+=2){
    for(i=0;i<e.length;i++){
      if(e.charAt(i)==parentheses[s]){
        for(k=0,j=i+1;j<e.length;j++){
          if(e.charAt(j)==parentheses[s]){
            k++;
          }
          else if(e.charAt(j)==parentheses[s+1]){
            if(k==0){
              try{
								calculate(e.substring(i+1, j))
              }
              catch(error){
                throw "unbalanced parentheses["+e.substring(i+1, j)+"]"
              }
              break;
            }
            k--;
          }
        }
        if(j==e.length){
          throw "unbalanced parentheses 96"
        }
      }
    }
  }
  
  //should replace before search matches
	from=["\\[|\\{","\\]|\\}"];
  to=["(",")"];
  for(i=0;i<from.length;i++){
    e=e.replace(new RegExp(from[i],'gi'),to[i])//replaceall
	}

	//javascript allows "random(bla,bla,bla)" but it's error test it
  if(new RegExp("random\\("+"(?!\\))").test(e)){
    throw "arguments of random function is not possible";
  }

	//javascript allows "2/+2" but it's error test it
	if(new RegExp("[+\\-\\*/][+\\-\\*/]").test(e)){
		throw new Exception("two operators in a row");
	}

	e=replaceRarelyFunctionText(e);
  
  for(j=0;j<replaceA.length;j++){
    r=replaceA[j]
    for(i=0;i<r.length;i++){
      s=r[i]
      if(j>0){
        k=s.indexOf('(');
        if(k==-1){//if no arguments toUpperCase pi->PI
          s=s.toUpperCase()
        }
        else{// remove brackets
          s=s.substring(0,k)
          if(s!='random'){
			  		//javascript allows "sin()" but it's error test it
			  		if(new RegExp(s+"\\("+"(?=\\))",'gi').test(e)){
			  			throw new Exception("function '"+s+"' should have argument(s)");
			  		}
          }
        }
        to='Math.'+s;
      }
      else{
        if(document.getElementById(r[i])==null){//if text fields 'a'... not exists
          continue;
        }
        to='('+document.getElementById(r[i]).value+')';//use brackets
      }
				
      e=e.replace(new RegExp("(^|[^a-z0-9_\.])"+s+"(?=\\W|$)",'gi'),'$1'+to)
		}
  }
	
	//eval('f') return normal value, check that all expressions starts with Math.
	s=e.replace(/Math\.\w+/g,"");
	if(/[^[^a-z]e]/.test(s)){//test all chars except 'e', so string '1e2' is possible. Fixed 13jul2014
		throw "unknown lexer";
	}
	
  e=eval(e);
	
	//check that eval returns not a string presents eval('log') returns string
  if(typeof e == 'number'){
    return e;
  }
  throw "invalid return type";
}

/*replaceFunctionText("2*cot(3)")=2*(1/tan(3)) 
string should not have whitespaces! and lowercased!
replace finctions which not Math object doesn't have
*/
function replaceRarelyFunctionText(s){
	var i,j,k,f,c,q;
	var fun=['cot','1/tan(#)'
		,'sec','1/cos(#)'
		,'csc','1/sin(#)'
		,'acot','pi/2-atan(#)'
		,'asec','acos(1/#)'
		,'acsc','asin(1/#)'
		,'sinh','(exp(#)-exp(-#))/2'
		,'cosh','(exp(#)+exp(-#))/2'
		,'tanh','(exp(2*#)-1)/(exp(2*#)+1)'
		,'coth','(exp(2*#)+1)/(exp(2*#)-1)'
		,'sech','2/(exp(#)+exp(-#))'
		,'csch','2/(exp(#)-exp(-#))'
		,'asinh','log(#+sqrt(#*#+1))'
		,'acosh','log(#+sqrt(#*#-1))'
		,'atanh','log((1+#)/(1-#))/2'
		,'acoth','log((#+1)/(#-1))/2'
		,'asech','log((1+sqrt(1-#*#))/#)'
		,'acsch','log(1/#+sqrt(1+#*#)/abs(#))']
	for(f=0;f<fun.length-1;f+=2){
		from=fun[f]+'('
		for(i=0;(i=s.indexOf(from,i))!=-1;){
			c=s.charAt(i-1)
			q=s.substring(0,i)+'(';
			i+=from.length;
			if(i!=from.length && c>='a' && c<='z' ){
				continue;
			}
			
			for(k=0,j=i;j<s.length;j++){
				c=s.charAt(j);
				if(c=='('){
					k++;
				}
				else if(c==')'){
					k--;
				}
				if(k==-1){
					break;
				}
			}
			
			s=q+fun[f+1].replace(/#/g ,'('+s.substring(i,j)+')' )+s.substring(j)
		}
	}
	return s
}

function com(){
  var e=texpression.value;
	
  e=e.replace(new RegExp('ю|б','gi'),".")//replaceall non case sensitive
  if(texpression.value!=e){//for chrome
    texpression.value=e;
  }
	
	e=e.replace(/\s+/g,"");//remove all whitespaces
  if(e.length==0){
    texpression.style.color = tresult.style.color = "black";//set color as normal
    tresult.value="";
    return;
  }
  
  
  try{
    tresult.value = calculate(e);
    texpression.style.color = tresult.style.color = "black";
  }
  catch(error){
    texpression.style.color = tresult.style.color = "red";
    tresult.value=getLanguageString('ERROR')
  }
}

function bclick(fun){
  v=document.getElementById('texpression')
  v.focus();//should set focus for correct working of function getInputSelection
  var s=getInputSelection(v)
  i=s.start;
  j=s.end;
  
  s=v.value;
  var k=fun.indexOf('(')
  if(k==-1){//no arguments
    q=fun+s.substring(i,j)
  }
  else{
    q=fun.substring(0,k)+'('+s.substring(i,j)+(fun.indexOf(',')==-1?'':',')+')'
  }
  
  v.value=(s.substring(0,i)+q+ s.substring(j) );
  com()
}

function changeLanguage(lng) {
  language=lng
  com()//need to change result value
  
  //set language dependent captions
  r=['EXPRESSION','RESULT','MEMORY','BUFFER','CLEAR','COPY_TO_MEMORY','ADD_TO_BUFFER'
  ,'COMMON_FUNCTIONS','CONSTANTS','TRIGONOMETRIC_FUNCTIONS','ROUNDING_FUNCTIONS','LIST_REFERENCE']
  for(i=0;i<r.length;i++){
    j=document.getElementById(r[i]);
    if(j==null){
      continue;
    }
    s=getLanguageString(r[i])
    if(j.type=='button'){
      j.value=s
    }
    else{
      j.innerHTML=s
    }
  }
}

function load(lng){
  var i,j,k,s;
  
  s=document.URL;
  if(s.charAt(s.length-1)=='#'){
    s=s.substring(0,s.length-1);
  }
  language=lng;
  useMemory=true;
  useBuffer=true;
  useLanguageSelector=true;
  
  i=s.indexOf('?');
  if(i!=-1){
    s=s.substring(i+1).toLowerCase().split('&');
    for(j=0;j<s.length;j++){//s='paramname=value'
      k=s[j].split('=');
      if(k[0]=='language'){
        for(i=0;i<LANGUAGES.length;i++){
          if(k[1]==LANGUAGES[i]){
            language=i;
            break;
          }
        }
      }
      else if(k[1]=='false'){
        if(k[0]=='usememory'){
          useMemory=false;
        }
        else if(k[0]=='usebuffer'){
          useBuffer=false;
        }
        else if(k[0]=='uselanguageselector'){
          useLanguageSelector=false;
        }
      }
    }
    
  }
  
  s='<table id="calculatorTable"><tr><td><table class="calculator"><tr>\
  <td id="EXPRESSION">\
  <td><input type="text" id="texpression" onkeyup="com()">\
  <td><input type="button" id="CLEAR" onclick="javascript:texpression.value=\'\';com();">\
  </table>\
  <tr><td id="tfield">\
  <tr><td><table class="calculator"><tr>\
  <td id="RESULT">\
  <td><input type="text" id="tresult">';
  if(useMemory){
    s+='<td><input type="button" id="COPY_TO_MEMORY" onclick="javascript:tmemory.value=tresult.value">'
  }
  if(useBuffer){
    s+='<td><input type="button" id="ADD_TO_BUFFER" onclick="addBuffer()">'
  }
  s+='</table>\
  <tr><td id="buttons">\
  <tr><td>\
  <table class="calculator">';
  if(useMemory){
    s+='<tr><td id="MEMORY"><td><input type="text" id="tmemory">'
  }

  s+='<tr><td><table class="calculatorP">'
  if(useBuffer){
    s+='<tr><td id="calculatorBufferUp"><tr><td id="BUFFER">'
  }
  s+='<tr><td id="calculatorBufferDown"></table>'
  if(useBuffer){
    s+='<td><textarea id="tbuffer"></textarea>'
  }
  s+='</table>'
	s+='<p id="LIST_REFERENCE">'
  document.getElementById('calculatorDiv').innerHTML=s;
  
  i=335;
  if(!useMemory){
    i+=170;
  }
  if(!useBuffer){
    i+=170;
  }
  j=document.getElementById('tresult')
  if(j!=null){
    j.style.width=i+'px'
  }
  
  texpression.focus();
  s='<table style="width:780px;"><tr>'

  for(i=0;i<replaceA[0].length;i++){
    s+='<td style="width:20px;text-align:right;">'+replaceA[0][i]+'=<td><input type="text" id="'+replaceA[0][i]+'" value="0" onkeyup="com()" style="width:165px;">'
  }
  s+='</table>'
  document.getElementById('tfield').innerHTML=s
  
  if(useLanguageSelector){
    s=''
    for(i=0;i<LANGUAGES.length;i++){
      j=LANGUAGES[i].toLowerCase().substring(0,2);
      s+='<a href="#" onclick="javascript:changeLanguage('+i+')" class="calculator"><img class="flag" src="img/'+j+'.gif" />'+j+'</a>&nbsp;'
    }
    document.getElementById('calculatorBufferDown').innerHTML=s
  }
  
  r=['COMMON_FUNCTIONS','CONSTANTS','TRIGONOMETRIC_FUNCTIONS','ROUNDING_FUNCTIONS']
  s='<table style="width:780px;">'
  for(j=1;j<replaceA.length;j++){
    if(j%2==1){
      s+='<tr>'
    }
    s+='<td style="width:380px;float:'+(j%2==1?'left':'right')+';"><fieldset><legend id="'+r[j-1]+'"></legend><table>';
    for(i=0;i<replaceA[j].length;i++){
      if(i%(j==4?2:4)==0){
        s+='<tr>'
      }
      s+= '<td><input type="button" onclick="bclick(this.value)" value="'+replaceA[j][i]+'" style="width:80px"></td>';
    }
    s+='</table></fieldset>'
  }
  s+='</table>'
	s+=''
  document.getElementById('buttons').innerHTML=s
  
  changeLanguage(language);
}

function addBuffer(){
  tbuffer.value+=texpression.value+'='+tresult.value+'\n'
}

function getInputSelection(el) {//function returns selection for (ie,and toher browsers)
    var start = 0, end = 0, normalizedValue, range,
        textInputRange, len, endRange;

    if (typeof el.selectionStart == "number" && typeof el.selectionEnd == "number") {
        start = el.selectionStart;
        end = el.selectionEnd;
    } else {
        range = document.selection.createRange();

        if (range && range.parentElement() == el) {
            len = el.value.length;
            normalizedValue = el.value.replace(/\r\n/g, "\n");

            // Create a working TextRange that lives only in the input
            textInputRange = el.createTextRange();
            textInputRange.moveToBookmark(range.getBookmark());

            // Check if the start and end of the selection are at the very end
            // of the input, since moveStart/moveEnd doesn't return what we want
            // in those cases
            endRange = el.createTextRange();
            endRange.collapse(false);

            if (textInputRange.compareEndPoints("StartToEnd", endRange) > -1) {
                start = end = len;
            } else {
                start = -textInputRange.moveStart("character", -len);
                start += normalizedValue.slice(0, start).split("\n").length - 1;

                if (textInputRange.compareEndPoints("EndToEnd", endRange) > -1) {
                    end = len;
                } else {
                    end = -textInputRange.moveEnd("character", -len);
                    end += normalizedValue.slice(0, end).split("\n").length - 1;
                }
            }
        }
    }

    return {
        start: start,
        end: end
    };
}
