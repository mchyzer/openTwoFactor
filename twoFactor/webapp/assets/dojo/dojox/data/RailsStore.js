//>>built
define("dojox/data/RailsStore",["dojo","dojox","dojox/data/JsonRestStore"],function(e,h){return e.declare("dojox.data.RailsStore",h.data.JsonRestStore,{constructor:function(){},preamble:function(a){if("string"==typeof a.target&&!a.service){var g=a.target.replace(/\/$/g,"");a.service=h.rpc.Rest(this.target,!0,null,function(a,b){b=b||{};var c=g,f,d;e.isObject(a)?(d="",f="?"+e.objectToQuery(a)):b.queryStr&&-1!=b.queryStr.indexOf("?")?(d=b.queryStr.replace(/\?.*/,""),f=b.queryStr.replace(/[^?]*\?/g,"?")):
e.isString(b.query)&&-1!=b.query.indexOf("?")?(d=b.query.replace(/\?.*/,""),f=b.query.replace(/[^?]*\?/g,"?")):(d=a?a.toString():"",f="");-1!=d.indexOf("\x3d")&&(f=d,d="");var k=h.rpc._sync;h.rpc._sync=!1;return{url:d?c+"/"+d+".json"+f:c+".json"+f,handleAs:"json",contentType:"application/json",sync:k,headers:{Accept:"application/json,application/javascript",Range:b&&(0<=b.start||0<=b.count)?"items\x3d"+(b.start||"0")+"-"+(b.count&&b.count+(b.start||0)-1||""):void 0}}})}},fetch:function(a){function g(b){null==
a.queryStr&&(null==a.queryStr&&(a.queryStr=""),e.isObject(a.query)?a.queryStr="?"+e.objectToQuery(a.query):e.isString(a.query)&&(a.queryStr=a.query));var c=a,f=a.queryStr,d;d=-1==a.queryStr.indexOf("?")?"?":"\x26";c.queryStr=f+d+e.objectToQuery(b)}a=a||{};if(a.start||a.count){if((a.start||0)%a.count)throw Error("The start parameter must be a multiple of the count parameter");g({page:(a.start||0)/a.count+1,per_page:a.count})}if(a.sort){var c={sortBy:[],sortDir:[]};e.forEach(a.sort,function(a){c.sortBy.push(a.attribute);
c.sortDir.push(a.descending?"DESC":"ASC")});g(c);delete a.sort}return this.inherited(arguments)},_processResults:function(a,g){var c;if("undefined"==typeof this.rootAttribute&&a[0])if(a[0][this.idAttribute])this.rootAttribute=!1;else for(c in a[0])a[0][c][this.idAttribute]&&(this.rootAttribute=c);c=this.rootAttribute?e.map(a,function(a){return a[this.rootAttribute]},this):a;var b=a.length;return{totalCount:g.fullLength||(g.request.count==b?(g.request.start||0)+2*b:b),items:c}}})});
//# sourceMappingURL=RailsStore.js.map