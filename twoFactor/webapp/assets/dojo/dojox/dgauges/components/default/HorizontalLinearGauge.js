//>>built
define("dojox/dgauges/components/default/HorizontalLinearGauge","dojo/_base/lang dojo/_base/declare dojo/_base/Color ../utils ../../RectangularGauge ../../LinearScaler ../../RectangularScale ../../RectangularValueIndicator ../../TextIndicator ../DefaultPropertiesMixin".split(" "),function(f,l,k,a,m,n,p,q,r,t){return l("dojox.dgauges.components.default.HorizontalLinearGauge",[m,t],{borderColor:"#C9DFF2",fillColor:"#FCFCFF",indicatorColor:"#F01E28",constructor:function(){this.borderColor=new k(this.borderColor);
this.fillColor=new k(this.fillColor);this.indicatorColor=new k(this.indicatorColor);this.addElement("background",f.hitch(this,this.drawBackground));var a=new n,b=new p;b.set("scaler",a);b.set("labelPosition","trailing");b.set("paddingTop",15);b.set("paddingRight",23);this.addElement("scale",b);a=new q;a.indicatorShapeFunc=f.hitch(this,function(a){return a.createPolyline([0,0,10,0,0,10,-10,0,0,0]).setStroke({color:"blue",width:.25}).setFill(this.indicatorColor)});a.set("paddingTop",5);a.set("interactionArea",
"gauge");b.addIndicator("indicator",a);this.addElement("indicatorTextBorder",f.hitch(this,this.drawTextBorder),"leading");b=new r;b.set("indicator",a);b.set("x",32.5);b.set("y",30);this.addElement("indicatorText",b)},drawBackground:function(h,b,d){d=49;var c=0,g=3,e=a.createGradient([0,a.brightness(this.borderColor,-20),.1,a.brightness(this.borderColor,-40)]);h.createRect({x:0,y:0,width:b,height:d,r:g}).setFill(f.mixin({type:"linear",x1:0,y1:0,x2:b,y2:d},e)).setStroke({color:"#A5A5A5",width:.2});
e=a.createGradient([0,a.brightness(this.borderColor,70),1,a.brightness(this.borderColor,-50)]);c=4;h.createRect({x:c,y:c,width:b-2*c,height:d-2*c,r:2}).setFill(f.mixin({type:"linear",x1:0,y1:0,x2:b,y2:d},e));c=6;g=1;e=a.createGradient([0,a.brightness(this.borderColor,60),1,a.brightness(this.borderColor,-40)]);h.createRect({x:c,y:c,width:b-2*c,height:d-2*c,r:g}).setFill(f.mixin({type:"linear",x1:0,y1:0,x2:b,y2:d},e));c=7;g=0;e=a.createGradient([0,a.brightness(this.borderColor,70),1,a.brightness(this.borderColor,
-40)]);h.createRect({x:c,y:c,width:b-2*c,height:d-2*c,r:g}).setFill(f.mixin({type:"linear",x1:b,y1:0,x2:0,y2:d},e));c=5;g=0;e=a.createGradient([0,[255,255,255,220],.8,a.brightness(this.fillColor,-5),1,a.brightness(this.fillColor,-30)]);h.createRect({x:c,y:c,width:b-2*c,height:d-2*c,r:g}).setFill(f.mixin({type:"radial",cx:b/2,cy:0,r:b},e)).setStroke({color:a.brightness(this.fillColor,-40),width:.4})},drawTextBorder:function(a){return a.createRect({x:5,y:5,width:60,height:39}).setStroke({color:"#CECECE",
width:1})}})});
//# sourceMappingURL=HorizontalLinearGauge.js.map