function(){return(function(){function d(a){var b=typeof a;if("object"==b)if(a){if(a instanceof Array)return"array";if(a instanceof Object)return b;var c=Object.prototype.toString.call(a);if("[object Window]"==c)return"object";if("[object Array]"==c||"number"==typeof a.length&&"undefined"!=typeof a.splice&&"undefined"!=typeof a.propertyIsEnumerable&&!a.propertyIsEnumerable("splice"))return"array";if("[object Function]"==c||"undefined"!=typeof a.call&&"undefined"!=typeof a.propertyIsEnumerable&&!a.propertyIsEnumerable("call"))return"function"}else return"null";
else if("function"==b&&"undefined"==typeof a.call)return"object";return b};function h(a){this.a=a}
function l(a,b,c){if(null==b)c.push("null");else{if("object"==typeof b){if("array"==d(b)){var g=b;b=g.length;c.push("[");for(var f="",e=0;e<b;e++)c.push(f),f=g[e],l(a,a.a?a.a.call(g,String(e),f):f,c),f=",";c.push("]");return}if(b instanceof String||b instanceof Number||b instanceof Boolean)b=b.valueOf();else{c.push("{");e="";for(g in b)Object.prototype.hasOwnProperty.call(b,g)&&(f=b[g],"function"!=typeof f&&(c.push(e),m(g,c),c.push(":"),l(a,a.a?a.a.call(b,g,f):f,c),e=","));c.push("}");return}}switch(typeof b){case "string":m(b,
c);break;case "number":c.push(isFinite(b)&&!isNaN(b)?String(b):"null");break;case "boolean":c.push(String(b));break;case "function":c.push("null");break;default:throw Error("Unknown type: "+typeof b);}}}var n={'"':'\\"',"\\":"\\\\","/":"\\/","\b":"\\b","\f":"\\f","\n":"\\n","\r":"\\r","\t":"\\t","\x0B":"\\u000b"},p=/\uffff/.test("\uffff")?/[\\\"\x00-\x1f\x7f-\uffff]/g:/[\\\"\x00-\x1f\x7f-\xff]/g;
function m(a,b){b.push('"',a.replace(p,function(a){var b=n[a];b||(b="\\u"+(a.charCodeAt(0)|65536).toString(16).substr(1),n[a]=b);return b}),'"')};var q=String.prototype.trim?function(a){return a.trim()}:function(a){return a.replace(/^[\s\xa0]+|[\s\xa0]+$/g,"")};function r(a){return-1!=t.indexOf(a)}
function u(a,b){var c=0;a=q(String(a)).split(".");b=q(String(b)).split(".");for(var g=Math.max(a.length,b.length),f=0;0==c&&f<g;f++){var e=a[f]||"",k=b[f]||"";do{e=/(\d*)(\D*)(.*)/.exec(e)||["","","",""];k=/(\d*)(\D*)(.*)/.exec(k)||["","","",""];if(0==e[0].length&&0==k[0].length)break;c=v(0==e[1].length?0:parseInt(e[1],10),0==k[1].length?0:parseInt(k[1],10))||v(0==e[2].length,0==k[2].length)||v(e[2],k[2]);e=e[3];k=k[3]}while(0==c)}}function v(a,b){return a<b?-1:a>b?1:0};var t;a:{var w=this.navigator;if(w){var x=w.userAgent;if(x){t=x;break a}}t=""};function y(){return(r("Chrome")||r("CriOS"))&&!r("Edge")};function z(){return r("iPhone")&&!r("iPod")&&!r("iPad")};var A=r("Firefox"),B=z()||r("iPod"),C=r("iPad"),D=r("Android")&&!(y()||r("Firefox")||r("Opera")||r("Silk")),E=y(),F=r("Safari")&&!(y()||r("Coast")||r("Opera")||r("Edge")||r("Silk")||r("Android"))&&!(z()||r("iPad")||r("iPod"));function G(a){return(a=a.exec(t))?a[1]:""}var H=function(){if(A)return G(/Firefox\/([0-9.]+)/);if(E)return z()||r("iPad")||r("iPod")?G(/CriOS\/([0-9.]+)/):G(/Chrome\/([0-9.]+)/);if(F&&!(z()||r("iPad")||r("iPod")))return G(/Version\/([0-9.]+)/);if(B||C){var a=/Version\/(\S+).*Mobile\/(\S+)/.exec(t);if(a)return a[1]+"."+a[2]}else if(D)return(a=G(/Android\s+([0-9.]+)/))?a:G(/Version\/([0-9.]+)/);return""}();/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function I(a){D?u(J,a):u(H,a)}var K;if(D){var L=/Android\s+([0-9\.]+)/.exec(t);K=L?L[1]:"0"}else K="0";var J=K;D&&I(2.3);D&&I(4);F&&I(6);function M(a,b){var c=[];l(new h(b),a,c);return c.join("")}var N=["_"],O=this;N[0]in O||!O.execScript||O.execScript("var "+N[0]);for(var P;N.length&&(P=N.shift());){var Q;if(Q=!N.length)Q=void 0!==M;Q?O[P]=M:O[P]&&O[P]!==Object.prototype[P]?O=O[P]:O=O[P]={}};;return this._.apply(null,arguments);}).apply({navigator:typeof window!="undefined"?window.navigator:null},arguments);}
