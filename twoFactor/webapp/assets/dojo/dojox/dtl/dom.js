//>>built
define("dojox/dtl/dom","dojo/_base/lang ./_base dojox/string/tokenize ./Context dojo/dom dojo/dom-construct dojo/_base/html dojo/_base/array dojo/_base/connect dojo/_base/sniff".split(" "),function(n,c,A,E,z,B,y,C,D,w){c.BOOLS={checked:1,disabled:1,readonly:1};c.TOKEN_CHANGE=-11;c.TOKEN_ATTR=-12;c.TOKEN_CUSTOM=-13;c.TOKEN_NODE=1;var u=c.text,r=c.dom={_attributes:{},_uppers:{},_re4:/^function anonymous\(\)\s*{\s*(.*)\s*}$/,_reTrim:/(?:^[\n\s]*(\{%)?\s*|\s*(%\})?[\n\s]*$)/g,_reSplit:/\s*%\}[\n\s]*\{%\s*/g,
getTemplate:function(a){if("undefined"==typeof this._commentable){this._commentable=!1;var b=document.createElement("div");b.innerHTML="\x3c!--Test comment handling, and long comments, using comments whenever possible.--\x3e";b.childNodes.length&&8==b.firstChild.nodeType&&"Test comment handling, and long comments, using comments whenever possible."==b.firstChild.data&&(this._commentable=!0)}this._commentable||(a=a.replace(/\x3c!--({({|%).*?(%|})})--\x3e/g,"$1"));w("ie")&&(a=a.replace(/\b(checked|disabled|readonly|style)="/g,
't$1\x3d"'));a=a.replace(/\bstyle="/g,'tstyle\x3d"');for(var b=w("webkit"),f=[[!0,"select","option"],[b,"tr","td|th"],[b,"thead","tr","th"],[b,"tbody","tr","td"],[b,"table","tbody|thead|tr","tr","td"]],d=[],e=0,c;c=f[e];e++)if(c[0]&&-1!=a.indexOf("\x3c"+c[1])){var g=new RegExp("\x3c"+c[1]+"(?:.|\n)*?\x3e((?:.|\n)+?)\x3c/"+c[1]+"\x3e","ig");for(;b=g.exec(a);){for(var k=c[2].split("|"),m=[],l=0,p;p=k[l];l++)m.push("\x3c"+p+"(?:.|\n)*?\x3e(?:.|\n)*?\x3c/"+p+"\x3e");var v=[],k=A(b[1],new RegExp("("+m.join("|")+
")","ig"),function(a){var b=/<(\w+)/.exec(a)[1];v[b]||(v[b]=!0,v.push(b));return{data:a}});if(v.length){m=1==v.length?v[0]:c[2].split("|")[0];p=[];for(var l=0,u=k.length;l<u;l++){var t=k[l];if(n.isObject(t))p.push(t.data);else if(t=t.replace(this._reTrim,""))for(var t=t.split(this._reSplit),r=0,y=t.length;r<y;r++){for(var x="",q=2,z=c.length;q<z;q++)2==q?x+="\x3c"+m+' dtlinstruction\x3d"{% '+t[r].replace('"','\\"')+' %}"\x3e':m!=c[q]&&(x+="\x3c"+c[q]+"\x3e");x+="DTL";for(q=c.length-1;1<q;q--)2==q?
x+="\x3c/"+m+"\x3e":m!=c[q]&&(x+="\x3c/"+c[q]+"\x3e");p.push("\u00ff"+d.length);d.push(x)}}a=a.replace(b[1],p.join(""))}}}for(e=d.length;e--;)a=a.replace("\u00ff"+e,d[e]);for(f=/\b([a-zA-Z_:][a-zA-Z0-9_\-\.:]*)=['"]/g;b=f.exec(a);)d=b[1].toLowerCase(),"dtlinstruction"!=d&&(d!=b[1]&&(this._uppers[d]=b[1]),this._attributes[d]=!0);b=document.createElement("div");b.innerHTML=a;for(a={nodes:[]};b.childNodes.length;)a.nodes.push(b.removeChild(b.childNodes[0]));return a},tokenize:function(a){for(var b=[],
f=0,d;d=a[f++];)1!=d.nodeType?this.__tokenize(d,b):this._tokenize(d,b);return b},_swallowed:[],_tokenize:function(a,b){var f=!1,d=this._swallowed,e,h,g;if(!b.first){var f=b.first=!0,k=c.register.getAttributeTags();for(e=0;h=k[e];e++)try{h[2]({swallowNode:function(){throw 1;}},new c.Token(c.TOKEN_ATTR,""))}catch(v){d.push(h)}}for(e=0;h=d[e];e++)if(k=a.getAttribute(h[0]))if(d=!1,h=h[2]({swallowNode:function(){d=!0;return a}},new c.Token(c.TOKEN_ATTR,h[0]+" "+k)),d){a.parentNode&&a.parentNode.removeChild&&
a.parentNode.removeChild(a);b.push([c.TOKEN_CUSTOM,h]);return}h=[];if(w("ie")&&"SCRIPT"==a.tagName)h.push({nodeType:3,data:a.text}),a.text="";else for(e=0;g=a.childNodes[e];e++)h.push(g);b.push([c.TOKEN_NODE,a]);k=!1;h.length&&(b.push([c.TOKEN_CHANGE,a]),k=!0);for(var m in this._attributes){e=!1;var l="";if("class"==m)l=a.className||l;else if("for"==m)l=a.htmlFor||l;else if("value"==m&&a.value==a.innerHTML)continue;else if(a.getAttribute)if(l=a.getAttribute(m,2)||l,"href"==m||"src"==m){if(w("ie")){var p=
location.href.lastIndexOf(location.hash),p=location.href.substring(0,p).split("/");p.pop();p=p.join("/")+"/";0==l.indexOf(p)&&(l=l.replace(p,""));l=decodeURIComponent(l)}}else"tstyle"==m?(e=m,m="style"):c.BOOLS[m.slice(1)]&&n.trim(l)?m=m.slice(1):this._uppers[m]&&n.trim(l)&&(e=this._uppers[m]);e&&(a.setAttribute(e,""),a.removeAttribute(e));"function"==typeof l&&(l=l.toString().replace(this._re4,"$1"));k||(b.push([c.TOKEN_CHANGE,a]),k=!0);b.push([c.TOKEN_ATTR,a,m,l])}e=0;for(g;g=h[e];e++)1==g.nodeType&&
(m=g.getAttribute("dtlinstruction"))&&(g.parentNode.removeChild(g),g={nodeType:8,data:m}),this.__tokenize(g,b);!f&&a.parentNode&&a.parentNode.tagName?(k&&b.push([c.TOKEN_CHANGE,a,!0]),b.push([c.TOKEN_CHANGE,a.parentNode]),a.parentNode.removeChild(a)):b.push([c.TOKEN_CHANGE,a,!0,!0])},__tokenize:function(a,b){var f=a.data;switch(a.nodeType){case 1:this._tokenize(a,b);break;case 3:if(!f.match(/[^\s\n]/)||-1==f.indexOf("{{")&&-1==f.indexOf("{%"))b.push([a.nodeType,a]);else for(var f=u.tokenize(f),d=
0,e;e=f[d];d++)"string"==typeof e?b.push([c.TOKEN_TEXT,e]):b.push(e);a.parentNode&&a.parentNode.removeChild(a);break;case 8:if(0==f.indexOf("{%")){e=n.trim(f.slice(2,-2));if("load "==e.substr(0,5))for(var d=n.trim(e).split(/\s+/g),h=1,g;g=d[h];h++)/\./.test(g)&&(g=g.replace(/\./g,"/")),require([g]);b.push([c.TOKEN_BLOCK,e])}0==f.indexOf("{{")&&b.push([c.TOKEN_VAR,n.trim(f.slice(2,-2))]);a.parentNode&&a.parentNode.removeChild(a)}}};c.DomTemplate=n.extend(function(a){if(!a.nodes){var b=z.byId(a);b&&
1==b.nodeType?(C.forEach(["class","src","href","name","value"],function(a){r._attributes[a]=!0}),a={nodes:[b]}):("object"==typeof a&&(a=u.getTemplateString(a)),a=r.getTemplate(a))}a=r.tokenize(a.nodes);c.tests&&(this.tokens=a.slice(0));this.nodelist=(new c._DomParser(a)).parse()},{_count:0,_re:/\bdojo:([a-zA-Z0-9_]+)\b/g,setClass:function(a){this.getRootNode().className=a},getRootNode:function(){return this.buffer.rootNode},getBuffer:function(){return new c.DomBuffer},render:function(a,b){b=this.buffer=
b||this.getBuffer();this.rootNode=null;for(var f=this.nodelist.render(a||new c.Context({}),b),d=0,e;e=b._cache[d];d++)e._cache&&(e._cache.length=0);return f},unrender:function(a,b){return this.nodelist.unrender(a,b)}});c.DomBuffer=n.extend(function(a){this._parent=a;this._cache=[]},{concat:function(a){var b=this._parent;if(b&&a.parentNode&&a.parentNode===b&&!b._dirty)return this;if(1==a.nodeType&&!this.rootNode)return this.rootNode=a||!0,this;if(!b){if(3==a.nodeType&&n.trim(a.data))throw Error("Text should not exist outside of the root node in template");
return this}if(this._closed){if(3!=a.nodeType||n.trim(a.data))throw Error("Content should not exist outside of the root node in template");return this}if(b._dirty){if(a._drawn&&a.parentNode==b){var c=b._cache;if(c){for(var d=0,e;e=c[d];d++)this.onAddNode&&this.onAddNode(e),b.insertBefore(e,a),this.onAddNodeComplete&&this.onAddNodeComplete(e);c.length=0}}b._dirty=!1}b._cache||(b._cache=[],this._cache.push(b));b._dirty=!0;b._cache.push(a);return this},remove:function(a){if("string"==typeof a)this._parent&&
this._parent.removeAttribute(a);else{if(1==a.nodeType&&!this.getRootNode()&&!this._removed)return this._removed=!0,this;a.parentNode&&(this.onRemoveNode&&this.onRemoveNode(a),a.parentNode&&a.parentNode.removeChild(a))}return this},setAttribute:function(a,b){var c=y.attr(this._parent,a);if(this.onChangeAttribute&&c!=b)this.onChangeAttribute(this._parent,a,c,b);"style"==a?this._parent.style.cssText=b:(y.attr(this._parent,a,b),"value"==a&&this._parent.setAttribute(a,b));return this},addEvent:function(a,
b,c,d){if(!a.getThis())throw Error("You must use Context.setObject(instance)");this.onAddEvent&&this.onAddEvent(this.getParent(),b,c);var e=c;n.isArray(d)&&(e=function(a){this[c].apply(this,[a].concat(d))});return D.connect(this.getParent(),b,a.getThis(),e)},setParent:function(a,b,c){this._parent||(this._parent=this._first=a);b&&c&&a===this._first&&(this._closed=!0);if(b){var d=this._parent,e="",f=w("ie")&&"SCRIPT"==d.tagName;f&&(d.text="");if(d._dirty){for(var g=d._cache,k="SELECT"==d.tagName&&!d.options.length,
m=0,l;l=g[m];m++)l!==d&&(this.onAddNode&&this.onAddNode(l),f?e+=l.data:(d.appendChild(l),k&&l.defaultSelected&&m&&(k=m)),this.onAddNodeComplete&&this.onAddNodeComplete(l));k&&(d.options.selectedIndex="number"==typeof k?k:0);g.length=0;d._dirty=!1}f&&(d.text=e)}this._parent=a;this.onSetParent&&this.onSetParent(a,b,c);return this},getParent:function(){return this._parent},getRootNode:function(){return this.rootNode}});c._DomNode=n.extend(function(a){this.contents=a},{render:function(a,b){this._rendered=
!0;return b.concat(this.contents)},unrender:function(a,b){if(!this._rendered)return b;this._rendered=!1;return b.remove(this.contents)},clone:function(a){return new this.constructor(this.contents)}});c._DomNodeList=n.extend(function(a){this.contents=a||[]},{push:function(a){this.contents.push(a)},unshift:function(a){this.contents.unshift(a)},render:function(a,b,f){b=b||c.DomTemplate.prototype.getBuffer();if(f)var d=b.getParent();for(f=0;f<this.contents.length;f++)if(b=this.contents[f].render(a,b),
!b)throw Error("Template node render functions must return their buffer");d&&b.setParent(d);return b},dummyRender:function(a,b,f){var d=document.createElement("div"),e=b.getParent(),h=e._clone;e._clone=d;var g=this.clone(b,d);e._clone=h?h:null;b=c.DomTemplate.prototype.getBuffer();g.unshift(new c.ChangeNode(d));g.unshift(new c._DomNode(d));g.push(new c.ChangeNode(d,!0));g.render(a,b);if(f)return b.getRootNode();a=d.innerHTML;return w("ie")?B.replace(/\s*_(dirty|clone)="[^"]*"/g,""):a},unrender:function(a,
b,c){if(c)var d=b.getParent();for(c=0;c<this.contents.length;c++)if(b=this.contents[c].unrender(a,b),!b)throw Error("Template node render functions must return their buffer");d&&b.setParent(d);return b},clone:function(a){for(var b=a.getParent(),f=this.contents,d=new c._DomNodeList,e=[],h=0;h<f.length;h++){var g=f[h].clone(a);if(g instanceof c.ChangeNode||g instanceof c._DomNode){var k=g.contents._clone;k?g.contents=k:b!=g.contents&&g instanceof c._DomNode&&(k=g.contents,g.contents=g.contents.cloneNode(!1),
a.onClone&&a.onClone(k,g.contents),e.push(k),k._clone=g.contents)}d.push(g)}for(h=0;g=e[h];h++)g._clone=null;return d},rtrim:function(){for(;;){var a=this.contents.length-1;if(this.contents[a]instanceof c._DomTextNode&&this.contents[a].isEmpty())this.contents.pop();else break}return this}});c._DomVarNode=n.extend(function(a){this.contents=new c._Filter(a)},{render:function(a,b){var f=this.contents.resolve(a),d="text";f&&(f.render&&f.getRootNode?d="injection":f.safe&&(f.nodeType?d="node":f.toString&&
(f=f.toString(),d="html")));this._type&&d!=this._type&&this.unrender(a,b);this._type=d;switch(d){case "text":return this._rendered=!0,this._txt=this._txt||document.createTextNode(f),this._txt.data!=f&&(d=this._txt.data,this._txt.data=f,b.onChangeData&&b.onChangeData(this._txt,d,this._txt.data)),b.concat(this._txt);case "injection":d=f.getRootNode();this._rendered&&d!=this._root&&(b=this.unrender(a,b));this._root=d;var e=this._injected=new c._DomNodeList;e.push(new c.ChangeNode(b.getParent()));e.push(new c._DomNode(d));
e.push(f);e.push(new c.ChangeNode(b.getParent()));this._rendered=!0;return e.render(a,b);case "node":return this._rendered=!0,this._node&&this._node!=f&&this._node.parentNode&&this._node.parentNode===b.getParent()&&this._node.parentNode.removeChild(this._node),this._node=f,b.concat(f);case "html":this._rendered&&this._src!=f&&(b=this.unrender(a,b));this._src=f;if(!this._rendered)for(this._rendered=!0,this._html=this._html||[],d=this._div=this._div||document.createElement("div"),d.innerHTML=f,f=d.childNodes;f.length;)e=
d.removeChild(f[0]),this._html.push(e),b=b.concat(e);return b;default:return b}},unrender:function(a,b){if(!this._rendered)return b;this._rendered=!1;switch(this._type){case "text":return b.remove(this._txt);case "injection":return this._injection.unrender(a,b);case "node":return this._node.parentNode===b.getParent()?b.remove(this._node):b;case "html":for(var c=0,d=this._html.length;c<d;c++)b=b.remove(this._html[c]);return b;default:return b}},clone:function(){return new this.constructor(this.contents.getExpression())}});
c.ChangeNode=n.extend(function(a,b,c){this.contents=a;this.up=b;this.root=c},{render:function(a,b){return b.setParent(this.contents,this.up,this.root)},unrender:function(a,b){return b.getParent()?b.setParent(this.contents):b},clone:function(){return new this.constructor(this.contents,this.up,this.root)}});c.AttributeNode=n.extend(function(a,b){this.key=a;this.contents=this.value=b;this._pool[b]?this.nodelist=this._pool[b]:((this.nodelist=c.quickFilter(b))||(this.nodelist=(new c.Template(b,!0)).nodelist),
this._pool[b]=this.nodelist);this.contents=""},{_pool:{},render:function(a,b){var f=this.key,d=this.nodelist.dummyRender(a);c.BOOLS[f]&&(d=!("false"==d||"undefined"==d||!d));return d!==this.contents?(this.contents=d,b.setAttribute(f,d)):b},unrender:function(a,b){this.contents="";return b.remove(this.key)},clone:function(a){return new this.constructor(this.key,this.value)}});c._DomTextNode=n.extend(function(a){this.contents=document.createTextNode(a);this.upcoming=a},{set:function(a){this.upcoming=
a;return this},render:function(a,b){if(this.contents.data!=this.upcoming){var c=this.contents.data;this.contents.data=this.upcoming;b.onChangeData&&b.onChangeData(this.contents,c,this.upcoming)}return b.concat(this.contents)},unrender:function(a,b){return b.remove(this.contents)},isEmpty:function(){return!n.trim(this.contents.data)},clone:function(){return new this.constructor(this.contents.data)}});c._DomParser=n.extend(function(a){this.contents=a},{i:0,parse:function(a){var b={},f=this.contents;
a||(a=[]);for(var d=0;d<a.length;d++)b[a[d]]=!0;for(d=new c._DomNodeList;this.i<f.length;){var e=f[this.i++],h=e[0],g=e[1];if(h==c.TOKEN_CUSTOM)d.push(g);else if(h==c.TOKEN_CHANGE)h=new c.ChangeNode(g,e[2],e[3]),g[h.attr]=h,d.push(h);else if(h==c.TOKEN_ATTR){var k=u.getTag("attr:"+e[2],!0);if(k&&e[3])-1==e[3].indexOf("{%")&&-1==e[3].indexOf("{{")||g.setAttribute(e[2],""),d.push(k(null,new c.Token(h,e[2]+" "+e[3])));else if(n.isString(e[3]))if("style"==e[2]||-1!=e[3].indexOf("{%")||-1!=e[3].indexOf("{{"))d.push(new c.AttributeNode(e[2],
e[3]));else if(n.trim(e[3]))try{y.attr(g,e[2],e[3])}catch(m){}}else if(h==c.TOKEN_NODE)(k=u.getTag("node:"+g.tagName.toLowerCase(),!0))&&d.push(k(null,new c.Token(h,g),g.tagName.toLowerCase())),d.push(new c._DomNode(g));else if(h==c.TOKEN_VAR)d.push(new c._DomVarNode(g));else if(h==c.TOKEN_TEXT)d.push(new c._DomTextNode(g.data||g));else if(h==c.TOKEN_BLOCK){if(b[g])return--this.i,d;e=g.split(/\s+/g);if(e.length){e=e[0];k=u.getTag(e);if("function"!=typeof k)throw Error("Function not found for "+e);
(g=k(this,new c.Token(h,g)))&&d.push(g)}}}if(a.length)throw Error("Could not find closing tag(s): "+a.toString());return d},next_token:function(){var a=this.contents[this.i++];return new c.Token(a[0],a[1])},delete_first_token:function(){this.i++},skip_past:function(a){return c._Parser.prototype.skip_past.call(this,a)},create_variable_node:function(a){return new c._DomVarNode(a)},create_text_node:function(a){return new c._DomTextNode(a||"")},getTemplate:function(a){return new c.DomTemplate(r.getTemplate(a))}});
return r});
//# sourceMappingURL=dom.js.map