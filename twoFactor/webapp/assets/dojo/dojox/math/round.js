//>>built
define("dojox/math/round",["dojo","dojox"],function(f,a){f.getObject("math.round",!0,a);f.experimental("dojox.math.round");a.math.round=function(c,a,d){d=10/(d||10);var b=Math.pow(10,-15+Math.log(Math.abs(c))/Math.log(10));return(d*(+c+(0<c?b:-b))).toFixed(a)/d};if(0==(.9).toFixed()){var g=a.math.round;a.math.round=function(c,a,d){var b=Math.pow(10,-a||0),e=Math.abs(c);if(!c||e>=b)b=0;else if(e/=b,.5>e||.95<=e)b=0;return g(c,a,d)+(0<c?b:-b)}}return a.math.round});
//# sourceMappingURL=round.js.map